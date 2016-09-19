package org.redpill.alfresco.archive.repo.it.action.executor;

import org.alfresco.model.ContentModel;
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

import  org.junit.Assert;

public class ConvertToPdfActionExecuterIntegrationTest extends AbstractRepoIntegrationTest{
  private static final String RENDITION_NAME_PDF = "pdf";
  private static final String RENDITION_NAME_PDFA = "pdfa";

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

  
  @Test
  public void testConvertDocxToPdf() throws InterruptedException {
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
  
  @Test
  public void testConvertPdfToPdfa() throws InterruptedException {
    NodeRef document = uploadDocument(site, "test.pdf").getNodeRef();
    
    Action action = actionService.createAction(ConvertToPdfActionExecuter.NAME);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_MIME_TYPE, ConvertToPdfActionExecuter.FAKE_MIMETYPE_PDFA);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_DESTINATION_FOLDER, document);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_TYPE_QNAME, RenditionModel.ASSOC_RENDITION);
    QName renditionQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) RENDITION_NAME_PDFA);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_QNAME, renditionQName);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_TARGET_NAME, RENDITION_NAME_PDFA);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_OVERWRITE_COPY, false);
    
    actionService.executeAction(action, document);
    
    //Assert that there is a child node with name pdfa
    NodeRef renditionedNode = _nodeService.getChildByName(document, RenditionModel.ASSOC_RENDITION, RENDITION_NAME_PDFA);
    
    //Assert.assertEquals(RENDITION_NAME_PDFA, (String)_nodeService.getProperty(renditionedNode, ContentModel.PROP_NAME));
  }
  @Override
  public void afterClassSetup() {
    deleteSite(site);

    _authenticationComponent.setCurrentUser(_authenticationComponent.getSystemUserName());

    deleteUser(DEFAULT_USERNAME);

    _authenticationComponent.clearCurrentSecurityContext();
  }
}
