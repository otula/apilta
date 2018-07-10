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
package otula.apilta.raspi.config.datatypes;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.annotations.SerializedName;

import core.tut.pori.http.Definitions;
import core.tut.pori.utils.JSONFormatter;

/**
 * JSON response
 */
public class Response {
	private static final Logger LOGGER = Logger.getLogger(Response.class);
	@SerializedName(value=Definitions.JSON_METHOD)
	private String _method = null;
	@SerializedName(value=Definitions.JSON_MESSAGE)
	private String _message = null;
	@SerializedName(value=Definitions.JSON_SERVICE)
	private String _service = null;
	@SerializedName(value=Definitions.JSON_STATUS)
	private Status _status = Status.OK;
	
	/**
	 * HTTP Status code enumerations.
	 * 
	 * As defined by http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
	 */
	public enum Status{
		/** 200 OK response */
		OK(200),
		/** 204 No Content */
		NO_CONTENT(204),
		/** 400 Bad Request response */
		BAD_REQUEST(400),
		/** 401 Unauthorized response */
		UNAUTHORIZED(401),
		/** 403 Forbidden response */
		FORBIDDEN(403),
		/** 404 Not Found response */
		NOT_FOUND(404),
		/** 440 Login Timeout response, Microsoft/Outlook extension. In this case used more liberally for all login timeout cases. */
		LOGIN_TIMEOUT(440),
		/** 500 Internal Server Error response */
		INTERNAL_SERVER_ERROR(500),
		/** 503 Service Unavailable response */
		SERVICE_UNAVAILABLE(503);
		
		private int _code;
		
		/**
		 * 
		 * @param code
		 */
		private Status(int code){
			_code = code;
		}
		
		/**
		 * convert to HTTP status code
		 * @return the status as HTTP status code
		 */
		public int toStatusCode(){
			return _code;
		}
		
		/**
		 * 
		 * @param code
		 * @return status code from the given integer code, returns INTERNAL_SERVER_ERROR on unknown error code
		 */
		public static Status fromStatusCode(int code){
			for(Status s : Status.values()){
				if(s._code == code){
					return s;
				}
			}
			return INTERNAL_SERVER_ERROR;
		}
	} // enum Status
	
	/**
	 * for serialization
	 */
	public Response(){
		// nothing needed
	}
	
	/**
	 * 
	 * @param status
	 */
	public Response(Status status){
		_status = status;
	}
	
	/**
	 * 
	 * @param status
	 * @param message
	 */
	public Response(Status status, String message){
		_status = status;
		_message = message;
	}
	
	/**
	 * This method replaces the default XML/JAXB output with JSON using GSON annotations
	 * and sets content type to {@value core.tut.pori.http.Definitions#CONTENT_TYPE_JSON}.
	 * These to parameters should be changed for the response if format is changed in the overriding method.
	 * 
	 * Additionally this method sets the HTTP basic authentication header if the status is set {@link Status#UNAUTHORIZED}.
	 * 
	 * @param response
	 * @throws IllegalStateException 
	 */
	public void writeTo(HttpServletResponse response) throws IllegalStateException {
		try {
			response.setContentType(Definitions.CONTENT_TYPE_JSON);
			response.setCharacterEncoding(Definitions.ENCODING_UTF8);
			response.getWriter().write(JSONFormatter.createGsonSerializer().toJson(this));
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			_status = Status.INTERNAL_SERVER_ERROR;
		}
		
		if(_status != Status.OK){	// don't change defaults if there is OK status
			response.setStatus(_status.toStatusCode());
			if(_status == Status.UNAUTHORIZED){
				setDefaultAuthenticationHeader(response);
			}
		}
	}
	
	/**
	 * Set the default HTTP authentication header in the response.
	 * 
	 * <a href="http://tools.ietf.org/html/rfc2617#section-2">HTTP Basic Authentication</a>
	 * 
	 * @param response
	 */
	public static void setDefaultAuthenticationHeader(HttpServletResponse response){
		response.setHeader(Definitions.HEADER_AUTHENTICATE, Definitions.HEADER_AUTHENTICATE_VALUE);
	}

	/**
	 * @return the method
	 */
	public String getMethod() {
		return _method;
	}

	/**
	 * @param method the method to set
	 */
	public void setMethod(String method) {
		_method = method;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return _message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		_message = message;
	}

	/**
	 * @return the service
	 */
	public String getService() {
		return _service;
	}

	/**
	 * @param service the service to set
	 */
	public void setService(String service) {
		_service = service;
	}

	/**
	 * @return the status
	 */
	public Status getStatus() {
		return _status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Status status) {
		_status = status;
	}
}
