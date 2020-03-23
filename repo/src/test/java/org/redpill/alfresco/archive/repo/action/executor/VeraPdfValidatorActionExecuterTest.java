package org.redpill.alfresco.archive.repo.action.executor;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.exec.RuntimeExec;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VeraPdfValidatorActionExecuterTest {
  Mockery m;
  ContentService contentService;
  NodeService nodeService;
  Action action;
  final NodeRef nodeRef = new NodeRef("test", "node", "id");
  InputStream pdfaResourceStream;
  InputStream pdfResourceStream;
  ContentReader contentReader;
  VeraPdfValidatorActionExecuter ae;

  @Before
  public void setUp() throws Exception {
    m = new Mockery();
    contentService = m.mock(ContentService.class);
    nodeService = m.mock(NodeService.class);
    RuntimeExec checkCommand = new RuntimeExec();
    {
      String[] args = {"/opt/verapdf/1.14.8/verapdf", "--version"};
      Map<String, String[]> stringMap = new HashMap<>();
      stringMap.put(".*", args);
      checkCommand.setCommandsAndArguments(stringMap);
    }

    RuntimeExec validationCommand = new RuntimeExec();
    {
      String[] args = {"/opt/verapdf/1.14.8/verapdf", "${flavour}", "${source}"};
      Map<String, String[]> stringMap = new HashMap<>();
      stringMap.put(".*", args);
      validationCommand.setCommandsAndArguments(stringMap);
    }

    ae = new VeraPdfValidatorActionExecuterMock();
    ae.setContentService(contentService);
    ae.setNodeService(nodeService);
    ae.setCheckCommand(checkCommand);
    ae.setValidationCommand(validationCommand);
    ae.afterPropertiesSet();


    action = m.mock(Action.class);

    pdfaResourceStream = this.getClass().getResourceAsStream("/pdfa_1.pdf");
    pdfResourceStream = this.getClass().getResourceAsStream("/test.pdf");
    contentReader = m.mock(ContentReader.class);
  }

  @Test
  public void test1b() throws Exception {
    //Expectations
    m.checking(new Expectations() {
      {
        oneOf(nodeService).exists(with(nodeRef));
        will(returnValue(true));
        oneOf(action).getParameterValue(with(VeraPdfValidatorActionExecuter.PARAM_VALIDATION_FLAVOUR));
        will(returnValue("1b"));
        oneOf(contentService).getReader(with(nodeRef), with(ContentModel.PROP_CONTENT));
        will(returnValue(contentReader));
        oneOf(contentReader).getContentInputStream();
        will(returnValue(pdfaResourceStream));
      }
    });
    ae.execute(action, nodeRef);
  }

  @Test
  public void testAutoDetect() throws Exception {
    //Expectations
    m.checking(new Expectations() {
      {
        oneOf(nodeService).exists(with(nodeRef));
        will(returnValue(true));
        oneOf(action).getParameterValue(with(VeraPdfValidatorActionExecuter.PARAM_VALIDATION_FLAVOUR));
        will(returnValue(null));
        oneOf(contentService).getReader(with(nodeRef), with(ContentModel.PROP_CONTENT));
        will(returnValue(contentReader));
        oneOf(contentReader).getContentInputStream();
        will(returnValue(pdfaResourceStream));
      }
    });
    ae.execute(action, nodeRef);
  }

  @Test
  public void testNodeDoesNotExist() throws Exception {
    //Expectations
    m.checking(new Expectations() {
      {
        oneOf(nodeService).exists(with(nodeRef));
        will(returnValue(false));
      }
    });
    try {
      ae.execute(action, nodeRef);
      assertTrue(false);
    } catch (NullPointerException e) {
      assertEquals("Node does not exist: " + nodeRef, e.getMessage());
    }
  }

  @Test
  public void testNodeContentReaderDoesNotExist() throws Exception {
    //Expectations
    m.checking(new Expectations() {
      {
        oneOf(nodeService).exists(with(nodeRef));
        will(returnValue(true));
        oneOf(action).getParameterValue(with(VeraPdfValidatorActionExecuter.PARAM_VALIDATION_FLAVOUR));
        will(returnValue("1b"));
        oneOf(contentService).getReader(with(nodeRef), with(ContentModel.PROP_CONTENT));
        will(returnValue(null));
        oneOf(contentReader).getContentInputStream();
        will(returnValue(pdfaResourceStream));
      }
    });
    try {
      ae.execute(action, nodeRef);
      assertTrue(false);
    } catch (NullPointerException e) {
      assertEquals("No content reader available for node " + nodeRef, e.getMessage());
    }
  }

  @Test
  public void testNodeInputStreamDoesNotExist() throws Exception {
    //Expectations
    m.checking(new Expectations() {
      {
        oneOf(nodeService).exists(with(nodeRef));
        will(returnValue(true));
        oneOf(action).getParameterValue(with(VeraPdfValidatorActionExecuter.PARAM_VALIDATION_FLAVOUR));
        will(returnValue("1b"));
        oneOf(contentService).getReader(with(nodeRef), with(ContentModel.PROP_CONTENT));
        will(returnValue(contentReader));
        oneOf(contentReader).getContentInputStream();
        will(returnValue(null));
      }
    });
    try {
      ae.execute(action, nodeRef);
      assertTrue(false);
    } catch (NullPointerException e) {
      assertEquals("No content available for node " + nodeRef, e.getMessage());
    }
  }

  @Test
  public void testValidationFailure() throws Exception {
    //Expectations
    m.checking(new Expectations() {
      {
        oneOf(nodeService).exists(with(nodeRef));
        will(returnValue(true));
        oneOf(action).getParameterValue(with(VeraPdfValidatorActionExecuter.PARAM_VALIDATION_FLAVOUR));
        will(returnValue("1b"));
        oneOf(contentService).getReader(with(nodeRef), with(ContentModel.PROP_CONTENT));
        will(returnValue(contentReader));
        oneOf(contentReader).getContentInputStream();
        will(returnValue(pdfResourceStream));
      }
    });
    try {
      ae.execute(action, nodeRef);
      assertTrue(false);
    } catch (VeraPdfValidationException e) {
      assertEquals("PDF/a Validation failed: " + nodeRef, e.getMessage());
    }
  }

}
