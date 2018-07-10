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
package otula.backend.tasks.datatypes;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import core.tut.pori.http.ResponseData;
import service.tut.pori.tasks.Definitions;

/**
 * lists a list of tasks
 * 
 * @see service.tut.pori.tasks.datatypes.Task
 */
@XmlRootElement(name=Definitions.ELEMENT_TASK_LIST)
@XmlAccessorType(XmlAccessType.NONE)
public class SensorTaskList extends ResponseData {
	@XmlElement(name = Definitions.ELEMENT_TASK)
	private List<SensorTask> _tasks = null;

	/**
	 * @return the tasks
	 * @see #setTasks(List)
	 */
	public List<SensorTask> getTasks() {
		return _tasks;
	}

	/**
	 * @param tasks the tasks to set
	 * @see #getTasks()
	 */
	public void setTasks(List<SensorTask> tasks) {
		_tasks = tasks;
	}
	
	/**
	 * 
	 * @param task
	 * @see #getTasks()
	 */
	public void addTask(SensorTask task) {
		if(_tasks == null){
			_tasks = new ArrayList<>();
		}
		_tasks.add(task);
	}
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if this list does not contain any tasks
	 * @see #isEmpty(SensorTaskList)
	 */
	protected boolean isEmpty() {
		return (_tasks == null || _tasks.isEmpty());
	}
	
	/**
	 * 
	 * @param taskList
	 * @return true if the list is null or empty
	 */
	public static boolean isEmpty(SensorTaskList taskList) {
		if(taskList == null){
			return true;
		}else{
			return taskList.isEmpty();
		}
	}
}
