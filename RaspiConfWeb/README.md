RaspiConfWeb
------------

This is a simple servlet web application for quickly implementing test back ends for Apilta task service.

### Instructions

Rename the build.properties.orig to build.properties. Modify build.properties to match your installation details. All required .jar files
are in the lib directory.

Importing the source code as existing ant project to eclipse will probably not
work. It is better to create a new java project and select the project directory
to be the source code location (RaspiConfWeb), and set the required build path
variables manually.

Common IDE's may require the servlet-api.jar of your preferred Tomcat installation to be added to the build path manually.

### Server deployment

- Any relatively new Apache Tomcat (tested on Tomcat 9, other servlet containers may also work) should work.
- For tomcat, the administration user (in tomcat-user.xml) requires the permission: manager-script when deployment using Ant is used. 
- Java 8+ is required (Oracle's or OpenJDK).
- The ant build script should create a working .war file automatically, which can be deployed to the servler container.
