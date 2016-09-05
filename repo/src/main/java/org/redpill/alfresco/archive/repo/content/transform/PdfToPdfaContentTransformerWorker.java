package org.redpill.alfresco.archive.repo.content.transform;

import org.alfresco.repo.content.transform.RuntimeExecutableContentTransformerWorker;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PdfToPdfaContentTransformerWorker extends RuntimeExecutableContentTransformerWorker {

  @Override
  public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options) {
    // TODO Auto-generated method stub
    return super.isTransformable(sourceMimetype, targetMimetype, options);
  }

  private static final Log logger = LogFactory.getLog(PdfToPdfaContentTransformerWorker.class);
}
