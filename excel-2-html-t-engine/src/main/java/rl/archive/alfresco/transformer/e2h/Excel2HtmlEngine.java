package rl.archive.alfresco.transformer.e2h;

import org.alfresco.transform.exceptions.TransformException;
import org.alfresco.transformer.executors.AbstractCommandExecutor;
import org.alfresco.transformer.executors.RuntimeExec;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component("excel2Html")
public class Excel2HtmlEngine extends AbstractCommandExecutor implements ConversionEngine {

    private static final Log LOG = LogFactory.getLog(Excel2HtmlEngine.class);

    @Value("${rl.archive.alfresco.libreoffice.exe}")
    public String libreOfficeExe;
    @Value("${rl.archive.alfresco.temp.transform.dir}")
    public String tempTransformDir;
    @Value("${rl.archive.alfresco.script.exec.path}")
    public String scriptExecPath;
    @Value("${rl.archive.alfresco.script.exec.exe}")
    public String scriptExecExe;

    @Value("${rl.archive.alfresco.transform.timeout}")
    public long timeOut;

    @Value("${rl.archive.alfresco.transform.file.extensions}")
    private Set<String> allowedFileExtensions;

    @PostConstruct
    public void init() {
        checkCommand = createCheckCommand();
        LOG.info("Created check command: \n" + checkCommand);
        transformCommand = createTransformCommand();
        LOG.info("Created transform command: \n" + transformCommand);
    }

    @Override
    public String getName() {
        return "excel2Html";
    }


    @Override
    public File transformExcelToHtml(File sourceFile, File targetFile) {
        String sourceFileExtension = FilenameUtils.getExtension(sourceFile.getName());
        if (!allowedFileExtensions.contains(sourceFileExtension)) {
            LOG.error(sourceFileExtension + " is not an accepted file extension");
            return null;
        }
        LOG.info("Transformation for " + sourceFile.getName() + " to pdf started using " + getName());
        long startTime = System.currentTimeMillis();


        Map<String, String> properties = new HashMap<>();
        properties.put("source", sourceFile.getAbsolutePath());
        properties.put("target", targetFile.getAbsolutePath());

        // execute the transformation
        try {
            LOG.info(this.transformCommand.toString());
            LOG.info(properties);

            run(properties, targetFile, timeOut);
        } catch (TransformException e) {
            LOG.error("Failed to transform PDF: ", e);
            throw e;
        }

        long endTime = System.currentTimeMillis();
        long runTime = (endTime - startTime);
        LOG.info(sourceFile.getName() + " => " + targetFile.getName() + " OK. Spent " + runTime + " ms using " + getName());
        return targetFile;

    }


    /**
     * Create the templated transform command
     * The source and target parameter are replaced by paths
     * to the source and target files
     *
     * @return The command
     */
    @Override
    protected RuntimeExec createTransformCommand() {

        RuntimeExec runtimeExec = new RuntimeExec();
        Map<String, String[]> commandsAndArguments = new HashMap<>();
        commandsAndArguments.put(".*", new String[]{scriptExecExe,
                "${source}",
                "${target}",
                libreOfficeExe,
                tempTransformDir,
                scriptExecPath});
        runtimeExec.setCommandsAndArguments(commandsAndArguments);

        runtimeExec.setErrorCodes("1");

        return runtimeExec;
    }

    /**
     * Creates a command to check the availability of the script
     *
     * @return The command
     */
    @Override
    protected RuntimeExec createCheckCommand() {
        RuntimeExec runtimeExec = new RuntimeExec();
        Map<String, String[]> commandsAndArguments = new HashMap<>();
        commandsAndArguments.put(".*", new String[]{scriptExecExe,
                "--version",});
        runtimeExec.setCommandsAndArguments(commandsAndArguments);

        runtimeExec.setErrorCodes("1");
        return runtimeExec;
    }

    @Override
    public String getTransformerId() {
        return getName();
    }
}
