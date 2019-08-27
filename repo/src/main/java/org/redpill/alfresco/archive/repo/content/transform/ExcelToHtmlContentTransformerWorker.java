/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.redpill.alfresco.archive.repo.content.transform;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.RuntimeExecutableContentTransformerWorker;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashSet;
import java.util.Set;

public class ExcelToHtmlContentTransformerWorker extends RuntimeExecutableContentTransformerWorker implements InitializingBean {

  private static final Log logger = LogFactory.getLog(ExcelToHtmlContentTransformerWorker.class);
  private static final Set<String> allowedMimetypes= new HashSet<>();
  @Override
  public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options) {
    if (!isAvailable()) {
      return false;
    }
    if (allowedMimetypes.contains(sourceMimetype) && MimetypeMap.MIMETYPE_HTML.equals(targetMimetype)) {
      if (logger.isTraceEnabled()) {
        logger.trace("Transform between "+MimetypeMap.MIMETYPE_EXCEL+" to "+MimetypeMap.MIMETYPE_HTML+" return true");
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void afterPropertiesSet() {
    allowedMimetypes.add(MimetypeMap.MIMETYPE_EXCEL); //xls
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET); //xlsx
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET_MACRO); //xlsm
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET_TEMPLATE_MACRO); //xltm
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET_ADDIN_MACRO); //xlam
    allowedMimetypes.add(MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET_BINARY_MACRO); //xlsb
    super.afterPropertiesSet();

  }
}
