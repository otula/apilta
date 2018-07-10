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
package service.tut.pori.tasks;

import service.tut.pori.tasks.datatypes.Task;
import service.tut.pori.tasks.utils.TaskUtils;
import core.tut.pori.http.Response;
import core.tut.pori.http.Response.Status;
import core.tut.pori.http.annotations.HTTPAuthenticationParameter;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.AuthenticationParameter;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.DateIntervalParameter;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.LongParameter;
import core.tut.pori.http.parameters.StringParameter;

/**
 * service declarations for the task service
 * 
 */
@HTTPService(name=Definitions.SERVICE_TASKS)
public class TaskService {

	/**
	 * 
	 * @param authenticatedUser 
	 * @param taskId
	 * @param backendId
	 * @param dataGroups
	 * @param limits
	 * @return response with up-to-date details for the requested task
	 * @see service.tut.pori.tasks.datatypes.Task
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_QUERY_TASK_DETAILS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response queryTaskDetails(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_BACKEND_ID) LongParameter backendId,
			@HTTPMethodParameter(name = Definitions.PARAMETER_TASK_ID) StringParameter taskId,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits
			) 
	{
		Task task = TasksCore.retrieveTask(authenticatedUser.getUserIdentity(), backendId.getValue(), dataGroups, limits, taskId.getValue());
		if(task == null){
			return new Response(Status.BAD_REQUEST, "Invalid "+Definitions.PARAMETER_TASK_ID+" or "+Definitions.PARAMETER_BACKEND_ID+".");
		}else{
			return new Response(task);
		}
	}
	
	/**
	 * Note: this will only return the very basic details of a task: the identifier, task name, task description, updated time and user identity, use {@link service.tut.pori.tasks.TaskService#queryTaskDetails(AuthenticationParameter, LongParameter, StringParameter, DataGroups, Limits)} for retrieving the full task details.
	 * @param authenticatedUser 
	 * 
	 * @param backendIdFilter
	 * @param createdFilter 
	 * @param stateFilter 
	 * @param limits 
	 * @return response with a list of tasks for the given back end
	 * @see service.tut.pori.tasks.datatypes.TaskList
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_RETRIEVE_TASKS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response getTasks(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_BACKEND_ID, required = false) LongParameter backendIdFilter,
			@HTTPMethodParameter(name = Definitions.PARAMETER_CREATED, required = false) DateIntervalParameter createdFilter,
			@HTTPMethodParameter(name = Definitions.PARAMETER_TASK_STATE, required = false) StringParameter stateFilter,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits
			)
	{
		return new Response(TasksCore.retrieveTaskList(authenticatedUser.getUserIdentity(), backendIdFilter.getValues(), createdFilter.getValues(), limits, TaskUtils.stringsToStates(stateFilter.getValues())));
	}
}
