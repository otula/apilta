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

//logging and debugging functions
var LOGGING = true;
var DEBUG = true;
if (LOGGING) {
	window.debug = {
		log: window.console.log.bind(window.console, (new Date()).toLocaleString() + ' %s: %s'),
		error: window.console.error.bind(window.console, (new Date()).toLocaleString() + ' ERROR: %s'),
		info: window.console.info.bind(window.console, (new Date()).toLocaleString() + ' INFO: %s'),
		warn: window.console.warn.bind(window.console, (new Date()).toLocaleString() + ' WARN: %s'),
		debug: window.console.warn.bind(window.console, (new Date()).toLocaleString() + ' DEBUG: %s'),
		xml: debugXML
	};
} else {
	var __no_op = function() {
		//nothing needed
	};

	window.debug = {
		log: __no_op,
		error: __no_op,
		info: __no_op,
		warn: __no_op,
		debug: __no_op,
		xml: __no_op
	};
}
if(!DEBUG){
	window.debug.debug = function(){
		//nothing needed
	};
}
function debugXML(xml) {
	debug.debug((new XMLSerializer()).serializeToString(xml));
}

//members, configs etc.
var _locale = localStorage.getItem('locale') || "en-US";		//prioritize use of saved locale, if not set, default to en-US
var _slideStack = undefined;

requirejs.config({
    //paths config is relative to the baseUrl, and
    //never includes a ".js" extension since
    //the paths config could be for a directory.
	paths: {
		jquery: 	'lib/jquery-3.1.1.min',
		jqueryui: 	'lib/jquery-ui/1.12.0.custom/jquery-ui.min',
		jsoneditor:	'lib/json-editor/0.7.28/jsoneditor.min',
		i18n:		'lib/i18n/2.0.6/i18n',
		tabulator:	'lib/tabulator/2.12.0/tabulator',
		text:		'lib/text/2.0.14/text',
		bootstrap:	'lib/bootstrap/3.3.5/js/bootstrap.min',
		marked:		'lib/marked/0.3.5/marked.min',
		holder:		'lib/holder/2.9.1/holder.min',
		select2:	'lib/select2/4.0.3/js/select2.min',
		iso6391:	'lang'
	},
	shim:{	//shimming adds a requirejs wrapper for a library
		'bootstrap': ['jquery'],
		'tabulator': ['jqueryui'],
		'jsoneditor': ['bootstrap']
	},
    config: {
        //Set the config for the i18n module ID
        i18n: {
           locale: _locale
        }
    },
	enforceDefine: false
});

//main entry point
require(['jquery', 'app/slidestack', 'app/utils', 'i18n!app/nls/translations'], function($, slideStack, Utils, Translations) {
	//setting some translations/i18n
	//main menus
	$(".i18n-navigation").text(Translations.navigation);
	$(".i18n-home").text(Translations.home);
	$(".i18n-tasks").text(Translations.title_tasks);
	$(".i18n-back-ends").text(Translations.title_backends);
	$(".i18n-results").text(Translations.title_results);
	$(".i18n-settings").text(Translations.settings);
	//other menus
	$(".i18n-about").text(Translations.about);
	$(".i18n-contact").text(Translations.contact);
	$(".i18n-privacy").text(Translations.privacy);
	$(".i18n-to-top").text(Translations.back_to_top);
	
	init();
	
	_slideStack = slideStack;
	_slideStack.initialize();
	Utils.updateClickHandlers();
	
	//check "context" of the application (the attribute "data-requirecontext" from first script tag of the document)
	var mainScript = document.getElementsByTagName("script")[0];
	if("secured" === mainScript.getAttribute("data-requirecontext")){	//we are accessing the secured site, so try to get login data
		require(['app/login'], function(login){
			login.initialize(loggedIn);
		});
	}else{	//otherwise just show the public site
		activatePublicSite();
	}
});

function loggedIn(userId){
	if(userId){
		debug.info("Logged in.", userId);
		require(['jquery', 'app/settings', 'app/utils'], function($, settings, Utils){
			settings.setLoggedInUserId(userId);
		});
	}else{	//login failed
		loginFailed();
	}
}

function loginFailed(){
	activatePublicSite();
}

function activatePublicSite(){
	require(['jquery', 'app/utils'], function($, Utils){
		if(window.location.hash != ""){
			debug.debug("Trying to resolve page ", window.location.hash);
			Utils.activateContent(window.location.hash.substr(1));
		}else{
			Utils.activateContent("/home");
		}
	});	
}

var init = function(){
    //initialize stylesheet for select bootstrap plug-in.
    var link = document.createElement("link");
    link.type = "text/css";
    link.rel = "stylesheet";
    link.href = definitions.basePathFix + definitions.CSS.SELECT2;		//fixed with basePathFix
    document.getElementsByTagName("head")[0].appendChild(link);
	
	//tabulator css
	link = document.createElement("link");
    link.type = "text/css";
    link.rel = "stylesheet";
    link.href = definitions.basePathFix + definitions.CSS.TABULATOR;		//fixed with basePathFix
    document.getElementsByTagName("head")[0].appendChild(link);
	
	//custom css for jquery ui (needed by tabulator)
  	link = document.createElement("link");
    link.type = "text/css";
    link.rel = "stylesheet";
    link.href = definitions.basePathFix + definitions.CSS.JQUERY_UI;		//fixed with basePathFix
    document.getElementsByTagName("head")[0].appendChild(link);
	
    debug.debug("Style sheets appended");
};

/**
 * Get URL Parameters Using Javascript
 * Reference: http://www.netlobo.com/url_query_string_javascript.html
 * @param {String} parameterName
 * @return {String} the requested parameter value, or null if not found.
 */
function getUrlParameter(parameterName){
	var name = parameterName.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
	var regexS = "[\\?&]"+name+"=([^&#]*)";
	var regex = new RegExp( regexS );
	var results = regex.exec(window.location.href);
	if(results === null){
		return null;
	}
	return results[1];
}

