/**
 * Copyright 2017 Tampere University of Technology, Pori Department
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
package otula.backend.tasks;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import core.tut.pori.http.Definitions;
import core.tut.pori.http.Response;
import core.tut.pori.http.Response.Status;
import core.tut.pori.utils.XMLFormatter;
import otula.backend.tasks.datatypes.SensorTask;

/**
 * handler for add task calls
 * 
 */
public class AddTask extends HttpServlet {
	/** serial UID */
	private static final long serialVersionUID = 7292432453463439145L;
	private static final XMLFormatter FORMATTER;
	private static final Logger LOGGER = Logger.getLogger(AddTask.class);
	static {
		FORMATTER = new XMLFormatter();
		FORMATTER.setThrowOnError(false);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Response response = new Response();
		String path[] = StringUtils.split(req.getPathInfo(), Definitions.SEPARATOR_URI_PATH, 2);	// get service and "the rest of the string"
		if(path != null && path.length == 2){
			response.setService(path[0]);
			response.setMethod((path[1].endsWith(Definitions.SEPARATOR_URI_PATH) ? path[1].substring(0, path[1].length()-1) : path[1]));
		}else{
			LOGGER.warn("Method name is missing.");
		}
		
		try{
			TasksCore.newTask(FORMATTER.toObject(req.getInputStream(), SensorTask.class));
		}catch(IllegalArgumentException ex){
			response.setStatus(Status.BAD_REQUEST);
			LOGGER.error(ex, ex);
		}
		response.writeTo(resp);
	}
}
