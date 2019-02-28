#!/bin/bash

# Script to launch convert a file to HTML using LibreOffice
# Usage: lo-to-html.sh /path/to/spreadsheet.xls /path/to/destination/dir /path/to/soffice.bin /path/to/libreoffice_userdir
INPUT_FILE_NAME=$1
OUTPUT_PATH=$2
LO_EXEC=$3
LO_USER_DIR=$4

if [ -z "$INPUT_FILE_NAME" ]
then
      echo "Input file name is missing (1)"
      echo "Usage: lo-to-html.sh /path/to/spreadsheet.xls /path/to/destination/dir /path/to/soffice.bin /path/to/libreoffice_userdir"
      exit 1
elif [ -z "$OUTPUT_PATH" ]
then
      echo "Input file name is missing (2)"
      echo "Usage: lo-to-html.sh /path/to/spreadsheet.xls /path/to/destination/dir /path/to/soffice.bin /path/to/libreoffice_userdir"
      exit 1
elif [ -z "$LO_EXEC" ]
then
      echo "Libreoffice bin is missing (3)"
      echo "Usage: lo-to-html.sh /path/to/spreadsheet.xls /path/to/destination/dir /path/to/soffice.bin /path/to/libreoffice_userdir"
      exit 1
elif [ -z "$LO_USER_DIR" ]
then
      echo "Libreoffice user dir is missing (4)"
      echo "Usage: lo-to-html.sh /path/to/spreadsheet.xls /path/to/destination/dir /path/to/soffice.bin /path/to/libreoffice_userdir"
      exit 1
fi

$LO_EXEC --convert-to html $INPUT_FILE_NAME --headless -env:UserInstallation=file://$LO_USER_DIR --outdir $OUTPUT_PATH


