package org.redpill.alfresco.archive.repo.service;

import java.io.InputStream;
import java.security.MessageDigest;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.io.IOUtils;
import org.redpill.alfresco.archive.repo.model.ArchiveToolkitModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class ArchiveToolkitServiceImpl implements ArchiveToolkitService, InitializingBean {
  private final static Log LOGGER = LogFactory.getLog(ArchiveToolkitServiceImpl.class);
  private ContentService contentService;
  private NodeService nodeService;
  
  // Default to md5, could be changed by injection
  private String checksumAlgorithm = ArchiveToolkitModel.CHECKSUM_MD5;
  
  private static final int STREAM_BUFFER_LENGTH = 32 * 1024;

  @Override
  public void afterPropertiesSet() throws Exception {
    Assert.notNull(contentService);
    Assert.notNull(nodeService);

  }

  @Override
  public void addChecksum(final NodeRef nodeRef) {
    final ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

    // if the content reader is null, no file content is attached
    if (contentReader == null) {
      return;
    }

    final InputStream inputStream = contentReader.getContentInputStream();

    final String checksum;

    try {
      checksum = getChecksum(inputStream, checksumAlgorithm);
    } finally {
      IOUtils.closeQuietly(inputStream);
    }
    
    // Add aspect if not present.
    if(!nodeService.hasAspect(nodeRef, ArchiveToolkitModel.ASPECT_CHECKSUMMED)){
      nodeService.addAspect(nodeRef, ArchiveToolkitModel.ASPECT_CHECKSUMMED, null);
    }
    
    if (LOGGER.isTraceEnabled()){
      LOGGER.trace("Adding " + checksumAlgorithm + " checksum: " + checksum + " property to NodeRef: " + nodeRef);
    }
    // set checksum property
    nodeService.setProperty(nodeRef, ArchiveToolkitModel.PROP_CHECKSUM, checksum);
  }
  
  public String getChecksum(final InputStream inputStream, String checksumAlgorithm) {
    
    // Make it possible to choose checksum algorithm, now we return md5 in both cases
    if (ArchiveToolkitModel.CHECKSUM_MD5.equals(checksumAlgorithm)){
      return md5Hex(inputStream);
    }else if (ArchiveToolkitModel.CHECKSUM_SHA1.equals(checksumAlgorithm)){
      return sha1Hex(inputStream);
    }else{
      if (LOGGER.isWarnEnabled()){
        LOGGER.warn("There has been no default checksum algorithm choosen, will use md5 as default.");
      }
      return md5Hex(inputStream); // default to md5
    }
  }
  
  private String md5Hex(final InputStream data) {
    return new String(Hex.encodeHex(md5(data)));
  }

  public byte[] md5(final InputStream data) {
    try {
      return digest(MessageDigest.getInstance("MD5"), data);
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private byte[] digest(final MessageDigest digest, final InputStream data) {
    try {
      final byte[] buffer = new byte[STREAM_BUFFER_LENGTH];
      int read = data.read(buffer, 0, STREAM_BUFFER_LENGTH);

      while (read > -1) {
        digest.update(buffer, 0, read);
        read = data.read(buffer, 0, STREAM_BUFFER_LENGTH);
      }

      return digest.digest();
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }
  
  private String sha1Hex(final InputStream data){
    return new String(Hex.encodeHex(sha1(data)));
  }
  
  public byte[] sha1(final InputStream data) {
    try {
      return digest(MessageDigest.getInstance("SHA1"), data);
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }


  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }
  
  public void setChecksumAlgorithm(String checksumAlgorithm) {
    this.checksumAlgorithm = checksumAlgorithm;
  }

}
