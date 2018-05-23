package org.redpill.alfresco.archive.repo.model;

import org.alfresco.service.namespace.QName;

public interface ArchiveToolkitModel {

  static final String URI = "http://www.redpill-linpro.com/model/at/1.0";

  static final QName ASPECT_CHECKSUMMED = QName.createQName(URI, "checksummed");
  static final QName PROP_CHECKSUM = QName.createQName(URI, "checksum");

  static final String CHECKSUM_MD5 = "md5";

  static final String CHECKSUM_SHA1 = "sha1";

}
