#!/bin/bash
#Build excel2html
mkdir -p ./buildDir/excel2html
cp excel-to-html/*.sh ./buildDir/excel2html/
cp html-embed-images/target/*.jar ./buildDir/excel2html/html-embed-images.jar
cd buildDir
zip excel2html.zip excel2html/*
mv excel2html.zip ../
rm ./excel2html/*.sh ./excel2html/*.jar
rmdir ./excel2html
cd ..
cp excel2html.zip excel-2-html-t-engine/

#Build pdf2pdfa
mkdir -p ./buildDir/pdf2pdfa
cp pdf2pdfa/*.sh ./buildDir/pdf2pdfa/
cp pdf2pdfa/*.pdf ./buildDir/pdf2pdfa/
cd buildDir
zip pdf2pdfa.zip pdf2pdfa/*
mv pdf2pdfa.zip ../
rm ./pdf2pdfa/*.sh ./pdf2pdfa/*.pdf
rmdir ./pdf2pdfa
cd ..
rmdir ./buildDir
