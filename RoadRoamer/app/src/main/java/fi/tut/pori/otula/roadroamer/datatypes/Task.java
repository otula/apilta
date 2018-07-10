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
package fi.tut.pori.otula.roadroamer.datatypes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * abstract base class for tasks
 * 
 */
public abstract class Task {
    private Date _created = null;
	private String _description = null;
	private String _name = null;
	private List<Long> _taskIds = null;
	private Set<String> _taskTypes = null;
	private Date _updated = null;
	private Long _userId = null;
	private List<Long> _backends = null;
    private String _callbackUri = null;

	/**
	 * @return the callbackUri or null if there is no need to respond to the task
	 */
	public String getCallbackUri(){
        return _callbackUri;
    }

    public void setCallbackUri(String callbackUri){
        _callbackUri = callbackUri;
    }

	/**
	 * @return the taskIds
	 * @see #setTaskIds(List)
	 */
	public List<Long> getTaskIds() {
		return _taskIds;
	}

	/**
	 * @param taskIds the taskIds to set
	 * @see #getTaskIds()
	 */
	public void setTaskIds(List<Long> taskIds) {
		_taskIds = taskIds;
	}

	/**
	 * @return the identity of the user who created this task or null if the task created by the system (not by a particular user)
	 * @see #setUserId(Long)
	 */
	public Long getUserId() {
		return _userId;
	}
	
	/**
	 * 
	 * @param taskId
	 * @see #getTaskIds()
	 */
	public void addTaskId(Long taskId) {
		if(_taskIds == null) {
			_taskIds = new ArrayList<>();
		}
		_taskIds.add(taskId);
	}

	/**
	 * @param userId the identity of the user who created this task or null if the task created by the system (not by a particular user)
	 * @see #getUserId()
	 */
	public void setUserId(Long userId) {
		_userId = userId;
	}

	/**
	 * @return the list of all back ends participating in the execution of this task
	 * @see #setBackends(List)
	 */
	public List<Long> getBackends() {
		return _backends;
	}

	/**
	 * @param backends the list of all back ends participating in the execution of this task
	 * @see #getBackends()
	 */
	public void setBackends(List<Long> backends) {
		_backends = backends;
	}

	/**
	 * @return the updated
	 * @see #setUpdated(Date)
	 */
	public Date getUpdated() {
		return _updated;
	}

	/**
	 * @param updated the updated to set
	 * @see #getUpdated()
	 */
	public void setUpdated(Date updated) {
		_updated = updated;
	}

	/**
	 * @return the created
	 * @see #setCreated(Date)
	 */
	public Date getCreated() {
		return _created;
	}

	/**
	 * @param created the created to set
	 * @see #getCreated()
	 */
	public void setCreated(Date created) {
		_created = created;
	}

	/**
	 * @return the taskTypes
	 * @see #setTaskTypes(Set)
	 */
	public Set<String> getTaskTypes() {
		return _taskTypes;
	}

	/**
	 * @param taskTypes the taskTypes to set
	 * @see #getTaskTypes()
	 */
	public void setTaskTypes(Set<String> taskTypes) {
		_taskTypes = taskTypes;
	}

	/**
	 * @return the description
	 * @see #setDescription(String)
	 */
	public String getDescription() {
		return _description;
	}

	/**
	 * @param description the description to set
	 * @see #getDescription()
	 */
	public void setDescription(String description) {
		_description = description;
	}

	/**
	 * @return the name
	 * @see #setName(String)
	 */
	public String getName() {
		return _name;
	}

	/**
	 * @param name the name to set
	 * @see #getName()
	 */
	public void setName(String name) {
		_name = name;
	}
	
	/**
	 * copy constructor for creating a shallow copy of the given object
	 * 
	 * @param task
	 */
	public Task(Task task) {
		super();
		_created = task._created;
		_description = task._description;
		_name = task._name;
		_taskIds = task._taskIds;
		_taskTypes = task._taskTypes;
		_updated = task._updated;
		_userId = task._userId;
		_backends = task._backends;
        _callbackUri = task._callbackUri;
	}
	
	/**
	 * 
	 */
	public Task() {
		super();
	}

}
