ParkingBackend
--------------

This is a test back end for retrieving parking information for a city, the only city currently supported is Pori (https://www.avoindata.fi/data/fi/dataset/porin-pysakointialueet).

This back end also demonstrates the use of single run tasks, which do not repeat over time.

Service end-point is in apilta_service (SensorsService/ServerService, https://otula.github.io/projektit/apilta-api/service/tut/pori/apilta/sensors/reference/package-summary.html). This is based on the backend_web template (https://github.com/otula/apilta/tree/master/backend_web).

### Instructions

Rename the build.properties.orig to build.properties. Modify build.properties to match your installation details. All required .jar files
are in the lib directory.

Importing the source code as existing ant project to eclipse will probably not
work. It is better to create a new java project and select the project directory
to be the source code location (ParkingBackend), and set the required build path
variables manually.

Common IDE's may require the servlet-api.jar of your preferred Tomcat installation to be added to the build path manually.

### Server deployment
- Any relatively new Apache Tomcat (tested on Tomcat 9, other servlet containers may also work) should work.
- For tomcat, the administration user (in tomcat-user.xml) requires the permission: manager-script when deployment using Ant is used. 
- Java 7+ is required (Oracle's or OpenJDK).
- The ant build script should create a working .war file automatically, which can be deployed to the servler container.
