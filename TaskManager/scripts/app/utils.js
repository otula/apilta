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

define(['app/slides/error_page', 'holder', 'iso6391', 'select2', 'definitions', "i18n!app/nls/translations"], 
		function(Errorpage, Holder, ISO6391, Select2, definitions, Translations) {

  	var languageData = [];

	//Preconditions checker
	var Preconditions = {
		checkArgument: function(condition, message) {
			if (!condition) {
				throw Error('IllegalArgumentException: ' + (message || ''));
			}
		},
		isGreaterThanZero: function(value){
			if(value && value > 0){
				return true;
			}
			return false;
		}
	};
	
	var setAnchorClickHandler = function(anchorElement){
		if(anchorElement){
			anchorElement.onclick = anchorClickHandler;
		}
	};

	var anchorClickHandler = function(event){
		if(event.which !== 1){	//if anything else than a normal click
			return false;
		}
		if(this.hash === ""){
			return true;
		}
		debug.debug(this.hash);
		activateContent(this.hash.substr(1));
		return true;	//prevent default browser action
	};
	
	var updateUrl = function(hash){
		history.pushState(null, null, hash);
		activateContent(hash.substr(1));
	};
	
	var updateClickHandlers = function(){
		$("a").off("click").click(anchorClickHandler);	//resetting all click handlers
	};

	var activateContent = function(hash){
		var parameters = hash.split("/");
		if(parameters.length < 2){
			return true;
		}
		
		var slideType = parameters[1];

		switch(slideType){
			case "":
			case "home":
				debug.debug("going home...");
				activateHomepage();
				break;
			case "backends":
				debug.info("opening backends viewer");
				require(['app/slides/backends_page'], function(Backendspage){
					try{
						var backends = new Backendspage(hash, parameters.slice(1));
						showPage(backends, true);
					}catch(e){
						return pageErrorHandler(e);
					}
				});
				break;
			case "info":
				debug.info("opening markdown viewer");
				require(['app/slides/markdown_page'], function(mdPage){
					try{
						var mdSlide = new mdPage(hash, parameters.slice(1));
						showPage(mdSlide, true);
					}catch(e){
						return pageErrorHandler(e);
					}
				});
				break;
			case "results":
				debug.info("opening results viewer");
				require(['app/slides/results_page'], function(Resultspage){
					try{
						var results = new Resultspage(hash, parameters.slice(1));
						showPage(results, true);
					}catch(e){
						return pageErrorHandler(e);
					}
				});
				break;
			case "tasks":
				debug.info("opening tasks viewer");
				require(['app/slides/tasks_page'], function(Taskspage){
					try{
						var tasks = new Taskspage(hash, parameters.slice(1));
						showPage(tasks, true);
					}catch(e){
						return pageErrorHandler(e);
					}
				});
				break;
			case "taskDetails":
				debug.info("opening task details viewer");
				require(['app/slides/task_details_page'], function(TaskDetailspage){
					try{
						var taskDetails = new TaskDetailspage(hash, parameters.slice(1));
						showPage(taskDetails);
					}catch(e){
						return pageErrorHandler(e);
					}
				});
				break;
			case "settings":
				require(['app/slides/settings_page'], function(Settingspage){
					showPage(new Settingspage(), true);
				});
				break;
			case "error":
			default:
				pageErrorHandler(new Error(translate(Translations.error_general, hash)));
				return false;
		}
		return true;
	};
	
	var activateHomepage = function(){
		_slideStack.activateSlide("home");
	};
	
	var showPage = function(page, clearOnAdd){
		_slideStack.appendToSlideStack(page, clearOnAdd);
	};
	
	var showErrorpage = function(error){
		_slideStack.appendToSlideStack(new Errorpage(error), true);
	};
	
	var pageErrorHandler = function(error){
		debug.warn(error);
		showErrorpage(error);
		return false;
	};
	
	var clearPage = function(){
		//TODO clear breadcrumbs/slidestack
		/*
		if(parameter === "clear"){
			debug.debug("Clearing all slides");
			_slideStack.activateSlide("home");
			_slideStack.clearStack();
		}
		*/
	};

	/**
	 * Helper function to generate similar text blocks
	 * @param {String} text
	 * @param {String} parentElementName
	 * @return {Element}
	 */
	var generateTextElement = function(text, parentElementName){
		var element = document.createElement("div");
		element.textContent = text;
		var parentNode = document.createElement(parentElementName);
		parentNode.appendChild(element);
		return parentNode;
	};

	/**
	 * Function to substitute arguments to the given sentence. 
	 * E.g. translate("%1 file copied out of %2", 1, 2) returns "1 file copied of 2"
	 * @param {String} sentence the string to be translated
	 * @param {...String} the following parameters are substituted to the given sentence in order of %n appearing in the original sentence.
	 * @returns {String} substituted translation
	 */
	var translate = function(sentence){
		if(arguments.length < 2){
			if(!sentence){
				debug.warn("Translation is invalid.");
				return "<STR></STR>";	//return a mock-up translation
			}
			debug.debug("Translation incomplete: No extra parameters were passed to translation.");
		}
		var substitutedSentence = sentence;
		
		for(var i=arguments.length-1; i>0; --i){
			if(arguments[i]){
				substitutedSentence = substitutedSentence.replace("%"+i, arguments[i]);
			}
		}
		if(definitions.emphasizeTranslatedElements){
			return "<STR>"+substitutedSentence+"</STR>";
		}
		return substitutedSentence;
	};

	/**
	 * Function to parse dates from e.g. "2012-01-02T12:00:00+0200" to Date object
	 * @param {String} isoDateString
	 * @return {Date} isoTime
	 */
	var parseDateFromISO8601 = function(isoDateString) {
		var parts = isoDateString.match(/\d+/g);
		var isoTime = Date.UTC(parts[0], parts[1] - 1, parts[2], parts[3], parts[4], parts[5]);
		if(parts.length > 6){
			var hourOffset = Number(parts[6].substring(0,2));
			var minOffset = Number(parts[6].substring(2,4));
			var offset = (hourOffset + minOffset/60)*3600000;
			if(isoDateString.substring(19,20) === "+"){
				isoTime = isoTime -	offset;
			}else if(isoDateString.substring(19,20) === "-"){
				isoTime = isoTime + offset;
			}
		}
		return new Date(isoTime);
	};
	
	var runHolderJs = function(){
		Holder.run();
	};
	
	/**
	 * Helper function for setting a select2 component into invalid state
	 * @param {Event} event
	 * return true if element was valid
	 */
	var checkInvalidState = function(event){
		var element = event.target;
		var value = $(element).val();
		if(value){
			element.setCustomValidity("");	//element is OK, clear the validity constraint
			return true;
		}
		debug.debug("Form was invalid");
		return false;
	};
	
	var getSelect2LanguageData = function(){
		if(languageData.length <= 1){	//the case when data only contains the placeholder item
			for(var langCode in ISO6391.langs){
				languageData.push({
					id: langCode,
					text: getISO6391LanguageText(langCode)
				});
			}
		}
		return languageData;
	};
	
	var getISO6391LanguageText = function(langCode){
		return ISO6391.getLanguageName(langCode) + "; " + ISO6391.getLanguageNativeName(langCode);
	};
	
	/**
	 * Helper function to setup Select2 auto complete select-elements
	 */
	var select2autocomplete = function(element, selectionData, options, displaySelectedId){
		$(element).select2({
			templateSelection: displaySelectedId ? templateAlternateSelection : undefined,
			data: selectionData,
			width: "100%",
			minimumInputLength: options.minimumInputLength ? options.minimumInputLength : 0,
			placeholder: options.placeholder ? options.placeholder : undefined
		});
		if(element.required){
			//Set custom validity by default so that the form notifies user to select
			element.setCustomValidity(Translations.forms_select_selection_is_required);
		}
		$(element).on("select2:select", checkInvalidState);
		return element;
	};
	
	function templateAlternateSelection(val) {
		return val.id;
	}
	
	var getMainContentWidth = function(){
		return $("#main-content").outerWidth();
	};
	
	/**
	 * A helper function to redraw the tabulator table. 
	 * The caller must bind this method to their own scope (hence the use of keyword this).
	 */
	var tabulatorSlideReady = function(){
		var tabulatorElement = document.getElementById(this._tabulatorId);
		debug.debug("redrawing element $('#"+this._tabulatorId+"').tabulator('redraw')");
		$(tabulatorElement).tabulator("redraw");
	};
	
	var showModalDialog = function(title, modalContent){
		var modal = $("#application-modal");
		if(modal.length < 1){
			//create a modal because it has not been initialized yet
			modal = document.createElement("div");
			modal.className = "modal";
			modal.id = "application-modal";
			modal = $(modal);
			//use jquery handle from now on
			modal.append(
					'<div class="modal-dialog">\
						<!-- Modal content-->\
						<div class="modal-content">\
							<div class="modal-header">\
								<button type="button" class="close" data-dismiss="modal">&times;</button>\
								<h4 class="modal-title">Default Title</h4>\
							</div>\
							<div class="modal-body">\
								<!-- empty when initialized -->\
							</div>\
							<div class="modal-footer">\
								<button type="button" class="btn btn-default" data-dismiss="modal">'+Translations.btn_close+'</button>\
							</div>\
						</div>\
					</div>');
		}
		//set title
		var modalTitle = modal.find(".modal-title");
		modalTitle.text(title);
		//set content
		var modalBody = modal.find(".modal-body");
		modalBody.empty();
		modalBody.append(modalContent);
		modal.modal({ backdrop: 'static' });
	};
	
	var fileDetailsHelper = function(data, element){
		var files = data.getElementsByTagName(definitions.ELEMENT.FILE_DETAILS);
		if(files.length > 0){
			var url = data.getElementsByTagName(definitions.ELEMENT.URL)[0].textContent;
			// style="background-image: url(\'\')"></div>\
			element.setAttribute('style', "background-image: url('"+url+"')");
		}
	};
	
	//returns the public functions (require.js stuff)
	return {
		Preconditions: Preconditions,
		setAnchorClickHandler: setAnchorClickHandler,
		updateUrl: updateUrl,
		updateClickHandlers: updateClickHandlers,
		activateContent: activateContent,
		clearPage: clearPage,
		generateTextElement: generateTextElement,
		translate: translate,
		parseDateFromISO8601: parseDateFromISO8601,
		runHolderJs: runHolderJs,
		getSelect2LanguageData: getSelect2LanguageData,
		getISO6391LanguageText: getISO6391LanguageText,
		select2autocomplete: select2autocomplete,
		getMainContentWidth: getMainContentWidth,
		tabulatorSlideReady: tabulatorSlideReady,
		showModalDialog: showModalDialog,
		fileDetailsHelper: fileDetailsHelper
	};
});
