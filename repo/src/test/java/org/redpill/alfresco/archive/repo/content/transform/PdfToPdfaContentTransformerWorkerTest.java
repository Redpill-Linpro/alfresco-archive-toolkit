package org.redpill.alfresco.archive.repo.content.transform;

import java.io.File;
import java.io.IOException;
import org.alfresco.util.exec.RuntimeExec;
import org.apache.commons.io.FileUtils;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Marcus Svartmark - Redpill Linpro AB
 */
public class PdfToPdfaContentTransformerWorkerTest {

  Mockery m;

  @Before
  public void setUp() {
    m = new JUnit4Mockery() {
      {
        setImposteriser(ClassImposteriser.INSTANCE);
      }
    };

  }

  @Test
  public void testInstantiation() throws IOException {

    PdfToPdfaContentTransformerWorker worker = new PdfToPdfaContentTransformerWorker();
    RuntimeExec transformCommand = m.mock(RuntimeExec.class);
    worker.setTransformCommand(transformCommand);
    worker.afterPropertiesSet();

  }
}
