/**
 * Copyright 2017 Tampere University of Technology, Pori Department
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
package otula.backend.tasks;

import org.apache.log4j.Logger;

import otula.backend.parking.ParkingCore;
import otula.backend.tasks.datatypes.SensorTask;
import service.tut.pori.tasks.datatypes.Task;
import service.tut.pori.tasks.datatypes.Task.State;

/**
 * Core methods for tasks
 *
 */
public final class TasksCore {
	private static final Logger LOGGER = Logger.getLogger(TasksCore.class);
	
	/**
	 * 
	 */
	private TasksCore() {
		// nothing needed
	}
	
	/**
	 * 
	 * @param task
	 */
	private static void scheduleTask(SensorTask task) {
		if(!Task.isValid(task) || task.getConditions() == null){ // it is enough to check either conditions or outputs
			throw new IllegalArgumentException("Invalid task.");
		}
		ParkingCore.scheduleTask(task);
	}
	
	/**
	 * 
	 * @param task
	 */
	private static void unscheduleTask(SensorTask task) {
		LOGGER.debug("Ignored task, id: "+task.getTaskIds().iterator().next()+" with state: "+task.getState()); // the tasks are executed immediately, so no need to cancel the schedule, simply ignore the task
	}
	
	/**
	 * Called when a new task is received
	 * 
	 * @param task
	 * @throws IllegalArgumentException on bad data
	 */
	public static void newTask(SensorTask task) throws IllegalArgumentException {
		if(!Task.isValid(task) || task.getConditions() == null){ // it is enough to check either conditions or outputs
			throw new IllegalArgumentException("Invalid task.");
		}
		
		String taskId = task.getTaskIds().iterator().next();
		State state = task.getState();
		if(state != State.ACTIVE){ // remove tasks that are not in the active state
			if(taskId == null){
				throw new IllegalArgumentException("Invalid task id: "+taskId);
			}
			unscheduleTask(task);
		}else{
			scheduleTask(task);
		}
	}
}
