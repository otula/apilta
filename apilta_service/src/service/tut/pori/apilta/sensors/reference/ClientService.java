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
import core.tut.pori.http.Response.Status;
import core.tut.pori.http.annotations.HTTPAuthenticationParameter;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.AuthenticationParameter;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.DateIntervalParameter;
import core.tut.pori.http.parameters.InputStreamParameter;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.LongParameter;
import core.tut.pori.http.parameters.StringParameter;
import core.tut.pori.utils.XMLFormatter;
import service.tut.pori.apilta.sensors.datatypes.SensorTask;
import service.tut.pori.tasks.datatypes.Task;
import service.tut.pori.tasks.datatypes.TaskList;

/**
 * Reference implementation for client API methods.
 * 
 * <h1>Implementation Service path {@value service.tut.pori.apilta.sensors.Definitions#SERVICE_SENSORS}</h1>
 * 
 * @see service.tut.pori.apilta.sensors.SensorService
 *
 */
@HTTPService(name = Definitions.SERVICE_SENSORS_REFERENCE_CLIENT)
public class ClientService {
	private static final Logger LOGGER = Logger.getLogger(ClientService.class);
	private XMLFormatter _formatter = new XMLFormatter();
	
	/**
	 * The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * POST /rest/{@value service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_CLIENT}/{@value service.tut.pori.apilta.sensors.Definitions#METHOD_CREATE_TASK}<br>
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
	 * {@doc.restlet service="[service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_CLIENT]" method="[service.tut.pori.apilta.sensors.Definitions#METHOD_CREATE_TASK]" type="POST" query="" body_uri="[service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_EXAMPLE]/[service.tut.pori.tasks.Definitions#ELEMENT_TASK]"}
	 * 
	 * @param authenticatedUser 
	 * @param xml Only the workload data should be in the body. See {@link service.tut.pori.apilta.sensors.datatypes.SensorTask}
	 * @return {@link service.tut.pori.tasks.datatypes.TaskList} with identifier for the generated task
	 */
	@HTTPServiceMethod(name = service.tut.pori.apilta.sensors.Definitions.METHOD_CREATE_TASK, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response createTask (
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml
			) 
	{
		try {
			String body = IOUtils.toString(xml.getValue(), core.tut.pori.http.Definitions.CHARSET_UTF8); // read the body
			LOGGER.debug(body); // print to debug
			try(InputStream input = IOUtils.toInputStream(body, core.tut.pori.http.Definitions.CHARSET_UTF8)){ // convert back to stream for unmarshal
				String taskId = SensorsReferenceCore.createTask(authenticatedUser.getUserIdentity(), _formatter.toObject(input, SensorTask.class));
				TaskList taskList = new TaskList();
				Task task = new SensorTask();
				task.addTaskId(taskId);
				taskList.addTask(task);
				return new Response(taskList);
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			return new Response(Status.BAD_REQUEST);
		}
	}
	
	/**
	 * The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * POST /rest/{@value service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_CLIENT}/{@value service.tut.pori.apilta.sensors.Definitions#METHOD_MODIFY_TASK}<br>
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
	 * {@doc.restlet service="[service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_CLIENT]" method="[service.tut.pori.apilta.sensors.Definitions#METHOD_MODIFY_TASK]" type="POST" query="" body_uri="[service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_EXAMPLE]/[service.tut.pori.tasks.Definitions#ELEMENT_TASK]"}
	 * 
	 * @param authenticatedUser 
	 * @param xml Only the workload data should be in the body. See {@link service.tut.pori.apilta.sensors.datatypes.SensorTask}
	 * @return {@link service.tut.pori.tasks.datatypes.TaskList} with identifier for the generated task
	 */
	@HTTPServiceMethod(name = service.tut.pori.apilta.sensors.Definitions.METHOD_MODIFY_TASK, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response modifyTask (
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml
			) 
	{
		try {
			String body = IOUtils.toString(xml.getValue(), core.tut.pori.http.Definitions.CHARSET_UTF8); // read the body
			LOGGER.debug(body); // print to debug
			try(InputStream input = IOUtils.toInputStream(body, core.tut.pori.http.Definitions.CHARSET_UTF8)){ // convert back to stream for unmarshal
				String taskId = SensorsReferenceCore.modifyTask(authenticatedUser.getUserIdentity(), _formatter.toObject(input, SensorTask.class));
				TaskList taskList = new TaskList();
				Task task = new SensorTask();
				task.addTaskId(taskId);
				taskList.addTask(task);
				return new Response(taskList);
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			return new Response(Status.BAD_REQUEST);
		}
	}
	
	/**
	 * This method can be used to retrieve measurements for a single task or for a list of tasks defined by the task ids.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_CLIENT}/{@value service.tut.pori.apilta.sensors.Definitions#METHOD_GET_MEASUREMENTS}?{@value service.tut.pori.tasks.Definitions#PARAMETER_TASK_ID}=1<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.apilta.sensors.reference.Definitions#SERVICE_SENSORS_REFERENCE_CLIENT]" method="[service.tut.pori.apilta.sensors.Definitions#METHOD_GET_MEASUREMENTS]" type="GET" query="[service.tut.pori.tasks.Definitions#PARAMETER_TASK_ID]=1" body_uri=""}
	 * 
	 * 
	 * @param authenticatedUser
	 * @param taskId
	 * @param backendIdFilter
	 * @param createdFilter
	 * @param dataGroups 
	 * @param limits
	 * @param measurementIdFilter
	 * @return see {@link service.tut.pori.apilta.sensors.datatypes.MeasurementList}
	 */
	@HTTPServiceMethod(name = service.tut.pori.apilta.sensors.Definitions.METHOD_GET_MEASUREMENTS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response getMeasurements(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = service.tut.pori.tasks.Definitions.PARAMETER_TASK_ID) StringParameter taskId,
			@HTTPMethodParameter(name = service.tut.pori.tasks.Definitions.PARAMETER_BACKEND_ID, required = false) LongParameter backendIdFilter,
			@HTTPMethodParameter(name = service.tut.pori.tasks.Definitions.PARAMETER_CREATED, required = false) DateIntervalParameter createdFilter,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits,
			@HTTPMethodParameter(name = service.tut.pori.apilta.sensors.Definitions.PARAMETER_MEASUREMENT_ID, required = false) StringParameter measurementIdFilter
			)
	{
		return new Response(SensorsReferenceCore.getMeasurements(authenticatedUser.getUserIdentity(), backendIdFilter.getValues(), createdFilter.getValues(), dataGroups, limits, measurementIdFilter.getValues(), taskId.getValues()));
	}
}
