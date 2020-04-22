package org.redpill.alfresco.archive.repo.action.executor;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.exec.RuntimeExec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Make sure this action is called from a new read-only transaction to prevent failure to destroy your current transaction
 */
public class VeraPdfValidatorActionExecuter extends ActionExecuterAbstractBase implements InitializingBean {
  public static final String NAME = "archive-toolkit-vera-pdf-validation";
  private final static Log LOG = LogFactory.getLog(VeraPdfValidatorActionExecuter.class);
  private static final String XML_VALIDATION_COMPLIANT = "<validationReports compliant=\"1\" nonCompliant=\"0\" failedJobs=\"0\">";

  protected RuntimeExec checkCommand;
  protected RuntimeExec validationCommand;
  static protected boolean isActive;

  private NodeService nodeService;
  private ContentService contentService;
  /*
   * Parameters
   */
  public static final String PARAM_VALIDATION_FLAVOUR = "validation-flavour";

  @Override
  protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
    final String result;
    if (isActive) {
      if (actionedUponNodeRef == null || !nodeService.exists(actionedUponNodeRef)) {
        throw new NullPointerException("Node does not exist: " + actionedUponNodeRef);
      }
      String validationFlavour = (String) action.getParameterValue(PARAM_VALIDATION_FLAVOUR);
      if (validationFlavour == null || validationFlavour.isEmpty()) {
        validationFlavour = "0";
      }
      final ContentReader reader = contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
      if (reader == null) {
        throw new NullPointerException("No content reader available for node " + actionedUponNodeRef);
      }
      try (final InputStream contentInputStream = reader.getContentInputStream()) {
        if (contentInputStream == null) {
          throw new NullPointerException("No content available for node " + actionedUponNodeRef);
        }
        try {
          File veraPdfValidation = TempFileProvider.createTempFile(contentInputStream, "VeraPdfValidation", ".pdf");
          Map<String, String> validationParams = new HashMap<>();
          validationParams.put("source", veraPdfValidation.getAbsolutePath());
          validationParams.put("flavour", validationFlavour);
          final RuntimeExec.ExecutionResult execute = validationCommand.execute(validationParams);

          if (execute.getSuccess()) {
            result = execute.getStdOut();
            LOG.debug("Validation result: " + result);
          } else {
            String err = execute.getStdErr();
            LOG.debug("Validation result stdErr: " + err);
            result = execute.getStdOut();
            LOG.debug("Validation result: " + result);
          }
        } catch (Exception e) {
          LOG.error("Error while trying to validate pdf with VeraPDF", e);
          throw new VeraPdfValidationException("Error while trying to validate pdf with VeraPDF: " + actionedUponNodeRef, e);
        }
      } catch (IOException e) {
        LOG.error("Error while trying to validate pdf with VeraPDF", e);
        throw new VeraPdfValidationException("Error while trying to validate pdf with VeraPDF: " + actionedUponNodeRef, e);
      }
    } else {
      LOG.error("Validation not possible due to tool not being available.: \" + actionedUponNodeRef");
      throw new VeraPdfValidationException("Validation not possible due to tool not being available.: " + actionedUponNodeRef);
    }

    if (result == null || !result.contains(XML_VALIDATION_COMPLIANT)) {
      LOG.debug("PDF/a Validation failed: " + actionedUponNodeRef);
      LOG.trace(result);
      throw new VeraPdfValidationException("PDF/a Validation failed: " + actionedUponNodeRef, result);
    }
  }


  /**
   * Add parameter definitions
   */
  @Override
  protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
    paramList.add(new ParameterDefinitionImpl(PARAM_VALIDATION_FLAVOUR, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_VALIDATION_FLAVOUR)));
  }


  public void setCheckCommand(RuntimeExec checkCommand) {
    this.checkCommand = checkCommand;
  }

  public void setValidationCommand(RuntimeExec validationCommand) {
    this.validationCommand = validationCommand;
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(checkCommand, "checkCommand is null");
    Assert.notNull(validationCommand, "validationCommand is null");
    Assert.notNull(nodeService, "nodeService is null");
    Assert.notNull(contentService, "contentService is null");

    final RuntimeExec.ExecutionResult execute = checkCommand.execute();
    if (!execute.getSuccess()) {
      StringBuilder sb = new StringBuilder();
      sb.append(validationCommand.getCommand());
      LOG.error("Could not verify that veraPDF was available. \nValidation command: " + sb.toString() + "\nStdErr: " + execute.getStdErr() + "\nStdOut: " + execute.getStdOut());
      isActive = false;
    } else {
      isActive = true;
    }

  }
}
