package org.redpill.alfresco.archive.repo.content.transform;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.RuntimeExecutableContentTransformerWorker;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.redpill.alfresco.archive.repo.action.executor.ConvertToPdfActionExecuter;

public class PdfToPdfaContentTransformerWorker extends RuntimeExecutableContentTransformerWorker {
  private static final Log logger = LogFactory.getLog(PdfToPdfaContentTransformerWorker.class);
  @Override
  public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options) {
    if (!isAvailable()) {
      return false;
    }

    if(MimetypeMap.MIMETYPE_PDF.equals(sourceMimetype) && ConvertToPdfActionExecuter.FAKE_MIMETYPE_PDFA.equals(targetMimetype)){
      if (logger.isTraceEnabled()){
        logger.trace("Transform between pdf to pdf/a return true");
      }
      return true;
    }else{
      return false;
    }
  }

  
}
