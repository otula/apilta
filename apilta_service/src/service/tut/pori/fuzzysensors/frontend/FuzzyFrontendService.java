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
package service.tut.pori.fuzzysensors.frontend;

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
import core.tut.pori.http.parameters.StringParameter;
import core.tut.pori.utils.XMLFormatter;
import service.tut.pori.apilta.sensors.datatypes.SensorTask;
import service.tut.pori.apilta.sensors.reference.SensorsReferenceCore;
import service.tut.pori.fuzzysensors.Definitions;
import service.tut.pori.fuzzysensors.FuzzyTask;
import service.tut.pori.tasks.datatypes.Task;
import service.tut.pori.tasks.datatypes.TaskList;

/**
 * fuzzy declarations for the fuzzy front end service
 *
 */
@HTTPService(name=Definitions.SERVICE_FUZZY_SENSORS_FRONTEND)
public class FuzzyFrontendService {
	private XMLFormatter _formatter = new XMLFormatter();
	
	/**
	 * 
	 * @param authenticatedUser 
	 * @param xml Only the result data should be in the body. See {@link service.tut.pori.fuzzysensors.FuzzyTask}
	 */
	@HTTPServiceMethod(name = service.tut.pori.tasks.Definitions.METHOD_TASK_FINISHED, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void taskFinished (
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser, // require authentication, but accept any known user
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml
			) 
	{
		SensorsReferenceCore.taskFinished(authenticatedUser.getUserIdentity(), _formatter.toObject(xml.getValue(), FuzzyTask.class)); // simply dump it into the default reference implementation, we do not process the task finished anyway
	}
	
	/**
	 * 
	 * @param authenticatedUser 
	 * @param taskId
	 * @param backendId
	 * @param dataGroups
	 * @param limits paging limits
	 * @return response See {@link service.tut.pori.fuzzysensors.FuzzyTask}
	 */
	@HTTPServiceMethod(name = service.tut.pori.tasks.Definitions.METHOD_QUERY_TASK_DETAILS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response queryTaskDetails (
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser, // require authentication, but accept any known user
			@HTTPMethodParameter(name = service.tut.pori.tasks.Definitions.PARAMETER_BACKEND_ID) LongParameter backendId,
			@HTTPMethodParameter(name = service.tut.pori.tasks.Definitions.PARAMETER_TASK_ID) StringParameter taskId,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits
			) 
	{
		return new Response(FuzzyFrontendCore.queryTaskDetails(authenticatedUser.getUserIdentity(), backendId.getValue(), taskId.getValue(), dataGroups, limits));
	}
	
	/**
	 * Implementation of create task for fuzzy sensors
	 * 
	 * @param authenticatedUser 
	 * @param xml Only the workload data should be in the body. See {@link service.tut.pori.fuzzysensors.FuzzyTask}
	 * @return {@link service.tut.pori.tasks.datatypes.TaskList} with identifier for the generated task
	 */
	@HTTPServiceMethod(name = service.tut.pori.apilta.sensors.Definitions.METHOD_CREATE_TASK, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response createTask (
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser, // require authentication, but accept any known user
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml
			)
	{
		String taskId = FuzzyFrontendCore.createTask(authenticatedUser.getUserIdentity(), _formatter.toObject(xml.getValue(), FuzzyTask.class));
		TaskList taskList = new TaskList();
		Task task = new SensorTask();
		task.addTaskId(taskId);
		taskList.addTask(task);
		return new Response(taskList);
	}
}
