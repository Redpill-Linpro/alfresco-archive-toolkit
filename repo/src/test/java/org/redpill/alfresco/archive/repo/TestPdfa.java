package org.redpill.alfresco.archive.repo;

import org.apache.tika.io.IOUtils;
import org.junit.Test;
import org.verapdf.pdfa.*;
import org.verapdf.pdfa.flavours.PDFAFlavour;

import java.io.File;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestPdfa {

  public void validateOldImpl(InputStream is) throws Exception {
    // The veraPDF library is unaware of the implementations and needs to be
    // initialised before first use
    VeraGreenfieldFoundryProvider.initialise();
    PDFAParser parser;
    PDFAFlavour flavour = PDFAFlavour.PDFA_1_B;
    org.verapdf.pdfa.results.ValidationResult vr = null;
    try (VeraPDFFoundry foundry = Foundries.defaultInstance();
         PDFAValidator validator = foundry.createValidator(flavour, false)) {
      parser = foundry.createParser(is);
      vr = validator.validate(parser);

      if (vr == null || !vr.isCompliant()) {
        throw new Exception("Failed to validate pdf/a, test Assertions: " + (vr == null ? "null" : (vr.getTestAssertions().toString())));
      }
      assertTrue(vr.isCompliant());
    }
  }

  @Test
  public void testFile() throws Exception {
    InputStream is = this.getClass().getResourceAsStream("/pdfa_1.pdf");
    assertNotNull(is);
    validateOldImpl(is);
    IOUtils.closeQuietly(is);
  }

  @Test
  public void testFile2() throws Exception {
    InputStream is = this.getClass().getResourceAsStream("/pdfa_2.pdf");
    assertNotNull(is);
    validateOldImpl(is);
    IOUtils.closeQuietly(is);
  }

}
