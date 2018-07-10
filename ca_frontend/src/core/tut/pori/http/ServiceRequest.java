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
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.HTTPParameterUtil;

/**
 * A service request definitions, which can be passed to ServiceHandler for service invocation.
 *
 */
public class ServiceRequest {
	private static final Logger LOGGER = Logger.getLogger(ServiceRequest.class);
	private UserIdentity _authenticatedUser = null;
	private boolean _bodyRequested = false;
	private CaseInsensitiveMap<String, String> _headers = null;
	private String _httpMethod = null; // e.g. GET, POST
	private String _methodName = null;
	private Map<String, List<String>> _rawParameters = null;	// the list of raw, URL decoded parameters
	private HttpServletRequest _request = null;
	private String _serviceName = null;
	
	/**
	 * 
	 */
	public ServiceRequest(){
		// nothing needed
	}
	
	/**
	 * this is only for sub-classing, use the static
	 * 
	 * @return true if service name, method name and http method are given
	 */
	protected boolean isValid(){
		if(_serviceName == null || _methodName == null || _httpMethod == null){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * 
	 * @param request
	 * @return true if the given request was valid and != null
	 */
	public static boolean isValid(ServiceRequest request){
		if(request == null){
			return false;
		}else{
			return request.isValid();
		}
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param httpServletRequest
	 * @return new request or null on failure
	 */
	public static ServiceRequest createRequest(UserIdentity authenticatedUser, HttpServletRequest httpServletRequest){
		String path[] = StringUtils.split(httpServletRequest.getPathInfo(), Definitions.SEPARATOR_URI_PATH, 2);	// get service and "the rest of the string"
		ServiceRequest r = null;
		if(path != null && path.length == 2){
			r = new ServiceRequest();
			r._serviceName = path[0];
			r._methodName = (path[1].endsWith(Definitions.SEPARATOR_URI_PATH) ? path[1].substring(0, path[1].length()-1) : path[1]);
			r._request = httpServletRequest;
			r._authenticatedUser = authenticatedUser;
			r._httpMethod = httpServletRequest.getMethod();
		}else{
			LOGGER.debug("Method name is missing.");
		}
		return r;
	}
	
	/**
	 * Note: you can only read the body once. Further attempts will return null
	 * 
	 * @return body or null if none available
	 */
	public InputStream getBody(){
		if(_bodyRequested){
			LOGGER.warn("Tried to read HTTP body, but it has already been read.");
		}else if(_request != null){
			_bodyRequested = true;
			try {
				return _request.getInputStream();
			} catch (IOException ex) {
				LOGGER.error(ex, ex);
			}
		}
		return null;
	}

	/**
	 * @return the serviceName
	 */
	public String getServiceName() {
		return _serviceName;
	}
	
	/**
	 * 
	 * @return the http session for this request. A new session will be created if one does not already exist. Returns null if no HttpRequest is associated with this request.
	 */
	public HttpSession getSession(){
		if(_request == null){
			LOGGER.warn("No request object.");
			return null;
		}else{
			return _request.getSession();
		}
	}

	/**
	 * @param serviceName the serviceName to set
	 */
	public void setServiceName(String serviceName) {
		_serviceName = serviceName;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return _methodName;
	}

	/**
	 * @param methodName the methodName to set
	 */
	public void setMethodName(String methodName) {
		_methodName = methodName;
	}

	/**
	 * @return the raw URL encoded parameters
	 */
	public Map<String, List<String>> getRawParameters() {
		if(_rawParameters == null){
			_rawParameters = HTTPParameterUtil.getParameterMap(_request, false);
		}
		return _rawParameters;
	}

	/**
	 * @return the authenticatedUser
	 */
	public UserIdentity getAuthenticatedUser() {
		return _authenticatedUser;
	}

	/**
	 * @param authenticatedUser the authenticatedUser to set
	 */
	public void setAuthenticatedUser(UserIdentity authenticatedUser) {
		_authenticatedUser = authenticatedUser;
	}

	/**
	 * @return the bodyRequested
	 */
	public boolean isBodyRequested() {
		return _bodyRequested;
	}

	/**
	 * @return the method, e.g. GET, POST
	 */
	public String getHttpMethod() {
		return _httpMethod;
	}

	/**
	 * @param method the method to set, e.g. GET, POST
	 */
	public void setHttpMethod(String method) {
		_httpMethod = method;
	}

	/**
	 * Set/override the previously set headers. 
	 * 
	 * Note that HTTP headers are case insensitive and thus duplicate header names with only difference being the case will be removed.
	 * 
	 * 
	 * @param headers the map of header names/values, note that the passed list will NOT be used, and an internal copy of the map will be made.
	 */
	public void setHeaders(Map<String, String> headers) {
		_headers = (headers == null || headers.isEmpty() ? null : new CaseInsensitiveMap<>(headers));
	}
	
	/**
	 * 
	 * @param headerName
	 * @return the value of header or null if the header does not exist
	 */
	public String getHeaderValue(String headerName) {
		if(_headers != null){	// use the set map if available
			return _headers.get(headerName);
		}else if(_request != null){
			return _request.getHeader(headerName);
		}else{
			return null;
		}
	}

	/**
	 * @return the request
	 */
	public HttpServletRequest getRequest() {
		return _request;
	}
}
