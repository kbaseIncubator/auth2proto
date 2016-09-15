FROM ubuntu:14.04
MAINTAINER Shane Canon <scanon@lbl.gov>

#
# Install build dependencies and openjdk-8
#
RUN apt-get update && apt-get -y install git ant make gcc wget software-properties-common unzip && \
   add-apt-repository -y ppa:openjdk-r/ppa && \
   apt-get update && \
   apt-get -y install openjdk-8-jdk

# Install jars and Jetty
RUN \
   mkdir /src && cd /src && git clone https://github.com/kbase/jars  && \
   apt-get -y install unzip  && \
   cd /src/ && wget http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/9.3.11.v20160721/jetty-distribution-9.3.11.v20160721.zip && \
   unzip jetty*zip && \
   rm jetty*zip 

ADD . /src/auth2proto

RUN \
    export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/ && \
    cd /src/auth2proto && \
    ant compile && \
    ant buildwar

#   24  /usr/lib/jvm/java-8-openjdk-amd64/bin/java -DSTOP.PORT=8079 -DSTOP.KEY=foo -jar /src/jetty-distribution-9.3.11.v20160721/start.jar  
ENV KB_DEPLOYMENT_CONFIG /config/deployment.cfg
WORKDIR /src/auth2proto/

ENTRYPOINT [ "./entrypoint.sh" ]
CMD [ ]
