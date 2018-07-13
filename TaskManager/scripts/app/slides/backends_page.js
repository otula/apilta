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
	
	var Backendspage = function(hash, params){
		CarouselSlide.call(this, "backends", hash);
		this.slideReadyHandler = Utils.tabulatorSlideReady.bind(this);
		this._tabulatorId = "tabulator_"+this.id;
		
		var titleElement = document.createElement("h2");
		titleElement.textContent = Translations.title_list_of_backends + " " + hash + " [" + params+"]";
		this.content.appendChild(titleElement);
		
		var tableElement = initPage(this.content, this._tabulatorId);
		network.getBackends(populateTable, {callbackParams: tableElement});
	};
	
	var initPage = function(parentNode, id){
		var tabulatorElement = document.createElement("div");
		tabulatorElement.id = id;
		parentNode.appendChild(tabulatorElement);
		
		$(tabulatorElement).tabulator({
			fitColumns: true, //fit columns to width of table (optional)
			tooltips: true,
			tooltipsHeader: true, //enable header tooltips
			rowClick: rowClickHandler,
			columns:[ //Define Table Columns
			    {title: Translations.title_analysis_uri, field: definitions.ELEMENT.ANALYSIS_URI, visible: false},
				{title: Translations.title_backend_id, field: definitions.ELEMENT.BACKEND_ID, visible: false},
				{title: Translations.title_name, field: definitions.ELEMENT.NAME},
				{title: Translations.title_description, field: definitions.ELEMENT.DESCRIPTION, formatter: "textarea"},
				{title: Translations.title_capabilities, field: definitions.ELEMENT.CAPABILITY, formatter: "textarea"},
				{title: Translations.title_enabled, field: definitions.ELEMENT.ENABLED, sorter:"boolean", formatter: "tickCross", align: "center", width: 100},
				{title: Translations.title_message, field: definitions.ELEMENT.MESSAGE, formatter: "textarea", visible: false},
				{title: Translations.title_task_status, field: definitions.ELEMENT.TASK_STATUS, formatter: "textarea", visible: false},
				{title: Translations.title_show_tasks, field: "tasks_button", formatter: tasksButtonFormatter, width:100, align:"center", sortable: false}
			]	//columns, last entry
		});
		return tabulatorElement;
	};
	
	var populateTable = function(data, tabulatorElement){
		var table = $(tabulatorElement);
		var backends = data.getElementsByTagName(definitions.ELEMENT.BACKEND);
		var tabledata = [];
		var count = backends.length;
		
		if(count < 1){
			table.tabulator("clear");
			return;
		}
		
		for(var i=0; i<count; ++i){
			var backend = $(backends[i]);
			
			var capabilities = backend.find(definitions.ELEMENT.CAPABILITY);
			var capabilityCount = capabilities.length;
			var capabilityArray = [];
			for(var j=0; j<capabilityCount; ++j){
				capabilityArray.push(capabilities[j].textContent);
			}
			
			tabledata.push({
				url: backend.children(definitions.ELEMENT.ANALYSIS_URI).text(),
				backendId: backend.children(definitions.ELEMENT.BACKEND_ID).text(),
				capability: capabilityArray.join(", "),
				description: backend.children(definitions.ELEMENT.DESCRIPTION).text(),
				enabled: backend.children(definitions.ELEMENT.ENABLED).text(),
				name: backend.children(definitions.ELEMENT.NAME).text(),
				message: backend.children(definitions.ELEMENT.MESSAGE).text(),
				taskStatus: backend.children(definitions.ELEMENT.TASK_STATUS).text()
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
		var targetElement = undefined;
		if(e.target.hasAttribute("data-field")){
			targetElement = e.target;
		}else{
			targetElement = e.target.parentNode;
		}
		
		if(targetElement.getAttribute("data-field") === "tasks_button"){
			//show tasks page with extra parameters
			Utils.updateUrl("#/tasks/backend/"+data[definitions.ELEMENT.BACKEND_ID]);
		}else{
			Utils.updateUrl("#/tasks/backend/"+data[definitions.ELEMENT.BACKEND_ID]);
			//otherwise do nothing
		}
	}
	
	/**
	 * @param {String} value - the value of the cell
	 * @param {Object} data - the data for the row the cell is in
	 * @param {Element} cell - the DOM element of the cell
	 * @param {Element} row - the DOM element of the row
	 * @param {Object} options - the options set for this tabulator
	 * @param {Object} formatterParams - parameters set for the column
	 */
	function tasksButtonFormatter(value, data, cell, row, options, formatterParams){
		return '<div class="glyphicon glyphicon-tasks"></div>';
	}
	
	//Backendspage class
	Backendspage.prototype = Object.create(CarouselSlide.prototype);
	Backendspage.prototype.constructor = Backendspage;

	//returns the public functions (require.js stuff)
	return Backendspage;

});
