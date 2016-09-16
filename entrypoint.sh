#!/bin/sh

cd /src/auth2proto/jettybase/

/usr/lib/jvm/java-8-openjdk-amd64/bin/java  -DSTOP.PORT=8079 -DSTOP.KEY=foo -jar /src/jetty-distribution-9.3.11.v20160721/start.jar 
