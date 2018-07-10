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
package core.tut.pori.http;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Prints data as raw strings format.
 * 
 */
public class StringResponse extends Response {
	private static final Logger LOGGER = Logger.getLogger(StringResponse.class);
	private StringData _stringData = null;

	/**
	 * for serialization
	 */
	public StringResponse(){
		// nothing needed
	}
	
	/**
	 * 
	 * @param status
	 */
	public StringResponse(Status status){
		super(status);
	}
	
	/**
	 * 
	 * @param data
	 */
	public StringResponse(StringData data){
		_stringData = data;
	}
	
	/**
	 * @throws UnsupportedOperationException if data is not of type {@link core.tut.pori.http.StringResponse.StringData}
	 * @see #setStringData(StringData)
	 */
	@Override
	public void setResponseData(ResponseData data) throws UnsupportedOperationException {
		if(data == null){
			setStringData((StringData)null);
		}else if(data instanceof StringData){
			setStringData((StringData) data);
		}else{
			throw new UnsupportedOperationException("Using "+ResponseData.class.toString()+" with class "+StringResponse.class.toString()+" is not supported.");
		}
	}

	@Override
	public void writeTo(HttpServletResponse response) {
		response.setCharacterEncoding((_stringData == null ? Definitions.ENCODING_UTF8 : _stringData.getEncoding()));
		
		Status status = getStatus();
		if(status != Status.OK){
			LOGGER.debug("Ignoring content because of error condition: "+status.name());
			String message = getMessage();
			if(!StringUtils.isBlank(message)){
				response.setContentType(Definitions.CONTENT_TYPE_TEXT);
				try {
					response.getWriter().write(message);
				} catch (IOException ex) {
					LOGGER.error(ex, ex);
					setStatus(Status.INTERNAL_SERVER_ERROR);
				}
			}
		}else if(_stringData == null){
			LOGGER.warn("No content given.");
			setStatus(Status.NO_CONTENT);
		}else{
			response.setContentType(_stringData.getContentType());
			try {
				response.getWriter().write(_stringData.toResponseString());
			} catch (IOException ex) {
				LOGGER.error(ex, ex);
				setStatus(Status.INTERNAL_SERVER_ERROR);
			}
		}
		
		status = getStatus();
		if(status != Status.OK){	// don't change defaults if there is OK status
			response.setStatus(status.toStatusCode());
			if(status == Status.UNAUTHORIZED){
				setDefaultAuthenticationHeader(response);
			}
		}
	}

	/**
	 * @return the stringData
	 */
	public StringData getStringData() {
		return _stringData;
	}

	/**
	 * @param stringData the stringData to set
	 */
	public void setStringData(StringData stringData) {
		_stringData = stringData;
	}


	/**
	 * Interface for classes, which should support conversion to string-based response data
	 * 
	 */
	public interface StringData {
		/**
		 * 
		 * @return the data as response
		 */
		public String toResponseString();
		
		/**
		 * 
		 * @return the content type (<a href="http://www.w3.org/Protocols/rfc1341/4_Content-Type.html">The Content-Type Header Field</a>) for the response.
		 */
		public String getContentType();
		
		/**
		 * 
		 * @return encoding for the response data, such as {@link core.tut.pori.http.Definitions#ENCODING_UTF8}}
		 * @see #toResponseString()
		 */
		public String getEncoding();
	} // interface SubtitleData
}
