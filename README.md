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
  <version>1.2.1</version>
</dependency>
```

Share dependency:
```xml
<dependency>
  <groupId>org.redpill-linpro.alfresco.archive</groupId>
  <artifactId>alfresco-archive-toolkit</artifactId>    
  <version>1.2.1</version>
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

The Excel to HTML tools are built using package.sh. This creates a zip file containing the necessary shell files and jar files for Excel to HTML conversions. Install on your server and configure according to the instructions below.

PDF to PDF/a conversion
-----------------------
Conversion from PDF to PDF/a is done using the Alfresco action ConvertToPdfActionExecuter using the fake file extension pdfa & fake mimetype application/pdfa

Install ghostscript on the server and set the path to the ghostscript executable in alfresco-global.properties: `gs.exe=path_to_ghostscript`

Install pdf2archive from https://github.com/marsv024/pdf2archive or https://github.com/matteosecli/pdf2archive. 
Add the path to the pdf2archive executable in alfresco-global.properties: `pdf2archive.exe=path_to_pdf2archive` 

PDF to PDF/a conversions can be audited at the page http://localhost:8080/share/page/archive-toolkit/audit

Excel to HTML conversion
------------------------
Converting Excel files to HTML with embedded images are supported with a set of command line tools and a custom transformer in Alfresco. Requires LibreOffice

**excel-to-html.sh**
Script to launch convert an excel file to HTML and embed images using LibreOffice. Uses lo-to-html.sh and embed-html-images.sh

Usage: `excel-to-html.sh /path/to/spreadsheet.xls /path/to/destination/dir /path/to/soffice.bin /path/to/libreoffice_userdir /path/to/html-embed-images.jar /optional/path/to/java`


Example: `./excel-to-html.sh testfiles/family_budget.xls testfiles /Applications/LibreOffice.app/Contents/MacOS/soffice /tmp ../html-embed-images/target/html-embed-images-1.2.0.jar`

Output:
```
convert /Users/mars/Documents/alfresco/alfresco-archive-toolkit/excel-to-html/testfiles/family_budget.xls -> /Users/mars/Documents/alfresco/alfresco-archive-toolkit/excel-to-html/testfiles/family_budget.html using filter : HTML (StarCalc)
Overwriting: /Users/mars/Documents/alfresco/alfresco-archive-toolkit/excel-to-html/testfiles/family_budget.html
Embedding images in testfiles/family_budget.html
```

**lo-to-html.sh**
Script to launch convert a file to HTML using LibreOffice

Usage: `lo-to-html.sh /path/to/spreadsheet.xls /path/to/destination/dir /path/to/soffice.bin /path/to/libreoffice_userdir`

**embed-html-images.sh**
Script to launch convert an output HTML file from LibreOffice to a HTML file with embedded images

Usage: `embed-html-images.sh /path/to/spreadsheet.html /path/to/html-embed-images.jar /optional/path/to/java/bin/dir`

**Alfresco Configuration**
```
#Path to excel-to-html.sh script
excelToHtml.exe=/opt/alfresco/excel2html/excel-to-html.sh
#The path to where the shell scripts and jar file exists
excelToHtml.path=/opt/alfresco/excel2html
#Path to LibreOffice binary
excel.soffice=${ooo.exe}
#Temporary directory, used for conversions and LibreOffice temporary files
excel.libreoffice.user=${java.io.tmpdir}
#Transformer settings
content.transformer.ExcelToHtml.priority=30
content.transformer.ExcelToHtml.extensions.xls.html.supported=true
content.transformer.ExcelToHtml.extensions.xls.html.priority=20
content.transformer.ExcelToHtml.extensions.xls.html.maxSourceSizeKBytes.use.index=9999
content.transformer.ExcelToHtml.extensions.xlsx.html.supported=true
content.transformer.ExcelToHtml.extensions.xlsx.html.priority=20
content.transformer.ExcelToHtml.extensions.xlsx.html.maxSourceSizeKBytes.use.index=9999
```

License
-------

This application is licensed under the LGPLv3 License. See the [LICENSE file](LICENSE) for details.

Authors
-------

Marcus Svartmark - Redpill Linpro AB

Erik Billerby - Redpill Linpro AB

Anton Häägg - Redpill Linpro AB
