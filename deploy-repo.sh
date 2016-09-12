#!/bin/bash
cd repo
mvn clean package
cp target/*.jar /opt/alfresco/enterprise/4.2.6/vgr/tomcat-repo/webapps/alfresco/WEB-INF/lib
echo "Restarting repo!"
cd /opt/alfresco/enterprise/4.2.6/vgr/
./alfresco-repo restart
