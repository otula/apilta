ca_frontend
-----------

- Core service platform
- A fork of VisualLabel core (https://github.com/visuallabel/CAFrontEnd)

### Instructions

Rename the build.properties.orig to build.properties. Edit the file for
appropriate modifications for your use case.

You need to manually include in your build path the files websocket-api.jar and 
servlet-api.jar, generally found in the lib directory of the Apache Tomcat installation.

All other required .jar files are in the lib directory.

Importing the source code as existing ant project to eclipse will probably not
work. It is better to create a new java project and select the project directory
to be the source code location (ca_frontend), and set the required build path
variables manually.

Modify build.properties and *.properties files in /conf to enable your preferred
services.

The path to the main build configuration property file must be given to Ant.
For example, ant -propertyfile build.properties

### Javadoc
To generate javadoc documentation you may use file options as a base. For example
# javadoc @options -J-Dtut.pori.javadocer.rest_uri="http://example.org/CAFrontEnd/rest/"

There are couple of parameters that need to be set for the javadoc JVM
REST Base URL
 -J-Dtut.pori.javadocer.rest_uri="http://example.org/CAFrontEnd/rest/"
Log4j configuration file
 -J-Dlog4j.configurationFile="..\javadocer\conf\log4j2.xml"
