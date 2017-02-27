Alfresco Archive Toolkit
=============================================

This module is sponsored by Redpill Linpro AB - http://www.redpill-linpro.com.

Description
-----------
This project contains some tools for handling different archiving scenarios.

Structure
------------

The project consists of a repository module and a share module packaged as jar files.

Building & Installation
------------
The build produces several jar files. Attach them to your own maven project using dependencies or put them under tomcat/shared/lib.

Repository dependency:
```xml
<dependency>
  <groupId>org.redpill-linpro.alfresco.archive</groupId>
  <artifactId>alfresco-archive-toolkit</artifactId>
  <version>1.0.0</version>
</dependency>
```

Share dependency:
```xml
<dependency>
  <groupId>org.redpill-linpro.alfresco.archive</groupId>
  <artifactId>alfresco-archive-toolkit</artifactId>    
  <version>1.0.0</version>
</dependency>
```

Maven repository:
```xml
<repository>
  <id>redpill-public</id>
  <url>http://maven.redpill-linpro.com/nexus/content/groups/public</url>
</repository>
```

The jar files are also downloadable from: https://maven.redpill-linpro.com/nexus/index.html#nexus-search;quick~alfresco-archive-toolkit

Ghostscript and ghostscript resource files
-------------------------------------------
 
To convert from pdf to pdf/a Ghostscript needs to be installed. Add the path to the ghostscript executable by setting 
gs.exe=path_to_ghostscript in your alfresco-global.properties file. Ghostscript makes use of a postscript definition file 
(PDFA_def.ps) and an ICC color profile (sRGB_IEC61966-2.1.icc) which you need to copy from the source files of this 
project to a runtime path on the server which is then pointed out in the alfresco-global.properties. 
```
pdfa.definition.file=/my/path/gs/PDFA_def.ps
```

The path to the color profile is pointed out inside the postscript definition file.
```
/ICCProfile (/my/path/gs/sRGB_IEC61966-2.1.icc)   % Customize.
```


Usage
-----

Components:
* An action which can be used to transform content into pdf (x->pdf) & pdf/a (pdf->pdf/a) (TODO Documentation)
* A web script which will display auditing of the action can be reached at: http://localhost:8080/share/page/archive-toolkit/audit
* 


License
-------

This application is licensed under the LGPLv3 License. See the [LICENSE file](LICENSE) for details.

Authors
-------

Marcus Svartmark - Redpill Linpro AB
Erik Billerby - Redpill Linpro AB
