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

define(['jquery', 'app/markdown_loader', 'app/slides/carousel_slide', 'app/utils', "i18n!app/nls/translations"], 
		function($, mdLoader, CarouselSlide, Utils, Translations) {

	//member variables
	
	var Markdownpage = function(hash, params){
		CarouselSlide.call(this, "markdown", hash);
		if(!mdLoader.loadContent(this.content, params.pop())){
			throw new Error(Utils.translate(Translations.error_general, hash));
		}
	};
	
	//Markdownpage class
	Markdownpage.prototype = Object.create(CarouselSlide.prototype);
	Markdownpage.prototype.constructor = Markdownpage;

	//returns the public functions (require.js stuff)
	return Markdownpage;

});
