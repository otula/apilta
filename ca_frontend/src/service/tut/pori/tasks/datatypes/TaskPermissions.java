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
package service.tut.pori.tasks.datatypes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import core.tut.pori.users.UserIdentity;
import service.tut.pori.backends.datatypes.Backend;
import service.tut.pori.backends.datatypes.BackendUserIdentity.UserPermission;
import service.tut.pori.tasks.datatypes.Task.Visibility;

/**
 * Container for back end / user permissions
 * 
 */
public class TaskPermissions {
	private static final Logger LOGGER = Logger.getLogger(TaskPermissions.class);
	private Visibility _dataVisibility = null;
	private boolean _isTaskOwner = false;
	private HashMap<Long, Set<UserPermission>> _permissions = null;
	private boolean _taskExists = false;
	private String _taskId = null;
	private UserIdentity _userId = null;

	/**
	 * 
	 * @param backendId
	 * @return the permissions or null if no permissions defined in this set
	 */
	public Set<UserPermission> getBackendPermissions(Long backendId) {
		return _permissions.get(backendId);
	}

	/**
	 * @return true if the user is the owner of this task
	 * @see #setTaskOwner(boolean)
	 */
	public boolean isTaskOwner() {
		return _isTaskOwner;
	}

	/**
	 * @return the taskId
	 * @see #setTaskId(String)
	 */
	public String getTaskId() {
		return _taskId;
	}
	
	/**
	 * @return the user of the permission set
	 * @see #setUserId(UserIdentity)
	 */
	public UserIdentity getUserId() {
		return _userId;
	}

	/**
	 * @param isTaskOwner the isTaskOwner to set
	 * @see #getUserId()
	 */
	public void setTaskOwner(boolean isTaskOwner) {
		_isTaskOwner = isTaskOwner;
	}

	/**
	 * @param taskId the taskId to set
	 * @see #getTaskId()
	 */
	public void setTaskId(String taskId) {
		_taskId = taskId;
	}

	/**
	 * @param userId the userId to set
	 * @see #getUserId()
	 */
	public void setUserId(UserIdentity userId) {
		_userId = userId;
	}
	
	/**
	 * @return the dataVisibility
	 * @see #setDataVisibility(Visibility)
	 */
	public Visibility getDataVisibility() {
		return _dataVisibility;
	}

	/**
	 * @param dataVisibility the dataVisibility to set
	 * @see #getDataVisibility()
	 */
	public void setDataVisibility(Visibility dataVisibility) {
		_dataVisibility = dataVisibility;
	}

	/**
	 * 
	 * @param taskId
	 * @param userId
	 */
	public TaskPermissions(String taskId, UserIdentity userId){
		_taskId = taskId;
		_userId = userId;
	}
	
	/**
	 * 
	 * @param backendId
	 * @param permissions if null or empty this will remove the permissions for the back end
	 * @throws IllegalArgumentException on invalid values
	 */
	public void setBackendPermissions(Long backendId, Set<UserPermission> permissions) throws IllegalArgumentException {
		if(backendId == null){
			throw new IllegalArgumentException("Back end id cannot be null.");
		}
		
		if(_permissions == null){
			_permissions = new HashMap<>();
		}
		
		if(permissions == null || permissions.isEmpty()){
			_permissions.remove(backendId);
		}else{
			_permissions.put(backendId, permissions);
		}
	}

	/**
	 * 
	 * @param backends
	 * @param permissions
	 * @return true if all of the given back ends in this collections have all of the given permissions
	 * @throws IllegalArgumentException on invalid arguments
	 */
	public boolean hasPermissions(Collection<? extends Backend> backends, Set<UserPermission> permissions) throws IllegalArgumentException{
		if(backends == null || backends.isEmpty()){
			throw new IllegalArgumentException("No back ends given.");
		}
		if(permissions == null || permissions.isEmpty()){
			throw new IllegalArgumentException("No permissions given.");
		}
		
		if(_permissions == null){
			LOGGER.debug("The permission list is empty.");
			return false;
		}
		
		for(Backend backend : backends){
			Long backendId = backend.getBackendId();
			if(backendId == null){
				LOGGER.warn("Ignored back end without an id.");
				continue;
			}
			
			if(!hasPermissions(backendId, permissions)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @param backendId if null, this will return false
	 * @param permissions
	 * @return true if the given back end has all of the given permissions
	 */
	public boolean hasPermissions(Long backendId, Set<UserPermission> permissions){
		Set<UserPermission> perms = getBackendPermissions(backendId);
		if(perms == null){
			LOGGER.debug("No permissions for back end, id: "+backendId);
			return false;
		}else if(!perms.containsAll(permissions)){
			LOGGER.debug("One or more of the required permissions were not present for back end, id: "+backendId);
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * 
	 * @return true if the user can access the data generated by this task
	 */
	public boolean canAccessData(){
		return (isTaskOwner() || _dataVisibility == Visibility.PUBLIC);
	}

	/**
	 * @return true if the task exists, all permission checks for non-existing task will resolve false
	 * @see #setTaskExists(boolean)
	 */
	public boolean isTaskExists() {
		return _taskExists;
	}

	/**
	 * @param taskExists the taskExists to set
	 * @see #isTaskExists()
	 */
	public void setTaskExists(boolean taskExists) {
		_taskExists = taskExists;
	}
}
