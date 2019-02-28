# Command line tools to convert Excel files to html with embedded images

## excel-to-html.sh

Script to launch convert an excel file to HTML and embed images using LibreOffice. Uses lo-to-html.sh and embed-html-images.sh

**Usage:** excel-to-html.sh /path/to/spreadsheet.xls /path/to/destination/dir /path/to/soffice.bin /path/to/libreoffice_userdir /path/to/html-embed-images.jar /optional/path/to/java


**Example:**
./excel-to-html.sh testfiles/family_budget.xls testfiles /Applications/LibreOffice.app/Contents/MacOS/soffice /tmp ../html-embed-images/target/html-embed-images-1.2.0-SNAPSHOT.jar 

**Output:**
```
convert /Users/mars/Documents/alfresco/alfresco-archive-toolkit/excel-to-html/testfiles/family_budget.xls -> /Users/mars/Documents/alfresco/alfresco-archive-toolkit/excel-to-html/testfiles/family_budget.html using filter : HTML (StarCalc)
Overwriting: /Users/mars/Documents/alfresco/alfresco-archive-toolkit/excel-to-html/testfiles/family_budget.html
Embedding images in testfiles/family_budget.html
```

## lo-to-html.sh
Script to launch convert a file to HTML using LibreOffice

**Usage:** lo-to-html.sh /path/to/spreadsheet.xls /path/to/destination/dir /path/to/soffice.bin /path/to/libreoffice_userdir

## embed-html-images.sh
Script to launch convert an excel file to HTML and embed images using LibreOffice

**Usage:** excel-to-html.sh /path/to/spreadsheet.html /path/output/dir /path/to/html-embed-images.jar /optional/path/to/java/bin/dir