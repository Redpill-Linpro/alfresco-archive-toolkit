package org.redpill.alfresco.archive.repo.content.transform;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.transform.RuntimeExecutableContentTransformerWorker;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;

public abstract class AbstractPdfToPdfaContentTranformer extends RuntimeExecutableContentTransformerWorker implements InitializingBean {
  private static final Log logger = LogFactory.getLog(AbstractPdfToPdfaContentTranformer.class);

  protected String pdfaDefinitionFile;
  protected String pdfaICCProfileFile;

  protected abstract String getPdfaDefinitionTemplatePath();

  protected abstract String getPdfaIccProfilePath();

  @Override
  public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options) {
    return false;
    //throw new UnsupportedOperation("Not implemented.");
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
        logger.trace("Copy and insert template variables in classpath resource " + getPdfaDefinitionTemplatePath() + " into " + pdfaDefinitionFile);
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
      InputStream initialStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(getPdfaDefinitionTemplatePath());
      Assert.notNull(initialStream, "Could not open template: " + getPdfaDefinitionTemplatePath());
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
        logger.trace("Copying classpath resource " + getPdfaIccProfilePath() + " into " + pdfaICCProfileFile);
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
      InputStream initialStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(getPdfaIccProfilePath());
      Assert.notNull(initialStream, "Could not open icc profile: " + getPdfaIccProfilePath());
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
