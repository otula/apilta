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

/**
 * Definitions for tasks
 * 
 */
public final class Definitions {
	/* xml elements */
	/** xml element declaration */
	public static final String ELEMENT_ABSTRACT_TASK = "abstractTask";
	/** xml element declaration */
	public static final String ELEMENT_CALLBACK_URI = "callbackUri";
	/** xml element declaration */
	public static final String ELEMENT_CREATED_TIMESTAMP = "createdTimestamp";
	/** xml element declaration */
	public static final String ELEMENT_DATA_VISIBILITY = "dataVisibility";
	/** xml element declaration */
	public static final String ELEMENT_DESCRIPTION = "description";
	/** xml element declaration */
	public static final String ELEMENT_MESSAGE = "message";
	/** xml element declaration */
	public static final String ELEMENT_NAME = "name";
	/** xml element declaration */
	public static final String ELEMENT_TASK = "task";
	/** xml element declaration */
	public static final String ELEMENT_TASK_ID = "taskId";
	/** xml element declaration */
	public static final String ELEMENT_TASK_ID_LIST = "taskIdList";
	/** xml element declaration */
	public static final String ELEMENT_TASK_LIST = "taskList";
	/** xml element declaration */
	public static final String ELEMENT_TASK_STATE = "taskState";
	/** xml element declaration */
	public static final String ELEMENT_TASK_STATUS = "taskStatus";
	/** xml element declaration */
	public static final String ELEMENT_TASK_TYPE = "taskType";
	/** xml element declaration */
	public static final String ELEMENT_TASK_TYPE_LIST = "taskTypeList";
	/** xml element declaration */
	public static final String ELEMENT_UPDATED_TIMESTAMP = "updatedTimestamp";
	
	/* methods */
	/** implemented by analysis back ends */
	public static final String METHOD_ADD_TASK = "addTask";
	/** implemented by front end */
	public static final String METHOD_QUERY_TASK_DETAILS = "queryTaskDetails";
	/** implemented by analysis back ends */
	public static final String METHOD_QUERY_TASK_STATUS = "queryTaskStatus";
	/** implemented by front end */
	public static final String METHOD_RETRIEVE_TASKS = "getTasks";
	/** implemented by back ends */
	public static final String METHOD_TASK_FINISHED = "taskFinished";
	
	/* parameters */
	/** service method parameter declaration */
	public static final String PARAMETER_BACKEND_ID = "backend_id";
	/** service method parameter declaration */
	public static final String PARAMETER_CREATED = "created";
	/** service method parameter declaration */
	public static final String PARAMETER_TASK_ID = "task_id";
	/** service method parameter declaration */
	public static final String PARAMETER_TASK_STATE = "task_state";
	
	/* services */
	/** service name declaration */
	public static final String SERVICE_TASKS = "tasks";
	
	/* task types */
	/** task is a virtual task and is not to be sent to any back ends */
	public static final String TASK_TYPE_VIRTUAL = "VIRTUAL";
	
	/* visibility string */
	/** visibility for limited access */
	public static final String VISIBILITY_LIMITED = "LIMITED";
	/** visibility for private access */
	public static final String VISIBILITY_PRIVATE = "PRIVATE";
	/** visibility for public access */
	public static final String VISIBILITY_PUBLIC = "PUBLIC";
	
	/* task state string */
	/** task state for archived task */
	public static final String TASK_STATE_ARCHIVED = "ARCHIVED";
	/** task state for active task */
	public static final String TASK_STATE_ACTIVE = "ACTIVE";
	/** task state for inactive task */
	public static final String TASK_STATE_INACTIVE = "INACTIVE";
	
	/* task status string */
	/** task status for completed task */
	public static final String TASK_STATUS_COMPLETED = "COMPLETED";
	/** task status for task, which finished with an error */
	public static final String TASK_STATUS_ERROR = "ERROR";
	/** task status for task that is currently being executed */
	public static final String TASK_STATUS_EXECUTING = "EXECUTING";
	/** task status for a task that has not yet started */
	public static final String TASK_STATUS_NOT_STARTED = "NOT_STARTED";
	/** task status for a task that has been delivered, but has not yet started execution */
	public static final String TASK_STATUS_PENDING = "PENDING";
	/** task is in an unknown state */
	public static final String TASK_STATUS_UNKNOWN = "UNKNOWN";
	
	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
