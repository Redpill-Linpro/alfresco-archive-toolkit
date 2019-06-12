#!/bin/bash
mkdir -p ./buildDir/excel2html
cp excel-to-html/*.sh ./buildDir/excel2html/
cp html-embed-images/target/*.jar ./buildDir/excel2html/html-embed-images.jar
cd buildDir
zip excel2html.zip excel2html/*
mv excel2html.zip ../
rm ./excel2html/*.sh ./excel2html/*.jar
rmdir ./excel2html
cd ..
rmdir ./buildDir
