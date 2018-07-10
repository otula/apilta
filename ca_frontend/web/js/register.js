/**
 * Copyright 2015 Tampere University of Technology, Pori Department
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
 * @returns {Boolean}
 */
function register(){
	var formElement = document.getElementById("regForm");
	var username = formElement.username.value;
	var password = formElement.password.value;
	var registerPassword = formElement.registeration_password.value;
	registerAPI(username, password, registerPassword);
	return false;
}

function showMessage(title, message){
	document.getElementById("dialog-title").textContent = title;
	document.getElementById("dialog-message").textContent = message;
	document.getElementById("overlay").className = "";
}

function dialogAccept(){
	document.getElementById("overlay").className = "hidden";
}

function httpRequestReady(xhrHttpRequest){
	if(!(xhrHttpRequest.readyState == 4) || !(xhrHttpRequest.responseText)){
		return;
	}

	console.log("Network status: '"+xhrHttpRequest.statusText+"' ("+xhrHttpRequest.status+")");
	if(xhrHttpRequest.status === 401){	//state UNAUTHORIZED
		console.log("An authentication error occurred.");
		showMessage("Unauthorized", "An authentication error occurred.");
		return;
	}else if(xhrHttpRequest.status === 400){
		var status = xhrHttpRequest.responseXML.getElementsByTagName('status')[0].textContent;
		var message = xhrHttpRequest.responseXML.getElementsByTagName('message')[0].textContent;
		showMessage("Bad Request", message);
		return;
	}else if(xhrHttpRequest.status !== 200){
		showMessage("Network Error", "Error status: '"+xhrHttpRequest.statusText+"' ("+xhrHttpRequest.status+")");
		return;
	}else{
	}
	
	var status = xhrHttpRequest.responseXML.getElementsByTagName('status')[0].textContent;
	var method = xhrHttpRequest.responseXML.documentElement.getAttribute('method');
	
	if(method == "register"){
		if(status.toLowerCase() == "ok"){
			registrationComplete();
		}
	}
	console.log(xhrHttpRequest.responseText);
}

/**
 * @param username
 * @param password
 * @param registerPassword
 */
function registerAPI(username, password, registerPassword){
	var data = document.implementation.createDocument(null, 'registration', null);
	var element_username=data.createElement("username");
	element_username.appendChild(data.createTextNode(username)); 
	data.documentElement.appendChild(element_username);
	var element_password=data.createElement("password");
	element_password.appendChild(data.createTextNode(password));
	data.documentElement.appendChild(element_password);
	var element_register_password=data.createElement("registerPassword");
	element_register_password.appendChild(data.createTextNode(registerPassword));
	data.documentElement.appendChild(element_register_password);
	
	var xhrHttp=new XMLHttpRequest();
	xhrHttp.open("POST", "/CAFrontEnd/rest/user/register", true);
	xhrHttp.setRequestHeader('Content-Type', 'text/xml');	//setting headers must be after OPEN but before SEND
	xhrHttp.onreadystatechange=function(){ httpRequestReady(xhrHttp); };
	xhrHttp.send(new XMLSerializer().serializeToString(data));	
}

/**
 * Function to be called after successful sign up.
 */
function registrationComplete(){
    document.getElementById("regForm").reset();
	showMessage("Registration Complete", "Your account has been set up.");
}
