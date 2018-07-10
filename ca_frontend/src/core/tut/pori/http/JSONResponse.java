/**
 * Copyright 2014 Tampere University of Technology, Pori Department
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
package core.tut.pori.http;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import core.tut.pori.utils.JSONFormatter;

/**
 * 
 * Response object that supports JSON output.
 * 
 * Note that you MUST use this class if you plan to use JSON output, the ResponseData is one you should use if you need XML output.
 *
 */
public class JSONResponse extends Response {
	private static final Logger LOGGER = Logger.getLogger(JSONResponse.class);
	private static final Gson FORMATTER = JSONFormatter.createGsonSerializer();
	private JSONResponseData _responseData = null;

	/**
	 * This method replaces the default XML/JAXB output with JSON using GSON annotations
	 * and sets content type to {@value core.tut.pori.http.Definitions#CONTENT_TYPE_JSON}.
	 * These to parameters should be changed for the response if format is changed in the overriding method.
	 * 
	 * Additionally this method sets the HTTP basic authentication header if the status is set {@link core.tut.pori.http.Response.Status#UNAUTHORIZED}.
	 * 
	 * @param response
	 */
	@Override
	public void writeTo(HttpServletResponse response) {
		JSONResponseData data = _responseData;
		if(data == null){ // create new response if one does not already exist
			data = new DefaultJSONResponse(getMessage(), getMethod(), getService(), getStatus());
		}else{ // make sure the details in the response are up to date
			data._message = getMessage();
			data._method = getMethod();
			data._service = getService();
			data._stat = getStatus();
		}
		
		Status stat = getStatus();
		if(stat != Status.OK){	// don't change defaults if there is OK status
			response.setStatus(stat.toStatusCode());
			if(stat == Status.UNAUTHORIZED){
				setDefaultAuthenticationHeader(response);
			}
		}
		
		try {
			response.setContentType(Definitions.CONTENT_TYPE_JSON);
			response.setCharacterEncoding(Definitions.ENCODING_UTF8);
			FORMATTER.toJson(data, response.getWriter());
		} catch (IOException ex) {  // could be broken socket or other unresolvable error
			LOGGER.error(ex, ex);
			setStatus(Status.INTERNAL_SERVER_ERROR);  // mark as internal error as we don't know the real reason
		}
	}
	
	/**
	 * @throws UnsupportedOperationException if data is not of type {@link core.tut.pori.http.JSONResponseData}
	 * @see #setResponseData(JSONResponseData)
	 */
	@Override
	public void setResponseData(ResponseData data) throws UnsupportedOperationException {
		if(data == null){
			setResponseData((JSONResponseData)null);
		}else if(data instanceof JSONResponseData){
			setResponseData((JSONResponseData)data);
		}else{
			throw new UnsupportedOperationException("Unsupported data type: "+data.getClass().toString());
		}
	}
	
	/**
	 * 
	 */
	public JSONResponse() {
		super();
	}

	/**
	 * 
	 * @param data
	 * @throws UnsupportedOperationException if the given data is not of type JSONResponseData
	 */
	public JSONResponse(ResponseData data) throws UnsupportedOperationException {
		super();
		setResponseData(data);
	}
	
	/**
	 * 
	 * @param data
	 */
	public JSONResponse(JSONResponseData data){
		_responseData = data;
	}

	/**
	 * 
	 * @param stat
	 * @param message
	 */
	public JSONResponse(Status stat, String message) {
		super(stat, message);
	}

	/**
	 * 
	 * @param stat
	 */
	public JSONResponse(Status stat) {
		super(stat);
	}

	/**
	 * 
	 * @param data
	 */
	public void setResponseData(JSONResponseData data){
		_responseData = data;
	}

	/**
	 * default implementation
	 *
	 */
	private class DefaultJSONResponse extends JSONResponseData{
		/**
		 * 
		 * @param message
		 * @param method
		 * @param service
		 * @param status
		 */
		public DefaultJSONResponse(String message, String method, String service, Status status){
			_message = message;
			_method = method;
			_service = service;
			_stat = status;
		}
	} // class DefaultGSONResponse 
}
