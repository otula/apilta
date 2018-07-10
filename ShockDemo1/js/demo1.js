"use strict";
/**
 * Shock demo1 javascript codes
 *
 */
var demo1 = {
	map : null, // the google map object
	uriGetMeasurements : "http://example.org/ApiltaService/rest/shock/getMeasurements",
	uriGetHighlights : "http://example.org/ApiltaService/rest/shock/getHighlights",
	uriGetUserDetails : "http://example.org/ApiltaService/rest/user/getUserDetails",
	username : "username", // the default username if uri parameter is not given
	password : "password", // the default password if uri parameter is not given
	shocks : [],
	groupMarkers : [],
	acc_l1 : 0, // level 1 shock threshold
	acc_l2 : 0, // level 2 shock threshold
	acc_l3 : 0, // level 3 shock threshold
	acc_l4 : 0, // level 4 shock threshold
	dynamicShockLevels : true, // set to false to use hardcoded acc_l1-acc_l4
	limits : null,
	locationLimits : null,
	timestamp : null,
	userIds : null,
	levels : null,
	calculateLevels : false,
	ACC_L0_MARKER : "icons/green.png",
	ACC_L1_MARKER : "icons/yellow.png",
	ACC_L2_MARKER : "icons/orange.png",
	ACC_L3_MARKER : "icons/red.png",
	ACC_L4_MARKER : "icons/black.png",
	ACC_L_UNKNOWN_MARKER : "icons/unknown.png",
	ACC_GROUP_MARKER : "icons/group.png",
	DEFAULT_LIMITS : "0-100",
	PARAMETER_USERNAME : "username",
	PARAMETER_PASSWORD : "password",
	PARAMETER_TASK_ID : "task_id",
	PARAMETER_LOCATION_LIMITS : "location_limits",
	PARAMETER_LIMITS : "limits",
	PARAMETER_TIMESTAMP : "timestamp",
	PARAMETER_DATA_GROUPS : "data_groups",
	PARAMETER_USER_ID : "user_id",
	PARAMETER_LEVEL : "level",
	PARAMETER_CALCULATE_LEVELS : "calculate_levels",
	PARAMETER_RANGE : "range",
	PARAMETER_MIN_MEASUREMENTS : "min_measurements",
	ELEMENT_MEASUREMENT : "measurement",
	ELEMENT_X_ACCELERATION : "x_acc",
	ELEMENT_Y_ACCELERATION : "y_acc",
	ELEMENT_Z_ACCELERATION : "z_acc",
	ELEMENT_LATITUDE : "latitude",
	ELEMENT_LONGITUDE : "longitude",
	ELEMENT_LEVEL : "level",
	ELEMENT_HEADING : "heading",
	ELEMENT_SPEED : "speed",
	ELEMENT_TIMESTAMP : "timestamp",
	ELEMENT_USER_ID : "userId",
	ELEMENT_FROM : "from",
	ELEMENT_MAX_LEVEL : "maxLevel",
	ELEMENT_MIN_LEVEL : "minLevel",
	ELEMENT_MEASUREMENT_COUNT : "measurementCount",
	ELEMENT_MAX_RANGE : "maxRange",
	ELEMENT_SHOCK_HIGHLIGHT : "highlight",
	ELEMENT_TO : "to",
	ELEMENT_USER_COUNT : "userCount",
	PLACEHOLDER_NA : "N/A",
	GROUP_MIN_TIME_DIFFERENCE : 60000, // minimum time difference between points in grouping (in ms)

	/**
	 *
	 * @param {boolean} show if true the dialog is shown, if false it is hidden
	 * @param {string} message
	 */
	showWaitDialog : function(show, message) {
		var dialog = $("#wait-dialog");
		if(show){
			dialog.removeClass("hidden");
		}else{
			dialog.attr("class", "hidden");
		}
		dialog.text(message);
	},

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
		temp = demo1.getUrlParameter(demo1.PARAMETER_CALCULATE_LEVELS);
		if(temp != null){
			demo1.calculateLevels = (temp[0] == "true");
			if(demo1.calculateLevels){
				console.log("Calculating levels.");
			}
		}

		demo1.locationLimits = demo1.getUrlParameter(demo1.PARAMETER_LOCATION_LIMITS);
		if(demo1.locationLimits == null){
			console.log("No parameter "+demo1.PARAMETER_LOCATION_LIMITS);
		}
		demo1.timestamp = demo1.getUrlParameter(demo1.PARAMETER_TIMESTAMP);
		if(demo1.timestamp == null){
			console.log("No parameter "+demo1.PARAMETER_TIMESTAMP);
		}

		demo1.initializeMap();
		demo1.getUserDetails();
	},

	/**
	 * get user details
	 *
	 */
	getUserDetails : function(){
		demo1.showWaitDialog(true, "Authenticating...");

		console.log("Calling "+demo1.uriGetUserDetails);
		$.ajax({
			url : demo1.uriGetUserDetails,
			beforeSend: function(jqXHR) {
				demo1.authorize(jqXHR);
			},
			success : function(data){
				var userId = data.getElementsByTagName(demo1.ELEMENT_USER_ID)[0].textContent;
				console.log("Authenticated with user id: "+userId);
				document.getElementById("userIds").value = userId;
				demo1.showWaitDialog(false, null);
				demo1.retrieveMeasurements();
			},
			error : function(jqXHR, textStatus, errorThrown){
					console.log("Get user details failed with status "+jqXHR.status+" "+textStatus);
					demo1.showWaitDialog(false, null);
			}
		});
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
	 * initializes the google map layout (map canvas)
	 */
	initializeMap : function(){
		console.log("Initializing map...");
		var mapOptions = { // map options
    		zoom: 8,
				center: new google.maps.LatLng(61.492581, 21.799730),
				mapTypeId: google.maps.MapTypeId.roadmap,
				disableDoubleClickZoom: true
		};

		demo1.map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);
		google.maps.event.addListener(demo1.map, 'click', function(event) {
			$("#lowerLeft").text(event.latLng.lat()+";"+event.latLng.lng());
		});
		google.maps.event.addListener(demo1.map, 'dblclick', function(event) {
			$("#upperRight").text(event.latLng.lat()+";"+event.latLng.lng());
		});
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
	 * checks if user has selected location boundaries from the map, and constructs the location limits string
	 */
	checkLimits : function() {
		var min = document.getElementById("minLimit").valueAsNumber;
		if(min == null){
			console.log("No min limit... using default limits: "+demo1.DEFAULT_LIMITS);
			demo1.limits = demo1.DEFAULT_LIMITS;
		}else{
			var max = document.getElementById("maxLimit").valueAsNumber;
			if(max == null){
				console.log("No max limit... using default limits: "+demo1.DEFAULT_LIMITS);
				demo1.limits = demo1.DEFAULT_LIMITS;
			}else{
				demo1.limits = min+"-"+max;
			}
		}

		var userIds = $("#userIds").val();
		if(!$.trim(userIds)){
			console.log("No user id filter.");
			demo1.userIds = null;
		}else{
			demo1.userIds = userIds;
		}

		var levels = $("#levels").val();
		if(!$.trim(levels)){
			console.log("No level filter.");
			demo1.levels = null;
		}else{
			levels = levels.split(",");
			for(var i=0;i<levels.length;++i){
				levels[i] = Number(levels[i]);
			}
			demo1.levels = levels;
		}

		var ll = $("#lowerLeft").text();
		if(!$.trim(ll)){
			console.log("No lower left...");
			return;
		}
		var ur = $("#upperRight").text();
		if(!$.trim(ur)){
			console.log("No upper right...");
			return;
		}
		demo1.locationLimits = ll+","+ur;
	},

	/**
	 * retrieve measurements and add markers to the map
	 *
	 */
	retrieveMeasurements : function(){
		demo1.showWaitDialog(true, "Retrieving...");

		demo1.clearShocks();

		demo1.checkLimits();

		var uri = demo1.uriGetMeasurements+"?"+demo1.PARAMETER_DATA_GROUPS+"=all";
		if(demo1.locationLimits != null){
			uri += "&"+demo1.PARAMETER_LOCATION_LIMITS+"="+demo1.locationLimits;
		}
		if(demo1.timestamp != null){
			uri += "&"+demo1.PARAMETER_TIMESTAMP+"="+demo1.timestamp;
		}
		if(demo1.limits != null){
			uri += "&"+demo1.PARAMETER_LIMITS+"="+demo1.limits;
		}
		if(demo1.userIds != null){
			uri += "&"+demo1.PARAMETER_USER_ID+"="+demo1.userIds;
		}
		if(demo1.levels != null){
			uri += "&"+demo1.PARAMETER_LEVEL+"="+demo1.levels;
		}

		console.log("Calling "+uri);
		$.ajax({
			url : uri,
			beforeSend: function(jqXHR) {
				demo1.authorize(jqXHR);
			},
			success : function(data){
				var measurements = data.getElementsByTagName(demo1.ELEMENT_MEASUREMENT);
				if(measurements.length < 1){
					console.log("No measurements returned.");
				}else{
					console.log("Measurements retrieved: "+measurements.length);
					demo1.processMeasurements(measurements);
					measurements = null;
					demo1.createMarkers();
				}
			},
			error : function(jqXHR, textStatus, errorThrown){
					console.log("Retrieve measurements failed with status "+jqXHR.status+" "+textStatus);
			},
			complete : function() {
				demo1.showWaitDialog(false, null);
			}
		});
	},

	/**
	 * process measurements
	 *
	 * @param {NodeList} measurements
	 */
	processMeasurements : function(measurements) {
		console.log("Processing measurements...");
		var accHigh = 0;
		var accLow = 1000; // any extreme number will do, in general, the accelerations do not exceed 20
		for(var i=0;i<measurements.length;++i) {
			var measurement = measurements[i];

			var shock = new Object();
			shock.lat = Number(measurement.getElementsByTagName(demo1.ELEMENT_LATITUDE)[0].textContent);
			shock.lon = Number(measurement.getElementsByTagName(demo1.ELEMENT_LONGITUDE)[0].textContent);

			var speed = measurement.getElementsByTagName(demo1.ELEMENT_SPEED);
			if(speed.length > 0){
				shock.speed = speed[0].textContent;
			}else{
				shock.speed = demo1.PLACEHOLDER_NA;
			}

			var level = measurement.getElementsByTagName(demo1.ELEMENT_LEVEL);
			if(level.length > 0){
				shock.level = Number(level[0].textContent);
			}else{
				shock.level = null;
			}

			var heading = measurement.getElementsByTagName(demo1.ELEMENT_HEADING);
			if(level.heading > 0){
				shock.heading = Number(level[0].textContent);
			}else{
				shock.heading = demo1.PLACEHOLDER_NA;
			}

			var xAcc = Number(measurement.getElementsByTagName(demo1.ELEMENT_X_ACCELERATION)[0].textContent);
			var yAcc = Number(measurement.getElementsByTagName(demo1.ELEMENT_Y_ACCELERATION)[0].textContent);
			var zAcc = Number(measurement.getElementsByTagName(demo1.ELEMENT_Z_ACCELERATION)[0].textContent);
			shock.xyz_acc = Math.sqrt(xAcc*xAcc+yAcc*yAcc+zAcc*zAcc);
			if(shock.xyz_acc > accHigh) {
				accHigh = shock.xyz_acc;
			}else if(shock.xyz_acc < accLow){
				accLow = shock.xyz_acc;
			}

			shock.timestamp = new Date(measurement.getElementsByTagName(demo1.ELEMENT_TIMESTAMP)[0].textContent); // we can use whatever timestamp we get, as for this purpose they are close enough

			var userIds = measurement.getElementsByTagName(demo1.ELEMENT_USER_ID);
			for(var j=0;j<userIds.length;++j){
				var id = userIds[j].textContent;
				if(id){
					shock.userId = id.trim();
					break; // take the first value
				}
			}

			shock.marker = null;

			demo1.shocks.push(shock);
		}

		if(demo1.calculateLevels && demo1.dynamicShockLevels){
			var step = (accHigh-accLow) / 5;
			demo1.acc_l1 = accLow+step;
			demo1.acc_l2 = demo1.acc_l1 + step;
			demo1.acc_l3 = demo1.acc_l2 + step;
			demo1.acc_l4 = demo1.acc_l3 + step;
		}
		console.log("Measurements processed.");
	},

	/**
	 *
	 * @param {integer} level
	 * @return markerIcon uri matching the given measurement level
	 */
	getMarkerIcon : function(level){
		if(level == null){
			return demo1.ACC_L_UNKNOWN_MARKER;
		}else if (level == 0) {
			return demo1.ACC_L0_MARKER;
		}else if(level == 1){
			return demo1.ACC_L1_MARKER;
		}else if(level == 2){
			return demo1.ACC_L2_MARKER;
		}else if(level == 3){
			return demo1.ACC_L3_MARKER;
		}else if(level == 4){
			return demo1.ACC_L4_MARKER;
		}else{
			console.log("Unknown measurement level: "+level+", using default icon: "+demo1.ACC_L_UNKNOWN_MARKER);
			return demo1.ACC_L_UNKNOWN_MARKER;
		}
	},

	/**
	 * draw markers based on the shocks array
	 *
	 */
	createMarkers : function() {
		console.log("Creating markers...");
		for(var i=0;i<demo1.shocks.length;++i){
			var shock = demo1.shocks[i];

			if(demo1.calculateLevels){ // update marker level based on the calculated levels if no existing level is known
				if(shock.xyz_acc < demo1.acc_l1) {
					shock.level = 0;
				}else if(shock.xyz_acc < demo1.acc_l2) {
					shock.level = 1;
				}else if(shock.xyz_acc < demo1.acc_l3) {
					shock.level = 2;
				}else if(shock.xyz_acc < demo1.acc_l4) {
					shock.level = 3;
				}else{
					shock.level = 4;
				}
			}

			shock.marker = new google.maps.Marker({
      			position : new google.maps.LatLng(shock.lat, shock.lon),
      			map : demo1.map,
      			title : shock.xyz_acc+" / "+shock.lat+","+shock.lon+" / "+shock.timestamp+" / "+shock.userId+" / "+shock.speed+" / "+shock.heading,
						icon : demo1.getMarkerIcon(shock.level)
			});
		}

		console.log("Markers created.");
	},

	/**
	 * clear the markers and shocks
	 *
	 */
	clearShocks : function() {
		console.log("Clearing shocks...");
		for(var i=0;i<demo1.shocks.length;++i){
			var shock = demo1.shocks[i];
			if(shock.marker == null){
				console.log("Markers are not initialized.");
				break;
			}
			shock.marker.setMap(null);
		}
		demo1.shocks = [];
		console.log("Shocks cleared...");
	},

	/**
	 *
	 * @param {Integer} level
	 */
	toggleMarkers : function(level) {
		console.log("Toggle markers, level: "+level);
		for(var i=0;i<demo1.shocks.length; ++i){
			var shock = demo1.shocks[i];
			if(shock.marker == null){
				console.log("Markers are not initialized.");
				break;
			}

			if(shock.level == level){
				if(shock.marker.getMap() == null){
					shock.marker.setMap(demo1.map);
				}else{
					shock.marker.setMap(null);
				}
			} // if
		} // for
		console.log("Markers toggled.");
	},

	/**
	 * clear group marker list
	 */
	clearGroupMarkers : function() {
		console.log("Clearing group markers...");
		for(var i=0;i<demo1.groupMarkers.length;++i){
			demo1.groupMarkers[i].setMap(null);
		}
		demo1.groupMarkers = [];
		console.log("Group markers cleared.");
	},

	/**
	 * calculate and draw group markers
	 */
	calculateGroups : function() {
		console.log("Calculating group markers...");
		demo1.showWaitDialog(true, "Calculating groups...");

		demo1.clearGroupMarkers();

		var groupRange = document.getElementById("groupRange").valueAsNumber;
		if(!groupRange){ // do not allow null or 0 range
			console.log("Invalid group range.");
			demo1.showWaitDialog(false, null);
			return;
		}
		groupRange = groupRange / 1000;

		var minMeasurements = document.getElementById("groupMinMeasurements").valueAsNumber;
		if(minMeasurements == null){
			console.log("Invalid minimum measurement count.");
			demo1.showWaitDialog(false, null);
			return;
		}

		var shockLevels = document.getElementById("groupShockLevel").value;
		if(!shockLevels){ // do not allow null or 0 level
			console.log("Invalid shock level(s).");
			demo1.showWaitDialog(false, null);
			return;
		}

		demo1.showWaitDialog(true, "Retrieving...");

		demo1.clearGroupMarkers();

		demo1.checkLimits();

		var uri = demo1.uriGetHighlights+"?"+demo1.PARAMETER_RANGE+"="+groupRange+"&"+demo1.PARAMETER_MIN_MEASUREMENTS+"="+minMeasurements+"&"+demo1.PARAMETER_LEVEL+"="+shockLevels;

		if(demo1.locationLimits != null){
			uri += "&"+demo1.PARAMETER_LOCATION_LIMITS+"="+demo1.locationLimits;
		}
		if(demo1.timestamp != null){
			uri += "&"+demo1.PARAMETER_TIMESTAMP+"="+demo1.timestamp;
		}
		if(demo1.limits != null){
			uri += "&"+demo1.PARAMETER_LIMITS+"="+demo1.limits;
		}
		if(demo1.userIds != null){
			uri += "&"+demo1.PARAMETER_USER_ID+"="+demo1.userIds;
		}
		if(demo1.levels != null){
			uri += "&"+demo1.PARAMETER_LEVEL+"="+demo1.levels;
		}

		console.log("Calling "+uri);
		$.ajax({
			url : uri,
			beforeSend: function(jqXHR) {
				demo1.authorize(jqXHR);
			},
			success : function(data){
				var highlights = data.getElementsByTagName(demo1.ELEMENT_SHOCK_HIGHLIGHT);
				if(highlights.length < 1){
					console.log("No highlights returned.");
				}else{
					console.log("Highlights retrieved: "+highlights.length);
					demo1.createHighlights(highlights);
				}
			},
			error : function(jqXHR, textStatus, errorThrown){
					console.log("Retrieve measurements failed with status "+jqXHR.status+" "+textStatus);
			},
			complete : function() {
				demo1.showWaitDialog(false, null);
			}
		});
	},

	/**
	 *
	 * @param {XmlNodeList} highlights
	 */
	createHighlights : function(highlights) {
		for(var i=0;i<highlights.length;++i){
			var hl = highlights[i];
			var marker = new google.maps.Marker({
				position : new google.maps.LatLng(Number(hl.getElementsByTagName(demo1.ELEMENT_LATITUDE)[0].textContent), Number(hl.getElementsByTagName(demo1.ELEMENT_LONGITUDE)[0].textContent)),
				map : demo1.map,
				title : hl.getElementsByTagName(demo1.ELEMENT_MIN_LEVEL)[0].textContent+"-"+hl.getElementsByTagName(demo1.ELEMENT_MAX_LEVEL)[0].textContent+" ("+hl.getElementsByTagName(demo1.ELEMENT_MEASUREMENT_COUNT)[0].textContent+") / "+hl.getElementsByTagName(demo1.ELEMENT_USER_COUNT)[0].textContent+" / "+hl.getElementsByTagName(demo1.ELEMENT_FROM)[0].textContent+"-"+hl.getElementsByTagName(demo1.ELEMENT_TO)[0].textContent,
				icon : demo1.ACC_GROUP_MARKER
			});

			var circle = new google.maps.Circle({
				map: demo1.map,
			  radius: Number(hl.getElementsByTagName(demo1.ELEMENT_MAX_RANGE)[0].textContent)*1000,
			  fillColor: "#FF00FF",
				strokeColor: "#FF00FF",
				fillOpacity: 0.3
			});
			circle.bindTo('center', marker, 'position');

			demo1.groupMarkers.push(marker);
		}
	}
};
