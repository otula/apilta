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

define(['jquery', 'marked'], function($, marked) {

	/**
	 * @param {Element} parentNode
	 * @param {String} type
	 * @return {Boolean} true if content was loaded properly
	 */
	var loadContent = function(parentNode, type){
		var mdDocumentName = undefined;
		switch(type){
			case "about":
				mdDocumentName = definitions.MARKDOWN.ABOUT;
				break;
			case "contact":
				mdDocumentName = definitions.MARKDOWN.CONTACT;
				break;
			case "privacy":
				mdDocumentName = definitions.MARKDOWN.PRIVACY;
				break;
			case "welcome":
				mdDocumentName = definitions.MARKDOWN.WELCOME;
				break;
			default:
				return false;
		}
		var requireUrl = "../resources/"+mdDocumentName;
		require(['text!'+requireUrl], function(txt){
			$(parentNode).empty();
			parentNode.appendChild(parseMarkdownContent(txt));
		});
		return true;
	};
	
	/**
	 * @param {String} data markdown document
	 * @return {Element} returns parsed markdown document encapsulated into a DOM element 
	 */
	var parseMarkdownContent = function(data){
		var mdElement = document.createElement("div");
		mdElement.className = "markdown-content";
		
		var parsedContent = marked(data);
		mdElement.innerHTML = parsedContent;
		return mdElement;
	};

	//returns the public functions (require.js stuff)
	return {
		loadContent: loadContent,
		parseMarkdownContent: parseMarkdownContent
	};

});
