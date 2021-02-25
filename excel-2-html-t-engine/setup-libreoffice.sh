#!/bin/sh

export VERSION=6.4.4.2

mkdir -p /tmp/libre
cd /tmp/libre
curl -OL http://downloadarchive.documentfoundation.org/libreoffice/old/${VERSION}/rpm/x86_64/LibreOffice_${VERSION}_Linux_x86-64_rpm.tar.gz
curl -OL http://downloadarchive.documentfoundation.org/libreoffice/old/${VERSION}/rpm/x86_64/LibreOffice_${VERSION}_Linux_x86-64_rpm_langpack_sv.tar.gz
tar xzf LibreOffice_${VERSION}_Linux_x86-64_rpm.tar.gz
tar xzf LibreOffice_${VERSION}_Linux_x86-64_rpm_langpack_sv.tar.gz
cd  LibreOffice_${VERSION}_Linux_x86-64_rpm/RPMS
yum -y install *.rpm
cd ../../LibreOffice_${VERSION}_Linux_x86-64_rpm_langpack_sv/RPMS
yum -y install *.rpm
cd /tmp
rm -r /tmp/libre
echo LibreOffce install