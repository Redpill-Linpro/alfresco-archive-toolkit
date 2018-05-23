package org.redpill.alfresco.archive.repo.content.transform;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.redpill.alfresco.archive.repo.action.executor.ConvertToPdfActionExecuter;

public class PdfToPdfaContentTransformerWorker extends AbstractPdfToPdfaContentTranformer {

  private static final Log logger = LogFactory.getLog(PdfToPdfaContentTransformerWorker.class);

  protected static final String PDFA_DEFINITION_TEMPLATE_PATH = "alfresco/module/archive-toolkit/gs/PDFA_RGB_def.ps";
  protected static final String PDFA_ICCPROFILE_PATH = "alfresco/module/archive-toolkit/gs/sRGB_IEC61966-2.1.icc";

  @Override
  protected String getPdfaDefinitionTemplatePath() {
    return PDFA_DEFINITION_TEMPLATE_PATH;
  }

  @Override
  protected String getPdfaIccProfilePath() {
    return PDFA_ICCPROFILE_PATH;
  }

  @Override
  public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options) {
    if (!isAvailable()) {
      return false;
    }

    if (MimetypeMap.MIMETYPE_PDF.equals(sourceMimetype) && ConvertToPdfActionExecuter.FAKE_MIMETYPE_PDFA.equals(targetMimetype)) {
      if (logger.isTraceEnabled()) {
        logger.trace("Transform between pdf to pdf/a return true");
      }
      return true;
    } else {
      return false;
    }
  }

}
