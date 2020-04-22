#!/bin/bash

SOFFICE_BIN=$1
SOFFICE_USER=$2
TEST_FILE=$3
TEMP_DIR=$SOFFICE_USER/transformtmp

USAGE="Usage: pdf2pdfa-validation.sh /path/to/soffice /path/to/soffice/user /path/to/testfile.pdf"
PDFA_CONFIG="<item oor:path=\"/org.openoffice.Office.Common/Filter/PDF/Export\"><prop oor:name=\"ExportBookmarks\" oor:op=\"fuse\"><value>false</value></prop></item>\n<item oor:path=\"/org.openoffice.Office.Common/Filter/PDF/Export\"><prop oor:name=\"ReduceImageResolution\" oor:op=\"fuse\"><value>false</value></prop></item>\n<item oor:path=\"/org.openoffice.Office.Common/Filter/PDF/Export\"><prop oor:name=\"SelectPdfVersion\" oor:op=\"fuse\"><value>1</value></prop></item>\n<item oor:path=\"/org.openoffice.Office.Common/Filter/PDF/Export\"><prop oor:name=\"UseLosslessCompression\" oor:op=\"fuse\"><value>true</value></prop></item>\n"

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
elif [ -z "$TEST_FILE" ]
then
      echo "Path to test file is missing (3)"
      echo $USAGE
      exit 1
fi

SED="sed"
TIMEOUT="timeout"


REGISTRY_FILE="$SOFFICE_USER/user/registrymodifications.xcu"
if [ ! -f "$REGISTRY_FILE" ]; then
    echo "$REGISTRY_FILE does not exist, trying to convert a non-existing file to create required user dir"
    "$SOFFICE_BIN" --headless "-env:UserInstallation=file://$SOFFICE_USER" --convert-to pdf --outdir "$TEMP_DIR" "nonexistingfile_$TEST_FILE"
fi

if grep -q SelectPdfVersion "$REGISTRY_FILE"; then
  echo "Tool already configured to produce pdf/a"
else
  echo "Configuring tool to produce pdf/a"
  if [[ "$OSTYPE" == "darwin"* ]]
  then
    # Mac OSX - sed does not work properly on macosx, use gnused (brew install gnused)
    SED="gsed"
    # Mac OSX - timeout does not work properly on macosx, use gtimeout (brew install coreutils)
    TIMEOUT="gtimeout"
  fi
  $SED -i "3i$PDFA_CONFIG" $REGISTRY_FILE

fi

if ! $TIMEOUT --preserve-status  --signal 9 "30" "$SOFFICE_BIN" --headless "-env:UserInstallation=file://$SOFFICE_USER" --convert-to pdf --outdir "$TEMP_DIR" "$TEST_FILE"
then
  echo "Timed out creating pdf/a from pdf" ;
  exit 1
else
  echo "Created pdf/a from pdf" ;
fi
