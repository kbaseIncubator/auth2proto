Temporary and experimental code for the auth server rewrite. Eventually 
useful code will be migrated (with unit tests & documentation) to kbase/auth2.

Unit tests & documentation for code in this repo is not required.

To start server
---------------
ant compile
ant buildwar
install jetty (see jetty-config.md for version)
./jettybase$ java -jar ~/jetty/jetty-distribution-9.3.11.v20160721/start.jar 

Start & stop server
-------------------
./jettybase$ java -DSTOP.PORT=8079 -DSTOP.KEY=foo -jar ~/jetty/jetty-distribution-9.3.11.v20160721/start.jar 
./jettybase$ java -DSTOP.PORT=8079 -DSTOP.KEY=foo -jar ~/jetty/jetty-distribution-9.3.11.v20160721/start.jar --stop

Omit the stop key to have jetty generate one for you.


