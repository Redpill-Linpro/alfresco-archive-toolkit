#!/bin/bash
mkdir ./buildDir
cp excel-to-html/*.sh ./buildDir/
cp html-embed-images/target/*.jar ./buildDir/html-embed-images.jar
zip excel-to-html.zip buildDir/*
rm ./buildDir/*.sh ./buildDir/*.jar
rmdir ./buildDir
