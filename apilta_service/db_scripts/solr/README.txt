This is an alternative setup structure to support multiple cores.

To run this configuration, start jetty in the example/ directory using:

java -Dsolr.solr.home=ca_frontend_solr -Djetty.host=127.0.0.1 -Djetty.port=8983 -jar start.jar

For general examples on standard solr configuration, see the "solr" directory.

copy the jts-core-1.14.0.jar to SOLR_INSTALL_DIR/server/solr-webapp/webapp/WEB-INF/lib/
