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

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

import otula.apilta.raspi.config.ConfigurationCore;
import otula.apilta.raspi.config.datatypes.Response;
import otula.apilta.raspi.config.datatypes.Response.Status;

/**
 * Photo servlet
 * 
 */
public class Photo extends HttpServlet {
	/** UID */
	private static final long serialVersionUID = -3189396584703813711L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		File file = ConfigurationCore.getPhotoFile();
		if(file == null){
			new Response(Status.NOT_FOUND).writeTo(resp);
			return;
		}
		
		FileUtils.copyFile(file, resp.getOutputStream());
	}
}
