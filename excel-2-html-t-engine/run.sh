#!/bin/sh

mvn clean install -Plocal
docker run -p 8091:8091 -p 8888:8888 --name excel-2-html-t-engine --rm excel-2-html-t-engine:latest