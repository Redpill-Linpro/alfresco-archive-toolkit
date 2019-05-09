#!/bin/bash

# Script to launch convert an excel file to HTML and embed images using LibreOffice
# Usage: embed-html-images.sh /path/to/spreadsheet.html /path/to/html-embed-images.jar /optional/path/to/java/bin/dir
INPUT_FILE_NAME=$1
EMBED_EXEC=$2
JAVA_BIN=$3

if [ -z "$INPUT_FILE_NAME" ]
then
      echo "Input file name is missing (1)"
      echo "Usage: embed-html-images.sh /path/to/spreadsheet.html /path/to/html-embed-images.jar /optional/path/to/java/bin/dir"
      exit 1
elif [ -z "$EMBED_EXEC" ]
then
      echo "Embedded binary path is missing (2)"
      echo "Usage: embed-html-images.sh /path/to/spreadsheet.html /path/to/html-embed-images.jar /optional/path/to/java/bin/dir"
      exit 1
elif [ -z "$JAVA_BIN" ]
then
      JAVA_BIN="java"
fi

$JAVA_BIN -jar $EMBED_EXEC $INPUT_FILE_NAME
