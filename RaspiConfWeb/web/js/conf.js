"use strict";
/**
 * conf page javascript codes
 */
var conf = {
	URI_LOAD_CONFIG : "/RaspiConfWeb/rest/config/loadConfig",
	URI_SAVE_CONFIG : "/RaspiConfWeb/rest/config/saveConfig",
	URI_PHOTO : "/RaspiConfWeb/rest/config/photo",
	TIME_INTERVAL_STEP : 1000, // time interval for page reload, in ms
	ctx : null,
	image : null,
	
	/**
	 * 
	 */
	initialize : function() {
		var linesShown = (sessionStorage.getItem("showLines") === "true");
		var showLinesCheckBox = document.getElementById("showLines");
		showLinesCheckBox.checked = linesShown;
		showLinesCheckBox.onchange=function(){conf.showLines(this.checked);};
		
		conf.ctx = document.getElementById("imageCanvas").getContext("2d");
		conf.image = new Image();
		conf.image.onload = function(){
			document.getElementById("imageSize").innerHTML = this.naturalWidth+"x"+this.naturalHeight;
			
			conf.ctx.canvas.width = this.naturalWidth;
			conf.ctx.canvas.height = this.naturalHeight;
			conf.ctx.font = "30px Arial";
		  
			conf.drawImage();
			if(linesShown){
				conf.drawLines();
			}
		};
		conf.image.src = conf.URI_PHOTO;
		conf.load();
	},
	
	/**
	 * @param show if true the helper lines are shown, if false the lines are hidden
	 */
	showLines : function(show) {
		sessionStorage.setItem("showLines", show);
		if(show){
			conf.drawLines();
		}else{
			conf.drawImage();
		}
	},
	
	/**
	 * draw the image on the canvas
	 */
	drawImage : function(){
		conf.ctx.drawImage(conf.image,0,0);
	},
	
	/**
	 * draw helper lines
	 */
	drawLines : function() {
		for(var i=100;i<conf.ctx.canvas.height;i+=100){
			conf.ctx.beginPath();
			conf.ctx.moveTo(0,i);
			conf.ctx.lineTo(conf.ctx.canvas.width,i);
			conf.ctx.stroke();
			
			conf.ctx.strokeText(''+i,0,i);
		}
		
		for(var i=100;i<conf.ctx.canvas.width;i+=100){
			conf.ctx.beginPath();
			conf.ctx.moveTo(i,0);
			conf.ctx.lineTo(i, conf.ctx.canvas.height);
			conf.ctx.stroke();
			
			conf.ctx.save();
			conf.ctx.translate(0,0);
			conf.ctx.rotate(Math.PI/2);
			conf.ctx.strokeText(''+i, 0, -1*i);
			conf.ctx.restore();
		}
		
		//draw a filled rectangle for people counters (up red -- down blue)
		//context.fillRect(x,y,width,height);
		var x = $("#leftLimit").val();
		var width = Math.abs($("#rightLimit").val() - $("#leftLimit").val());
		var upY = Math.min($("#up").val(), $("#upLimit").val());
		var downY = Math.min($("#down").val(), $("#downLimit").val());
		var upHeight = Math.abs($("#up").val() - $("#upLimit").val());
		var downHeight = Math.abs($("#down").val() - $("#downLimit").val());
		
		conf.ctx.fillStyle = "rgba(255, 0, 0, 0.3)";
		conf.ctx.fillRect(x, upY, width, upHeight);
		conf.ctx.fillStyle = "rgba(0, 0, 255, 0.3)";
		conf.ctx.fillRect(x, downY, width, downHeight);
		
	},
		
	/**
	 * 
	 */
	load : function(){
		console.log("Calling "+conf.URI_LOAD_CONFIG);
		$.ajax({
			url : conf.URI_LOAD_CONFIG,
			success : function(data){
				document.getElementById("down").value = data.raspiIni.lineDown;
				document.getElementById("downLimit").value = data.raspiIni.lineDownLimit;
				document.getElementById("up").value = data.raspiIni.lineUp;
				document.getElementById("upLimit").value = data.raspiIni.lineUpLimit;
				document.getElementById("leftLimit").value = data.raspiIni.lineLeftLimit;
				document.getElementById("rightLimit").value = data.raspiIni.lineRightLimit;
				document.getElementById("thresholdMin").value = data.raspiIni.areaThresholdMin;
				document.getElementById("thresholdMax").value = data.raspiIni.areaThresholdMax;
				document.getElementById("cropLimits").checked = data.raspiIni.cropLimits;
				
				conf.showStatusMessage("Values loaded.");
			},
			error : function(jqXHR, textStatus, errorThrown){
				console.log("Retrieve measurements failed with status "+jqXHR.status+" "+textStatus);
			}
		});
	},
	
	/**
	 * 
	 */
	save : function(){
		var raspiini = new Object();
		raspiini.lineDown = document.getElementById("down").value;
		raspiini.lineDownLimit = document.getElementById("downLimit").value;
		raspiini.lineUp = document.getElementById("up").value;
		raspiini.lineUpLimit = document.getElementById("upLimit").value;
		raspiini.lineLeftLimit = document.getElementById("leftLimit").value;
		raspiini.lineRightLimit = document.getElementById("rightLimit").value;
		raspiini.areaThresholdMin = document.getElementById("thresholdMin").value;
		raspiini.areaThresholdMax = document.getElementById("thresholdMax").value;
		raspiini.cropLimits = document.getElementById("cropLimits").checked;
		
		console.log("Calling "+conf.URI_SAVE_CONFIG);
		$.ajax({
			type: "POST",
			contentType: 'application/json',
			url : conf.URI_SAVE_CONFIG,
			data : JSON.stringify(raspiini),
			dataType: "json",
			success : function(data){
				conf.reloadPage(3000);
			},
			error : function(jqXHR, textStatus, errorThrown){
				console.log("Retrieve measurements failed with status "+jqXHR.status+" "+textStatus);
			}
		});
	},
	
	/**
	 * 
	 * @param {int} time in ms, when page is reloaded, if <= 0, reload immediately
	 */
	reloadPage : function(time) {
		if(time <= 0){
			location.reload();
		}else{
			conf.showStatusMessage("Reloading page in "+time/1000+" seconds...");
			window.setTimeout(function(){conf.reloadPage(time-conf.TIME_INTERVAL_STEP);}, time);
		}
	},
	
	/**
	 * 
	 * @param {string} message
	 */
	showStatusMessage : function(message) {
		document.getElementById("status").innerHTML = message;
	}
};
