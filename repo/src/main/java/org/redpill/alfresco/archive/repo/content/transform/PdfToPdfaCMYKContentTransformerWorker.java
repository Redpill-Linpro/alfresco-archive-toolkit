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

public class PdfToPdfaCMYKContentTransformerWorker extends RuntimeExecutableContentTransformerWorker implements InitializingBean {

  private static final Log logger = LogFactory.getLog(PdfToPdfaCMYKContentTransformerWorker.class);

  protected String pdfaDefinitionFile;
  protected String pdfaICCProfileFile;
  protected static final String PDFA_DEFINITION_TEMPLATE_PATH = "alfresco/module/archive-toolkit/gs/PDFA_def2.ps";
  protected static final String PDFA_ICCPROFILE_PATH = "alfresco/module/archive-toolkit/gs/CoatedGRACoL2006.icc";

  @Override
  public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options) {
    if (!isAvailable()) {
      return false;
    }

    if (MimetypeMap.MIMETYPE_PDF.equals(sourceMimetype) && ConvertToPdfActionExecuter.FAKE_MIMETYPE_PDFA_CMYK.equals(targetMimetype)) {
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
    //Initialize GS configuration files
    try {
      ensurePdfaDefinitionFileExists();
      ensurePdfaICCProfileFileExists();
    } catch (Exception ex) {
      throw new AlfrescoRuntimeException("Error instantiating PDFA transformer: " + ex.getMessage(), ex);
    }
  }

  public void setPdfaDefinitionFile(String pdfaDefinitionFile) {
    this.pdfaDefinitionFile = pdfaDefinitionFile;
  }

  public void setPdfaICCProfileFile(String pdfaICCProfileFile) {
    this.pdfaICCProfileFile = pdfaICCProfileFile;
  }

  protected void ensurePdfaDefinitionFileExists() throws AccessDeniedException, IOException {
    Assert.notNull(pdfaDefinitionFile);
    File f = new File(pdfaDefinitionFile);
    if (f.exists() && f.canRead()) {
      //The file already exists and everything seems to be in order
      return;
    } else if (f.exists() && !f.canRead()) {
      throw new AccessDeniedException(pdfaDefinitionFile, null, "Cannot read file");
    } else {
      if (logger.isTraceEnabled()) {
        logger.trace("Copy and insert template variables in classpath resource " + PDFA_DEFINITION_TEMPLATE_PATH + " into " + pdfaDefinitionFile);
      }
      //We are good to go to create the pdfa definition file. Use the template as a base.
      String baseName = FilenameUtils.getBaseName(pdfaDefinitionFile);
      String extension = FilenameUtils.getExtension(pdfaDefinitionFile);
      String path = FilenameUtils.getFullPathNoEndSeparator(pdfaDefinitionFile);
      File dir = new File(path);
      if (logger.isTraceEnabled()) {
        logger.trace("Checking existance of path " + path);
      }
      if (!dir.exists()) {
        if (logger.isTraceEnabled()) {
          logger.trace("Trying to create directory " + path);
        }
        //The directory we want does not exist. Lets try and create it.
        if (!dir.mkdirs() || !dir.exists()) {
          throw new AccessDeniedException(path, null, "Could not create directory" + path);
        }
      }
      //Copy the template and add the correct icc file path
      InputStream initialStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PDFA_DEFINITION_TEMPLATE_PATH);
      Assert.notNull(initialStream, "Could not open template: " + PDFA_DEFINITION_TEMPLATE_PATH);
      try {
        if (logger.isTraceEnabled()) {
          logger.trace("Trying to create file " + pdfaDefinitionFile);
        }
        File targetFile = new File(pdfaDefinitionFile);
        targetFile.createNewFile();
        String template = IOUtils.toString(initialStream);
        template = template.replace("%%ICC_FILE_PATH%%", pdfaICCProfileFile);
        FileUtils.write(targetFile, template);
      } finally {
        IOUtils.closeQuietly(initialStream);
      }
    }
  }

  protected void ensurePdfaICCProfileFileExists() throws AccessDeniedException, IOException {
    Assert.notNull(pdfaICCProfileFile);
    File f = new File(pdfaICCProfileFile);
    if (f.exists() && f.canRead()) {
      //The file already exists and everything seems to be in order
      return;
    } else if (f.exists() && !f.canRead()) {
      throw new AccessDeniedException(pdfaICCProfileFile, null, "Cannot read file");
    } else {
      if (logger.isTraceEnabled()) {
        logger.trace("Copying classpath resource " + PDFA_ICCPROFILE_PATH + " into " + pdfaICCProfileFile);
      }
      //We are good to go to create the pdfa definition file. Use the template as a base.
      String baseName = FilenameUtils.getBaseName(pdfaICCProfileFile);
      String extension = FilenameUtils.getExtension(pdfaICCProfileFile);
      String path = FilenameUtils.getFullPathNoEndSeparator(pdfaDefinitionFile);
      File dir = new File(path);
      if (logger.isTraceEnabled()) {
        logger.trace("Checking existance of path " + path);
      }
      if (!dir.exists()) {
        //The directory we want does not exist. Lets try and create it.
        dir.mkdirs();
        if (!dir.exists()) {
          throw new AccessDeniedException(path, null, "Could not create directory" + path);
        }
      }

      //Copy the template and add the correct icc file path
      InputStream initialStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PDFA_ICCPROFILE_PATH);
      Assert.notNull(initialStream, "Could not open icc profile: " + PDFA_ICCPROFILE_PATH);
      try {
        File targetFile = new File(pdfaICCProfileFile);
        targetFile.createNewFile();
        FileUtils.copyInputStreamToFile(initialStream, targetFile);
      } finally {
        IOUtils.closeQuietly(initialStream);
      }
    }
  }

}
