"use strict";
/**
 * Traffic demo1 javascript codes
 */
var demo1 = {
	map : null, // the google map object
	uriGetMeasurements : "http://example.org/ApiltaService/rest/sensors/getMeasurements",
	uriQueryTaskDetails : "http://example.org/ApiltaService/rest/sensors/queryTaskDetails",
	username : "username",
	password : "password",
	taskIds : ["1"],
	activeDataPointSets : [],
	activeDataPointSetIndex : 0,
	activePosition : null, // for streetview
	globalDataPoints : [],
	resultMarkers : [],
	PARAMETER_USERNAME : "username",
	PARAMETER_PASSWORD : "password",
	PARAMETER_TASK_ID : "task_id",
	PARAMETER_MEASUREMENT_ID : "measurement_id",
	PARAMETER_DATA_GROUPS : "data_groups",
	ELEMENT_MEASUREMENT : "measurement",
	ELEMENT_MEASUREMENT_ID : "measurementId",
	ELEMENT_DATAPOINT : "dataPoint",
	ELEMENT_KEY : "key",
	ELEMENT_VALUE : "value",
	ELEMENT_CREATED_TIMESTAMP : "createdTimestamp",
	ELEMENT_URL : "url",
	ELEMENT_TERMS : "terms",
	ELEMENT_ENTRY : "entry",
	ELEMENT_GUID : "GUID",
	MARKER_DISTANCE_THRESHOLD : 1, // distance threshold for grouping nearby markers, in km

	/**
	 * initializes the basic settings
	 */
	initialize : function(){
		console.log("Initializing...");

		var temp = demo1.getUrlParameter(demo1.PARAMETER_USERNAME);
		if(temp == null){
			console.log("No parameter "+demo1.PARAMETER_USERNAME+", using default: "+demo1.username);
		}else{
			demo1.username = temp[0];	
		}
		temp = demo1.getUrlParameter(demo1.PARAMETER_PASSWORD);
		if(temp == null){
			console.log("No parameter "+demo1.PARAMETER_PASSWORD+", using default: "+demo1.password);
		}else{
			demo1.password = temp[0];	
		}
		temp = demo1.getUrlParameter(demo1.PARAMETER_TASK_ID);
		if(temp == null){
			console.log("No parameter "+demo1.PARAMETER_TASK_ID+", using default: "+demo1.taskIds);
		}else{
			demo1.taskIds = temp;	
		}
		
		demo1.initializeMap();
		demo1.retrieveMeasurements();
	},

	/**
	 * initializes the google map layout (map canvas)
	 */
	initializeMap : function(){
		console.log("Initializing map...");
		var mapOptions = { // map options
    		zoom: 5,
   			disableDoubleClickZoom: true
		};
		
		demo1.map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
	},

	/**
	 * Helper method for setting http basic authorization
	 * 
	 * @param {jqXHR} jqXHR
	 */
	authorize : function(jqXHR){
		jqXHR.setRequestHeader("Authorization", "Basic " + btoa(demo1.username + ":" + demo1.password));
	},

	/**
	 * retrieve measurements and add markers to the map
	 */
	retrieveMeasurements : function(){
		var uri = demo1.uriGetMeasurements+"?"+demo1.PARAMETER_TASK_ID+"="+demo1.taskIds+"&"+demo1.PARAMETER_DATA_GROUPS+"=all";
		console.log("Calling "+uri);
		$.ajax({
			url : uri,
			beforeSend: function(jqXHR) {
				demo1.authorize(jqXHR);
			}, 
			success : function(data){
				var measurements = data.getElementsByTagName(demo1.ELEMENT_MEASUREMENT);
				demo1.showResultMarkers(measurements);
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

		var firstLat = null;
		var firstLng = null;
		for(var i=0;i<measurements.length;++i){
			var measurement = measurements.item(i);
			var measurementId = measurement.getElementsByTagName(demo1.ELEMENT_MEASUREMENT_ID)[0].textContent;

			var dataPoints = measurement.getElementsByTagName(demo1.ELEMENT_DATAPOINT);
			if(dataPoints.length < 1){
				console.log("No data points for measurement, id: "+measurementId);
				continue;
			}
			
			var lat = null;
			var lng = null;
			for(var j=0;j<dataPoints.length;++j){
				var dataPoint = dataPoints.item(j);
				var key = dataPoint.getElementsByTagName(demo1.ELEMENT_KEY)[0].textContent;
				if(key == "sensor/location" || key == "location/point"){
					var location = dataPoint.getElementsByTagName(demo1.ELEMENT_VALUE)[0].textContent.split(",");
					lat = Number(location[0]);
					lng = Number(location[1]);
					if(firstLat == null){
						firstLat = lat;
						firstLng = lng;
					}
					break;
				}
			} // for datapoints
			
			if(lat == null){
				console.log("No location for measurement, id: "+measurementId+", assuming global data...");
				demo1.addToGlobalDataPoints(dataPoints);
			}else{
				demo1.showResultMarker(lat, lng, measurementId, dataPoints);
			}
		} // for measurements

		if(firstLat == null){ // cannot center map
			console.log("No locations for measurement, id: "+measurementId);
		}else{
			demo1.map.panTo(new google.maps.LatLng(firstLat, firstLng)); // center map to the first coordinate
			demo1.map.setZoom(12);
		}
	},
	
	/**
	 * adds the list of points to the global data points array
	 * 
	 * @param {NodeList} dataPoints
	 */
	addToGlobalDataPoints : function(dataPoints) {
		for(var i=0;i<dataPoints.length;++i){
			var dataPoint = dataPoints.item(i);
			var date = demo1.getDate(dataPoint);
			var points = demo1.globalDataPoints[date];
			if(typeof points === 'undefined'){
				points = [];
				demo1.globalDataPoints[date] = points;
			}
			points.push(dataPoint);
		}
	},

	/**
	 * add marker to the map
	 *
	 * @param {string} lat
	 * @param {string} lng
	 * @param {string} measurementId
	 * @param {NodeList} dataPoints
	 */
	showResultMarker : function(lat, lng, measurementId, dataPoints){
		var marker = demo1.findExistingMarker(lat, lng);
		if(marker == null){
			marker = new google.maps.Marker({
      			position : new google.maps.LatLng(lat,lng),
      			map : demo1.map,
      			title : measurementId
  			});
  			marker.dataPoints = [];
  			google.maps.event.addListener(marker, 'click', function() {demo1.activePosition = marker.position; demo1.showDataPoints(marker.dataPoints); });
  			demo1.resultMarkers.push(marker);
		}
	
		for(var i=0; i < dataPoints.length; ++i){
			marker.dataPoints.push(dataPoints.item(i));
		}
	},
	
	/**
	 * @param {string} lat
	 * @param {string} lng
	 * @return google.maps.Marker that is in or very near to the given coordinates or null if no matching markers found
	 */
	findExistingMarker : function(lat, lng) {
		var marker = null;
		for(var i=0;i<demo1.resultMarkers.length;++i){
			var existing = demo1.resultMarkers[i];
			var position = existing.getPosition();
			if(demo1.haversine(lat, lng, position.lat(), position.lng()) < demo1.MARKER_DISTANCE_THRESHOLD){
				marker = existing;
				break;
			}
		}
		return marker;
	},
	
	/**
	 * From https://rosettacode.org/wiki/Haversine_formula#JavaScript
	 * 
	 * @param {Arguments} arguments in format latitude1, longitude1, latitude2, longitude2
	 */
	haversine : function() {
       var radians = Array.prototype.map.call(arguments, function(deg) { return deg/180.0 * Math.PI; });
       var lat1 = radians[0], lon1 = radians[1], lat2 = radians[2], lon2 = radians[3];
       var R = 6372.8; // km
       var dLat = lat2 - lat1;
       var dLon = lon2 - lon1;
       var a = Math.sin(dLat / 2) * Math.sin(dLat /2) + Math.sin(dLon / 2) * Math.sin(dLon /2) * Math.cos(lat1) * Math.cos(lat2);
       var c = 2 * Math.asin(Math.sqrt(a));
       return R * c;
	},
	
	/**
	 * @param {XMLNode} dataPoint
	 * @return {Number} created timestamp as a number from the given data point
	 */
	getDate : function(dataPoint) {
		return Number(dataPoint.getElementsByTagName(demo1.ELEMENT_CREATED_TIMESTAMP)[0].textContent.split("T")[0].replace(/-/g,""));
	},
	
	/**
	 * show the given list of data poins
	 *
	 * @param {Array} dataPoints
	 */
	showDataPoints : function(dataPoints) {
		if(dataPoints.length < 1){
			console.log("No data points to show.");
			return;
		}
		
		demo1.activeDataPointSets = [];
		var dates = [];
				
		for(var i=0;i<dataPoints.length;++i) {
			var dataPoint = dataPoints[i];
			var date = demo1.getDate(dataPoint);
			var points = null;
			for(var j=0;j<dates.length;++j){
				var d = dates[j];
				if(date < d){
					dates.splice(j, 0, date);
					points = [];
					demo1.activeDataPointSets.splice(j, 0, points);
					break;
				}else if(date == d){
					points = demo1.activeDataPointSets[j];
					break;
				}
			} // for dates
			
			if(points == null){
				dates.push(date);
				points = [];
				demo1.activeDataPointSets.push(points);
				
				var globals = demo1.globalDataPoints[date]; // get global data points for this date
				if(typeof globals !== 'undefined'){
					for(var j=0;j<globals.length; ++j){
						points.push(globals[j]);
					}
				}
			} // if points == null
			points.push(dataPoint);
		} // for data points
		
		if(demo1.activeDataPointSets.length < 2){
			$("#previousButton").addClass("hidden");
			$("#nextButton").addClass("hidden");
		}else{
			$("#previousButton").removeClass("hidden");
			$("#nextButton").removeClass("hidden");
		}
		
		demo1.activeDataPointSetIndex = 0;
		demo1.populateDataPointOverlay(demo1.activeDataPointSetIndex);
		$("#datapoints-overlay").removeClass("hidden");
	},
	
	/**
	 * @param {integer} dataPointSetIndex
	 */
	populateDataPointOverlay : function(dataPointSetIndex) {
		demo1.activeDataPointSetIndex = dataPointSetIndex;
		
		var dpcontainer = $("#datapoint-container");
		dpcontainer.empty();
		var pcontainer = $("#photo-container");
		pcontainer.empty();
		
		var dataPoints = demo1.activeDataPointSets[dataPointSetIndex];
		for(var i=0;i<dataPoints.length;++i){
			var dataPoint = dataPoints[i];
			var timestamp = dataPoint.getElementsByTagName(demo1.ELEMENT_CREATED_TIMESTAMP)[0].textContent;
			var key = dataPoint.getElementsByTagName(demo1.ELEMENT_KEY)[0].textContent;
			var value = dataPoint.getElementsByTagName(demo1.ELEMENT_VALUE)[0].textContent;
			if(key == "file/details/url"){
				var photo = $("<div class='photo'>"+timestamp+"</div>");
				pcontainer.append(photo);
				demo1.retrieveFileDetails(value, photo);
			}else{
				dpcontainer.append("<div class='dataPointData'>"+timestamp+" "+key+" "+value+"</div>");
			}
		}
	},

	/**
	 * retrieve file details from the uri and resolve the real photo url
	 *
	 * @param {string} detailsUri
	 * @param {JQuery} photo the created div element as a JQuery object
	 */
	retrieveFileDetails : function(detailsUri, photo){
		console.log("Calling "+detailsUri);
		$.ajax({
			url : detailsUri,
			beforeSend: function(jqXHR) {
				demo1.authorize(jqXHR);
			}, 
			success : function(data){	
				photo.attr("style", "background-image: url('"+data.getElementsByTagName(demo1.ELEMENT_URL)[0].textContent+"')");
			},
			error : function(jqXHR, textStatus, errorThrown){
				console.log("Retrieve file details failed with status "+jqXHR.status+" "+textStatus);
			}
		});
	},

	/**
	 * move to previous data point set
	 *
	 */
	previousDataPointSet : function(){
		if(demo1.activeDataPointSetIndex == 0){
			demo1.activeDataPointSetIndex = demo1.activeDataPointSets.length-1;
		}else{
			--demo1.activeDataPointSetIndex;
		}
		demo1.populateDataPointOverlay(demo1.activeDataPointSetIndex);
	},
	
	/**
	 * move to next data point set
	 *
	 */
	nextDataPointSet : function(){
		++demo1.activeDataPointSetIndex;
		if(demo1.activeDataPointSetIndex == demo1.activeDataPointSets.length){
			demo1.activeDataPointSetIndex = 0;
		}
		demo1.populateDataPointOverlay(demo1.activeDataPointSetIndex);
	},

	/**
	 * hide the data points overlay
	 */
	hideDataPointsOverlay : function(){
		$("#datapoints-overlay").addClass("hidden");
	},

	/**
	 * show street view in the current (marker) location
	 */
	showStreetView : function(){
		var panorama = demo1.map.getStreetView();
		panorama.setPosition(demo1.activePosition);
		panorama.setVisible(true);
		demo1.hideDataPointsOverlay();
	},

	/**
 	 * Get URL Parameters Using Javascript
 	 * Reference: http://www.netlobo.com/url_query_string_javascript.html
 	 * @param {string} parameterName
 	 * @return {Array[string]} the requested parameter values, or null if not found.
 	 */
	getUrlParameter : function(parameterName){
		var name = parameterName.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
		var regexS = "[\\?&]"+name+"=([^&#]*)";
		var regex = new RegExp( regexS );
		var results = regex.exec(window.location.href);
		if(results === null){
			return null;
		}
		return results[1].split(",");
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
			var entries = terms.item(i).getElementsByTagName(demo1.ELEMENT_ENTRY);
			if(entries.length < 1){
				console.log("Ignored empty term...");
				continue;
			}
			
			for(var j=0;j<entries.length;++j){
				var entry = entries.item(j);
				var termKey = entry.getElementsByTagName(demo1.ELEMENT_KEY)[0].textContent;
				if(termKey == "location/point"){
					var location = entry.getElementsByTagName(demo1.ELEMENT_VALUE)[0].textContent.split(",");
					lat = location[0];
					lng = location[1];
				}else if(termKey == "text/description"){
					description = entry.getElementsByTagName(demo1.ELEMENT_VALUE)[0].textContent;
				}
			} // for terms
			if(lat == null){
				console.log("Ignored term without location.");
			}else{
				demo1.showTaskMarker(lat, lng, description);
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
      			map : demo1.map,
      			title : description,
			icon : "http://maps.google.com/mapfiles/ms/icons/green-dot.png"
  		});
	}
};

