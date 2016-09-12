package org.redpill.alfresco.archive.repo.action.executor;

import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

public class ConvertPdfToPdfaActionExecuter extends ActionExecuterAbstractBase {

  private ActionService actionService;
  
  public void setActionService(ActionService actionService) {
    this.actionService = actionService;
  }
  
  @Override
  protected void executeImpl(Action action, NodeRef nodeRef) {
    Action createPdf = actionService.createAction(CreatePdfActionExecuter.NAME);
    createPdf.setParameterValue(CreatePdfActionExecuter.PARAM_MIME_TYPE, "application/pdfa");

  }

  @Override
  protected void addParameterDefinitions(List<ParameterDefinition> arg0) {
    // TODO Auto-generated method stub

  }

}
