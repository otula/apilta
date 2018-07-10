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
package core.tut.pori.http.parameters;

import javax.servlet.http.HttpSession;

import core.tut.pori.http.ServiceRequest;
import core.tut.pori.users.UserIdentity;

/**
 * 
 * Special HTTP Parameter which (if used) contains details of the authenticated user
 * 
 * Note: when using this class as a method parameter, the use of \@HTTPAuthenticationParameter annotation is required
 *
 * Note: there can be only one AuthenticationParameter per method
 * 
 * The AuthenticationParameter object will always be present (not null) for the method in which is it declared, but
 * getUserIdentity() can return null if authentication was NOT required and the user did NOT provide credentials
 */
public class AuthenticationParameter{
	private UserIdentity _userId = null;
	private HttpSession _session = null;

	/**
	 * 
	 * @return true if the parameter has values
	 */
	public boolean hasValues() {
		return (_userId != null);
	}

	/**
	 * @return the userId or null if not authenticated
	 */
	public UserIdentity getUserIdentity() {
		return _userId;
	}

	/**
	 * initialize the authentication parameter
	 * @param serviceRequest 
	 * @return true on successful initialization
	 */
	public boolean initialize(ServiceRequest serviceRequest) {
		_userId = serviceRequest.getAuthenticatedUser();
		_session = serviceRequest.getSession();
		return true;
	}

	/**
	 * @return the session information or null if none available
	 */
	public HttpSession getSession() {
		return _session;
	}
}
