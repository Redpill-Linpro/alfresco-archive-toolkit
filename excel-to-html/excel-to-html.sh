#!/bin/bash

# Script to launch convert an excel file to HTML and embed images using LibreOffice
# Usage: excel-to-html.sh /path/to/spreadsheet.xls /path/to/destination/dir /path/to/soffice.bin /path/to/libreoffice_userdir /path/to/html-embed-images.jar /optional/path/to/java

INPUT_FILE_NAME=$1
OUTPUT_PATH=$2
LO_EXEC=$3
LO_USER_DIR=$4
EMBED_EXEC=$5
JAVA_BIN=$6
OUTPUT_FILE_NAME=${INPUT_FILE_NAME%.*}.html
dirpath=`pwd`

if [ -z "$INPUT_FILE_NAME" ]
then
      echo "Input file name is missing (1)"
      echo "Usage: excel-to-html.sh /mandatory/path/to/spreadsheet.xls /path/to/soffice.bin /mandatory/path/to/libreoffice_userdir /mandatory/path/to/html-embed-images.jar /optional/path/to/java"
      exit 1
elif [ -z "$OUTPUT_PATH" ]
then
      echo "Output path is missing (2)"
      echo "Usage: excel-to-html.sh /mandatory/path/to/spreadsheet.xls /path/to/soffice.bin /mandatory/path/to/libreoffice_userdir /mandatory/path/to/html-embed-images.jar /optional/path/to/java"
      exit 1
elif [ -z "$LO_EXEC" ]
then
      echo "Libreoffice bin is missing (3)"
      echo "Usage: excel-to-html.sh /mandatory/path/to/spreadsheet.xls /path/to/soffice.bin /mandatory/path/to/libreoffice_userdir /mandatory/path/to/html-embed-images.jar /optional/path/to/java"
      exit 1
elif [ -z "$LO_USER_DIR" ]
then
      echo "Libreoffice user dir is missing (4)"
      echo "Usage: excel-to-html.sh /mandatory/path/to/spreadsheet.xls /path/to/soffice.bin /mandatory/path/to/libreoffice_userdir /mandatory/path/to/html-embed-images.jar /optional/path/to/java"
      exit 1
elif [ -z "$EMBED_EXEC" ]
then
      echo "Embedded binary path is missing (5)"
      echo "Usage: excel-to-html.sh /mandatory/path/to/spreadsheet.xls /path/to/soffice.bin /mandatory/path/to/libreoffice_userdir /mandatory/path/to/html-embed-images.jar /optional/path/to/java"
      exit 1
elif [ -z "$JAVA_BIN" ]
then
      JAVA_BIN="java"
fi

$dirpath/lo-to-html.sh $INPUT_FILE_NAME $OUTPUT_PATH $LO_EXEC $LO_USER_DIR
$dirpath/embed-html-images.sh $OUTPUT_FILE_NAME $EMBED_EXEC $JAVA_BIN