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

define(['jquery', 'bootstrap', 'app/utils', 'app/slides/home', 'definitions'], function($, Bootstrap, Utils, Homepage, definitions) {

	//member variables
	var slideStack = [];
	var activeSlideIndex = null;
	var homeSlide = null;
	var isFirstRun = true;
	var breadCrumbsElement = $("#breadcrumbs");
	var mainContentElement = $('#page-content');
	
	//functions
	
	var initialize = function(){
		if(homeSlide === null){
			homeSlide = new Homepage();
			activeSlideIndex = 0;
			appendToSlideStack(homeSlide);
		}
	};
	
	var activateSlide = function(slideName){
		var slideFoundIndex = null;
		for(var index=0; index<slideStack.length; ++index){
			if(slideStack[index].getSlideName() === slideName){
				debug.info("requested slide found from index ", index);
				slideFoundIndex = index;
				break;
			}
		}
		if(slideFoundIndex >= 0){
			if(isFirstRun){
				isFirstRun = false;
			}else{
				slideStack[activeSlideIndex].deactivate(slideStack[slideFoundIndex]);
			}
			activeSlideIndex = slideFoundIndex;
		}else{
			slideFoundIndex = -1;
		}
		updateBreadCrumbs(activeSlideIndex);
		return slideFoundIndex;
	};
	
	/**
	 * @param {CarouselSlide} slide js-object containing slide element and name
	 * @param {boolean} clearStackBeforeAppend should the stack be cleared first
	 */
	var appendToSlideStack = function(slide, clearStackBeforeAppend){
		if(clearStackBeforeAppend){
			clearStack();
		}
		slide.appendTo(mainContentElement);
		slideStack.push(slide);
		activateSlide(slide.getSlideName());
	};
	
	var clearStack = function(){
		debug.debug("clearing stack");
		for(var i=1; i < slideStack.length; ++i){
			slideStack[i].clear();
		}
		slideStack.length = 1;		//leaves the "home" slide intact
		activeSlideIndex = 0;
	};
	
	var updateBreadCrumbs = function(activeIndex){
		breadCrumbsElement.empty();
		for(var i=0; i<slideStack.length; ++i){
			var slide = slideStack[i];
			if(!slide.includeInBreadcrumbs){
				continue;
			}
			var breadCrumbText = slide.getLocalizedName();
			var breadCrumbItem = document.createElement("li");
			breadCrumbItem.setAttribute("title", slide.getAnchorHash());
			
			if(i == activeIndex){
				breadCrumbItem.textContent = breadCrumbText;
				breadCrumbItem.className = "active";
			}else{
				var anchor = document.createElement("a");
				anchor.setAttribute("href", "#"+slide.getAnchorHash());
				anchor.textContent = breadCrumbText;
				Utils.setAnchorClickHandler(anchor);
				breadCrumbItem.appendChild(anchor);
			}
			breadCrumbsElement.append(breadCrumbItem);
		}
	};

	//returns the public functions (require.js stuff)
	return {
		initialize: initialize,
		activateSlide: activateSlide,
		appendToSlideStack: appendToSlideStack,
		clearStack: clearStack
	};

});
