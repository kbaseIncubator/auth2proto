Temporary and experimental code for the auth server rewrite. Eventually 
useful code will be migrated (with unit tests & documentation) to kbase/auth2.

Unit tests & documentation for code in this repo is not required.

To start server
---------------
install jetty (see jetty-config.md for version)  
clone kbase/jars into the parent folder of this repo  
ant compile  
ant buildwar  
copy deploy.cfg.example to deploy.cfg and fill in appropriately  
`export KB_DEPLOYMENT_CONFIG=<path to deploy.cfg>`  
start mongodb  
cd jettybase  
./jettybase$ java -jar -Djetty.port=20002 ~/jetty/jetty-distribution-9.3.11.v20160721/start.jar  

Start & stop server
-------------------
./jettybase$ java -DSTOP.PORT=8079 -DSTOP.KEY=foo -jar ~/jetty/jetty-distribution-9.3.11.v20160721/start.jar  
./jettybase$ java -DSTOP.PORT=8079 -DSTOP.KEY=foo -jar ~/jetty/jetty-distribution-9.3.11.v20160721/start.jar --stop  

Omit the stop key to have jetty generate one for you.


