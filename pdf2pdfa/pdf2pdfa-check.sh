#!/bin/bash

SOFFICE_BIN=$1
SOFFICE_USER=$2
TEST_FILE=$3
TEMP_DIR=$SOFFICE_USER/transformtmp

USAGE="Usage: pdf2pdfa-validation.sh /path/to/soffice /path/to/soffice/user /path/to/testfile.pdf"
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
elif [ -z "$TEST_FILE" ]
then
      echo "Path to test file is missing (3)"
      echo $USAGE
      exit 1
fi

SED="sed"
TIMEOUT="timeout"
$SOFFICE_BIN --headless "-env:UserInstallation=file://$SOFFICE_USER" --convert-to pdf --outdir $TEMP_DIR $TEST_FILE

if grep -q SelectPdfVersion "$SOFFICE_USER/user/registrymodifications.xcu"; then
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
  $SED -i "3i$PDFA_CONFIG" $SOFFICE_USER/user/registrymodifications.xcu

  $TIMEOUT 30 $SOFFICE_BIN --headless "-env:UserInstallation=file://$SOFFICE_USER" --convert-to pdf --outdir $TEMP_DIR $TEST_FILE
fi
