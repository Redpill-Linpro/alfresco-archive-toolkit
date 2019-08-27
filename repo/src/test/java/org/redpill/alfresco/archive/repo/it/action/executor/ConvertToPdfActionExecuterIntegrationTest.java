package org.redpill.alfresco.archive.repo.it.action.executor;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleServiceException;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.redpill.alfresco.archive.repo.action.executor.ConvertToPdfActionExecuter;
import org.redpill.alfresco.archive.repo.model.ArchiveToolkitModel;
import org.redpill.alfresco.test.AbstractRepoIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.verapdf.pdfa.*;
import org.verapdf.pdfa.flavours.PDFAFlavour;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ConvertToPdfActionExecuterIntegrationTest extends AbstractRepoIntegrationTest {

  private static final String RENDITION_NAME_PDF = "pdf";
  private static final String RENDITION_NAME_PDFA = "pdfa";

  private static final String DEFAULT_USERNAME = "testuser_" + System.currentTimeMillis();

  private static SiteInfo site;

  @Autowired
  @Qualifier("ActionService")
  private ActionService actionService;

  @Autowired
  @Qualifier("AuditService")
  private AuditService auditService;

  @Override
  public void beforeClassSetup() {
    super.beforeClassSetup();

    createUser(DEFAULT_USERNAME);

    _authenticationComponent.setCurrentUser(DEFAULT_USERNAME);

    site = createSite();
  }


  protected boolean validatePdfaVeraPdf(InputStream is) throws Exception {
    // The veraPDF library is unaware of the implementations and needs to be
    // initialised before first use
    VeraGreenfieldFoundryProvider.initialise();
    PDFAParser parser;
    PDFAFlavour flavour = PDFAFlavour.PDFA_1_B;
    org.verapdf.pdfa.results.ValidationResult vr = null;
    try (VeraPDFFoundry foundry = Foundries.defaultInstance();
         PDFAValidator validator = foundry.createValidator(flavour, false)) {
      parser = foundry.createParser(is, flavour);
      vr = validator.validate(parser);

      if (vr == null || !vr.isCompliant()) {
        throw new Exception("Failed to validate pdf/a, test Assertions: " + (vr == null ? "null" : (vr.getTestAssertions().toString())));
      }
    }
    return vr.isCompliant();
  }

  @Test
  public void testConvertOdtToPdf() throws Exception {

    NodeRef document = uploadDocument(site, "test.docx", null, null, "test" + System.currentTimeMillis() + ".docx").getNodeRef();

    //Make sure we have an odt document. For some reason Alfresco does not seem to detect it automatically. Instead it recognizes an application/zip.
    //ContentData cd = (ContentData) _nodeService.getProperty(document, ContentModel.PROP_CONTENT);
    //ContentData newCD = ContentData.setMimetype(cd, MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT);
    //_nodeService.setProperty(document, ContentModel.PROP_CONTENT, newCD);
    Action action = actionService.createAction(ConvertToPdfActionExecuter.NAME);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_MIME_TYPE, MimetypeMap.MIMETYPE_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_DESTINATION_FOLDER, document);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_TYPE_QNAME, RenditionModel.ASSOC_RENDITION);
    QName renditionQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) RENDITION_NAME_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_QNAME, renditionQName);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_TARGET_NAME, RENDITION_NAME_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ADD_EXTENSION, true);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_OVERWRITE_COPY, false);

    actionService.executeAction(action, document);
    List<ChildAssociationRef> childAssocs = _nodeService.getChildAssocs(document);
    assertNotNull(childAssocs);
    assertEquals(1, childAssocs.size());
    ChildAssociationRef childNode = childAssocs.get(0);
    NodeRef pdfNodeRef = childNode.getChildRef();
    assertNotNull(pdfNodeRef);
    //Assert that there is a child node with name pdf.pdf
    assertEquals("pdf.pdf", _nodeService.getProperty(pdfNodeRef, ContentModel.PROP_NAME));
    ContentReader reader = _contentService.getReader(pdfNodeRef, ContentModel.PROP_CONTENT);
    try (InputStream is = reader.getContentInputStream()) {
      boolean result = validatePdfaVeraPdf(is);
      assertFalse("Expected invalid pdf", result);
    }
  }

  protected void testConvert(String fileName) throws Exception {

    NodeRef document = uploadDocument(site, fileName, null, null, "test" + System.currentTimeMillis() + ".pdf").getNodeRef();

    Action action = actionService.createAction(ConvertToPdfActionExecuter.NAME);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_MIME_TYPE, ConvertToPdfActionExecuter.FAKE_MIMETYPE_PDFA);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_DESTINATION_FOLDER, document);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_TYPE_QNAME, RenditionModel.ASSOC_RENDITION);
    QName renditionQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) RENDITION_NAME_PDFA);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_QNAME, renditionQName);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_TARGET_NAME, RENDITION_NAME_PDFA);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ADD_EXTENSION, false);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_OVERWRITE_COPY, true);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_TARGET_TYPE, ContentModel.TYPE_THUMBNAIL);

    actionService.executeAction(action, document);

    List<ChildAssociationRef> childAssocs = _nodeService.getChildAssocs(document);
    assertNotNull(childAssocs);
    assertEquals(1, childAssocs.size());
    ChildAssociationRef childNode = childAssocs.get(0);
    NodeRef pdfANodeRef = childNode.getChildRef();
    assertNotNull(pdfANodeRef);
    //Assert that there is a child node with name pdfa
    assertEquals("pdfa", _nodeService.getProperty(pdfANodeRef, ContentModel.PROP_NAME));
    assertEquals(ContentModel.TYPE_THUMBNAIL, _nodeService.getType(pdfANodeRef));
    ContentReader reader = _contentService.getReader(pdfANodeRef, ContentModel.PROP_CONTENT);

    {
      //InputStream is = reader.getContentInputStream();
      File file = new File("/tmp/pdfa_1.pdf");
      if (!file.exists())
        assertTrue(file.createNewFile());
      assertTrue(file.exists());
      //FileUtils.copyInputStreamToFile(is, file);
      reader.getContent(file);
//      is.close();
    }
    reader = _contentService.getReader(pdfANodeRef, ContentModel.PROP_CONTENT);
    try (InputStream is = reader.getContentInputStream()) {
      boolean result = validatePdfaVeraPdf(is);
      assertTrue("Expected valid pdf", result);
    }
    ContentData contentData = reader.getContentData();

    assertEquals("Wrong mimetype", MimetypeMap.MIMETYPE_PDF, contentData.getMimetype());


    // Assert there is a checksum written.
    String checksum = (String) _nodeService.getProperty(pdfANodeRef, ArchiveToolkitModel.PROP_CHECKSUM);
    assertNotNull(checksum);
  }

  //@Test
  public void testConvertRGBPdfToPdfa() throws Exception {
    testConvert("test.pdf");
  }

  //@Test
  public void testConvertCMYKPdfToPdfa() throws Exception {
    testConvert("cmyk.pdf");
  }

  //@Test
  public void testConvertCallasProblematicPdfToPdfa() throws Exception {
    testConvert("callas.pdf");
  }

  //@Test
  public void testConvertExportedWebPagePdfToPdfa() throws Exception {
    testConvert("webpage.pdf");
  }

  @Test
  public void testConvertPdfToPdfaUsingNames() throws InterruptedException {
    NodeRef document = uploadDocument(site, "test.pdf", null, null, "test" + System.currentTimeMillis() + ".pdf").getNodeRef();

    Action action = actionService.createAction(ConvertToPdfActionExecuter.NAME);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_MIME_TYPE, ConvertToPdfActionExecuter.FAKE_MIMETYPE_PDFA);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_DESTINATION_FOLDER, document);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_TYPE_QNAME, RenditionModel.ASSOC_RENDITION);
    QName renditionQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) RENDITION_NAME_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_QNAME, renditionQName);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_TARGET_NAME, RENDITION_NAME_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ADD_EXTENSION, false);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_OVERWRITE_COPY, true);

    actionService.executeAction(action, document);

    List<ChildAssociationRef> childAssocs = _nodeService.getChildAssocs(document);
    assertNotNull(childAssocs);
    assertEquals(1, childAssocs.size());
    ChildAssociationRef childNode = childAssocs.get(0);
    NodeRef childNodeRef = childNode.getChildRef();
    assertNotNull(childNodeRef);
    //Assert that there is a child node with name pdfa
    assertEquals(RENDITION_NAME_PDF, _nodeService.getProperty(childNodeRef, ContentModel.PROP_NAME));
    assertEquals(ContentModel.TYPE_CONTENT, _nodeService.getType(childNodeRef));

    action = actionService.createAction(ConvertToPdfActionExecuter.NAME);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_MIME_TYPE, ConvertToPdfActionExecuter.FAKE_MIMETYPE_PDFA);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_DESTINATION_FOLDER, document);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_TYPE_QNAME, RenditionModel.ASSOC_RENDITION);
    renditionQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) RENDITION_NAME_PDFA);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_QNAME, renditionQName);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_TARGET_NAME, RENDITION_NAME_PDFA);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ADD_EXTENSION, false);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_OVERWRITE_COPY, true);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_SOURCE_FILENAME, RENDITION_NAME_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_SOURCE_FOLDER, document);

    actionService.executeAction(action, childNodeRef);

    childAssocs = _nodeService.getChildAssocs(document);
    assertNotNull(childAssocs);
    assertEquals(2, childAssocs.size());
    childNode = childAssocs.get(0);
    childNodeRef = childNode.getChildRef();
    assertNotNull(childNodeRef);
    //Assert that there is a child node with name pdfa
    assertEquals(RENDITION_NAME_PDF, _nodeService.getProperty(childNodeRef, ContentModel.PROP_NAME));
    childNode = childAssocs.get(1);
    childNodeRef = childNode.getChildRef();
    assertNotNull(childNodeRef);
    //Assert that there is a child node with name pdfa
    assertEquals(RENDITION_NAME_PDFA, _nodeService.getProperty(childNodeRef, ContentModel.PROP_NAME));

  }

  @Test
  public void testActionAuditSuccess() {
    NodeRef document = uploadDocument(site, "test.pdf", null, null, "test" + System.currentTimeMillis() + ".pdf").getNodeRef();

    Action action = actionService.createAction(ConvertToPdfActionExecuter.NAME);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_MIME_TYPE, ConvertToPdfActionExecuter.FAKE_MIMETYPE_PDFA);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_DESTINATION_FOLDER, document);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_TYPE_QNAME, RenditionModel.ASSOC_RENDITION);
    QName renditionQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) RENDITION_NAME_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_QNAME, renditionQName);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_TARGET_NAME, RENDITION_NAME_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ADD_EXTENSION, true);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_OVERWRITE_COPY, false);

    actionService.executeAction(action, document);

    final Map<String, Serializable> postValues = new HashMap<>();
    final Map<String, Serializable> preValues = new HashMap<>();

    final List<Boolean> assertList = new ArrayList<>();
    AuditQueryCallback callback = new AuditQueryCallback() {

      @Override
      public boolean valuesRequired() {
        return true;
      }

      @Override
      public boolean handleAuditEntry(Long entryId, String applicationName, String user, long time, Map<String, Serializable> values) {
        assertList.add(new Boolean(true));
        System.out.println("-----------------------------");
        if (values != null) {
          if (values.containsKey("/alfresco-archive-toolkit/action/archive-toolkit-transform-to-pdf/pre/params/destination-folder")) {
            preValues.putAll(values);
          } else if (values.containsKey("/alfresco-archive-toolkit/action/archive-toolkit-transform-to-pdf/post/target/node")) {
            postValues.putAll(values);
          } else {
            assertFalse("Unexpected audit entry: " + values.toString(), true);
          }
          for (String key : values.keySet()) {
            System.out.println(key + ": " + values.get(key));
          }
        } else {
          System.out.println("Empty set of audit values");
        }
        System.out.println("-----------------------------");

        return true;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error) {
        assertTrue("Did not expect any errors while processing audit records", false);
        return false;
      }
    };
    AuditQueryParameters parameters = new AuditQueryParameters();
    parameters.setApplicationName(ConvertToPdfActionExecuter.AUDIT_APPLICATION_NAME);
    parameters.addSearchKey("/alfresco-archive-toolkit/action/archive-toolkit-transform-to-pdf/node", document);
    String oldUserName = _authenticationComponent.getCurrentUserName();
    _authenticationComponent.clearCurrentSecurityContext();
    _authenticationComponent.setCurrentUser(_authenticationComponent.getSystemUserName());
    auditService.auditQuery(callback, parameters, 0);
    _authenticationComponent.clearCurrentSecurityContext();
    _authenticationComponent.setCurrentUser(oldUserName);

    assertEquals("Expected correct number of audit records", 2, assertList.size());

    assertEquals("Wrong number of expected audit valeus in pre audit record", 10, preValues.size());

    assertEquals("Wrong number of expected audit valeus in post audit record", 12, postValues.size());
  }

  @Test
  public void testActionAuditError() {
    NodeRef document = uploadDocument(site, "test.pdf", null, null, "test" + System.currentTimeMillis() + ".pdf").getNodeRef();

    Action action = actionService.createAction(ConvertToPdfActionExecuter.NAME);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_MIME_TYPE, "application/xxxFake");
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_DESTINATION_FOLDER, document);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_TYPE_QNAME, RenditionModel.ASSOC_RENDITION);
    QName renditionQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) RENDITION_NAME_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ASSOC_QNAME, renditionQName);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_TARGET_NAME, RENDITION_NAME_PDF);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_ADD_EXTENSION, false);
    action.setParameterValue(ConvertToPdfActionExecuter.PARAM_OVERWRITE_COPY, false);
    try {
      actionService.executeAction(action, document);
      assertTrue(false);
    } catch (RuleServiceException e) {

    }
    final Map<String, Serializable> errorValues = new HashMap<>();
    final Map<String, Serializable> preValues = new HashMap<>();

    final List<Boolean> assertList = new ArrayList<>();
    AuditQueryCallback callback = new AuditQueryCallback() {

      @Override
      public boolean valuesRequired() {
        return true;
      }

      @Override
      public boolean handleAuditEntry(Long entryId, String applicationName, String user, long time, Map<String, Serializable> values) {
        assertList.add(new Boolean(true));
        System.out.println("-----------------------------");
        if (values != null) {
          if (values.containsKey("/alfresco-archive-toolkit/action/archive-toolkit-transform-to-pdf/pre/params/destination-folder")) {
            preValues.putAll(values);
          } else if (values.containsKey("/alfresco-archive-toolkit/action/archive-toolkit-transform-to-pdf/error/params/mime-type")) {
            errorValues.putAll(values);
          } else {
            assertFalse("Unexpected audit entry: " + values.toString(), true);
          }
          for (String key : values.keySet()) {
            System.out.println(key + ": " + values.get(key));
          }
        } else {
          System.out.println("Empty set of audit values");
        }
        System.out.println("-----------------------------");

        return true;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error) {
        assertTrue("Did not expect any errors while processing audit records", false);
        return false;
      }
    };
    AuditQueryParameters parameters = new AuditQueryParameters();
    parameters.setApplicationName(ConvertToPdfActionExecuter.AUDIT_APPLICATION_NAME);
    parameters.addSearchKey("/alfresco-archive-toolkit/action/archive-toolkit-transform-to-pdf/node", document);
    String oldUserName = _authenticationComponent.getCurrentUserName();
    _authenticationComponent.clearCurrentSecurityContext();
    _authenticationComponent.setCurrentUser(_authenticationComponent.getSystemUserName());
    auditService.auditQuery(callback, parameters, 0);
    _authenticationComponent.clearCurrentSecurityContext();
    _authenticationComponent.setCurrentUser(oldUserName);

    assertEquals("Expected correct number of audit record", 2, assertList.size());

    assertEquals("Wrong number of expected audit valeus in pre audit record", 10, preValues.size());

    assertEquals("Wrong number of expected audit valeus in error audit record", 11, errorValues.size());
  }

  @Override
  public void afterClassSetup() {
    deleteSite(site);

    _authenticationComponent.setCurrentUser(_authenticationComponent.getSystemUserName());

    deleteUser(DEFAULT_USERNAME);

    _authenticationComponent.clearCurrentSecurityContext();
  }
}
