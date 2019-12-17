#!/bin/bash

SOFFICE_BIN=$1
SOFFICE_USER=$2
INPUT_FILE=$3
OUTPUT_FILE=$4
TEMP_DIR=$SOFFICE_USER/transformtmp

USAGE="Usage: pdf2pdfa-transform.sh /path/to/soffice /path/to/soffice/user /path/to/inputfile.pdf /path/to/outputfile.pdf"
PDFA_CONFIG="<item oor:path=\"/org.openoffice.Office.Common/Filter/PDF/Export\"><prop oor:name=\"SelectPdfVersion\" oor:op=\"fuse\"><value>1</value></prop></item>"

if [ -z "$SOFFICE_BIN" ]
then
      echo "Path to soffice bin is missing (1)"
      echo $USAGE
      exit 1
elif [ -z "$SOFFICE_USER" ]
then
      echo "Path to soffice user dir is missing (2)"
      echo $USAGE
      exit 1
elif [ -z "$INPUT_FILE" ]
then
      echo "Path to input file is missing (3)"
      echo $USAGE
      exit 1
elif [ -z "$OUTPUT_FILE" ]
then
      echo "Path to output file is missing (4)"
      echo $USAGE
      exit 1
fi

#Calculate expected output filename from LO with original file extension
TMP_FILENAME=$(basename "$INPUT_FILE")
#Calculate expected output filename from LO with new file extension
OUTPUT_FILE_NAME=${TMP_FILENAME%.*}.pdf
SED="sed"
TIMEOUT="timeout"
$SOFFICE_BIN --headless "-env:UserInstallation=file://$SOFFICE_USER" --convert-to pdf --outdir "$TEMP_DIR" "$INPUT_FILE"

if grep -q SelectPdfVersion "$SOFFICE_USER/user/registrymodifications.xcu"; then
  echo "Created pdf/a from pdf (1)"
else
  echo "Configuring tool to produce pdf/a"
  if [[ "$OSTYPE" == "darwin"* ]]
  then
    # Mac OSX - sed does not work properly on macosx, use gnused
    SED="gsed"
    TIMEOUT="gtimeout"
  fi
  $SED -i "3i$PDFA_CONFIG" $SOFFICE_USER/user/registrymodifications.xcu
  $TIMEOUT 60 $SOFFICE_BIN --headless "-env:UserInstallation=file://$SOFFICE_USER" --convert-to pdf --outdir "$TEMP_DIR" "$INPUT_FILE"
  echo "Created pdf/a from pdf (2)"
fi

echo "Copying $TEMP_DIR/$OUTPUT_FILE_NAME to $OUTPUT_FILE"
cp "$TEMP_DIR/$OUTPUT_FILE_NAME" "$OUTPUT_FILE"
rm "$TEMP_DIR/$OUTPUT_FILE_NAME"