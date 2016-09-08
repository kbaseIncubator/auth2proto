Temporary and experimental code for the auth server rewrite. Eventually 
useful code will be migrated (with unit tests & documentation) to kbase/auth2.

Unit tests & documentation for code in this repo is not required.

Requirements
------------
MongoDB 2.4+ (https://www.mongodb.com/)  
Jetty 9.3+ (http://www.eclipse.org/jetty/download.html)
    (see jetty-config.md for version used for testing)  
This repo (git clone https://github.com/kbaseIncubator/auth2proto)  
The jars repo (git clone https://github.com/kbase/jars)  
The two repos above need to be in the same parent folder.

To start server
---------------
start mongodb  
cd into the auth2 repo
ant compile  
ant buildwar  
copy deploy.cfg.example to deploy.cfg and fill in appropriately  
`export KB_DEPLOYMENT_CONFIG=<path to deploy.cfg>`  
cd jettybase  
./jettybase$ java -jar -Djetty.port=<port> <path to jetty install>/start.jar  

Start & stop server w/o a pid
-----------------------------
./jettybase$ java -DSTOP.PORT=8079 -DSTOP.KEY=foo -jar ~/jetty/jetty-distribution-9.3.11.v20160721/start.jar  
./jettybase$ java -DSTOP.PORT=8079 -DSTOP.KEY=foo -jar ~/jetty/jetty-distribution-9.3.11.v20160721/start.jar --stop  

Omit the stop key to have jetty generate one for you.


