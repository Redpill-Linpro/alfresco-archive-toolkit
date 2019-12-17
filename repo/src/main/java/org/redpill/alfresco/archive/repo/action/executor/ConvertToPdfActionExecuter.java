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

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.audit.AuditComponent;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.rule.RuleServiceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.redpill.alfresco.archive.repo.service.ArchiveToolkitService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  private static final String ERR_SOURCE_NOT_SUBTYPE_OF_CONTENT = "Source node is not subtype of cm:content";
  private static final String ERR_TARGET_NOT_SUBTYPE_OF_CONTENT = "Target type is not subtype of cm:content";


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
  public static final String PARAM_TARGET_TYPE = "target-type";
  public static final String PARAM_TIMEOUT = "timeout";
  public static final Long DEFAULT_TIMEOUT = 60000L; //Timeout in MS
  public static final String AUDIT_APPLICATION_NAME = "alfresco-archive-toolkit";
  /*
   * Injected services
   */
  protected DictionaryService dictionaryService;
  protected NodeService nodeService;
  protected CheckOutCheckInService checkOutCheckInService;
  protected ContentService contentService;
  protected CopyService copyService;
  protected MimetypeService mimetypeService;
  protected AuditComponent auditComponent;
  protected RetryingTransactionHelper retryingTransactionHelper;
  protected FileFolderService fileFolderService;
  protected ArchiveToolkitService archiveToolkitService;
  protected RenditionService renditionService;

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
    paramList.add(new ParameterDefinitionImpl(PARAM_TARGET_TYPE, DataTypeDefinition.QNAME, false, getParamDisplayLabel(PARAM_TARGET_TYPE)));
    paramList.add(new ParameterDefinitionImpl(PARAM_TIMEOUT, DataTypeDefinition.LONG, false, getParamDisplayLabel(PARAM_TIMEOUT)));
  }

  /**
   * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(Action, NodeRef)
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
      QName targetType = (QName) ruleAction.getParameterValue(PARAM_TARGET_TYPE);

      Long timeout = (Long) ruleAction.getParameterValue(PARAM_TIMEOUT);
      if (timeout == null) {
        timeout = DEFAULT_TIMEOUT;
      }
      try {
        {

          auditPre(actionedUponNodeRef, sourceFolder, sourceFilename, mimeType, destinationParent, destinationAssocTypeQName, destinationAssocQName, overwriteValue, addExtensionValue, targetName, targetType, timeout);
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
          throw new RuleServiceException(ERR_SOURCE_NOT_SUBTYPE_OF_CONTENT);
        }

        // First check that the node is a sub-type of content
        if (targetType != null && this.dictionaryService.isSubClass(targetType, ContentModel.TYPE_CONTENT) == false) {
          throw new RuleServiceException(ERR_TARGET_NOT_SUBTYPE_OF_CONTENT);
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

        //Set a timeout
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("Setting timeout to " + timeout);
        }
        options.setTimeoutMs(timeout);

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

        if (targetType == null) {
          //Default to content type
          targetType = ContentModel.TYPE_CONTENT;
        }
        // Since we are overwriting we need to figure out whether the destination node exists
        NodeRef destinationNodeRef = null;
        if (overwrite == true) {
          List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(destinationParent);

          for (ChildAssociationRef child : childAssocs) {
            NodeRef childNodeRef = child.getChildRef();
            String childName = (String) nodeService.getProperty(childNodeRef, ContentModel.PROP_NAME);

            // We know that it is in the destination parent, but avoid working copies
            if (checkOutCheckInService.isWorkingCopy(childNodeRef)) {
              // It is a working copy, skip it
            } else if (newName.equals(childName)) {
              destinationNodeRef = childNodeRef;
              break;
            }

          }
        }

        if (destinationNodeRef == null) {
          Map<QName, Serializable> properties = new HashMap<>();
          properties.put(ContentModel.PROP_NAME, newName);
          String originalTitle = (String) nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_TITLE);

          if (originalTitle != null) {
            properties.put(ContentModel.PROP_TITLE, originalTitle);
          }
          ChildAssociationRef createNode = nodeService.createNode(destinationParent, destinationAssocTypeQName, destinationAssocQName, targetType, properties);
          destinationNodeRef = createNode.getChildRef();

        }

        // Only do the transformation if some content is available
        // get the writer and set it up
        ContentWriter contentWriter = this.contentService.getWriter(destinationNodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype(mimeType);                        // new mimetype
        contentWriter.setEncoding(contentReader.getEncoding());     // original encoding

        // Try and transform the content - failures are caught and allowed to fail silently.
        // This is unique to this action, and is essentially a broken pattern.
        // Clients should rather get the exception and then decide to replay with rules/actions turned off or not.
        // TODO: Check failure patterns for actions.
        try {
          //Set a timeout
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Calling transformation for: " + actionedUponNodeRef + " timeout set to " + timeout);
          }
          doTransform(ruleAction, actionedUponNodeRef, contentReader, destinationNodeRef, contentWriter, timeout);

          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Transformation done to destination nodeRef: " + destinationNodeRef + " setting content property.");
          }
          ruleAction.setParameterValue(PARAM_RESULT, destinationNodeRef);
        } catch (NoTransformerException e) {
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("No transformer found to execute rule: \n"
              + "   reader: " + contentReader + "\n"
              + "   writer: " + contentWriter + "\n"
              + "   action: " + this);
          }
          throw new RuleServiceException(TRANSFORMING_ERROR_MESSAGE, e);
        }

        //ContentData contentData = contentWriter.getContentData();
        ContentData contentData = (ContentData) nodeService.getProperty(destinationNodeRef, ContentModel.PROP_CONTENT);
        if (FAKE_MIMETYPE_PDFA.equalsIgnoreCase(contentData.getMimetype())) {
          ContentData newContentData = ContentData.setMimetype(contentData, MimetypeMap.MIMETYPE_PDF);
          nodeService.setProperty(destinationNodeRef, ContentModel.PROP_CONTENT, newContentData);
        }

        // To avoid thumbnail node node beeing marked as incomplete, we need to add the targetContentProperty
        if (ContentModel.TYPE_THUMBNAIL.equals(nodeService.getType(destinationNodeRef))) {
          nodeService.setProperty(destinationNodeRef, ContentModel.PROP_CONTENT_PROPERTY_NAME, ContentModel.PROP_CONTENT);

          // is the destination assoc a rendition, alfresco states it must have a correct rendition aspect
          if (destinationAssocTypeQName != null && destinationAssocTypeQName.equals(RenditionModel.ASSOC_RENDITION)) {
            // Now add one of the two aspects depending on parent location.
            ChildAssociationRef sourceNode = renditionService.getSourceNode(destinationNodeRef);
            ChildAssociationRef primaryParent = nodeService.getPrimaryParent(destinationNodeRef);
            QName aspectToApply;
            if (primaryParent.getParentRef().equals(sourceNode.getParentRef())) {
              aspectToApply = RenditionModel.ASPECT_HIDDEN_RENDITION;
            } else {
              aspectToApply = RenditionModel.ASPECT_VISIBLE_RENDITION;
            }

            if (LOGGER.isDebugEnabled()) {
              StringBuilder msg = new StringBuilder();
              msg.append("Applying aspect ")
                .append(aspectToApply)
                .append(" to node ")
                .append(destinationNodeRef);
              LOGGER.debug(msg.toString());
            }
            nodeService.addAspect(destinationNodeRef, aspectToApply, null);
          }

        }

        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("Finished transformation to pdf for " + actionedUponNodeRef + " adding checksum calculation to node.");
        }

        try {
          archiveToolkitService.addChecksum(destinationNodeRef);
        } catch (Exception e) {
          // A failed checksum should not rollback everything, just log it
          LOGGER.warn("Failed to set checksum on nodeRef: " + destinationNodeRef);
        }

        {
          auditPost(actionedUponNodeRef, sourceFolder, sourceFilename, mimeType, destinationParent, destinationAssocTypeQName, destinationAssocQName, overwriteValue, addExtensionValue, targetName, destinationNodeRef, newName, targetType, timeout);
        }
      } catch (Exception e) {
        auditError(e, actionedUponNodeRef, sourceFolder, sourceFilename, mimeType, destinationParent, destinationAssocTypeQName, destinationAssocQName, overwriteValue, addExtensionValue, targetName, targetType, timeout);
        throw e;
      } finally {

      }
    }
  }

  protected void auditPre(NodeRef actionedUponNodeRef, NodeRef sourceFolder, String sourceFilename, String mimeType, NodeRef destinationParent, QName destinationAssocTypeQName, QName destinationAssocQName, Boolean overwriteValue, Boolean addExtensionValue, String targetName, QName targetType, Long timeout) {
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
    auditValues.put("/pre/params/" + PARAM_TARGET_TYPE, targetType);
    auditValues.put("/pre/params/" + PARAM_TIMEOUT, timeout);
    audit(auditValues);
  }

  protected void auditPost(NodeRef actionedUponNodeRef, NodeRef sourceFolder, String sourceFilename, String mimeType, NodeRef destinationParent, QName destinationAssocTypeQName, QName destinationAssocQName, Boolean overwriteValue, Boolean addExtensionValue, String targetName, NodeRef copyNodeRef, String newName, QName targetType, Long timeout) {
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
    auditValues.put("/post/params/" + PARAM_TARGET_TYPE, targetType);
    auditValues.put("/post/params/" + PARAM_TIMEOUT, timeout);
    auditValues.put("/post/target/node", copyNodeRef);
    auditValues.put("/post/target/name", newName);

    audit(auditValues);
  }

  protected void auditError(Exception e, NodeRef actionedUponNodeRef, NodeRef sourceFolder, String sourceFilename, String mimeType, NodeRef destinationParent, QName destinationAssocTypeQName, QName destinationAssocQName, Boolean overwriteValue, Boolean addExtensionValue, String targetName, QName targetType, Long timeout) {
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
    auditValues.put("/error/params/" + PARAM_TARGET_TYPE, targetType);
    auditValues.put("/post/params/" + PARAM_TIMEOUT, timeout);
    audit(auditValues);
  }

  protected void audit(final Map<String, Serializable> auditValues) {
    retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>() {
      @Override
      public Void execute() throws Throwable {
        auditComponent.recordAuditValues("/" + AUDIT_APPLICATION_NAME + "/action/" + NAME, auditValues);
        return null;
      }
    }, false, true);

  }

  protected TransformationOptions newTransformationOptions(Action ruleAction, NodeRef sourceNodeRef) {
    return new TransformationOptions(sourceNodeRef, ContentModel.PROP_NAME, null, ContentModel.PROP_NAME);
  }

  /**
   * Executed in a new transaction so that failures don't cause the entire
   * transaction to rollback.
   *
   * @param ruleAction         the action
   * @param sourceNodeRef      the source node
   * @param contentReader      the source reader
   * @param destinationNodeRef the destination node
   * @param contentWriter      the destination reader
   * @param timeout            timeout in ms for the transformation
   */
  protected synchronized void doTransform(Action ruleAction,
                             NodeRef sourceNodeRef, ContentReader contentReader,
                             NodeRef destinationNodeRef, ContentWriter contentWriter, Long timeout) {
    // transform - will throw NoTransformerException if there are no transformers
    TransformationOptions options = newTransformationOptions(ruleAction, sourceNodeRef);
    options.setTimeoutMs(timeout);
    options.setTargetNodeRef(destinationNodeRef);
    this.contentService.transform(contentReader, contentWriter, options);
  }

  /**
   * Transform a name from original extension to new extension, if appropriate.
   * If the original name seems to end with a reasonable file extension, then
   * the name will be transformed such that the old extension is replaced with
   * the new. Otherwise the name will be returned unaltered.
   *
   * The original name will be deemed to have a reasonable extension if there
   * are one or more characters after the (required) final dot, none of which
   * are spaces.
   *
   * @param mimetypeService the mimetype service
   * @param original        the original name
   * @param newMimetype     the new mime type
   * @param alwaysAdd       if the name has no extension, then add the new one
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

  /**
   * Sets the transaction helper
   *
   * @param retryingTransactionHelper transaction helper
   */
  public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper) {
    this.retryingTransactionHelper = retryingTransactionHelper;
  }

  /**
   * Sets the audit component
   *
   * @param auditComponent audit component
   */
  public void setAuditComponent(AuditComponent auditComponent) {
    this.auditComponent = auditComponent;
  }

  /**
   * Set the mime type service
   *
   * @param mimetypeService mimetype service
   */
  public void setMimetypeService(MimetypeService mimetypeService) {
    this.mimetypeService = mimetypeService;
  }

  /**
   * Set the node service
   *
   * @param nodeService node service
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * Set the service to determine check-in and check-out status
   *
   * @param checkOutCheckInService checkout checkin service
   */
  public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService) {
    this.checkOutCheckInService = checkOutCheckInService;
  }

  /**
   * Set the dictionary service
   *
   * @param dictionaryService dictionary service
   */
  @Override
  public void setDictionaryService(DictionaryService dictionaryService) {
    this.dictionaryService = dictionaryService;
  }

  /**
   * Set the content service
   *
   * @param contentService content service
   */
  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  /**
   * Set the copy service
   *
   * @param copyService copy service
   */
  public void setCopyService(CopyService copyService) {
    this.copyService = copyService;
  }

  /**
   * Set the rendition service
   *
   * @param renditionService rendition service
   */
  public void setRenditionService(RenditionService renditionService) {
    this.renditionService = renditionService;
  }

  /**
   * Set the file folder service
   *
   * @param fileFolderService file folder service
   */
  public void setFileFolderService(FileFolderService fileFolderService) {
    this.fileFolderService = fileFolderService;
  }

  /**
   * Set the archive toolkit service
   * @param archiveToolkitService archive toolkit service
   */
  public void setArchiveToolkitService(ArchiveToolkitService archiveToolkitService) {
    this.archiveToolkitService = archiveToolkitService;
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
    Assert.notNull(retryingTransactionHelper);
    Assert.notNull(fileFolderService);
    Assert.notNull(archiveToolkitService);
    Assert.notNull(renditionService);
  }

}
