/**
 * Copyright 2016 Tampere University of Technology, Pori Department
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
package service.tut.pori.apilta.sensors.reference;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import core.tut.pori.http.Response;
import core.tut.pori.http.annotations.HTTPAuthenticationParameter;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.AuthenticationParameter;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.InputStreamParameter;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.LongParameter;
import core.tut.pori.utils.XMLFormatter;
import service.tut.pori.apilta.sensors.datatypes.SensorTask;

/**
 * 
 * Reference implementation for Back-end APIs
 *
 */
@HTTPService(name = Definitions.SERVICE_SENSORS_REFERENCE_BACKEND)
public class BackendService {
	private static final Logger LOGGER = Logger.getLogger(BackendService.class);
	private XMLFormatter _formatter = new XMLFormatter();
	
	/**
	 * The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * POST /rest/{@value service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_BACKEND}/{@value service.tut.pori.tasks.Definitions#METHOD_ADD_TASK}<br>
	 * Content-Type: text/xml; charset=UTF-8<br><br>
	 *
	 * <b>[HTTP BODY STARTS]</b><br>
	 * 
	 * {@doc.restlet service="[service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_EXAMPLE]" method="[service.tut.pori.tasks.Definitions#ELEMENT_TASK]" type="GET" query="" body_uri=""} <br>
	 * 
	 * <b>[HTTP BODY ENDS]</b><br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_BACKEND]" method="[service.tut.pori.tasks.Definitions#METHOD_ADD_TASK]" type="POST" query="" body_uri="[service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_EXAMPLE]/[service.tut.pori.tasks.Definitions#ELEMENT_TASK]"}
	 * 
	 * @param authenticatedUser 
	 * @param xml Only the workload data should be in the body. See {@link service.tut.pori.apilta.sensors.datatypes.SensorTask}
	 */
	@HTTPServiceMethod(name = service.tut.pori.tasks.Definitions.METHOD_ADD_TASK, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void addTask (
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml
			) 
	{
		try {
			String body = IOUtils.toString(xml.getValue(), core.tut.pori.http.Definitions.CHARSET_UTF8); // read the body
			LOGGER.debug(body); // print to debug
			try(InputStream input = IOUtils.toInputStream(body, core.tut.pori.http.Definitions.CHARSET_UTF8)){ // convert back to stream for unmarshal
				SensorsReferenceCore.addTask(authenticatedUser.getUserIdentity(), _formatter.toObject(input, SensorTask.class));
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
	}
	
	/**
	 * This method can be used to query the current status of a sensor task from the back-end.
	 * 
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_BACKEND}/{@value service.tut.pori.tasks.Definitions#METHOD_QUERY_TASK_STATUS}?{@value service.tut.pori.tasks.Definitions#PARAMETER_TASK_ID}=1<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_BACKEND]" method="[service.tut.pori.tasks.Definitions#METHOD_QUERY_TASK_STATUS]" type="GET" query="[service.tut.pori.tasks.Definitions#PARAMETER_TASK_ID]=1" body_uri=""}
	 * 
	 * @param authenticatedUser 
	 * @param taskId
	 * @param dataGroups
	 * @param limits paging limits
	 * @return See {@link service.tut.pori.apilta.sensors.datatypes.SensorTask}
	 */
	@HTTPServiceMethod(name = service.tut.pori.tasks.Definitions.METHOD_QUERY_TASK_STATUS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response queryTaskStatus(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = service.tut.pori.tasks.Definitions.PARAMETER_TASK_ID) LongParameter taskId,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits
			)
	{
		return new Response(SensorsReferenceCore.generateTaskResults(authenticatedUser.getUserIdentity(), dataGroups, limits, service.tut.pori.apilta.sensors.Definitions.TASK_TYPE_DATA_COLLECT)); // we can simply return pseudo randomly generated task results with hard-coded task type
	}
}
