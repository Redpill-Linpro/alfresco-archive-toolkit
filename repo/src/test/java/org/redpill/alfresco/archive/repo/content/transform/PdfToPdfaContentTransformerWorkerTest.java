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
    final String PDFA_DEF_TARGET = "target/gs/PDF_def.ps";
    final String PDFA_ICC_TARGET = "target/gs/sRGB_IEC61966-2.1.icc";
    PdfToPdfaContentTransformerWorker worker = new PdfToPdfaContentTransformerWorker();
    RuntimeExec transformCommand = m.mock(RuntimeExec.class);
    worker.setTransformCommand(transformCommand);
    worker.setPdfaDefinitionFile(PDFA_DEF_TARGET);
    worker.setPdfaICCProfileFile(PDFA_ICC_TARGET);
    worker.afterPropertiesSet();

    File file = new File(PDFA_DEF_TARGET);
    assertNotNull(file);
    assertTrue(file.exists());
    String readFileToString = FileUtils.readFileToString(file);
    assertTrue(readFileToString.contains(PDFA_ICC_TARGET));

    file = new File(PDFA_ICC_TARGET);
    assertNotNull(file);
    assertTrue(file.exists());

  }
}
