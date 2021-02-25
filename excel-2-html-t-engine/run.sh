#!/bin/sh

mvn clean install -Plocal
docker run -p 8091:8091 --name excel-2-html-t-engine --rm excel-2-html-t-engine:latest