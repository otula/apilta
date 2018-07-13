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
	
	var Taskspage = function(hash, params){
		CarouselSlide.call(this, "tasks", hash);
		this.slideReadyHandler = Utils.tabulatorSlideReady.bind(this);
		this._tabulatorId = "tabulator_"+this.id;
		
		var titleElement = document.createElement("h2");
		titleElement.textContent = Translations.title_list_of_tasks + " " + hash + " [" + params+"]";
		this.content.appendChild(titleElement);
		
		var tableElement = initPage(this.content, this._tabulatorId);
		var backendId = params.length > 2 && params[1] == 'backend' ? params[2] : null;
		network.getTasks(populateTable, {callbackParams: tableElement, backendId: backendId});
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
			    {title: Translations.title_backend_id, field: definitions.ELEMENT.BACKEND_ID, sortable: false, visible: false},      
			    {title: Translations.title_task_id, field: definitions.ELEMENT.TASK_ID, sortable: false, visible: false},
				{title: Translations.title_user_id, field: definitions.ELEMENT.USER_ID, visible: false},
				{title: Translations.title_name, field: definitions.ELEMENT.NAME, width: 150},
				{title: Translations.title_description, field: definitions.ELEMENT.DESCRIPTION, formatter: "textarea"},
				{title: Translations.title_updated, field: definitions.ELEMENT.UPDATED_TIMESTAMP, width: 150},
				{title: Translations.title_edit, field: "edit_button", formatter: buttonFormatter, width:75, align:"center", sortable: false}
			]	//columns, last entry
		});
		return tabulatorElement;
	};
	
	var populateTable = function(data, tabulatorElement, backendId){
		var table = $(tabulatorElement);
		var tasks = data.getElementsByTagName(definitions.ELEMENT.TASK);
		var tabledata = [];
		var count = tasks.length;
		
		if(count < 1){
			table.tabulator("clear");
			return;
		}
		
		for(var i=0; i<count; ++i){
			var task = $(tasks[i]);
			tabledata.push({
				backendId: backendId,
				taskId: task.find(definitions.ELEMENT.TASK_ID_LIST).children(definitions.ELEMENT.TASK_ID).text(),
				userId: task.find(definitions.ELEMENT.USER_DETAILS + ">" + definitions.ELEMENT.USER_ID).text(),
				name: task.children(definitions.ELEMENT.NAME).text(),
				description: task.children(definitions.ELEMENT.DESCRIPTION).text(),
				updatedTimestamp: task.children(definitions.ELEMENT.UPDATED_TIMESTAMP).text(),

			});
		}
		if(!!!backendId){
			table.tabulator("hideCol","edit_button"); //hide the column
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
		
		if(targetElement.getAttribute("data-field") === "edit_button"){
			//activate editable mode of task
			Utils.updateUrl("#/taskDetails/task/"+data[definitions.ELEMENT.TASK_ID]+"/backend/"+data[definitions.ELEMENT.BACKEND_ID]+"/edit");
		}else{
			//show results of the task
			Utils.updateUrl("#/results/task/"+data[definitions.ELEMENT.TASK_ID]);
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
	function buttonFormatter(value, data, cell, row, options, formatterParams){
		return '<div class="glyphicon glyphicon-edit"></div>';
	}
	
	//Taskspage class
	Taskspage.prototype = Object.create(CarouselSlide.prototype);
	Taskspage.prototype.constructor = Taskspage;

	//returns the public functions (require.js stuff)
	return Taskspage;

});
