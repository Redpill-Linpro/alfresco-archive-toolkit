package org.redpill.alfresco.archive.repo.service;

import org.alfresco.service.cmr.repository.NodeRef;

public interface ArchiveToolkitService {

  void addChecksum(final NodeRef nodeRef);

}
