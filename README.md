The APILTA project source code repository

https://www.avoinsatakunta.fi/apilta


Contents of this repository
---------------------------

AlertApplication
 - Android application for reporting events (accidents, animals on road, traffic congestion, etc...) while during a car.
- The application can also be used for listening for events submitted by other road users.
- Server end-point is in apilta_service (AlertService/ClientService, https://otula.github.io/projektit/apilta-api/service/tut/pori/apilta/alerts/reference/package-summary.html)


apilta_service
- Apilta Service end point (https://otula.github.io/projektit/apilta-api)
- For compilation requires ca_frontend


backend_cmd
- Java command-line client for retrieving task details from the service and submitting task results to the service
- can be used individually or for submitting data stored into a sqlite database by PeopleCounter or RPiCameraProto


ca_frontend
- Core service platform
- A fork of VisualLabel core (https://github.com/visuallabel/CAFrontEnd)


DigitrafficBackend, ParkingBackend, SampoBackend, TrafficDemo1 and backend_web
- Example back ends for accessing data by utilizing DigiTraffic, Pori parking data and Sampo satellite images and visualizing the data on map (TrafficDemo1)
- all three are based on the backend_web template
- Service end-point is in apilta_service (SensorsService/ServerService, https://otula.github.io/projektit/apilta-api/service/tut/pori/apilta/sensors/reference/package-summary.html)


Mapicker
- Simple html page for showing measurement details on map
- Service end-point is in apilta_service (SensorsService/ClientService, https://otula.github.io/projektit/apilta-api/service/tut/pori/apilta/sensors/reference/package-summary.html)


PeopleCounter
- Counter for tracking people (object) movement using Raspberry Pi camera
- Can be used individually or in combination with backend_cmd and RaspiConfWeb


RaspiConfWeb
- Simple web-based application for configuring basic settings for PeopleCounter
- Example configuration files can be found in PeopleCounter/templates


RoadRoamer
- Android application for automatically capturing photos when approaching designated targets
- Service end-point is in apilta_service (SensorsService/ClientService, https://otula.github.io/projektit/apilta-api/service/tut/pori/apilta/sensors/reference/package-summary.html)


RPiCameraProto
- Application for automatically capturing photos when approaching designated targets using the Raspberry Pi platform
- Can be utilized individually or in combination with backend_cmd
- Example target and configuration files can be found in PeopleCounter/templates


ShockApplication
- Simple Android application (and foreground service) for utilizing device sensors (location and accelerometer) for submitting "shock" data to the service
- Service end-point is in apilta_service (SensorsService/ClientService, https://otula.github.io/projektit/apilta-api/service/tut/pori/apilta/sensors/reference/package-summary.html)


ShockDemo1 and ShockDemo2
- html pages for illustrating shock data on map using Google Maps (ShockDemo1) and OpenStreetMaps/OpenLayers (ShockDemo2)
- Service end-point is in apilta_service (SensorsService/ClientService, https://otula.github.io/projektit/apilta-api/service/tut/pori/apilta/sensors/reference/package-summary.html)


TaskManager
- TBA


Configuration files
-------------------
The configuration files are generally located inside the conf-directory, at the root directory of the application. 

If additional configuration is required, check the README located at the root directory of the application.


License
-------

The contents of this repository are licensed under the Apache License, Version 2.0, unless otherwise stated.

The 3rd party components included are licensed under their respective licenses. Check the individual application directories for further details.
