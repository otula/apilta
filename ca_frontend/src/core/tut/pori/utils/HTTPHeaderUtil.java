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
package core.tut.pori.utils;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpMessage;
import org.apache.log4j.Logger;

import core.tut.pori.http.Definitions;

/**
 * common HTTP parameter utility methods
 *
 */
public final class HTTPHeaderUtil {
	private static final Logger LOGGER = Logger.getLogger(HTTPHeaderUtil.class);
	
	/**
	 * 
	 */
	private HTTPHeaderUtil(){
		// nothing needed
	}
	
	/**
	 * Create and set <a href="http://tools.ietf.org/html/rfc2617">HTTP Basic Authentication</a> header based on the given values.
	 * 
	 * @param message the message object to set the header to
	 * @param username if null or empty, this method is a no-op
	 * @param password non-null and non-empty password
	 * @throws IllegalArgumentException on bad values
	 */
	public static void setHTTPBasicAuthHeader(HttpMessage message, String username, String password) throws IllegalArgumentException {
		if(StringUtils.isBlank(username)){
			LOGGER.debug("Ignored empty username.");
			return;
		}
		if(StringUtils.isBlank(password)){ // the RFC does not exactly mention whether the password can or cannot be an empty string, but for possible compatibility reason we'll reject all empty strings
			throw new IllegalArgumentException("Invalid password : "+password);
		}
		try {
			message.setHeader("Authorization", "Basic " + new Base64().encodeToString((username+":"+password).getBytes(Definitions.ENCODING_UTF8)).trim());	
		} catch (UnsupportedEncodingException ex) { // should never happen
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to process the header using encoding "+Definitions.ENCODING_UTF8);
		}
	}
}
