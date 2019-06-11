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
  <version>1.2.0</version>
</dependency>
```

Share dependency:
```xml
<dependency>
  <groupId>org.redpill-linpro.alfresco.archive</groupId>
  <artifactId>alfresco-archive-toolkit</artifactId>    
  <version>1.2.0</version>
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

PDF2ARCHIVE
-----------
Follow installation instructions at https://github.com/matteosecli/pdf2archive. Note that to convert from pdf to pdf/a Ghostscript needs to be installed. 

Ghostscript and ghostscript resource files
-------------------------------------------
 
Add the path to the ghostscript executable by setting 
`gs.exe=path_to_ghostscript` in your alfresco-global.properties file. A converter called pdf2archive is also used in the 
conversion proccess. Add the path to the pdf2archive executable in alfresco-global.properties: `pdf2archive.exe=path_to_pdf2archive` 


Usage
-----

Components:
* An action which can be used to transform content into pdf (x->pdf) & pdf/a (pdf->pdf/a) (TODO Documentation)
* A web script which will display auditing of the action can be reached at: http://localhost:8080/share/page/archive-toolkit/audit
* 

## Command line tools to convert Excel files to html with embedded images

### excel-to-html.sh

Script to launch convert an excel file to HTML and embed images using LibreOffice. Uses lo-to-html.sh and embed-html-images.sh

**Usage:** excel-to-html.sh /path/to/spreadsheet.xls /path/to/destination/dir /path/to/soffice.bin /path/to/libreoffice_userdir /path/to/html-embed-images.jar /optional/path/to/java


**Example:**
./excel-to-html.sh testfiles/family_budget.xls testfiles /Applications/LibreOffice.app/Contents/MacOS/soffice /tmp ../html-embed-images/target/html-embed-images-1.2.0.jar

**Output:**
```
convert /Users/mars/Documents/alfresco/alfresco-archive-toolkit/excel-to-html/testfiles/family_budget.xls -> /Users/mars/Documents/alfresco/alfresco-archive-toolkit/excel-to-html/testfiles/family_budget.html using filter : HTML (StarCalc)
Overwriting: /Users/mars/Documents/alfresco/alfresco-archive-toolkit/excel-to-html/testfiles/family_budget.html
Embedding images in testfiles/family_budget.html
```

### lo-to-html.sh
Script to launch convert a file to HTML using LibreOffice

**Usage:** lo-to-html.sh /path/to/spreadsheet.xls /path/to/destination/dir /path/to/soffice.bin /path/to/libreoffice_userdir

### embed-html-images.sh
Script to launch convert an excel file to HTML and embed images using LibreOffice

**Usage:** excel-to-html.sh /path/to/spreadsheet.html /path/output/dir /path/to/html-embed-images.jar /optional/path/to/java/bin/dir

License
-------

This application is licensed under the LGPLv3 License. See the [LICENSE file](LICENSE) for details.

Authors
-------

Marcus Svartmark - Redpill Linpro AB

Erik Billerby - Redpill Linpro AB

Anton Häägg - Redpill Linpro AB
