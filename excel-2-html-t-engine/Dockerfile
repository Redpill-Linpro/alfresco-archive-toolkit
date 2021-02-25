# Image provides a container in which to run the Excel-2-Html docker transformer.

FROM alfresco/alfresco-base-java:11.0.1-openjdk-centos-7-6784d76a7b81

ENV COPY_DIR=/opt/install

# Install ghostscript & other deps
RUN yum install ghostscript bc glibc-common which unzip cairo wget -y

RUN mkdir -p $COPY_DIR

#Install libreoffice
ADD setup-libreoffice.sh $COPY_DIR
RUN /bin/bash -xe $COPY_DIR/setup-libreoffice.sh

ENV JAVA_OPTS=""

# Set default user information
ARG GROUPNAME=Alfresco
ARG GROUPID=1000
ARG HWUSERNAME=transform-excel-2-html-t-engine
ARG USERID=33004

# Copy excel2html to remote
COPY excel2html.zip $COPY_DIR
RUN mkdir -p /opt/excel2html && \
 unzip $COPY_DIR/excel2html.zip -d /opt && \
 chmod +x /opt/excel2html/*.sh && \
 rm $COPY_DIR/excel2html.zip

COPY target/excel-2-html-t-engine-${env.project_version}.jar /usr/bin

RUN ln /usr/bin/excel-2-html-t-engine-${env.project_version}.jar /usr/bin/excel-2-html-t-engine.jar && \
    yum clean all

RUN groupadd -g ${GROUPID} ${GROUPNAME} && \
    useradd -u ${USERID} -G ${GROUPNAME} ${HWUSERNAME} && \
    chgrp -R ${GROUPNAME} /usr/bin/excel-2-html-t-engine.jar && \
    chgrp -R ${GROUPNAME} /opt/*

EXPOSE 8091

USER ${HWUSERNAME}

ENTRYPOINT java $JAVA_OPTS -jar /usr/bin/excel-2-html-t-engine.jar
