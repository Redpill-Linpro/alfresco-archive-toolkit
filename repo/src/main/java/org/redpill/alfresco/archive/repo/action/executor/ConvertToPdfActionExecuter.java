/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.redpill.alfresco.archive.repo.action.executor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.audit.AuditComponent;
import org.alfresco.repo.content.MimetypeMap;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NoTransformerException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.rule.RuleServiceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Action to create pdf or pdfa. Based on the
 * org.alfresco.repo.action.executer.TransformActionExecuter
 *
 * @author Marcus Svartmark - Redpill Linpro AB
 */
public class ConvertToPdfActionExecuter extends ActionExecuterAbstractBase implements InitializingBean {

  public static final String NAME = "archive-toolkit-transform-to-pdf";
  private final static Log LOGGER = LogFactory.getLog(ConvertToPdfActionExecuter.class);
  /* Error messages */
  public static final String ERR_OVERWRITE = "Unable to overwrite copy because more than one have been found.";
  private static final String CONTENT_READER_NOT_FOUND_MESSAGE = "Can not find Content Reader for document. Operation can't be performed";
  private static final String TRANSFORMING_ERROR_MESSAGE = "Some error occurred during document transforming. Error message: ";

  private static final String TRANSFORMER_NOT_EXISTS_MESSAGE_PATTERN = "Transformer for '%s' source mime type and '%s' target mime type was not found. Operation can't be performed";

  public static final String AUDIT_APPLICATION_NAME = "alfresco-archive-toolkit";

  /*
     * Action constants
   */
  public static final String PARAM_MIME_TYPE = "mime-type";
  public static final String PARAM_DESTINATION_FOLDER = "destination-folder";
  public static final String PARAM_ASSOC_TYPE_QNAME = "assoc-type";
  public static final String PARAM_ASSOC_QNAME = "assoc-name";
  public static final String PARAM_TARGET_NAME = "target-name";
  public static final String PARAM_OVERWRITE_COPY = "overwrite-copy";
  public static final String PARAM_ADD_EXTENSION = "add-extension";
  public static final String FAKE_MIMETYPE_PDFA = "application/pdfa";
  public static final String PARAM_SOURCE_FOLDER = "source-folder";
  public static final String PARAM_SOURCE_FILENAME = "source-filename";
  /*
     * Injected services
   */
  private DictionaryService dictionaryService;
  private NodeService nodeService;
  private CheckOutCheckInService checkOutCheckInService;
  private ContentService contentService;
  private CopyService copyService;
  private MimetypeService mimetypeService;
  protected AuditComponent auditComponent;

  /**
   * Sets the audit component
   *
   * @param auditComponent
   */
  public void setAuditComponent(AuditComponent auditComponent) {
    this.auditComponent = auditComponent;
  }

  /**
   * Set the mime type service
   *
   * @param mimetypeService
   */
  public void setMimetypeService(MimetypeService mimetypeService) {
    this.mimetypeService = mimetypeService;
  }

  /**
   * Set the node service
   *
   * @param nodeService
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * Set the service to determine check-in and check-out status
   *
   * @param checkOutCheckInService
   */
  public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService) {
    this.checkOutCheckInService = checkOutCheckInService;
  }

  /**
   * Set the dictionary service
   *
   * @param dictionaryService
   */
  @Override
  public void setDictionaryService(DictionaryService dictionaryService) {
    this.dictionaryService = dictionaryService;
  }

  /**
   * Set the content service
   *
   * @param contentService
   */
  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  /**
   * Set the copy service
   *
   * @param copyService
   */
  public void setCopyService(CopyService copyService) {
    this.copyService = copyService;
  }

  /**
   * Add parameter definitions
   */
  @Override
  protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
    paramList.add(new ParameterDefinitionImpl(PARAM_MIME_TYPE, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_MIME_TYPE), false, "ac-mimetypes"));
    paramList.add(new ParameterDefinitionImpl(PARAM_DESTINATION_FOLDER, DataTypeDefinition.NODE_REF, true, getParamDisplayLabel(PARAM_DESTINATION_FOLDER)));
    paramList.add(new ParameterDefinitionImpl(PARAM_ASSOC_TYPE_QNAME, DataTypeDefinition.QNAME, false, getParamDisplayLabel(PARAM_ASSOC_TYPE_QNAME)));
    paramList.add(new ParameterDefinitionImpl(PARAM_ASSOC_QNAME, DataTypeDefinition.QNAME, false, getParamDisplayLabel(PARAM_ASSOC_QNAME)));
    paramList.add(new ParameterDefinitionImpl(PARAM_OVERWRITE_COPY, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_OVERWRITE_COPY)));
    paramList.add(new ParameterDefinitionImpl(PARAM_TARGET_NAME, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_TARGET_NAME)));
    paramList.add(new ParameterDefinitionImpl(PARAM_ADD_EXTENSION, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_ADD_EXTENSION)));
    //As a fallback we can also look for a node to convert by source folder and filename. If these are supplied they superseed the node ref supplied with the action
    paramList.add(new ParameterDefinitionImpl(PARAM_SOURCE_FOLDER, DataTypeDefinition.NODE_REF, false, getParamDisplayLabel(PARAM_SOURCE_FOLDER)));
    paramList.add(new ParameterDefinitionImpl(PARAM_SOURCE_FILENAME, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_SOURCE_FILENAME)));
  }

  /**
   * @see
   * org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.repository.NodeRef,
   * org.alfresco.service.cmr.repository.NodeRef)
   */
  @Override
  protected void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef) {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Starting transformation to pdf for " + actionedUponNodeRef);
    }

    {

      NodeRef sourceFolder = (NodeRef) ruleAction.getParameterValue(PARAM_SOURCE_FOLDER);
      String sourceFilename = (String) ruleAction.getParameterValue(PARAM_SOURCE_FILENAME);
      String mimeType = (String) ruleAction.getParameterValue(PARAM_MIME_TYPE);
      NodeRef destinationParent = (NodeRef) ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER);
      QName destinationAssocTypeQName = (QName) ruleAction.getParameterValue(PARAM_ASSOC_TYPE_QNAME);
      QName destinationAssocQName = (QName) ruleAction.getParameterValue(PARAM_ASSOC_QNAME);
      Boolean overwriteValue = (Boolean) ruleAction.getParameterValue(PARAM_OVERWRITE_COPY);
      Boolean addExtensionValue = (Boolean) ruleAction.getParameterValue(PARAM_ADD_EXTENSION);
      String targetName = (String) ruleAction.getParameterValue(PARAM_TARGET_NAME);
      try {
        {
          Map<String, Serializable> auditValues = new HashMap<>();
          auditValues.put("/node", actionedUponNodeRef);
          auditValues.put("/pre/params/" + PARAM_SOURCE_FOLDER, sourceFolder);
          auditValues.put("/pre/params/" + PARAM_SOURCE_FILENAME, sourceFilename);
          auditValues.put("/pre/params/" + PARAM_MIME_TYPE, mimeType);
          auditValues.put("/pre/params/" + PARAM_DESTINATION_FOLDER, destinationParent);
          auditValues.put("/pre/params/" + PARAM_ASSOC_TYPE_QNAME, destinationAssocTypeQName);
          auditValues.put("/pre/params/" + PARAM_ASSOC_QNAME, destinationAssocQName);
          auditValues.put("/pre/params/" + PARAM_OVERWRITE_COPY, overwriteValue);
          auditValues.put("/pre/params/" + PARAM_ADD_EXTENSION, addExtensionValue);
          auditValues.put("/pre/params/" + PARAM_TARGET_NAME, targetName);

          auditComponent.recordAuditValues("/" + AUDIT_APPLICATION_NAME + "/action/" + NAME, auditValues);
        }
        if (sourceFolder != null && sourceFilename != null && nodeService.exists(sourceFolder)) {
          List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(sourceFolder);
          for (ChildAssociationRef childAssoc : childAssocs) {
            NodeRef childRef = childAssoc.getChildRef();
            Serializable property = nodeService.getProperty(childRef, ContentModel.PROP_NAME);
            if (sourceFilename.equals(property)) {
              actionedUponNodeRef = childRef;
              break;
            }
          }
        }
        if (this.nodeService.exists(actionedUponNodeRef) == false) {
          // node doesn't exist - can't do anything
          return;
        }
        // First check that the node is a sub-type of content
        QName typeQName = this.nodeService.getType(actionedUponNodeRef);
        if (this.dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT) == false) {
          // it is not content, so can't transform
          return;
        }

        // Get the content reader
        ContentReader contentReader = this.contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
        if (null == contentReader || !contentReader.exists()) {
          throw new RuleServiceException(CONTENT_READER_NOT_FOUND_MESSAGE);
        }

        TransformationOptions options = newTransformationOptions(ruleAction, actionedUponNodeRef);
        // getExecuteAsychronously() is not true for async convert content rules, so using Thread name
        //        options.setUse(ruleAction.getExecuteAsychronously() ? "asyncRule" :"syncRule");
        options.setUse(Thread.currentThread().getName().contains("Async") ? "asyncRule" : "syncRule");
        if (null == contentService.getTransformer(contentReader.getContentUrl(), contentReader.getMimetype(), contentReader.getSize(), mimeType, options)) {
          throw new RuleServiceException(String.format(TRANSFORMER_NOT_EXISTS_MESSAGE_PATTERN, contentReader.getMimetype(), mimeType));
        }

        // default the assoc params if they're not present
        if (destinationAssocTypeQName == null) {
          destinationAssocTypeQName = ContentModel.ASSOC_CONTAINS;
        }
        if (destinationAssocQName == null) {
          destinationAssocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "copy");
        }

        // Get the overwrite value
        boolean overwrite = true;

        if (overwriteValue != null) {
          overwrite = overwriteValue.booleanValue();
        }

        // Calculate the destination name
        String originalName = (String) nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_NAME);

        String selectedName;
        if (targetName != null) {
          selectedName = targetName;
        } else {
          selectedName = originalName;
        }
        String newMimetype = mimeType;
        if (FAKE_MIMETYPE_PDFA.equalsIgnoreCase(mimeType)) {
          newMimetype = MimetypeMap.MIMETYPE_PDF;
        }
        // Get the overwrite value
        boolean addExtension = true;

        if (addExtensionValue != null) {
          addExtension = addExtensionValue.booleanValue();
        }
        String newName = transformName(this.mimetypeService, selectedName, newMimetype, addExtension);

        // Since we are overwriting we need to figure out whether the destination node exists
        NodeRef copyNodeRef = null;
        if (overwrite == true) {
          // Try and find copies of the actioned upon node reference.
          // Include the parent folder because that's where the copy will be if this action
          // had done the first copy.
          PagingResults<CopyService.CopyInfo> copies = copyService.getCopies(
                  actionedUponNodeRef,
                  destinationParent,
                  new PagingRequest(1000));
          for (CopyService.CopyInfo copyInfo : copies.getPage()) {
            NodeRef copy = copyInfo.getNodeRef();
            String copyName = copyInfo.getName();
            // We know that it is in the destination parent, but avoid working copies
            if (checkOutCheckInService.isWorkingCopy(copy)) {
              // It is a working copy
              continue;
            } else if (!newName.equals(copyName)) {
              // The copy's name is not what this action would have set it to
              continue;
            }
            if (copyNodeRef == null) {
              copyNodeRef = copy;
            } else {
              throw new RuleServiceException(ERR_OVERWRITE);
            }
          }
        }

        if (copyNodeRef == null) {
          // Copy the content node
          copyNodeRef = this.copyService.copy(
                  actionedUponNodeRef,
                  destinationParent,
                  destinationAssocTypeQName,
                  QName.createQName(destinationAssocQName.getNamespaceURI(), newName));

          // Adjust the name of the copy
          nodeService.setProperty(copyNodeRef, ContentModel.PROP_NAME, newName);
          String originalTitle = (String) nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_TITLE);

          if (originalTitle != null) {
            nodeService.setProperty(copyNodeRef, ContentModel.PROP_TITLE, originalTitle);
          }
        }

        // Only do the transformation if some content is available
        if (contentReader != null) {
          // get the writer and set it up
          ContentWriter contentWriter = this.contentService.getWriter(copyNodeRef, ContentModel.PROP_CONTENT, true);
          contentWriter.setMimetype(newMimetype);                        // new mimetype
          contentWriter.setEncoding(contentReader.getEncoding());     // original encoding

          // Try and transform the content - failures are caught and allowed to fail silently.
          // This is unique to this action, and is essentially a broken pattern.
          // Clients should rather get the exception and then decide to replay with rules/actions turned off or not.
          // TODO: Check failure patterns for actions.
          try {
            doTransform(ruleAction, actionedUponNodeRef, contentReader, copyNodeRef, contentWriter);
            ruleAction.setParameterValue(PARAM_RESULT, copyNodeRef);
          } catch (NoTransformerException e) {
            if (LOGGER.isDebugEnabled()) {
              LOGGER.debug("No transformer found to execute rule: \n"
                      + "   reader: " + contentReader + "\n"
                      + "   writer: " + contentWriter + "\n"
                      + "   action: " + this);
            }
            throw new RuleServiceException(TRANSFORMING_ERROR_MESSAGE + e.getMessage());
          }
        }

        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("Finished transformation to pdf for " + actionedUponNodeRef);
        }
        {
          Map<String, Serializable> auditValues = new HashMap<>();
          auditValues.put("/node", actionedUponNodeRef);
          auditValues.put("/post/params/" + PARAM_SOURCE_FOLDER, sourceFolder);
          auditValues.put("/post/params/" + PARAM_SOURCE_FILENAME, sourceFilename);
          auditValues.put("/post/params/" + PARAM_MIME_TYPE, mimeType);
          auditValues.put("/post/params/" + PARAM_DESTINATION_FOLDER, destinationParent);
          auditValues.put("/post/params/" + PARAM_ASSOC_TYPE_QNAME, destinationAssocTypeQName);
          auditValues.put("/post/params/" + PARAM_ASSOC_QNAME, destinationAssocQName);
          auditValues.put("/post/params/" + PARAM_OVERWRITE_COPY, overwriteValue);
          auditValues.put("/post/params/" + PARAM_ADD_EXTENSION, addExtensionValue);
          auditValues.put("/post/params/" + PARAM_TARGET_NAME, targetName);
          auditValues.put("/post/target/node", copyNodeRef);
          auditValues.put("/post/target/name", newName);

          auditComponent.recordAuditValues("/" + AUDIT_APPLICATION_NAME + "/action/" + NAME, auditValues);
        }
      } catch (Exception e) {
        Map<String, Serializable> auditValues = new HashMap<>();
        auditValues.put("/node", actionedUponNodeRef);
        auditValues.put("/error/message", e.getMessage());
        Throwable cause = e.getCause();
        int i = 1;
        while (cause != null) {
          auditValues.put("/error/causes/" + i, cause.getMessage());
          cause = cause.getCause();
          i++;
        }
        auditValues.put("/error/params/" + PARAM_SOURCE_FOLDER, sourceFolder);
        auditValues.put("/error/params/" + PARAM_SOURCE_FILENAME, sourceFilename);
        auditValues.put("/error/params/" + PARAM_MIME_TYPE, mimeType);
        auditValues.put("/error/params/" + PARAM_DESTINATION_FOLDER, destinationParent);
        auditValues.put("/error/params/" + PARAM_ASSOC_TYPE_QNAME, destinationAssocTypeQName);
        auditValues.put("/error/params/" + PARAM_ASSOC_QNAME, destinationAssocQName);
        auditValues.put("/error/params/" + PARAM_OVERWRITE_COPY, overwriteValue);
        auditValues.put("/error/params/" + PARAM_ADD_EXTENSION, addExtensionValue);
        auditValues.put("/error/params/" + PARAM_TARGET_NAME, targetName);

        auditComponent.recordAuditValues("/" + AUDIT_APPLICATION_NAME + "/action/" + NAME, auditValues);
        throw e;
      } finally {

      }
    }
  }

  protected TransformationOptions newTransformationOptions(Action ruleAction, NodeRef sourceNodeRef) {
    return new TransformationOptions(sourceNodeRef, ContentModel.PROP_NAME, null, ContentModel.PROP_NAME);
  }

  /**
   * Executed in a new transaction so that failures don't cause the entire
   * transaction to rollback.
   */
  protected void doTransform(Action ruleAction,
          NodeRef sourceNodeRef, ContentReader contentReader,
          NodeRef destinationNodeRef, ContentWriter contentWriter) {
    // transform - will throw NoTransformerException if there are no transformers
    TransformationOptions options = newTransformationOptions(ruleAction, sourceNodeRef);
    options.setTargetNodeRef(destinationNodeRef);
    this.contentService.transform(contentReader, contentWriter, options);
  }

  /**
   * Transform a name from original extension to new extension, if appropriate.
   * If the original name seems to end with a reasonable file extension, then
   * the name will be transformed such that the old extension is replaced with
   * the new. Otherwise the name will be returned unaltered.
   * <P/>
   * The original name will be deemed to have a reasonable extension if there
   * are one or more characters after the (required) final dot, none of which
   * are spaces.
   *
   * @param mimetypeService the mimetype service
   * @param original the original name
   * @param newMimetype the new mime type
   * @param alwaysAdd if the name has no extension, then add the new one
   *
   * @return name with new extension as appropriate for the mimetype
   */
  public static String transformName(MimetypeService mimetypeService, String original, String newMimetype, boolean alwaysAdd) {
    // get the current extension
    int dotIndex = original.lastIndexOf('.');
    StringBuilder sb = new StringBuilder(original.length());
    if (dotIndex > -1) {
      // we found it
      String nameBeforeDot = original.substring(0, dotIndex);
      String originalExtension = original.substring(dotIndex + 1, original.length());

      // See ALF-1937, which actually relates to cm:title not cm:name.
      // It is possible that the text after the '.' is not actually an extension
      // in which case it should not be replaced.
      boolean originalExtensionIsReasonable = isExtensionReasonable(originalExtension);
      String newExtension = mimetypeService.getExtension(newMimetype);
      if (originalExtensionIsReasonable) {
        sb.append(nameBeforeDot);
        sb.append('.').append(newExtension);
      } else {
        sb.append(original);
        if (alwaysAdd == true) {
          if (sb.charAt(sb.length() - 1) != '.') {
            sb.append('.');
          }
          sb.append(newExtension);
        }
      }
    } else {
      // no extension so don't add a new one
      sb.append(original);

      if (alwaysAdd == true) {
        // add the new extension - defaults to .bin
        String newExtension = mimetypeService.getExtension(newMimetype);
        sb.append('.').append(newExtension);
      }
    }
    // done
    return sb.toString();
  }

  /**
   * Given a String, this method tries to determine whether it is a reasonable
   * filename extension. There are a number of checks that could be performed
   * here, including checking whether the characters in the string are all
   * 'letters'. However these are complicated by unicode and other
   * considerations. Therefore this method adopts a simple approach and returns
   * true if the string is of non-zero length and contains no spaces.
   *
   * @param potentialExtensionString the string which may be a file extension.
   * @return <code>true</code> if it is deemed reasonable, else
   * <code>false</code>
   * @since 3.3
   */
  private static boolean isExtensionReasonable(String potentialExtensionString) {
    return potentialExtensionString.length() > 0 && potentialExtensionString.indexOf(' ') == -1;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(dictionaryService);
    Assert.notNull(nodeService);
    Assert.notNull(checkOutCheckInService);
    Assert.notNull(contentService);
    Assert.notNull(copyService);
    Assert.notNull(mimetypeService);
    Assert.notNull(auditComponent);
    Assert.notNull(dictionaryService);
    Assert.notNull(dictionaryService);
    Assert.notNull(dictionaryService);
  }

}
