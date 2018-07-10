"use strict";
/**
 * Shock demo2 javascript codes
 *
 */
var demo2 = {
	map : null, // the google map object
	shockMarkerSource : null,
	uriGetMeasurements : "http://example.org/ApiltaService/rest/shock/getMeasurements",
	uriGetHighlights : "http://example.org/ApiltaService/rest/shock/getHighlights",
	uriGetUserDetails : "http://example.org/ApiltaService/rest/user/getUserDetails",
	username : "username", // the default username if uri parameter is not given
	password : "password", // the default password if uri parameter is not given
	shocks : [],
	highlightMarkerSource : null,
	selectionRectSource : null,
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
	statusBar : null,
	urCoordinate : null, // upper right coordinate for drawing bounding box selection (in pixel format)
	llCoordinate : null, // lower left coordinate for drawing bounding box (in pixel format)
	llSelectActive  : false,
	ACC_L0_MARKER : "icons/green.png",
	ACC_L1_MARKER : "icons/yellow.png",
	ACC_L2_MARKER : "icons/orange.png",
	ACC_L3_MARKER : "icons/red.png",
	ACC_L4_MARKER : "icons/black.png",
	ACC_L_UNKNOWN_MARKER : "icons/unknown.png",
	ACC_HIGHLIGHT_MARKER : "icons/highlight.png",
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
	PARAMETER_GROUP_RANGE : "group_range",
	PARAMETER_GROUP_METHOD : "group_method",
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

		var temp = demo2.getUrlParameter(demo2.PARAMETER_USERNAME);
		if(temp == null){
			console.log("No parameter "+demo2.PARAMETER_USERNAME+", using default: "+demo2.username);
		}else{
			demo2.username = temp[0];
		}
		temp = demo2.getUrlParameter(demo2.PARAMETER_PASSWORD);
		if(temp == null){
			console.log("No parameter "+demo2.PARAMETER_PASSWORD+", using default: "+demo2.password);
		}else{
			demo2.password = temp[0];
		}
		temp = demo2.getUrlParameter(demo2.PARAMETER_CALCULATE_LEVELS);
		if(temp != null){
			demo2.calculateLevels = (temp[0] == "true");
			if(demo2.calculateLevels){
				console.log("Calculating levels.");
			}
		}

		demo2.locationLimits = demo2.getUrlParameter(demo2.PARAMETER_LOCATION_LIMITS);
		if(demo2.locationLimits == null){
			console.log("No parameter "+demo2.PARAMETER_LOCATION_LIMITS);
		}
		demo2.timestamp = demo2.getUrlParameter(demo2.PARAMETER_TIMESTAMP);
		if(demo2.timestamp == null){
			console.log("No parameter "+demo2.PARAMETER_TIMESTAMP);
		}

		//TODO load settings from localstorage

		demo2.statusBar = $("#status-bar");

		demo2.initializeMap();
		demo2.getUserDetails();
	},

	/**
	 * get user details
	 *
	 */
	getUserDetails : function(){
		demo2.showWaitDialog(true, "Authenticating...");

		console.log("Calling "+demo2.uriGetUserDetails);
		$.ajax({
			url : demo2.uriGetUserDetails,
			beforeSend: function(jqXHR) {
				demo2.authorize(jqXHR);
			},
			success : function(data){
				var userId = data.getElementsByTagName(demo2.ELEMENT_USER_ID)[0].textContent;
				console.log("Authenticated with user id: "+userId);
				document.getElementById("userIds").value = userId;
				demo2.showWaitDialog(false, null);
				demo2.retrieveMeasurements();
			},
			error : function(jqXHR, textStatus, errorThrown){
					console.log("Get user details failed with status "+jqXHR.status+" "+textStatus);
					demo2.showWaitDialog(false, null);
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
	 * initializes the ol map layout
	 */
	initializeMap : function(){
		console.log("Initializing map...");

		demo2.shockMarkerSource = new ol.source.Vector();
		var sLayer = new ol.layer.Vector({
        source: demo2.shockMarkerSource
    });

		demo2.highlightMarkerSource = new ol.source.Vector();
		var gLayer = new ol.layer.Vector({
        source: demo2.highlightMarkerSource
    });

		demo2.selectionRectSource = new ol.source.Vector();
		var rLayer = new ol.layer.Vector({
        source: demo2.selectionRectSource
    });

		demo2.map = new ol.Map({
        layers: [
          new ol.layer.Tile({
            source: new ol.source.OSM()
          }),
					sLayer,
					gLayer,
					rLayer],
        target: 'map',
        controls: ol.control.defaults({
          attributionOptions: {
            collapsible: false
          }
        }).extend([new ol.control.ScaleLine()]),
				interactions: ol.interaction.defaults({
					doubleClickZoom: false
				}),
        view: new ol.View({
          center: ol.proj.fromLonLat([21.799730, 61.492581]),
          zoom: 2
        })
    });

		demo2.map.getViewport().addEventListener("dblclick", function(e) {
			var ec = demo2.map.getEventCoordinate(e);
	  	var coordinate = ol.proj.transform(ec, "EPSG:3857", "EPSG:4326");
			if(demo2.llSelectActive){
				$("#lowerLeft").text(coordinate[1]+";"+coordinate[0]);
				demo2.llCoordinate = ec;
			}else{
				$("#upperRight").text(coordinate[1]+";"+coordinate[0]);
				demo2.urCoordinate = ec;
			}
			demo2.llSelectActive = !demo2.llSelectActive;
			demo2.drawSelectionRect();
		});

		// select interaction working on "pointermove"
    var select = new ol.interaction.Select({
      condition: ol.events.condition.pointerMove
    });
		demo2.map.addInteraction(select);
    select.on('select', function(e) {
			if(e.selected.length > 0){
				var selected = e.selected[e.selected.length-1];
				for(var i=0;i<demo2.shocks.length;++i){
					var shock = demo2.shocks[i];
					if(shock.marker == selected){
						demo2.statusBar.text(""+shock.xyz_acc+" / "+shock.lat+","+shock.lon+" / "+shock.timestamp+" / "+shock.userId+" / "+shock.speed+" / "+shock.heading);
						break;
					}
				}
			}
    });
	},

	/**
	 *
	 * draw selection rectangle on map if both upper right and lower right coordinate are given
	 */
	drawSelectionRect : function() {
		if(demo2.urCoordinate && demo2.llCoordinate){
			demo2.clearSelectionRect();
			var polygon = new ol.geom.Polygon( [[
				demo2.urCoordinate,
				[demo2.urCoordinate[0],demo2.llCoordinate[1]],
				demo2.llCoordinate,
				[demo2.llCoordinate[0],demo2.urCoordinate[1]],
				demo2.urCoordinate
			]]);
			var rect = new ol.Feature({
				geometry: polygon
			});
			demo2.selectionRectSource.addFeature(rect);
		}
	},

	/**
	 * Helper method for setting http basic authorization
	 *
	 * @param {jqXHR} jqXHR
	 */
	authorize : function(jqXHR){
		jqXHR.setRequestHeader("Authorization", "Basic " + btoa(demo2.username + ":" + demo2.password));
	},

	/**
	 * checks if user has selected location boundaries from the map, and constructs the location limits string
	 */
	checkLimits : function() {
		var min = document.getElementById("minLimit").valueAsNumber;
		if(min == null){
			console.log("No min limit... using default limits: "+demo2.DEFAULT_LIMITS);
			demo2.limits = demo2.DEFAULT_LIMITS;
		}else{
			var max = document.getElementById("maxLimit").valueAsNumber;
			if(max == null){
				console.log("No max limit... using default limits: "+demo2.DEFAULT_LIMITS);
				demo2.limits = demo2.DEFAULT_LIMITS;
			}else{
				demo2.limits = min+"-"+max;
			}
		}

		var userIds = $("#userIds").val();
		if(!$.trim(userIds)){
			console.log("No user id filter.");
			demo2.userIds = null;
		}else{
			demo2.userIds = userIds;
		}

		var levels = $("#levels").val();
		if(!$.trim(levels)){
			console.log("No level filter.");
			demo2.levels = null;
		}else{
			levels = levels.split(",");
			for(var i=0;i<levels.length;++i){
				levels[i] = Number(levels[i]);
			}
			demo2.levels = levels;
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
		demo2.locationLimits = ll+","+ur;
	},

	/**
	 * clear selection rectangle
	 */
	clearSelectionRect : function() {
		demo2.selectionRectSource.clear();
	},

	/**
	 * retrieve measurements and add markers to the map
	 *
	 */
	retrieveMeasurements : function(){
		demo2.showWaitDialog(true, "Retrieving...");

		demo2.clearShocks();
		demo2.clearSelectionRect();

		demo2.checkLimits();

		var uri = demo2.uriGetMeasurements+"?"+demo2.PARAMETER_DATA_GROUPS+"=all";
		if(demo2.locationLimits != null){
			uri += "&"+demo2.PARAMETER_LOCATION_LIMITS+"="+demo2.locationLimits;
		}
		if(demo2.timestamp != null){
			uri += "&"+demo2.PARAMETER_TIMESTAMP+"="+demo2.timestamp;
		}
		if(demo2.limits != null){
			uri += "&"+demo2.PARAMETER_LIMITS+"="+demo2.limits;
		}
		if(demo2.userIds != null){
			uri += "&"+demo2.PARAMETER_USER_ID+"="+demo2.userIds;
		}
		if(demo2.levels != null){
			uri += "&"+demo2.PARAMETER_LEVEL+"="+demo2.levels;
		}

		var groupMethod = $("#groupMethod").val();
		if($.trim(groupMethod)){
			var groupRange = $("#groupRange").val();
			if($.trim(groupRange)){
				console.log("Grouping...");
				uri += "&"+demo2.PARAMETER_GROUP_METHOD+"="+groupMethod+"&"+demo2.PARAMETER_GROUP_RANGE+"="+groupRange;
			}
		}

		console.log("Calling "+uri);
		$.ajax({
			url : uri,
			beforeSend: function(jqXHR) {
				demo2.authorize(jqXHR);
			},
			success : function(data){
				var measurements = data.getElementsByTagName(demo2.ELEMENT_MEASUREMENT);
				if(measurements.length < 1){
					console.log("No measurements returned.");
				}else{
					console.log("Measurements retrieved: "+measurements.length);
					demo2.processMeasurements(measurements);
					measurements = null;
					demo2.createMarkers();
				}
			},
			error : function(jqXHR, textStatus, errorThrown){
					console.log("Retrieve measurements failed with status "+jqXHR.status+" "+textStatus);
			},
			complete : function() {
				demo2.showWaitDialog(false, null);
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
			shock.lat = Number(measurement.getElementsByTagName(demo2.ELEMENT_LATITUDE)[0].textContent);
			shock.lon = Number(measurement.getElementsByTagName(demo2.ELEMENT_LONGITUDE)[0].textContent);

			var speed = measurement.getElementsByTagName(demo2.ELEMENT_SPEED);
			if(speed.length > 0){
				shock.speed = speed[0].textContent;
			}else{
				shock.speed = demo2.PLACEHOLDER_NA;
			}

			var level = measurement.getElementsByTagName(demo2.ELEMENT_LEVEL);
			if(level.length > 0){
				shock.level = Number(level[0].textContent);
			}else{
				shock.level = null;
			}

			var heading = measurement.getElementsByTagName(demo2.ELEMENT_HEADING);
			if(level.heading > 0){
				shock.heading = Number(level[0].textContent);
			}else{
				shock.heading = demo2.PLACEHOLDER_NA;
			}

			var xAcc = Number(measurement.getElementsByTagName(demo2.ELEMENT_X_ACCELERATION)[0].textContent);
			var yAcc = Number(measurement.getElementsByTagName(demo2.ELEMENT_Y_ACCELERATION)[0].textContent);
			var zAcc = Number(measurement.getElementsByTagName(demo2.ELEMENT_Z_ACCELERATION)[0].textContent);
			shock.xyz_acc = Math.sqrt(xAcc*xAcc+yAcc*yAcc+zAcc*zAcc);
			if(shock.xyz_acc > accHigh) {
				accHigh = shock.xyz_acc;
			}else if(shock.xyz_acc < accLow){
				accLow = shock.xyz_acc;
			}

			shock.timestamp = new Date(measurement.getElementsByTagName(demo2.ELEMENT_TIMESTAMP)[0].textContent); // we can use whatever timestamp we get, as for this purpose they are close enough

			var userIds = measurement.getElementsByTagName(demo2.ELEMENT_USER_ID);
			for(var j=0;j<userIds.length;++j){
				var id = userIds[j].textContent;
				if(id){
					shock.userId = id.trim();
					break; // take the first value
				}
			}

			shock.marker = null;

			demo2.shocks.push(shock);
		}

		if(demo2.calculateLevels && demo2.dynamicShockLevels){
			var step = (accHigh-accLow) / 5;
			demo2.acc_l1 = accLow+step;
			demo2.acc_l2 = demo2.acc_l1 + step;
			demo2.acc_l3 = demo2.acc_l2 + step;
			demo2.acc_l4 = demo2.acc_l3 + step;
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
			return demo2.ACC_L_UNKNOWN_MARKER;
		}else if (level == 0) {
			return demo2.ACC_L0_MARKER;
		}else if(level == 1){
			return demo2.ACC_L1_MARKER;
		}else if(level == 2){
			return demo2.ACC_L2_MARKER;
		}else if(level == 3){
			return demo2.ACC_L3_MARKER;
		}else if(level == 4){
			return demo2.ACC_L4_MARKER;
		}else{
			console.log("Unknown measurement level: "+level+", using default icon: "+demo2.ACC_L_UNKNOWN_MARKER);
			return demo2.ACC_L_UNKNOWN_MARKER;
		}
	},

	/**
	 * draw markers based on the shocks array
	 *
	 */
	createMarkers : function() {
		console.log("Creating markers...");
		for(var i=0;i<demo2.shocks.length;++i){
			var shock = demo2.shocks[i];

			if(demo2.calculateLevels){ // update marker level based on the calculated levels if no existing level is known
				if(shock.xyz_acc < demo2.acc_l1) {
					shock.level = 0;
				}else if(shock.xyz_acc < demo2.acc_l2) {
					shock.level = 1;
				}else if(shock.xyz_acc < demo2.acc_l3) {
					shock.level = 2;
				}else if(shock.xyz_acc < demo2.acc_l4) {
					shock.level = 3;
				}else{
					shock.level = 4;
				}
			}

			shock.marker = new ol.Feature({
        geometry: new ol.geom.Point(ol.proj.fromLonLat([shock.lon, shock.lat]))
      });
			shock.marker.setStyle(new ol.style.Style({
        image: new ol.style.Icon(({
          src: demo2.getMarkerIcon(shock.level)
        }))
      }));

			demo2.shockMarkerSource.addFeature(shock.marker);
			shock.visible = true;
		}

		console.log("Markers created.");
	},

	/**
	 * clear the markers and shocks
	 *
	 */
	clearShocks : function() {
		console.log("Clearing shocks...");
		demo2.shockMarkerSource.clear();
		demo2.shocks = [];
		console.log("Shocks cleared...");
	},

	/**
	 *
	 * @param {Integer} level
	 */
	toggleMarkers : function(level) {
		console.log("Toggle markers, level: "+level);
		for(var i=0;i<demo2.shocks.length; ++i){
			var shock = demo2.shocks[i];
			if(shock.marker == null){
				console.log("Markers are not initialized.");
				break;
			}

			if(shock.level == level){
				if(shock.visible){
					demo2.shockMarkerSource.removeFeature(shock.marker);
					shock.visible = false;
				}else{
					demo2.shockMarkerSource.addFeature(shock.marker);
					shock.visible = true;
				}
			} // if
		} // for
		console.log("Markers toggled.");
	},

	/**
	 * clear highlight marker list
	 */
	clearHighlightMarkers : function() {
		console.log("Clearing highlight markers...");
		demo2.highlightMarkerSource.clear();
		console.log("Highlight markers cleared.");
	},

	/**
	 * calculate and draw highlight markers
	 */
	calculateHighlights : function() {
		console.log("Calculating highlight markers...");
		demo2.showWaitDialog(true, "Calculating highlights...");

		demo2.clearHighlightMarkers();
		demo2.clearSelectionRect();

		var highlightRange = document.getElementById("highlightRange").valueAsNumber;
		if(!highlightRange){ // do not allow null or 0 range
			console.log("Invalid highlight range.");
			demo2.showWaitDialog(false, null);
			return;
		}
		highlightRange = highlightRange / 1000;

		var minMeasurements = document.getElementById("highlightMinMeasurements").valueAsNumber;
		if(minMeasurements == null){
			console.log("Invalid minimum measurement count.");
			demo2.showWaitDialog(false, null);
			return;
		}

		var shockLevels = document.getElementById("highlightShockLevel").value;
		if(!shockLevels){ // do not allow null or 0 level
			console.log("Invalid shock level(s).");
			demo2.showWaitDialog(false, null);
			return;
		}

		demo2.showWaitDialog(true, "Retrieving...");

		demo2.checkLimits();

		var uri = demo2.uriGetHighlights+"?"+demo2.PARAMETER_RANGE+"="+highlightRange+"&"+demo2.PARAMETER_MIN_MEASUREMENTS+"="+minMeasurements+"&"+demo2.PARAMETER_LEVEL+"="+shockLevels;

		if(demo2.locationLimits != null){
			uri += "&"+demo2.PARAMETER_LOCATION_LIMITS+"="+demo2.locationLimits;
		}
		if(demo2.timestamp != null){
			uri += "&"+demo2.PARAMETER_TIMESTAMP+"="+demo2.timestamp;
		}
		if(demo2.limits != null){
			uri += "&"+demo2.PARAMETER_LIMITS+"="+demo2.limits;
		}
		if(demo2.userIds != null){
			uri += "&"+demo2.PARAMETER_USER_ID+"="+demo2.userIds;
		}
		if(demo2.levels != null){
			uri += "&"+demo2.PARAMETER_LEVEL+"="+demo2.levels;
		}

		console.log("Calling "+uri);
		$.ajax({
			url : uri,
			beforeSend: function(jqXHR) {
				demo2.authorize(jqXHR);
			},
			success : function(data){
				var highlights = data.getElementsByTagName(demo2.ELEMENT_SHOCK_HIGHLIGHT);
				if(highlights.length < 1){
					console.log("No highlights returned.");
				}else{
					console.log("Highlights retrieved: "+highlights.length);
					demo2.createHighlights(highlights);
				}
			},
			error : function(jqXHR, textStatus, errorThrown){
					console.log("Retrieve measurements failed with status "+jqXHR.status+" "+textStatus);
			},
			complete : function() {
				demo2.showWaitDialog(false, null);
			}
		});
	},

	/**
	 * show/hide settings overview
	 *
	 * @param {HTMLDivElement} div
	 */
	toggleDiv : function(div) {
		var settings = $(div);
		if(settings.hasClass("hidden")){
			settings.removeClass("hidden");
		}else{
			settings.addClass("hidden");
		}
	},

	/**
	 *
	 * @param {XmlNodeList} highlights
	 */
	createHighlights : function(highlights) {
		for(var i=0;i<highlights.length;++i){
			var hl = highlights[i];

			var lon = Number(hl.getElementsByTagName(demo2.ELEMENT_LONGITUDE)[0].textContent);
			var lat = Number(hl.getElementsByTagName(demo2.ELEMENT_LATITUDE)[0].textContent);
			var marker = new ol.Feature({
        geometry: new ol.geom.Point(ol.proj.fromLonLat([lon, lat]))
      });
			marker.setStyle(new ol.style.Style({
        image: new ol.style.Icon(({
          src: demo2.ACC_HIGHLIGHT_MARKER
        })),
				text: new ol.style.Text({
        	font: '18px Calibri,sans-serif',
        	fill: new ol.style.Fill({ color: '#000' }),
        		stroke: new ol.style.Stroke({
          	color: '#fff', width: 3
        	}),
        	text: hl.getElementsByTagName(demo2.ELEMENT_MIN_LEVEL)[0].textContent+"-"+hl.getElementsByTagName(demo2.ELEMENT_MAX_LEVEL)[0].textContent+" ("+hl.getElementsByTagName(demo2.ELEMENT_MEASUREMENT_COUNT)[0].textContent+") / "+hl.getElementsByTagName(demo2.ELEMENT_USER_COUNT)[0].textContent+" / "+hl.getElementsByTagName(demo2.ELEMENT_FROM)[0].textContent+"-"+hl.getElementsByTagName(demo2.ELEMENT_TO)[0].textContent
      })
      }));
			demo2.highlightMarkerSource.addFeature(marker);

			demo2.highlightMarkerSource.addFeature(new ol.Feature(new ol.geom.Circle(ol.proj.transform([lon, lat], 'EPSG:4326','EPSG:3857'), Number(hl.getElementsByTagName(demo2.ELEMENT_MAX_RANGE)[0].textContent)*1000)));
		}
	}
};
