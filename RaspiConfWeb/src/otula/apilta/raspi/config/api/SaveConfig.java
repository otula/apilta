/**
 * Copyright 2018 Tampere University of Technology, Pori Department
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
package otula.apilta.raspi.config.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import core.tut.pori.utils.JSONFormatter;
import otula.apilta.raspi.config.ConfigurationCore;
import otula.apilta.raspi.config.datatypes.RaspiIni;
import otula.apilta.raspi.config.datatypes.Response;
import otula.apilta.raspi.config.datatypes.Response.Status;

/**
 * save config interface
 *
 */
public class SaveConfig extends HttpServlet {
	private static final Logger LOGGER = Logger.getLogger(SaveConfig.class);
	/** serial UID */
	private static final long serialVersionUID = -5106657315391065685L;
	private static final Gson FORMATTER = JSONFormatter.createGsonSerializer();
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Response response = new Response();
		try {
			ConfigurationCore.saveConfig(FORMATTER.fromJson(req.getReader(), RaspiIni.class));		
		}catch(IllegalArgumentException | JsonSyntaxException | JsonIOException ex){
			response.setStatus(Status.BAD_REQUEST);
			LOGGER.error(ex, ex);
		}
		response.writeTo(resp);
	}
}
