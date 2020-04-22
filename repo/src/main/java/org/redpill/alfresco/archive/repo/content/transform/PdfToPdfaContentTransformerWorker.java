package org.redpill.alfresco.archive.repo.content.transform;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.RuntimeExecutableContentTransformerWorker;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.redpill.alfresco.archive.repo.action.executor.ConvertToPdfActionExecuter;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashSet;
import java.util.Set;

public class PdfToPdfaContentTransformerWorker extends RuntimeExecutableContentTransformerWorker implements InitializingBean {

  private static final Log logger = LogFactory.getLog(PdfToPdfaContentTransformerWorker.class);
  private static final Set<String> allowedMimetypes = new HashSet<>();

  @Override
  public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options) {
    if (!isAvailable()) {
      return false;
    }

    if (allowedMimetypes.contains(sourceMimetype) && ConvertToPdfActionExecuter.FAKE_MIMETYPE_PDFA.equals(targetMimetype)) {
      if (logger.isTraceEnabled()) {
        logger.trace("Transform from " + sourceMimetype + " to " + targetMimetype + " return true");
      }
      return true;
    } else {
      if (logger.isTraceEnabled()) {
        logger.trace("Transform from " + sourceMimetype + " to " + targetMimetype + " return false");
      }
      return false;
    }
  }

  @Override
  public void afterPropertiesSet() {
    //PDF
    allowedMimetypes.add(MimetypeMap.MIMETYPE_PDF); //pdf

    //Excel 1997-2003
    allowedMimetypes.add(MimetypeMap.MIMETYPE_EXCEL); //xls, xlt
    //Excel
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET); //xlsx
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET_MACRO); //xlsm
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET_TEMPLATE_MACRO); //xltm
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET_TEMPLATE); //xltx

    //Powerpoint 1997-2003
    allowedMimetypes.add(MimetypeMap.MIMETYPE_PPT); //ppt, pot
    //Powerpoint
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION); //pptx
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_MACRO); //pptm
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_TEMPLATE_MACRO); //potm
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_TEMPLATE); //potx
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_ADDIN); //ppam
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_SLIDESHOW); //ppsx
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_SLIDESHOW_MACRO); //ppsm
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_SLIDE); //sldx
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_SLIDE_MACRO); //sldm

    //Word 1997-2003
    allowedMimetypes.add(MimetypeMap.MIMETYPE_WORD); //doc, dot
    //Word
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING); //docx
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING_MACRO); //docm
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_WORD_TEMPLATE); //dotx
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_WORD_TEMPLATE_MACRO); //dotm

    super.afterPropertiesSet();

  }

}
