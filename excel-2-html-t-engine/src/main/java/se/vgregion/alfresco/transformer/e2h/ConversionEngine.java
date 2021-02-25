package se.vgregion.alfresco.transformer.e2h;

import java.io.File;

/**
 *
 * Interface for a conversion engine
 *
 */
public interface ConversionEngine {
    /**
     * Returns the name of the conversion engine implementing this interface
     *
     * @return the name of the conversion engine
     */
    public String getName();

    /**
     *
     * Transforms the source file to html format, and writes the result to
     * the target file
     * @param sourceFile file to convert
     * @param targetFile file to write result
     * @return targetFile if the transformation was successful, null if not
     */
    File transformExcelToHtml(File sourceFile, File targetFile);

}
