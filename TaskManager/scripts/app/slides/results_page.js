/**
 * Copyright 2016-2017 Tampere University of Technology, Pori Department
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

"use strict";

define(['jquery', 'tabulator', 'app/slides/carousel_slide', 'app/utils', 'app/network', "i18n!app/nls/translations", 'definitions'], 
		function($, Tabulator, CarouselSlide, Utils, network, Translations, definitions) {

	//member variables
	
	var Resultspage = function(hash, params){
		CarouselSlide.call(this, "results", hash);
		this.slideReadyHandler = Utils.tabulatorSlideReady.bind(this);
		this._tabulatorId = "tabulator_"+this.id;
		
		var titleElement = document.createElement("h2");
		titleElement.textContent = Translations.title_task_results + " " +hash + " [" + params+"]";
		this.content.appendChild(titleElement);
		
		var tableElement = initPage(this.content, this._tabulatorId);
		if(params.length > 2 && params[1] === "task"){
			var taskId = params[2];
			network.getResults(populateTable, {callbackParams: tableElement, taskId: taskId});
		}
	};
	
	var initPage = function(parentNode, id){
		
		var tabulatorElement = document.createElement("div");
		tabulatorElement.id = id;
		parentNode.appendChild(tabulatorElement);
		
		$(tabulatorElement).tabulator({
			fitColumns: true, //fit columns to width of table (optional)
			colMinWidth: 50,
			tooltips: true,
			tooltipsHeader: true, //enable header tooltips
			colVertAlign: "bottom", //align header contents to bottom of cell
			groupBy: definitions.ELEMENT.MEASUREMENT_ID,
			groupHeader:function(value, count, data){
				return Translations.title_measurement_id + ": " + value + "<span style='color:#d00; margin-left:10px;'>(" + count + " item)</span>";
			},
			sortBy: definitions.ELEMENT.CREATED_TIMESTAMP,
			sortDir: "desc",
			rowDblClick: rowClickHandler,
			columns:[ //Define Table Columns
			    {title: Translations.title_task_id, field: definitions.ELEMENT.TASK_ID, sortable: false, visible: false},
			    {title: Translations.title_created, field: definitions.ELEMENT.CREATED_TIMESTAMP, width: 175},
			    {title: Translations.title_measurement_id, field: definitions.ELEMENT.MEASUREMENT_ID, sortable: false, visible:false},
				{title: Translations.title_backend_id, field: definitions.ELEMENT.BACKEND_ID, width: 75},
				{title: Translations.title_datapoint_id, field: definitions.ELEMENT.DATAPOINT_ID, sortable: false, visible: false},
				{title: Translations.title_description, field: definitions.ELEMENT.DESCRIPTION, width: 300},
				{title: Translations.title_key, field: definitions.ELEMENT.KEY, width: 150},
				{title: Translations.title_value, field: definitions.ELEMENT.VALUE}
			]	//columns, last entry
		});
		return tabulatorElement;
	};
	
	var populateTable = function(data, tabulatorElement, taskId){
		var table = $(tabulatorElement);
		var measurements = data.getElementsByTagName(definitions.ELEMENT.MEASUREMENT);
		
		if(measurements.length < 1){
			table.tabulator("clear");
			return;
		}
		
		var taskIdHeader = table.find(".tabulator-header > .extra-header").length > 0 ? 
				table.find(".tabulator-header > .extra-header")[0] : document.createElement("div");
		taskIdHeader.textContent = Utils.translate(Translations.results_header, taskId);
		taskIdHeader.className = "extra-header";
		table.children(".tabulator-header").prepend(taskIdHeader);
		
		var dataPoints = data.getElementsByTagName(definitions.ELEMENT.DATAPOINT);
		var dataPointCount = dataPoints.length;
		var tabledata = [];
		for(var i=0; i<dataPointCount; ++i){
			var dataPoint = $(dataPoints[i]);
			var measurement = dataPoint.parents(definitions.ELEMENT.MEASUREMENT);
			tabledata.push({
				taskId: taskId,
				measurementId: measurement.children(definitions.ELEMENT.MEASUREMENT_ID).text(),
				backendId: measurement.children(definitions.ELEMENT.BACKEND_ID).text(),
				dataPointId: dataPoint.children(definitions.ELEMENT.DATAPOINT_ID).text(),
				createdTimestamp: dataPoint.children(definitions.ELEMENT.CREATED_TIMESTAMP).text(),
				description: dataPoint.children(definitions.ELEMENT.DESCRIPTION).text(),
				key: dataPoint.children(definitions.ELEMENT.KEY).text(),
				value: dataPoint.children(definitions.ELEMENT.VALUE).text()
			});
		}

		table.tabulator("setData", tabledata);
	};
	
	/**
	 * @param {Event} e the click event object
	 * @param {String} id the id of the row
	 * @param {Object} data the data for the row
	 * @param {Element} row the DOM element of the row
	 */
	function rowClickHandler(e, id, data, row){
		var tableData = row.parents(".tabulator").tabulator("getData");
		var measurementData = [];
		for(var i=0; i < tableData.length; ++i){
			if(tableData[i].measurementId === data.measurementId){
				measurementData.push(tableData[i]);
			}
		}
		measurementData.sort(function(a,b){
			if(a.createdTimestamp == b.createdTimestamp){
				return 0;
			}else if(a.createdTimestamp > b.createdTimestamp){
				return 1;
			}else{
				return -1;
			}
		});	//makes sure the array is ordered correctly
		var modalContainer = document.createElement("div");
		var carousel = createImageCarousel(data.measurementId, measurementData);
		if(carousel){
			modalContainer.appendChild(carousel);
		}
		Utils.showModalDialog("Data for " + data.measurementId, modalContainer);	//TODO i18n
	}
	
	function createImageCarousel(measurementId, data){
		var carousel = document.createElement("div");
		var indicators = document.createElement("ol");
		var slides = document.createElement("div");
		carousel.className = "carousel slide";
		carousel.id = "carousel_"+measurementId;
		indicators.className = "carousel-indicators";
		slides.className = "carousel-inner";
				
		var active = " active";	//cheap trick for setting class to carousel items
		var carouselIndex = 0;
		for(var j=0; j < data.length; ++j){
			var row = data[j];
			
			if(row.key === "file/details/url"){
				var url = row.value;
				$(indicators).append('<li data-target="#'+carousel.id+'" data-slide-to="'+carouselIndex+'" class="'+active+'"></li>');
				var item = $('<div class="item'+active+'">\
						<div class="image"></div>\
						<div class="carousel-caption">\
							<h4 title="'+row.key+'">'+row.description+'</h4>\
						    <p>Created '+row.createdTimestamp+'</p>\
						</div>\
					</div>').appendTo(slides);			//TODO i18n i18n i18n
				//// style="background-image: url(\'\')"></div>\
				var imageItem = item.children(".image");
				network.getFileDetails(Utils.fileDetailsHelper, {url: url, callbackParams: imageItem[0]});
				active = "";
				++carouselIndex;
			}
		}
		if(carouselIndex < 1){	//this is the case when there were no images to append to the carousel
			return null;
		}

		carousel.appendChild(indicators);
		carousel.appendChild(slides);
		$(carousel).append(
				'<!-- Controls -->\
				<a class="left carousel-control" href="#'+carousel.id+'" role="button" data-slide="prev">\
					<span class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span>\
				    <span class="sr-only">Previous</span>\
				</a>\
				<a class="right carousel-control" href="#'+carousel.id+'" role="button" data-slide="next">\
					<span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span>\
				    <span class="sr-only">Next</span>\
				</a>');	//TODO i18n i18n
		$(carousel).carousel({interval: false});
		return carousel;
	}
	
	//Resultspage class
	Resultspage.prototype = Object.create(CarouselSlide.prototype);
	Resultspage.prototype.constructor = Resultspage;

	//returns the public functions (require.js stuff)
	return Resultspage;

});
