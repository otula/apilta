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

define(['jquery', 'definitions', 'app/utils'], function($, definitions, Utils) {

	//member variables
	
	//functions
	
	var getBackends = function(callback, options){
		var request = $.ajax({
			url: definitions.restAPI+definitions.SERVICE.BACKENDS+definitions.METHOD.BACKENDS,
			contentType: 'text/xml',
			processData: false,
			type: 'GET'
		});
		request.done(function(data, textStatus, jqXHR){
			if(callback && options){
				callback(data, options.callbackParams);
			}
		});
	};
	
	var getResults = function(callback, options){
		var params = [];
		if(options && options.taskId){
			params.push(definitions.PARAMETER.DATA_GROUPS+"=all");
			params.push(definitions.PARAMETER.TASK_ID+"="+options.taskId);
			params.push(definitions.PARAMETER.LIMITS+"="+definitions.DEFAULT_LIMITS);
		}else{
			return;
		}
		
		var request = $.ajax({
			url: definitions.restAPI+definitions.SERVICE.SENSORS+definitions.METHOD.MEASUREMENTS + parameterBuilder(params),
			contentType: 'text/xml',
			processData: false,
			type: 'GET'
		});
		request.done(function(data, textStatus, jqXHR){
			if(callback && options){
				callback(data, options.callbackParams, options.taskId);
			}
		});
	};
	
	var getTasks = function(callback, options){
		var params = [];
		if(options){
			if(options.backendId){
				params.push(definitions.PARAMETER.BACKEND_ID+"="+options.backendId);
			}
		}
		
		var request = $.ajax({
			url: definitions.restAPI+definitions.SERVICE.TASKS+definitions.METHOD.TASKS + parameterBuilder(params),
			contentType: 'text/xml',
			processData: false,
			type: 'GET'
		});
		request.done(function(data, textStatus, jqXHR){
			if(callback && options){
				callback(data, options.callbackParams, options.backendId);
			}
		});
	};
	
	var getTaskDetails = function(callback, options){
		var params = [];
		if(options && options.backendId && options.taskId){
			params.push(definitions.PARAMETER.DATA_GROUPS+"=all");
			params.push(definitions.PARAMETER.BACKEND_ID+"="+options.backendId);
			params.push(definitions.PARAMETER.TASK_ID+"="+options.taskId);
		}else{
			return;
		}
		
		var request = $.ajax({
			url: definitions.restAPI+definitions.SERVICE.SENSORS+definitions.METHOD.QUERY_TASK_DETAILS + parameterBuilder(params),
			contentType: 'text/xml',
			type: 'GET'
		});
		request.done(function(data, textStatus, jqXHR){
			if(callback && options){
				callback(data, options.callbackParams);
			}
		});
	};
	
	var getFileDetails = function(callback, options){
		if(options && options.url){
			var request = $.ajax({
				url: options.url,
				contentType: 'text/xml',
				type: 'GET'
			});
			request.done(function(data, textStatus, jqXHR){
				if(callback && options){
					callback(data, options.callbackParams);
				}
			});
		}
	};
	
	var getUserDetails = function(callback){
		var request = $.ajax({
			cache: false,	//try to bypass the cache
			dataType: 'xml',
			url: definitions.restAPI+definitions.SERVICE.USER+definitions.METHOD.USER_DETAILS
		});
		request.done(function(data, textStatus, jqXHR){
			var userId = $(data).find(definitions.ELEMENT.USER_ID).text();
			if(callback){
				callback(userId);
			}
		});
		request.fail(function(jqXHR, textStatus, errorThrown){
			debug.error("Login failed, Error", errorThrown);
			if(callback){
				callback(undefined);
			}
			//TODO notify user?
		});
	};
	
	/**
	 * @param {Function} callback
	 */
	var getSettings = function(callback){
		if(!!!callback){
			return;
		}
		var settingsRequest = $.ajax({
			cache: false,	//try to bypass the cache
			dataType: 'xml',
			url: definitions.restAPI+definitions.SERVICE.SENSORS+definitions.METHOD.SETTINGS,
			type: 'GET'
		});
		settingsRequest.done(function(data, textStatus, jqXHR){
			if(callback){
				callback(data, textStatus);
			}
		});
	};
	
	/**
	 * @param {Function} callback
	 * @param {Document} settingsDocument
	 */
	var updateSettings = function(callback, settingsDocument){
		var settingsRequest = $.ajax({
			url: definitions.restAPI+definitions.SERVICE.SENSORS+definitions.METHOD.SETTINGS,
			data: settingsDocument,
			contentType: 'text/xml',
			processData: false,
			type: 'POST'
		});
		settingsRequest.done(function(data, textStatus, jqXHR){
			if(callback){
				callback(data, textStatus);
			}
		});
	};
	
	/**
	 * @param {String[]} parameterArray
	 * @return {String} string representation of parameters
	 */
	var parameterBuilder = function(parameterArray){
		if(parameterArray && parameterArray.length > 0){
			return "?"+parameterArray.join("&");
		}
		return "";
	};
	
	//returns the public functions (require.js stuff)
	return {
		getBackends: getBackends,
		getFileDetails: getFileDetails,
		getResults: getResults,
		getTasks: getTasks,
		getTaskDetails: getTaskDetails,
		getUserDetails: getUserDetails,
		getSettings: getSettings,
		updateSettings: updateSettings
	};

});
