#!/bin/bash

# Script to launch convert an excel file to HTML and embed images using LibreOffice
# Usage: excel-to-html.sh /path/to/spreadsheet.xls /path/to/destination/dir/filename.html /path/to/soffice.bin /path/to/libreoffice_userdir /path/to/executables /optional/path/to/java

INPUT_FILE_NAME=$1
OUTPUT_FILE=$2
OUTPUT_PATH=$(dirname "${OUTPUT_FILE}")
LO_EXEC=$3
LO_USER_DIR=$4
EXEC_PATH=$5
JAVA_BIN=$6
OUTPUT_FILE_NAME=${INPUT_FILE_NAME%.*}.html
dirpath=`pwd`

if [ "$INPUT_FILE_NAME" == "--version" ]
then
      echo "Version is 1.2.0"
      exit 0
elif [ -z "$INPUT_FILE_NAME" ]
then
      echo "Input file name is missing (1)"
      echo "Usage: excel-to-html.sh /path/to/spreadsheet.xls /path/to/destination/dir/filename.html /path/to/soffice.bin /path/to/libreoffice_userdir /path/to/executables /optional/path/to/java"
      exit 1
elif [ -z "$OUTPUT_FILE" ]
then
      echo "Output file is missing (2)"
      echo "Usage: excel-to-html.sh /path/to/spreadsheet.xls /path/to/destination/dir/filename.html /path/to/soffice.bin /path/to/libreoffice_userdir /path/to/executables /optional/path/to/java"
      exit 1
elif [ -z "$LO_EXEC" ]
then
      echo "Libreoffice bin is missing (3)"
      echo "Usage: excel-to-html.sh /path/to/spreadsheet.xls /path/to/destination/dir/filename.html /path/to/soffice.bin /path/to/libreoffice_userdir /path/to/executables /optional/path/to/java"
      exit 1
elif [ -z "$LO_USER_DIR" ]
then
      echo "Libreoffice user dir is missing (4)"
      echo "Usage: excel-to-html.sh /path/to/spreadsheet.xls /path/to/destination/dir/filename.html /path/to/soffice.bin /path/to/libreoffice_userdir /path/to/executables /optional/path/to/java"
      exit 1
elif [ -z "$EXEC_PATH" ]
then
      echo "Embedded binary path is missing (5)"
      echo "Usage: excel-to-html.sh /path/to/spreadsheet.xls /path/to/destination/dir/filename.html /path/to/soffice.bin /path/to/libreoffice_userdir /path/to/executables /optional/path/to/java"
      exit 1
elif [ -z "$JAVA_BIN" ]
then
      JAVA_BIN="java"
fi

$EXEC_PATH/lo-to-html.sh $INPUT_FILE_NAME $OUTPUT_PATH $LO_EXEC $LO_USER_DIR
$EXEC_PATH/embed-html-images.sh $OUTPUT_FILE_NAME $EXEC_PATH/html-embed-images.jar $JAVA_BIN
cp $OUTPUT_FILE_NAME $OUTPUT_FILE
exit 0