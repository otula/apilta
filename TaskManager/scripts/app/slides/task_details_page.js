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

define(['jquery', 'jsoneditor', 'tabulator', 'app/slides/carousel_slide', 'app/utils', 'app/network', "i18n!app/nls/translations", 'definitions'], 
		function($, jsoneditor, Tabulator, CarouselSlide, Utils, network, Translations, definitions) {

	//member variables
	
	var TaskDetailspage = function(hash, params){
		CarouselSlide.call(this, "task_details", hash);
		this.slideReadyHandler = Utils.tabulatorSlideReady.bind(this);
		this._tabulatorId = "tabulator_"+this.id;
				
		var titleElement = document.createElement("h2");
		titleElement.textContent = Translations.title_task_details + " " +hash + " [" + params+"]";
		this.content.appendChild(titleElement);
		var additionalContent = document.createElement("div");
		this.content.appendChild(additionalContent);
		
		if(params.length < 5){
			throw new Error(Utils.translate(Translations.error_general, hash));
		}

		var taskId = params[2];
		var backendId = params[4];
		network.getTaskDetails(parseData, {callbackParams: additionalContent, taskId: taskId, backendId: backendId});
	};
	
	var parseData = function(data, parentElement){
		var taskDetailsSchema = {
				title: Translations.title_task_details,
				type: "object",
				properties: {
					description: {
						title: Translations.title_description,
						type: "string",
						format: "textarea",
						description: Translations.help_task_description,
						propertyOrder: 2
					},
					name: {
						title: Translations.title_name,
						type: "string",
						description: Translations.help_task_name,
						propertyOrder: 1
					},
					createdTimestamp: {
						title: Translations.title_created,
						type: "string",
						description: Translations.help_task_created,
						format: "datetime",
						propertyOrder: 5
					},
					updatedTimestamp: {
						title: Translations.title_updated,
						type: "string",
						description: Translations.help_task_updated,
						format: "datetime",
						propertyOrder: 5
					},
					userDetails: {
						title: Translations.title_user_details,
						type: "string",
						description: Translations.help_task_user_details,
						propertyOrder: 5
					},
					
					taskIdList: {
						title: Translations.title_task_ids,
						type: "array",
						description: Translations.help_task_id,
						format: "table",
						items: {
							title: Translations.title_task_id,
							type: "string"
						}
					},
					taskTypeList: {
						title: Translations.title_task_types,
						type: "array",
						description: Translations.help_task_types,
						format: "checkbox",
						uniqueItems: true,
						items: {
							type: "string",
							enum: ["gather", "analysis", "feedback"]
						},
						propertyOrder: 4
					},
					backendList: {
						title: Translations.title_backends,
						type: "array",
						description: Translations.help_task_backends,
						format: "table",
						items: {
							title: Translations.title_backend_id,
							type: "string"
						}
					},
					
					when: {	//conditions
						title: Translations.title_when,
						type: "array",
						description: Translations.help_task_when,
						format: "tabs",
						items: {
							title: Translations.title_condition,
							headerTemplate: Translations.header_template_condition,
							type: "array",
							description: Translations.help_task_condition,
							format: "table",
							items:{
								title: Translations.title_term,
								type: "object",
								properties: {
									key:{
										title: Translations.title_term,
										type: "string"
										//TODO: enums seem to be somewhat problematic with jsoneditor
//												enum: ["location/area", "location/point", "location/heading", "location/range",
//												       "sensor/velocity",
//												       "time/validFromToRange"]
									},
									value:{
										title: Translations.title_value,
										type: "string"
									}
								}
							}
						},
						propertyOrder: 3
					},
					what: {	//outputs
						title: Translations.title_what,
						type: "array",
						description: Translations.help_task_what,
						format: "checkbox",
						uniqueItems: true,
						items: {
							title: Translations.title_feature,
							type: "string",
							enum: ["sensor/location", "sensor/camera", "sensor/video", "sensor/velocity", "sensor/gyro"]
						},
						propertyOrder: 3
					}
				}
			};
			var element = document.createElement("div");
			// Set an option during instantiation
			var editor = new JSONEditor(element, {
				disable_edit_json: true,
				disable_properties: true,
				disable_collapse: true,
				disable_array_reorder: true,
				schema: taskDetailsSchema,
				theme: 'bootstrap3'
			});
		parentElement.appendChild(element);
		
		//set data
		var xml = $(data);
		editor.getEditor('root.name').setValue(xml.find(definitions.ELEMENT.NAME).text());
		editor.getEditor('root.description').setValue(xml.find(definitions.ELEMENT.DESCRIPTION).text().trim());
		editor.getEditor('root.createdTimestamp').setValue(xml.find(definitions.ELEMENT.CREATED_TIMESTAMP).text());
		editor.getEditor('root.updatedTimestamp').setValue(xml.find(definitions.ELEMENT.UPDATED_TIMESTAMP).text());
		editor.getEditor('root.taskIdList').setValue(arrayParser(xml.find(definitions.ELEMENT.TASK_ID_LIST).children(definitions.ELEMENT.TASK_ID)));
		editor.getEditor('root.taskTypeList').setValue(arrayParser(xml.find(definitions.ELEMENT.TASK_TYPE_LIST).children(definitions.ELEMENT.TASK_TYPE)));
		editor.getEditor('root.userDetails').setValue(xml.find(definitions.ELEMENT.USER_DETAILS).children(definitions.ELEMENT.USER_ID).text());
		editor.getEditor('root.backendList').setValue(arrayParser(xml.find(definitions.ELEMENT.BACKEND_LIST).children(definitions.ELEMENT.BACKEND).children(definitions.ELEMENT.BACKEND_ID)));
		editor.getEditor('root.when').setValue(objectArrayParser(xml.find(definitions.ELEMENT.WHEN).children(definitions.ELEMENT.CONDITION), definitions.ELEMENT.TERMS, [definitions.ELEMENT.ENTRY, [definitions.ELEMENT.KEY, definitions.ELEMENT.VALUE]]));
		editor.getEditor('root.what').setValue(arrayParser(xml.find(definitions.ELEMENT.WHAT).children(definitions.ELEMENT.OUTPUT).children(definitions.ELEMENT.FEATURE)));
		
		editor.disable();
		var allowEdit = true;
		if(allowEdit){
			//only allow editing of these:
			editor.getEditor('root.name').enable();
			editor.getEditor('root.description').enable();
			editor.getEditor('root.when').enable();
		}
	};
	
	function arrayParser(elements){
		var array = [];
		var elementCount = elements.length;
		for(var i=0; i<elementCount; ++i){
			array.push(elements[i].textContent);
		}
		return array;
	}
	
	/**
	 * Recursively iterates through given xml fragment
	 * @param elements {JQuery Element Array}
	 * @param nextArray {String} the name of the next xml collection
	 * @param childObjects {Array} array containing the next element names
	 * @return array of parsed parameters
	 */
	function objectArrayParser(elements, nextArray, childObjects){
		var array = [];
		var childs = childObjects;
		var elementCount = elements.length;
		for(var i=0; i<elementCount; ++i){
			var nextArrayElements = $(elements[i]).children(nextArray);
			if(typeof childs[0] === "string"){
				array.push(objectArrayParser(nextArrayElements, childs[0], childs.slice(1)));
			}else{
				var childObjectArray = childObjects[0];
				var childObjectArrayCount = childObjectArray.length;
				var nextArrayElementCount = nextArrayElements.length;
				for(var k=0; k<nextArrayElementCount; ++k){
					var obj = {};
					for(var j=0; j<childObjectArrayCount; ++j){	//set requested variables to the object
						obj[childObjectArray[j]] = $(nextArrayElements[k]).children(childObjectArray[j]).text();
					}
					array.push(obj);
				}
			}
		}
		return array;
	}
	
	//TaskDetailspage class
	TaskDetailspage.prototype = Object.create(CarouselSlide.prototype);
	TaskDetailspage.prototype.constructor = TaskDetailspage;

	//returns the public functions (require.js stuff)
	return TaskDetailspage;

});
