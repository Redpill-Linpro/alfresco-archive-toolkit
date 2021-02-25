# Vgregion Excel to Html T-Engine

## Purpose

This is a custom transformation engine used by alfresco-acs to transform excel files into a html format.


## Prerequisites
* Java 11
* Maven
* Docker

## Production requirements
* Libreoffice 6.4
* Excel-2-html script files from archive toolkit project

## Build and run script
First run the ./package.sh script of the parent.
Use the script ./run.sh to build the project and run the created docker-container
The container is automatically destroyed on exit.

## Configuration

To use this T-Engine, some configurations are required. These are listed below, with their default values:
````
se.vgregion.alfresco.libreoffice.exe=/opt/libreoffice6.4/program/soffice
se.vgregion.alfresco.temp.transform.dir=${java.io.tmpdir}/excel2html
se.vgregion.alfresco.script.exec.exe=${se.vgregion.alfresco.script.exec.path}/excel-to-html.sh
se.vgregion.alfresco.script.exec.path=/opt/excel2html
````
These are credentials for the remote transformation engine.

Use:
`````
se.vgregion.alfresco.libreoffice.exe
`````
to point to the libreoffice soffice bin.

Use:
`````
se.vgregion.alfresco.temp.transform.dir
`````
to point to a directory used to store temporary transformation files.

Use:
`````
se.vgregion.alfresco.script.exec.exe
`````
to point to the excel-to-html.sh script file.

Use:
`````
se.vgregion.alfresco.script.exec.path
`````
to point to the directory containing the excel-2-html script files.



## Endpoints

Test transformations can be accessed at:
````
http://localhost:8091/
````

To check T-engine configuration, use:
````
http://localhost:8091/transform/config
````

To check T-engine availability, use:
````
http://localhost:8091/ready
http://localhost:8091/live
````

To request a transformation, post request to:
````
http://localhost:8091/transform
````

The required parameters for the transformation request are:

|Parameter name|Parameter value|
|--------------|---------------|
|file| The file to transform|
|sourceMimetype| The mimetype of the source file|
|targetMimetype| The mimetype to transform to, should be text/html|
|targetExtension| The extension of the transformed file, should be ".html"|

# Deployment

When deploying this T-Engine as a jar file, use the following commands:

## Local transformation

```
#!/bin/sh
java
-De.vgregion.alfresco.libreoffice.exe="SOFFICE_BIN"
-Dse.vgregion.alfresco.temp.transform.dir="TEMP_DIR"
-Dse.vgregion.alfresco.script.exec.exe="SCRIPT_SH_FILE"
-Dse.vgregion.alfresco.script.exec.path="SCRIPT_DIR"
-jar /opt/alfresco/e2h/excel-2-html-t-engine.jar
```

#ATS with transform router

````
java
-De.vgregion.alfresco.libreoffice.exe="SOFFICE_BIN"
-Dse.vgregion.alfresco.temp.transform.dir="TEMP_DIR"
-Dse.vgregion.alfresco.script.exec.exe="SCRIPT_SH_FILE"
-Dse.vgregion.alfresco.script.exec.path="SCRIPT_DIR"
-DACTIVEMQ_URL="failover:(ACTIVE_MQ_URL)?timeout=3000"
-DACTIVEMQ_USER: "ACTIVEMQ_USERNAME"
-DACTIVEMQ_PASSWORD: "ACTIVEMQ_PASSWORD"
-DFILE_STORE_URL="FILE_STORE_URL"
-jar /opt/alfresco/e2h/excel-2-html-t-engine.jar
````
## Additional notes

##### Issues with fabric8-maven-plugin
Ensure that the Docker installation has Docker Experimental Features enabled.
