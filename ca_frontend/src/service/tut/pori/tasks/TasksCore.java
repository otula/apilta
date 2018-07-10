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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.springframework.context.ApplicationListener;

import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.DateIntervalParameter.Interval;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserEvent.EventType;
import core.tut.pori.users.UserIdentity;
import service.tut.pori.backends.BackendDAO;
import service.tut.pori.backends.BackendsCore;
import service.tut.pori.backends.datatypes.BackendEvent;
import service.tut.pori.backends.datatypes.BackendUserIdentity.UserPermission;
import service.tut.pori.tasks.datatypes.Task;
import service.tut.pori.tasks.datatypes.Task.State;
import service.tut.pori.tasks.datatypes.TaskList;
import service.tut.pori.tasks.datatypes.TaskPermissions;
import service.tut.pori.users.UserCore;
import service.tut.pori.users.UserServiceEvent;

/**
 * the task scheduler
 * 
 */
public class TasksCore {
	private static final EnumSet<UserPermission> ENUMSET_AUTH_BACKENDS = EnumSet.of(UserPermission.AUTH_BACKENDS);
	private static final Logger LOGGER = Logger.getLogger(TasksCore.class);
	
	/**
	 * 
	 */
	private TasksCore() {
		// nothing needed
	}
	
	/**
	 * 
	 * @param authenticatedUser 
	 * @param backendId
	 * @param dataGroups
	 * @param limits
	 * @param taskId
	 * @return the task or null if task was not found
	 */
	public static Task retrieveTask(UserIdentity authenticatedUser, Long backendId, DataGroups dataGroups, Limits limits, String taskId) {
		Class<? extends TaskDAO> daoClass = ServiceInitializer.getDAOHandler().getDAO(TaskDAO.class).getDAOClass(taskId); // resolve task and DAO type
		if(daoClass == null){
			LOGGER.warn("Task not found, task id: "+taskId);
			return null;
		}else{
			TaskDAO dao = ServiceInitializer.getDAOHandler().getDAO(daoClass);
			
			TaskPermissions permissions = dao.getTaskPermissions(true, taskId, authenticatedUser);
			if(!permissions.isTaskExists()){
				throw new IllegalArgumentException("Invalid task identifiers.");
			}else if(!permissions.hasPermissions(backendId, ENUMSET_AUTH_BACKENDS)){ // check that the user can really authenticate as the given back end
				throw new IllegalArgumentException("Invalid back end identifier.");
			}
			
			return dao.getTask(backendId, dataGroups, limits, taskId);
		}
	}
	
	/**
	 * Note: this will only return the very basic details of a task: the identifier, task name, task description, updated time and user identity, use {@link #retrieveTask(UserIdentity, Long, DataGroups, Limits, String)} for retrieving the full task details.
	 * 
	 * @param authenticatedUser 
	 * @param backendIdFilter optional back end id filter. The authenticated user must have the permission {@link UserPermission#AUTH_BACKENDS} for accessing the listed back ends. If no ids are given this will list all tasks created by the authenticated user.
	 * @param createdFilter can be used to filter the result based on predefined time intervals
	 * @param limits
	 * @param stateFilter optional state filter, if no values are given, all tasks will be returned
	 * @return list of tasks or null if no tasks exists for the given back end
	 * @throws IllegalArgumentException on invalid parameters
	 */
	public static TaskList retrieveTaskList(UserIdentity authenticatedUser, long[] backendIdFilter, Set<Interval> createdFilter, Limits limits, Set<State> stateFilter) throws IllegalArgumentException {
		long[] userIds = null;
		if(ArrayUtils.isEmpty(backendIdFilter)){ // use the current user as the filter, i.e. retrieve only tasks created by the authenticated user
			userIds = new long[]{authenticatedUser.getUserId()};
		}else{ // check that the user has access to the requested back ends
			BackendDAO bDAO = ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class);
			for(long backendId : backendIdFilter){
				Set<UserPermission> permissions = bDAO.getBackendPermissions(backendId, authenticatedUser);
				if(permissions == null || !permissions.contains(UserPermission.AUTH_BACKENDS)){
					throw new IllegalArgumentException("Back end, id: "+backendId+" does not exist or permission was denied.");
				}
			}
		}
		
		return ServiceInitializer.getDAOHandler().getDAO(TaskDAO.class).getTaskList(backendIdFilter, createdFilter, limits, stateFilter, userIds);
	}
	
	/**
	 * Schedule the given task at the earliest possible time. This will also add the task into the database tables.
	 * 
	 * If the task does not contain an identifier (taskId), the task is added as a new task. If identifier is present, this is assumed to be an existing task, and it is simply re-scheduled with the provided updated details.
	 * 
	 * If the task has multiple identifiers, this will throw an exception.
	 * 
	 * @param task
	 * @return the task identifier of the scheduled task or null on failure
	 * @throws IllegalArgumentException if the schedule failed
	 */
	public static String scheduleTask(Task task) throws IllegalArgumentException {
		if(!Task.isValid(task)){
			throw new IllegalArgumentException("Invalid task.");
		}
		
		List<String> taskIds = task.getTaskIds();
		String newTaskId = null;
		if(taskIds == null || taskIds.isEmpty()){
			newTaskId = task.getTaskDao().createTask(task);
			LOGGER.debug("Created new task with id: "+newTaskId);
		}else if(taskIds.size() != 1){
			throw new IllegalArgumentException("Multiple task Ids are not allowed.");
		}else if(task.getTaskDao().updateTask(task)){
			newTaskId = taskIds.iterator().next();
			LOGGER.debug("Updated task, id: "+newTaskId);
		}else{
			LOGGER.warn("Failed to update task, id: "+taskIds.iterator().next()+": task does not exist.");
			return null;
		}
		
		try {
			ServiceInitializer.getExecutorHandler().getScheduler().scheduleJob(task.getBuilder().build(), TriggerBuilder.newTrigger().startNow().build());
		} catch (SchedulerException ex) { // in general, this should not happen
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Task was created/updated, but could not be scheduled with the given arguments. Task id: "+newTaskId);
		}
		
		return newTaskId;
	}
	
	/**
	 * Event listener for user related events.
	 * 
	 * Automatically instantiated by Spring as a bean.
	 */
	@SuppressWarnings("unused")
	private static class UserEventListener implements ApplicationListener<UserServiceEvent>{

		@Override
		public void onApplicationEvent(UserServiceEvent event) {
			EventType type = event.getType();
			if(type == EventType.USER_REMOVED && event.getSource().equals(UserCore.class)){
				UserIdentity userId = event.getUserId();
				LOGGER.debug("Detected event of type "+type.name()+", removing tasks for user, id: "+userId.getUserId());
				TaskDAO dao = ServiceInitializer.getDAOHandler().getDAO(TaskDAO.class);
				
				List<String> taskIds = dao.getTaskIds(userId); // use the basic task dao to resolve list of task ids
				if(taskIds == null){
					LOGGER.debug("No tasks for user, id: "+userId.getUserId());
					return;
				}
				
				for(String taskId : taskIds) {
					Class<? extends TaskDAO> daoClass = dao.getDAOClass(taskId); // resolve the class for the dao
					if(daoClass == null){
						LOGGER.warn("Not removing task: could not resolve DAO for task, id: "+taskId);
						continue;
					}
					TaskDAO taskDAO = ServiceInitializer.getDAOHandler().getDAO(daoClass); // get the applicable dao for the task type 
					if(taskDAO == null){
						LOGGER.warn("Not removing task: could not find DAO for task, id: "+taskId);
					}else{
						taskDAO.removeTask(taskId);
					}
				}
				LOGGER.debug("Tasks removed for user, id: "+userId.getUserId());
			}
		}
	} // class UserEventListener
	
	/**
	 * Event listener for back end related events.
	 * 
	 * Automatically instantiated by Spring as a bean.
	 */
	@SuppressWarnings("unused")
	private static class BackendListener implements ApplicationListener<BackendEvent>{

		@Override
		public void onApplicationEvent(BackendEvent event) {
			service.tut.pori.backends.datatypes.BackendEvent.EventType type = event.getType();
			if(type == service.tut.pori.backends.datatypes.BackendEvent.EventType.BACKEND_REMOVED && event.getSource().equals(BackendsCore.class)){
				Long backendId = event.getBackendId();
				LOGGER.debug("Detected event of type "+type.name()+", removing back end, id: "+backendId+" from all tasks.");
				
				ServiceInitializer.getDAOHandler().getDAO(TaskDAO.class).removeBackend(backendId);
				
				LOGGER.debug("Back end removed from tasks, back end id: "+backendId);
			}
		}
	} // class BackendListener
}
