/*
 * #%L
 * Alfresco Transform Core
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package se.vgregion.alfresco.transformer.e2h;

import org.alfresco.transform.exceptions.TransformException;
import org.alfresco.transformer.AbstractTransformerController;
import org.alfresco.transformer.probes.ProbeTestTransform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.util.Map;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;


/**
 * Controller for transforming excel files to html format
 * Uses the excel-2-html tool to transform
 */
@Controller
public class Excel2HtmlTEngineController extends AbstractTransformerController {
    private static final Logger LOG = LoggerFactory.getLogger(Excel2HtmlTEngineController.class);


    @Autowired
    @Qualifier("excel2Html")
    ConversionEngine conversionEngine;

    public Excel2HtmlTEngineController() {
        LOG.info("-------------------------------------------");
        LOG.info(getTransformerName() + " is starting up");
        LOG.info("-------------------------------------------");

    }

    /**
     * Simple transform excel -> html
     *
     * @return A quick transform used to check the health of the T-Engine
     * @see <a href="https://github.com/Alfresco/alfresco-transform-core/blob/master/docs/Probes.md">Probes.md</a>
     */
    @Override
    public ProbeTestTransform getProbeTestTransform() {
        return new ProbeTestTransform(this, "simple.xlsx", "simple.html",
                21346, 20, 150, 1024,
                10, 1) {
            @Override
            protected void executeTransformCommand(File sourceFile, File targetFile) {
                transformImpl("excel2Html", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "text/html", null, sourceFile, targetFile);
            }
        };
    }

    @Override
    public String getTransformerName() {
        return "excel2Html";
    }

    @Override
    public String version() {
        return getTransformerName() + " available";
    }

    /**
     * The actual transformation code.
     *
     * @param transformName    - will always be {@code "excel2Html"} as there is only one transformer defined in the
     *                         {@code engine_config.json}.
     * @param sourceMimetype   - the media type of the source
     * @param targetMimetype   - the media type to be generated
     * @param transformOptions - options that have been supplied to the transformer
     * @param sourceFile       - The received source file
     * @param targetFile       - The target file representing the result of the transformation
     */
    @Override
    public void transformImpl(String transformName, String sourceMimetype, String targetMimetype, Map<String, String> transformOptions, File sourceFile, File targetFile) {


        LOG.trace("Got request to transform file " + sourceFile.getName()
                + " with mimetype " + sourceMimetype
                + " to mimetype " + targetMimetype);

        File file;
        try {
            file = conversionEngine.transformExcelToHtml(sourceFile, targetFile);
        } catch (Exception e) {
            LOG.error("Failed to transform file " + sourceFile.getName(), e);
            throw new TransformException(INTERNAL_SERVER_ERROR.value(),
                    "There was a problem during transformation: " + e.getMessage(), e);
        }

        if(file == null){
            LOG.error("Failed to transform file," + sourceFile.getName() + " file is null" );
        }
    }
}
