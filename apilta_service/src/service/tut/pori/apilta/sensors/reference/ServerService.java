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

import service.tut.pori.apilta.files.datatypes.FileDetails;
import service.tut.pori.apilta.files.datatypes.FileDetailsList;
import service.tut.pori.apilta.sensors.datatypes.SensorTask;
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

/**
 * 
 * Reference implementation for Server APIs.
 * 
 * <h1>Implementation Service path {@value service.tut.pori.apilta.sensors.Definitions#SERVICE_SENSORS}</h1>
 * 
 * @see service.tut.pori.apilta.sensors.SensorService
 *
 */
@HTTPService(name = Definitions.SERVICE_SENSORS_REFERENCE_SERVER)
public class ServerService {
	private static final Logger LOGGER = Logger.getLogger(ServerService.class);
	private XMLFormatter _formatter = new XMLFormatter();
	
	/**
	 * The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".
	 * 
	 * The method can be called multiple times by the back-ends. Each call is assumed to be an incremental update on the previous one and can contain any amount of new information. 
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * POST /rest/{@value service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_SERVER}/{@value service.tut.pori.tasks.Definitions#METHOD_TASK_FINISHED}<br>
	 * Content-Type: text/xml; charset=UTF-8<br><br>
	 *
	 * <b>[HTTP BODY STARTS]</b><br>
	 * 
	 * {@doc.restlet service="[service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_EXAMPLE]" method="[service.tut.pori.apilta.sensors.reference.Definitions#METHOD_TASK_RESULTS]" type="GET" query="" body_uri=""} <br>
	 * 
	 * <b>[HTTP BODY ENDS]</b><br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_SERVER]" method="[service.tut.pori.tasks.Definitions#METHOD_TASK_FINISHED]" type="POST" query="" body_uri="[service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_EXAMPLE]/[service.tut.pori.apilta.sensors.reference.Definitions#METHOD_TASK_RESULTS]?[core.tut.pori.http.parameters.DataGroups#PARAMETER_DEFAULT_NAME]=[service.tut.pori.apilta.sensors.Definitions#DATA_GROUP_DATA_POINTS]"}
	 * 
	 * @param authenticatedUser 
	 * @param xml Only the result data should be in the body. See {@link service.tut.pori.apilta.sensors.datatypes.SensorTask}
	 */
	@HTTPServiceMethod(name = service.tut.pori.tasks.Definitions.METHOD_TASK_FINISHED, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void taskFinished (
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml
			) 
	{
		try {
			String body = IOUtils.toString(xml.getValue(), core.tut.pori.http.Definitions.CHARSET_UTF8); // read the body
			LOGGER.debug(body); // print to debug
			try(InputStream input = IOUtils.toInputStream(body, core.tut.pori.http.Definitions.CHARSET_UTF8)){ // convert back to stream for unmarshal
				SensorsReferenceCore.taskFinished(authenticatedUser.getUserIdentity(), _formatter.toObject(input, SensorTask.class));
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
	}
	
	/**
	 * This method can be used to retrieve the up-to-date details and progress of a previously scheduled task.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_SERVER}/{@value service.tut.pori.tasks.Definitions#METHOD_QUERY_TASK_DETAILS}?{@value service.tut.pori.tasks.Definitions#PARAMETER_BACKEND_ID}=1&amp;{@value service.tut.pori.tasks.Definitions#PARAMETER_TASK_ID}=1<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_SERVER]" method="[service.tut.pori.tasks.Definitions#METHOD_QUERY_TASK_DETAILS]" type="GET" query="[service.tut.pori.tasks.Definitions#PARAMETER_BACKEND_ID]=1&[service.tut.pori.tasks.Definitions#PARAMETER_TASK_ID]=1" body_uri=""}
	 * 
	 * @param authenticatedUser 
	 * @param taskId
	 * @param backendId
	 * @param dataGroups
	 * @param limits paging limits
	 * @return response See {@link service.tut.pori.apilta.sensors.datatypes.SensorTask}
	 */
	@HTTPServiceMethod(name = service.tut.pori.tasks.Definitions.METHOD_QUERY_TASK_DETAILS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response queryTaskDetails(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = service.tut.pori.tasks.Definitions.PARAMETER_BACKEND_ID) LongParameter backendId,
			@HTTPMethodParameter(name = service.tut.pori.tasks.Definitions.PARAMETER_TASK_ID) LongParameter taskId,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits
			) 
	{
		return new Response(SensorsReferenceCore.generateTaskDetails(authenticatedUser.getUserIdentity(), backendId.getValue(), dataGroups, limits, service.tut.pori.apilta.sensors.Definitions.TASK_TYPE_DATA_COLLECT)); // we can simply return pseudo randomly generated task details with hard-coded task type
	}
	
	/**
	 * The request is to be sent in the body of POST method.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * POST /rest/{@value service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_SERVER}/{@value service.tut.pori.apilta.sensors.Definitions#METHOD_CREATE_FILE}?{@value service.tut.pori.tasks.Definitions#PARAMETER_BACKEND_ID}=1<br>
	 * Content-Type: text/xml; charset=UTF-8<br><br>
	 *
	 * <b>[HTTP BODY STARTS]</b><br>
	 * 
	 * [ANY FILE CONTENT] <br>
	 * 
	 * <b>[HTTP BODY ENDS]</b><br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_SERVER]" method="[service.tut.pori.apilta.sensors.Definitions#METHOD_CREATE_FILE]" type="POST" query="[service.tut.pori.tasks.Definitions#PARAMETER_BACKEND_ID]=1" body_uri="[service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_EXAMPLE]/[service.tut.pori.tasks.Definitions#ELEMENT_TASK]"}
	 * 
	 * @param authenticatedUser 
	 * @param backendId 
	 * @param file only the file contents should be in the body.
	 * @return {@link service.tut.pori.apilta.files.datatypes.FileDetailsList} with identifier for the generated file
	 */
	@HTTPServiceMethod(name = service.tut.pori.apilta.sensors.Definitions.METHOD_CREATE_FILE, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response createFile (
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = service.tut.pori.tasks.Definitions.PARAMETER_BACKEND_ID) LongParameter backendId,
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter file
			) 
	{
		FileDetailsList list = new FileDetailsList();
		FileDetails details = new FileDetails();
		details.setGUID(SensorsReferenceCore.createFile(authenticatedUser.getUserIdentity(), backendId.getValue(), file.getValue()));
		list.addFile(details);
		return new Response(list);
	}
}
