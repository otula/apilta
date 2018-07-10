"use strict";
/**
 * mapicker javascript codes
 */
var mapicker = {
	map : null, // the google map object
	uriGetMeasurements : "http://example.org/ApiltaService/rest/sensors/getMeasurements",
	uriQueryTaskDetails : "http://example.org/ApiltaService/rest/sensors/queryTaskDetails",
	username : "user",
	password : "password",
	backendId : "1",
	taskId : "1",
	activePhotosUris : null,
	activePhotoIndex : 0,
	activePosition : null, // for streetview
	PARAMETER_USERNAME : "username",
	PARAMETER_PASSWORD : "password",
	PARAMETER_TASK_ID : "task_id",
	PARAMETER_BACKEND_ID : "backend_id",
	PARAMETER_MEASUREMENT_ID : "measurement_id",
	PARAMETER_DATA_GROUPS : "data_groups",
	ELEMENT_MEASUREMENT : "measurement",
	ELEMENT_MEASUREMENT_ID : "measurementId",
	ELEMENT_DATAPOINT : "dataPoint",
	ELEMENT_KEY : "key",
	ELEMENT_VALUE : "value",
	ELEMENT_URL : "url",
	ELEMENT_TERMS : "terms",
	ELEMENT_ENTRY : "entry",

	/**
	 * initializes the basic settings
	 */
	initialize : function(){
		console.log("Initializing...");

		var temp = mapicker.getUrlParameter(mapicker.PARAMETER_USERNAME);
		if(temp == null){
			console.log("No parameter"+mapicker.PARAMETER_USERNAME+", using default: "+mapicker.username);
		}else{
			mapicker.username = temp;	
		}
		var temp = mapicker.getUrlParameter(mapicker.PARAMETER_PASSWORD);
		if(temp == null){
			console.log("No parameter"+mapicker.PARAMETER_PASSWORD+", using default: "+mapicker.password);
		}else{
			mapicker.password = temp;	
		}
		var temp = mapicker.getUrlParameter(mapicker.PARAMETER_TASK_ID);
		if(temp == null){
			console.log("No parameter"+mapicker.PARAMETER_TASK_ID+", using default: "+mapicker.taskId);
		}else{
			mapicker.taskId = temp;	
		}
		var temp = mapicker.getUrlParameter(mapicker.PARAMETER_BACKEND_ID);
		if(temp == null){
			console.log("No parameter"+mapicker.PARAMETER_BACKEND_ID+", using default: "+mapicker.backendId);
		}else{
			mapicker.backendId = temp;	
		}
		
		mapicker.initializeMap();
		mapicker.retrieveMeasurements();
		mapicker.retrieveTaskDetails();
	},

	/**
	 * initializes the google map layout (map canvas)
	 */
	initializeMap : function(){
		console.log("Initializing map...");
		var mapOptions = { // map options
    		zoom: 5,
   			disableDoubleClickZoom: true,
    		center: new google.maps.LatLng(0,0)
		};
		
		mapicker.map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
	},

	/**
	 * Helper method for setting http basic authorization
	 * 
	 * @param {jqXHR} jqXHR
	 */
	authorize : function(jqXHR){
		jqXHR.setRequestHeader("Authorization", "Basic " + btoa(mapicker.username + ":" + mapicker.password));
	},

	/**
	 * retrieve measurements and add markers to the map
	 */
	retrieveMeasurements : function(){
		var uri = mapicker.uriGetMeasurements+"?"+mapicker.PARAMETER_TASK_ID+"="+mapicker.taskId+"&"+mapicker.PARAMETER_DATA_GROUPS+"=all";
		console.log("Calling "+uri);
		$.ajax({
			url : uri,
			beforeSend: function(jqXHR) {
				mapicker.authorize(jqXHR);
			}, 
			success : function(data){
				var measurements = data.getElementsByTagName(mapicker.ELEMENT_MEASUREMENT);
				mapicker.showResultMarkers(measurements);
			},
			error : function(jqXHR, textStatus, errorThrown){
				console.log("Retrieve measurements failed with status "+jqXHR.status+" "+textStatus);
			}
		});
	},

	/**
	 * retrieve data points for the measurement and show the retrieved points
	 *
	 * @param {string} measurementId
	 */
	retrieveDataPoints : function(measurementId){
		var uri = mapicker.uriGetMeasurements+"?"+mapicker.PARAMETER_TASK_ID+"="+mapicker.taskId+"&"+mapicker.PARAMETER_MEASUREMENT_ID+"="+measurementId+"&"+mapicker.PARAMETER_DATA_GROUPS+"=all";
		console.log("Calling "+uri);
		$.ajax({
			url : uri,
			beforeSend: function(jqXHR) {
				mapicker.authorize(jqXHR);
			}, 
			success : function(data){
				var dataPoints = data.getElementsByTagName(mapicker.ELEMENT_DATAPOINT);
				mapicker.showPhotos(dataPoints);
			},
			error : function(jqXHR, textStatus, errorThrown){
				console.log("Retrieve measurements failed with status "+jqXHR.status+" "+textStatus);
			}
		});
	},

	/**
	 * show markers on map based on the list of measurements (if any)
	 *
	 * @param {NodeList} measurements
	 */
	showResultMarkers : function(measurements){
		if(measurements.length < 1){
			console.log("No measurements.");
			return;
		}
		console.log("Showing markers for "+measurements.length+" measurements.");

		var lat = null;
		var lng = null;
		for(var i=0;i<measurements.length;++i){
			var measurement = measurements.item(i);
			var measurementId = measurement.getElementsByTagName(mapicker.ELEMENT_MEASUREMENT_ID)[0].textContent;

			var dataPoints = measurement.getElementsByTagName(mapicker.ELEMENT_DATAPOINT);
			if(dataPoints.length < 1){
				console.log("No data points for measurement, id: "+measurementId);
				continue;
			}
			
			for(var j=0;j<dataPoints.length;++j){
				var dataPoint = dataPoints.item(j);
				if(dataPoint.getElementsByTagName("key")[0].textContent == "sensor/location"){
					var location = dataPoint.getElementsByTagName(mapicker.ELEMENT_VALUE)[0].textContent.split(",");
					lat = location[0];
					lng = location[1];
					break;
				}
			} // for datapoints
			if(lat == null){
				console.log("No location for measurement, id: "+measurementId);
			}else{
				mapicker.showResultMarker(lat, lng, measurementId);
			}
		} // for measurements

		if(lat == null){
			console.log("No locations for measurement, id: "+measurementId);
		}else{
			mapicker.map.panTo(new google.maps.LatLng(lat, lng)); // center map to the last coordinate
			mapicker.map.setZoom(12);
		}
	},

	/**
	 * add marker to the map
	 *
	 * @param {string} lat
	 * @param {string} lng
	 * @param {string} measurementId
	 */
	showResultMarker : function(lat, lng, measurementId){
		var marker = new google.maps.Marker({
      			position : new google.maps.LatLng(lat,lng),
      			map : mapicker.map,
      			title : measurementId
  		});
		marker.measurementId = measurementId;
		google.maps.event.addListener(marker, 'click', function() {mapicker.activePosition = marker.position; mapicker.retrieveDataPoints(marker.measurementId); });
	},

	/**
	 * show photos from the given list of data poins (if any)
	 *
	 * @param {NodeList} dataPoints
	 */
	showPhotos : function(dataPoints){
		if(dataPoints.length < 1){
			console.log("No data points.");
			return;
		}
		console.log("Showing photos...");
		//TODO open wait dialog

		mapicker.activePhotosUris = [];
		mapicker.activePhotoIndex = 0;
		var uris = [];
		for(var i=0;i<dataPoints.length;++i){
			var dataPoint = dataPoints.item(i);
			if(dataPoint.getElementsByTagName(mapicker.ELEMENT_KEY)[0].textContent == "file/details/url"){
				uris.push(dataPoint.getElementsByTagName(mapicker.ELEMENT_VALUE)[0].textContent);
			}
		} // for data points

		if(uris.length < 1){
			console.log("No photos.");
			//TODO close wait dialog
		}else{
			for(var i=0;i<uris.length;++i){
				mapicker.retrieveFileDetails(uris[i], (i == uris.length-1)); // open on the last uri
			}
		}
	},

	/**
	 * retrieve file details from the uri and resolve the real photo url
	 *
	 * @param {string} detailsUri
	 * @param {boolean} show if true, the photo is shown after retrieval
	 */
	retrieveFileDetails : function(detailsUri, show){
		console.log("Calling "+detailsUri);
		$.ajax({
			url : detailsUri,
			beforeSend: function(jqXHR) {
				mapicker.authorize(jqXHR);
			}, 
			success : function(data){
				mapicker.activePhotosUris.push(data.getElementsByTagName(mapicker.ELEMENT_URL)[0].textContent);
				if(show){
					mapicker.showPhoto(mapicker.activePhotoIndex);
				}
			},
			error : function(jqXHR, textStatus, errorThrown){
				console.log("Retrieve file details failed with status "+jqXHR.status+" "+textStatus);
				if(show){
					mapicker.hidePhotoOverlay(); // hide the photo overlay as the list is now incomplete
				}
			}
		});
	},

	/**
	 * show photo from the given index
	 *
	 * @param {integer} index
	 */
	previousPhoto : function(index){
		if(mapicker.activePhotoIndex == 0){
			mapicker.activePhotoIndex = mapicker.activePhotosUris.length-1;
		}else{
			--mapicker.activePhotoIndex;
		}
		mapicker.showPhoto(mapicker.activePhotoIndex);
	},
	
	/**
	 * show photo from the given index
	 *
	 * @param {integer} index
	 */
	nextPhoto : function(index){
		++mapicker.activePhotoIndex;
		if(mapicker.activePhotoIndex == mapicker.activePhotosUris.length){
			mapicker.activePhotoIndex = 0;
		}
		mapicker.showPhoto(mapicker.activePhotoIndex);
	},

	/**
	 * show photo from the given index and change the active index marker
	 *
	 * @param {integer} index
	 */
	showPhoto : function(index){
		// TODO close wait dialog, if open
		mapicker.activePhotoIndex = index;
		$("#photo").attr("style", "background-image: url('"+mapicker.activePhotosUris[index]+"')");
		$("#photo-overlay").removeClass("hidden");
	},

	/**
	 * hide the photo overlay
	 */
	hidePhotoOverlay : function(){
		$("#photo-overlay").addClass("hidden");
	},

	/**
	 * show street view in the current location
	 */
	showStreetView : function(){
		var panorama = mapicker.map.getStreetView();
		panorama.setPosition(mapicker.activePosition);
		panorama.setVisible(true);
		mapicker.hidePhotoOverlay();
	},

	/**
 	 * Get URL Parameters Using Javascript
 	 * Reference: http://www.netlobo.com/url_query_string_javascript.html
 	 * @param {String} parameterName
 	 * @return {String} the requested parameter value, or null if not found.
 	 */
	getUrlParameter : function(parameterName){
		var name = parameterName.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
		var regexS = "[\\?&]"+name+"=([^&#]*)";
		var regex = new RegExp( regexS );
		var results = regex.exec(window.location.href);
		if(results === null){
			return null;
		}
		return results[1];
	},

	/**
	 * retrieve task details (location details) and add markers to the map
	 */
	retrieveTaskDetails : function(){
		var uri = mapicker.uriQueryTaskDetails+"?"+mapicker.PARAMETER_TASK_ID+"="+mapicker.taskId+"&"+mapicker.PARAMETER_BACKEND_ID+"="+mapicker.backendId;
		console.log("Calling "+uri);
		$.ajax({
			url : uri,
			beforeSend: function(jqXHR) {
				mapicker.authorize(jqXHR);
			}, 
			success : function(data){
				var terms = data.getElementsByTagName(mapicker.ELEMENT_TERMS);
				mapicker.showTaskMarkers(terms);
			},
			error : function(jqXHR, textStatus, errorThrown){
				console.log("Retrieve task details failed with status "+jqXHR.status+" "+textStatus);
			}
		});
	},

	/**
	 * show markers on map based on the list of task terms (if any)
	 *
	 * @param {NodeList} terms
	 */
	showTaskMarkers : function(terms){
		if(terms.length < 1){
			console.log("No terms.");
			return;
		}
		console.log("Showing markers for "+terms.length+" terms.");

		var lat = null;
		var lng = null;
		var description = null;
		for(var i=0;i<terms.length;++i){
			description = null;
			var entries = terms.item(i).getElementsByTagName(mapicker.ELEMENT_ENTRY);
			if(entries.length < 1){
				console.log("Ignored empty term...");
				continue;
			}
			
			for(var j=0;j<entries.length;++j){
				var entry = entries.item(j);
				var termKey = entry.getElementsByTagName(mapicker.ELEMENT_KEY)[0].textContent;
				if(termKey == "location/point"){
					var location = entry.getElementsByTagName(mapicker.ELEMENT_VALUE)[0].textContent.split(",");
					lat = location[0];
					lng = location[1];
				}else if(termKey == "text/description"){
					description = entry.getElementsByTagName(mapicker.ELEMENT_VALUE)[0].textContent;
				}
			} // for terms
			if(lat == null){
				console.log("Ignored term without location.");
			}else{
				mapicker.showTaskMarker(lat, lng, description);
			}
		} // for terms
	},

	/**
	 * add marker to the map
	 *
	 * @param {string} lat
	 * @param {string} lng
	 * @param {string} description
	 */
	showTaskMarker : function(lat, lng, description){
		var marker = new google.maps.Marker({
      			position : new google.maps.LatLng(lat,lng),
      			map : mapicker.map,
      			title : description,
			icon : "http://maps.google.com/mapfiles/ms/icons/green-dot.png"
  		});
	}
};

