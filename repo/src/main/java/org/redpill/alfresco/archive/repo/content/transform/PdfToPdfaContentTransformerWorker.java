package org.redpill.alfresco.archive.repo.content.transform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.RuntimeExecutableContentTransformerWorker;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.redpill.alfresco.archive.repo.action.executor.ConvertToPdfActionExecuter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class PdfToPdfaContentTransformerWorker extends RuntimeExecutableContentTransformerWorker implements InitializingBean {

  private static final Log logger = LogFactory.getLog(PdfToPdfaContentTransformerWorker.class);

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

  @Override
  public void afterPropertiesSet() {

    super.afterPropertiesSet();

  }

}
