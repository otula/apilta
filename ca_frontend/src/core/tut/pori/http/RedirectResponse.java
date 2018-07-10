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

/**
 * Simple Response class for sending a redirect to a provided URL
 * 
 */
public class RedirectResponse extends Response{
	private static final Logger LOGGER = Logger.getLogger(RedirectResponse.class);
	private String _redirectUrl = null;

	@Override
	public void writeTo(HttpServletResponse response) {
		try {
			if(_redirectUrl == null){
				LOGGER.debug("No redirect URL given, will not redirect...");
				super.writeTo(response);
			}else{
				response.sendRedirect(_redirectUrl);
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			setStatus(Status.INTERNAL_SERVER_ERROR);
			super.writeTo(response);
		}
	}
	
	/**
	 * for serialization
	 */
	public RedirectResponse(){
		// nothing needed
	}
	
	/**
	 * 
	 * @param url where to redirect
	 */
	public RedirectResponse(String url){
		_redirectUrl = url;
	}
}
