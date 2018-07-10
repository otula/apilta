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
package service.tut.pori.apilta.sensors;

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
import service.tut.pori.apilta.files.datatypes.FileDetails;
import service.tut.pori.apilta.files.datatypes.FileDetailsList;
import service.tut.pori.apilta.sensors.datatypes.SensorTask;
import service.tut.pori.tasks.TasksCore;
import service.tut.pori.tasks.datatypes.Task;
import service.tut.pori.tasks.datatypes.TaskList;

/**
 * Service declaration for the sensor service
 * 
 */
@HTTPService(name = Definitions.SERVICE_SENSORS)
public class SensorService {
	private XMLFormatter _formatter = new XMLFormatter();
	
	/**
	 * 
	 * @param authenticatedUser 
	 * @param xml Only the result data should be in the body. See {@link service.tut.pori.apilta.sensors.datatypes.SensorTask}
	 */
	@HTTPServiceMethod(name = service.tut.pori.tasks.Definitions.METHOD_TASK_FINISHED, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void taskFinished (
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml
			) 
	{
		SensorsCore.taskFinished(authenticatedUser.getUserIdentity(), _formatter.toObject(xml.getValue(), SensorTask.class));
	}
	
	/**
	 * 
	 * @param authenticatedUser 
	 * @param taskId
	 * @param backendId
	 * @param dataGroups
	 * @param limits paging limits
	 * @return response See {@link service.tut.pori.apilta.sensors.datatypes.SensorTask}
	 */
	@HTTPServiceMethod(name = service.tut.pori.tasks.Definitions.METHOD_QUERY_TASK_DETAILS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response queryTaskDetails (
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = service.tut.pori.tasks.Definitions.PARAMETER_BACKEND_ID) LongParameter backendId,
			@HTTPMethodParameter(name = service.tut.pori.tasks.Definitions.PARAMETER_TASK_ID) StringParameter taskId,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits
			) 
	{
		return new Response(TasksCore.retrieveTask(authenticatedUser.getUserIdentity(), backendId.getValue(), dataGroups, limits, taskId.getValue()));
	}
	
	/**
	 * Implementation of create task
	 * @param authenticatedUser 
	 * @param xml Only the workload data should be in the body. See {@link service.tut.pori.apilta.sensors.datatypes.SensorTask}
	 * @return {@link service.tut.pori.tasks.datatypes.TaskList} with identifier for the generated task
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_CREATE_TASK, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response createTask (
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml
			) 
	{
		String taskId = SensorsCore.createTask(authenticatedUser.getUserIdentity(), _formatter.toObject(xml.getValue(), SensorTask.class));
		if(taskId == null){
			return new Response(Status.FORBIDDEN);
		}else{
			TaskList taskList = new TaskList();
			Task task = new SensorTask();
			task.addTaskId(taskId);
			taskList.addTask(task);
			return new Response(taskList);
		}
	}
	
	/**
	 * Implementation of modify task
	 * @param authenticatedUser 
	 * @param xml Only the workload data should be in the body. See {@link service.tut.pori.apilta.sensors.datatypes.SensorTask}
	 * @return {@link service.tut.pori.tasks.datatypes.TaskList} with identifier for the generated task
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_MODIFY_TASK, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response modifyTask (
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml
			) 
	{
		String taskId = SensorsCore.modifyTask(authenticatedUser.getUserIdentity(), _formatter.toObject(xml.getValue(), SensorTask.class));
		if(taskId == null){
			return new Response(Status.FORBIDDEN);
		}else{
			TaskList taskList = new TaskList();
			Task task = new SensorTask();
			task.addTaskId(taskId);
			taskList.addTask(task);
			return new Response(taskList);
		}
	}
	
	/**
	 * Implementation of create file
	 * @param authenticatedUser 
	 * @param backendId
	 * @param file only the file contents should be in the body.
	 * @return {@link service.tut.pori.apilta.files.datatypes.FileDetailsList} with identifier for the generated file
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_CREATE_FILE, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response createFile (
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = service.tut.pori.tasks.Definitions.PARAMETER_BACKEND_ID) LongParameter backendId,
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter file
			) 
	{
		FileDetails details = SensorsCore.createFile(authenticatedUser.getUserIdentity(), backendId.getValue(), file.getValue());
		if(details == null){
			return new Response(Status.FORBIDDEN);
		}else{
			FileDetailsList list = new FileDetailsList();
			list.addFile(details);
			return new Response(list);
		}
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param taskId
	 * @param backendIdFilter
	 * @param createdFilter
	 * @param dataGroups for valid data groups see {@link service.tut.pori.apilta.sensors.SensorsCore#getMeasurements(core.tut.pori.users.UserIdentity, long[], java.util.Set, DataGroups, Limits, java.util.List, java.util.List)}
	 * @param limits
	 * @param measurementIdFilter
	 * @return see {@link service.tut.pori.apilta.sensors.datatypes.MeasurementList}
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_GET_MEASUREMENTS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response getMeasurements(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = service.tut.pori.tasks.Definitions.PARAMETER_TASK_ID) StringParameter taskId,
			@HTTPMethodParameter(name = service.tut.pori.tasks.Definitions.PARAMETER_BACKEND_ID, required = false) LongParameter backendIdFilter,
			@HTTPMethodParameter(name = service.tut.pori.tasks.Definitions.PARAMETER_CREATED, required = false) DateIntervalParameter createdFilter,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits,
			@HTTPMethodParameter(name = Definitions.PARAMETER_MEASUREMENT_ID, required = false) StringParameter measurementIdFilter
			)
	{
		return new Response(SensorsCore.getMeasurements(authenticatedUser.getUserIdentity(), backendIdFilter.getValues(), createdFilter.getValues(), dataGroups, limits, measurementIdFilter.getValues(), taskId.getValues()));
	}
}
