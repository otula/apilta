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

define(['jquery', "i18n!app/nls/i18n_slides"], function($, SlideTranslations) {

	/**
	 * Prototype for CarouselSlide
	 */
	var CarouselSlide = function(name, anchorHash){
		this.slideName = name;
		this.anchorHash = anchorHash;
		this.isActive = false;
		this.includeInBreadcrumbs = true;
		this.id = name + "_" + Math.random().toString(36).slice(-10);
		this.content = document.createElement("div");
		this.content.id = this.id;
		this.slideReadyHandler = undefined;	//handler to be called after slide is brought visible
	};
	
	CarouselSlide.prototype.appendTo = function(element){
		if(this.isActive){
			$(this.content).appendTo(element);
		}else{
			$(this.content)
					.hide()
					.appendTo(element);
		}
		return this;
	};
	
	CarouselSlide.prototype.deactivate = function(nextSlide){
		debug.debug(this, nextSlide);
		if(nextSlide){
			if(this.id === nextSlide.id){
				$(nextSlide.content).show();	//make sure the content stays visible
				return;
			}
			$(this.content).fadeOut("fast", nextSlide.activate.bind(nextSlide));
		}else{
			$(this.content).fadeOut("fast");
		}
		this.isActive = false;
	};
	
	CarouselSlide.prototype.activate = function(previousSlide){
		debug.debug(this, previousSlide);
		if(previousSlide){
			if(this.id === previousSlide.id){
				$(this.content).show();	//make sure the content stays visible
				return;
			}
			$(previousSlide.content).hide();
		}
		$(this.content).fadeIn("fast", this.slideReadyHandler);
		this.isActive = true;
	};
	
	CarouselSlide.prototype.clear = function(){
		if(this.content){
			debug.debug("emptying slide");
			if(this.isActive){
				this.deactivate();
			}
			$(this.content).detach().empty();
			this.content = null;
		}
	};
	
	CarouselSlide.prototype.getSlideName = function(){
		return this.slideName;
	};
	
	CarouselSlide.prototype.getAnchorHash = function(){
		return this.anchorHash;
	};
	
	CarouselSlide.prototype.getLocalizedName = function(){
		return SlideTranslations[this.slideName];
	};

	//returns the public functions (require.js stuff)
	return CarouselSlide;
});
