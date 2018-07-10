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
package core.tut.pori.context;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import core.tut.pori.http.Response;
import core.tut.pori.http.Response.Status;
import core.tut.pori.http.ServiceRequest;
import core.tut.pori.users.UserIdentity;

/**
 * Servlet class which processes the incoming requests to service requests and delegates them to the service handler.
 *
 */
public class RESTHandler extends HttpServlet{
	/** REST service uri path */
	public static final String PATH_REST = "rest/";
	private static final Logger LOGGER = Logger.getLogger(RESTHandler.class);
	private static final long serialVersionUID = -2268900222019910706L;

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handleRequest(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handleRequest(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handleRequest(req, resp);
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handleRequest(req, resp);
	}

	/**
	 * 
	 * @param req
	 * @param resp
	 */
	private void handleRequest(HttpServletRequest req, HttpServletResponse resp){
		String pathInfo = req.getPathInfo();
		LOGGER.debug("Received request "+req.getMethod()+" "+pathInfo+" from "+req.getRemoteAddr());	

		Response r = null;
		ServiceRequest serviceRequest = null;
		try{
			serviceRequest = ServiceRequest.createRequest(getAuthenticatedUser(), req);
			r = ServiceInitializer.getServiceHandler().invoke(serviceRequest);	// chop the trailing separator from method name if present
		}catch(Throwable ex){	// do not allow exceptions to get through
			LOGGER.error(ExceptionUtils.getStackTrace(ex));	// make sure the stacktrace gets printed if something else than IllegalArgumentException is thrown
			r = new Response();
			r.setStatus(Status.INTERNAL_SERVER_ERROR);
		}
		
		if(serviceRequest != null){	// check if no service or method name is set, and set the default if needed
			String service = r.getService();
			if(service == null){
				r.setService(serviceRequest.getServiceName());
			}
			String method = r.getMethod();
			if(method == null){
				r.setMethod(serviceRequest.getMethodName());
			}
		}else{
			LOGGER.warn("Failed to create "+ServiceRequest.class.toString());
		}
		r.writeTo(resp);
	}
	
	/**
	 * 
	 * @return authenticated user or null if user has not authenticated
	 */
	private UserIdentity getAuthenticatedUser(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth != null && auth.isAuthenticated()){
			Object principal = auth.getPrincipal();
			if(principal.getClass() == UserIdentity.class){
				return (UserIdentity) principal;
			}else{
				LOGGER.debug("UserDetails not available.");
			}			
		}
		return null;
	}
}
