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

/**
 * Global definitions
 */
var definitions = {
	restAPI: "/ApiltaService/rest",
	basePathFix: "",
	supportedLocalizations: ["en", "fi"],
	
	/** Highlights translated element */
	emphasizeTranslatedElements: false,
	
	DEFAULT_LIMITS: "0-99",
	
	/** Service endpoints */
	SERVICE:{
		BACKENDS: "/backends/",
		SENSORS: "/sensors/",
		TASKS: "/tasks/",
		USER: "/user/"
	},
	
	METHOD:{
		ADD_VIDEO: "addVideo",
		BACKENDS: "getBackends",
		MEASUREMENTS: "getMeasurements",
		QUERY_TASK_DETAILS: "queryTaskDetails",
		REDIRECT: "r",
		SETTINGS: "settings",
		TASKS: "getTasks",
		USER_DETAILS: "getUserDetails"
	},
	
	/*HTTP Parameter names*/
	PARAMETER:{
		BACKEND_ID: "backend_id",
		DATA_GROUPS: "data_groups",
		TASK_ID: "task_id",
		DATA_TYPE: "data_type",
		FILENAME: "filename",
		GUID: "guid",
		LIMITS: "limits",
		USER_ID: "user_id"
	},
	
	/** CSS locations */
	CSS:{
		BOOTSTRAP: "scripts/lib/bootstrap/3.3.5/css/bootstrap.min.css",
		SELECT2: "scripts/lib/select2/4.0.3/css/select2.min.css",
		TABULATOR: "scripts/lib/tabulator/2.7.0/tabulator.css",
		JQUERY_UI: "scripts/lib/jquery-ui/1.12.0.custom/jquery-ui.min.css"
	},
	
	/** Markdown documents */
	MARKDOWN:{
		ABOUT: "about.md",
		CONTACT: "contact.md",
		PRIVACY: "privacy.md",
		WELCOME: "welcome.md"
	},
	
	/* Data types*/
	DATA_TYPES:{
		ANSWER: "ANSWER",
		CHALLENGE: "CHALLENGE"
	},
	
	/*XML elements*/
	ELEMENT: {
		ANALYSIS_URI: "url",
		BACKEND_LIST: "backendList",
		BACKEND: "backend",
		BACKEND_ID: "backendId",
		CAPABILITY_LIST: "capabilityList",
		CAPABILITY: "capability",
		CONDITION: "condition",
		CREATED: "created",
		CREATED_TIMESTAMP: "createdTimestamp",
		DATAPOINT: "dataPoint",
		DATAPOINT_ID: "dataPointId",
		DATAPOINT_TYPE: "dataPointType",
		DESCRIPTION: "description",
		ENABLED: "enabled",
		ENTRY: "entry",
		FEATURE: "feature",
		FILE_DETAILS: "fileDetails",
		GUID: "GUID",
		KEY: "key",
		MEASUREMENT: "measurement",
		MEASUREMENT_ID: "measurementId",
		MESSAGE: "message",
		NAME: "name",
		NICK_NAME: "nickname",
		OUTPUT: "output",
		OWNER_USER: "ownerUser",
		SETTINGS: "settings",
		TASK: "task",
		TASK_ID: "taskId",
		TASK_ID_LIST: "taskIdList",
		TASK_LIST: "taskList",
		TASK_STATUS: "taskStatus",
		TERMS: "terms",
		TITLE: "title",
		UPDATED: "updated",
		UPDATED_TIMESTAMP: "updatedTimestamp",
		URL: "url",
		USER_DETAILS: "userDetails",
		USER_ID: "userId",
		USER_NAME: "username",
		VALUE: "value",
		WHAT: "what",
		WHEN: "when"
	}
};

define([], function() {
	//fix the base path (related to definitions.CSS & definitions.MARKDOWN)
	definitions.basePathFix = require.toUrl("../");	//Generate URL relative to module
	//returns the public functions (require.js stuff)
	return definitions;
});
