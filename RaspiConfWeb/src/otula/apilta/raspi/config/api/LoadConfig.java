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

import otula.apilta.raspi.config.ConfigurationCore;
import otula.apilta.raspi.config.datatypes.ConfigResponse;
import otula.apilta.raspi.config.datatypes.Response.Status;

/**
 * load config interface
 * 
 */
public class LoadConfig extends HttpServlet {
	private static final Logger LOGGER = Logger.getLogger(LoadConfig.class);
	/** serial UID */
	private static final long serialVersionUID = -7093722181305983142L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ConfigResponse response = new ConfigResponse();
		try{
			response.setRaspiIni(ConfigurationCore.loadConfig());
		}catch(IllegalArgumentException ex){
			response.setStatus(Status.BAD_REQUEST);
			LOGGER.error(ex, ex);
		}
		response.writeTo(resp);
	}
}
