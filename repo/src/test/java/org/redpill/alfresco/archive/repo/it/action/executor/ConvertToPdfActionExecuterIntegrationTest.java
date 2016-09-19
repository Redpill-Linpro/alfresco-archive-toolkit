package org.redpill.alfresco.archive.repo.it.action.executor;

import org.alfresco.model.RenditionModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.redpill.alfresco.archive.repo.action.executor.ConvertToPdfActionExecuter;
import org.redpill.alfresco.test.AbstractRepoIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class ConvertToPdfActionExecuterIntegrationTest extends AbstractRepoIntegrationTest{
  private static final String RENDITION_NAME_PDF = "pdf";

  private static final String DEFAULT_USERNAME = "testuser_" + System.currentTimeMillis();

  private static SiteInfo site;
  
  @Autowired
  @Qualifier("ActionService")
  private ActionService actionService;
  
  @Override
  public void beforeClassSetup() {
    super.beforeClassSetup();

    createUser(DEFAULT_USERNAME);

    _authenticationComponent.setCurrentUser(DEFAULT_USERNAME);

    site = createSite();
  }

  /**
   *               <param name="function">onActionSimpleRepoAction</param>
              <param name="action">archive-toolkit-transform-to-pdf</param>
              <param name="mime-type">application/pdf</param>
              <param name="destination-folder">{nodeRef}</param>
              <param name="assoc-type">rn:rendition</param>
              <param name="assoc-name">cm:pdf</param>
              <param name="target-name">pdf</param>
              <param name="overwrite-copy">false</param>
              <param name="successMessage">message.archive-toolkit-convert-to-pdf-rendition.success</param>
              <param name="failureMessage">message.archive-toolkit-convert-to-pdf-rendition.failure</param>
              
               public static final String PARAM_MIME_TYPE = "mime-type";
  public static final String PARAM_DESTINATION_FOLDER = "destination-folder";
  public static final String PARAM_ASSOC_TYPE_QNAME = "assoc-type";
  public static final String PARAM_ASSOC_QNAME = "assoc-name";
  public static final String PARAM_TARGET_NAME = "target-name";
  public static final String PARAM_OVERWRITE_COPY = "overwrite-copy";

  public static final String FAKE_MIMETYPE_PDFA = "application/pdfa";

   */
  @Test
  public void testConvertToPdf() throws InterruptedException {
    NodeRef document = uploadDocument(site, "test.docx").getNodeRef();
    
    Action action = actionService.createAction(ConvertToPdfActionExecuter.NAME);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_MIME_TYPE, MimetypeMap.MIMETYPE_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_DESTINATION_FOLDER, document);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_TYPE_QNAME, RenditionModel.ASSOC_RENDITION);
    QName renditionQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) RENDITION_NAME_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_QNAME, renditionQName);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_TARGET_NAME, RENDITION_NAME_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_OVERWRITE_COPY, false);
    
    actionService.executeAction(action, document);
  }
  @Override
  public void afterClassSetup() {
    deleteSite(site);

    _authenticationComponent.setCurrentUser(_authenticationComponent.getSystemUserName());

    deleteUser(DEFAULT_USERNAME);

    _authenticationComponent.clearCurrentSecurityContext();
  }
}
