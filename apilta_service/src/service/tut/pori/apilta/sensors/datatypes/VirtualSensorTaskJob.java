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
package service.tut.pori.apilta.sensors.datatypes;

import org.apache.commons.lang3.NotImplementedException;
import org.quartz.JobBuilder;

import service.tut.pori.tasks.TaskJob;
import service.tut.pori.tasks.datatypes.Task;

/**
 * job class for executing virtual sensor tasks
 * 
 */
public class VirtualSensorTaskJob extends TaskJob {

	@Override
	protected void executeTask() {
		//TODO go through the public (or the tasks the user has permissions to access) tasks and the tasks that have used the selected back ends, create result sets with matching values
		throw new NotImplementedException("Not implemented");
	}
	
	/**
	 * This method will return a builder initialized with the default values set in the task. 
	 * 
	 * This basic implementation only sets the task identifier.
	 * 
	 * Note: the given task must have a single unique identifier.
	 * 
	 * @param task the task used for initialization
	 * @return job builder
	 * @throws IllegalArgumentException if the task contains invalid data for builder initialization
	 */
	public static JobBuilder getBuilder(Task task)  throws IllegalArgumentException {
		return getBuilder(task, VirtualSensorTaskJob.class);
	}
}
