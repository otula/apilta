/**
 * Copyright 2016 Tampere University of Technology, Pori Department
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

define(['jquery', 'app/network', 'app/utils'], function(jQ, network, Utils) {

	//member variables
	var _nickname = undefined;
	var _configObject = undefined;
	var _userId = undefined;
	
	var settingsLoadedHandler = function(data, status){
		if(status !== "success"){
			return;
		}
		var settingsDocument = $(data).find(definitions.ELEMENT.SETTINGS);
		_nickname = settingsDocument.children(definitions.ELEMENT.NICK_NAME).text();
		_configObject = settingsDocument.children(definitions.ELEMENT.CONFIG).text();
		
		require(['app/forms', 'app/slides/settings_page'], function(forms, settingsSlide){
			var contentElement = settingsSlide.getElement();
			forms.appendSettingsForm({config: _configObject, nickname: _nickname}, contentElement, formHandler);
		});
	};
	
	//functions
	var getSettings = function(){
		network.getSettings(settingsLoadedHandler);
	};
	
	var generateSettingsDocument = function(configurations){
		if(!!!configurations){
			return;
		}
		var newSettingsDoc = document.implementation.createDocument(null, definitions.ELEMENT.SETTINGS, null);
		if(configurations.nickname){
			var nicknameElement = newSettingsDoc.createElement(definitions.ELEMENT.NICK_NAME);
			nicknameElement.textContent = configurations.nickname;
			newSettingsDoc.documentElement.appendChild(nicknameElement);
		}
		return newSettingsDoc;
	};
	
	var formHandler = function(event){
		debug.info("Processing settings form");
		event.preventDefault();	//prevent default form action
		
		var configurations = {};
		var settingsForm = event.target;
		
		var formArray = $(settingsForm).serializeArray();
		
		for(var i=0; i<formArray.length; ++i){
			var formValue = formArray[i];
			switch(formValue.name){
				case "settings_"+definitions.ELEMENT.NICK_NAME:
					configurations[definitions.ELEMENT.NICK_NAME] = formValue.value;
					break;
				default:
					break;
			}
		}
		var newSettingsDocument = generateSettingsDocument(configurations);
		network.updateSettings(Utils.clearPage, newSettingsDocument);
	};
	
	
	var getLoggedInUserId = function(){
		return _userId;
	};

	
	var setLoggedInUserId = function(userId){
		_userId = userId;
	};
	
	var isLoggedInUser = function(userId){
		if(_userId === userId){
			return true;
		}
		return false;
	};

	//returns the public functions (require.js stuff)
	return {
		getSettings: getSettings,
		getLoggedInUserId: getLoggedInUserId,
		setLoggedInUserId: setLoggedInUserId,
		isLoggedInUser: isLoggedInUser
	};

});
