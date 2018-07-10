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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import core.tut.pori.http.Response;
import core.tut.pori.http.Response.Status;

/**
 * The generic fall-back error handler.
 * 
 * Called when a critical internal error (such as null pointer exception) happens during servlet execution.
 * 
 * This class will convert all errors to a 500 INTERNAL SERVER ERROR HTTP response hiding the actual error
 * from the user, and prints the exception stacktrace (if available) to the system log.
 * 
 */
public class ErrorHandler extends HttpServlet {
	/** serial id */
	private static final long serialVersionUID = -2642062282485268614L;
	private static final String ATTRIBUTE_EXCEPTION = "javax.servlet.error.exception";
	private static final Logger LOGGER = Logger.getLogger(ErrorHandler.class);

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processGeneric(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processGeneric(req, resp);
	}

	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processGeneric(req, resp);
	}

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processGeneric(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processGeneric(req, resp);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processGeneric(req, resp);
	}

	@Override
	protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processGeneric(req, resp);
	}

	/**
	 * 
	 * @param req
	 * @param resp
	 */
	private void processGeneric(HttpServletRequest req, HttpServletResponse resp){
		String requestUri = (String) req.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
		int status = resp.getStatus();
		LOGGER.error(req.getMethod()+" "+requestUri+", status: "+status, (Throwable) req.getAttribute(ATTRIBUTE_EXCEPTION));
		
		Response r = new Response(Status.fromStatusCode(status));
		r.setMessage(requestUri);
		r.writeTo(resp);
	}
}
