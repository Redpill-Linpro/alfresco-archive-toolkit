package org.redpill.alfresco.archive.repo.action.executor;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

public class VeraPdfValidatorActionExecuterMock extends VeraPdfValidatorActionExecuter {


  @Override
  public void execute(Action action, NodeRef actionedUponNodeRef) {
    executeImpl(action, actionedUponNodeRef);
  }
}
