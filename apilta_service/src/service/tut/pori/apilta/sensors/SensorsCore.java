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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationListener;

import service.tut.pori.apilta.files.FilesCore;
import service.tut.pori.apilta.files.datatypes.FileDetails;
import service.tut.pori.apilta.sensors.datatypes.DataPoint;
import service.tut.pori.apilta.sensors.datatypes.Measurement;
import service.tut.pori.apilta.sensors.datatypes.MeasurementList;
import service.tut.pori.apilta.sensors.datatypes.SensorTask;
import service.tut.pori.backends.BackendDAO;
import service.tut.pori.backends.BackendsCore;
import service.tut.pori.backends.datatypes.BackendEvent;
import service.tut.pori.backends.datatypes.BackendUserIdentity.UserPermission;
import service.tut.pori.tasks.TaskDAO;
import service.tut.pori.tasks.TasksCore;
import service.tut.pori.tasks.datatypes.TaskBackend;
import service.tut.pori.tasks.datatypes.TaskBackend.Status;
import service.tut.pori.tasks.datatypes.TaskPermissions;
import service.tut.pori.users.UserDAO;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.DateIntervalParameter.Interval;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserGroup.Permission;
import core.tut.pori.users.UserIdentity;

/**
 * the core methods for sensor service
 * 
 */
public final class SensorsCore {
	private static final EnumSet<UserPermission> ENUMSET_AUTH_BACKENDS = EnumSet.of(UserPermission.AUTH_BACKENDS);
	private static final Logger LOGGER = Logger.getLogger(SensorsCore.class);
	
	/**
	 * 
	 */
	private SensorsCore() {
		// nothing needed
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param task
	 * @throws IllegalArgumentException on invalid task
	 */
	public static void taskFinished(UserIdentity authenticatedUser, SensorTask task) throws IllegalArgumentException {
		List<String> taskIds = task.getTaskIds();
		if(taskIds == null || taskIds.isEmpty()){
			throw new IllegalArgumentException("Invalid task: task identifier missing.");
		}
		
		if(!SensorTask.isValid(task) || task.getConditions() != null){ // it is enough to check that either conditions or output is not present as the validity is checked through isValid()
			throw new IllegalArgumentException("Invalid task.");
		}
		
		List<TaskBackend> backends = task.getBackends();
		
		ArrayList<String> validTaskIds = new ArrayList<>(taskIds.size());
		SensorTaskDAO taskDAO = task.getTaskDao();
		for(String taskId : taskIds){ // check that the user has permissions to modify the tasks
			TaskPermissions permissions = taskDAO.getTaskPermissions(true, taskId, authenticatedUser);
			if(!permissions.isTaskExists()){
				LOGGER.warn("Ignored non-existing task, id: "+taskId+" for user, id: "+authenticatedUser.getUserId());  // simply ignore tasks that do not exist
			}else if(!permissions.hasPermissions(backends, ENUMSET_AUTH_BACKENDS)){ // check that the user can really authenticate as every one of the given back ends
				throw new IllegalArgumentException("Invalid back end identifiers.");
			}else{
				validTaskIds.add(taskId);
			}
		}
		
		if(validTaskIds.isEmpty()){
			throw new IllegalArgumentException("Invalid task: no valid task identifiers.");
		}
		
		for(TaskBackend backend : backends) { // check that the user has permissions to provide data for the reported back ends
			Long backendId = backend.getBackendId();
			for(String taskId : validTaskIds){
				if(!taskDAO.statusUpdated(backend, taskId)){ // update the status information for the back end
					throw new IllegalArgumentException("Failed to update status for back end, id: "+backendId+", for task, id: "+taskId); // something is wrong if this fails, to do no allow to proceed
				}
			}
		}
		
		SensorsDAO sensorsDAO = ServiceInitializer.getDAOHandler().getDAO(SensorsDAO.class);
		List<Measurement> measurements = task.getMeasurements().getMeasurements();
		for(Measurement measurement : measurements){ // check that the file GUIDs (if present) has been associated with the given back ends (i.e. the back end has previously uploaded the file)
			Long backendId = measurement.getBackendId();
			for(DataPoint dp : measurement.getDataPoints()){ 
				if(Definitions.DATA_POINT_KEY_FILE_GUID.equals(dp.getKey()) && !sensorsDAO.backendHasGUID(backendId, dp.getValue())){
					throw new IllegalArgumentException("File GUID: "+dp.getValue()+" is not associated with back end, id: "+backendId);
				} // if
			} // for
		}
		
		sensorsDAO.addMeasurements(measurements, validTaskIds); // finally, add the measurements for all valid identifiers
	}

	/**
	 * Status information for back ends (if given) is ignored and set to {@link service.tut.pori.tasks.datatypes.TaskBackend.Status#NOT_STARTED}
	 * 
	 * Task identifier, if given is ignored and a new identifier is generated.
	 * 
	 * @param authenticatedUser
	 * @param task
	 * @return identifier for the created task or null on failure (permission denied)
	 * @throws IllegalArgumentException on invalid task
	 */
	public static String createTask(UserIdentity authenticatedUser, SensorTask task) throws IllegalArgumentException {
		List<TaskBackend> backends = task.getBackends();
		if(backends == null){
			throw new IllegalArgumentException("Invalid task: no back ends.");
		}
		BackendDAO backendDAO = ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class);
		for(TaskBackend backend : backends){ // reset all status information to not started before task validation
			Long backendId = backend.getBackendId();
			Set<UserPermission> permissions = backendDAO.getBackendPermissions(backendId, authenticatedUser);
			if(permissions == null || !permissions.contains(UserPermission.TASKS)){
				LOGGER.warn("User, id: "+authenticatedUser.getUserId()+" is not allowed to create tasks for back end, id: "+backendId);
				return null;
			}
			backend.setStatus(Status.NOT_STARTED);
		}
		
		List<String> taskIds = task.getTaskIds();
		if(taskIds != null && !taskIds.isEmpty()){
			LOGGER.debug("Ignoring task ids for new task.");
		}
		task.setTaskIds(null);
		
		if(!SensorTask.isValid(task) || task.getConditions() == null){ // it is enough to check that either conditions or output exists as the validity is checked through isValid()
			throw new IllegalArgumentException("Invalid task.");
		}
		
		UserIdentity target = task.getUserId();
		if(UserIdentity.isValid(target)){
			if(!ServiceInitializer.getDAOHandler().getDAO(UserDAO.class).hasPermission(authenticatedUser, target, Permission.MODIFY_USERS)){
				LOGGER.warn("User, id: "+authenticatedUser.getUserId()+" attempted to modify user, id: "+target.getUserId()+" without permission "+Permission.MODIFY_USERS.name());
				return null;
			}
		}else{
			LOGGER.debug("No user id for the task, defaulting to the authenticated user.");
			task.setUserId(authenticatedUser);
		}
		
		Set<String> taskTypes = task.getTaskTypes();
		if(taskTypes == null || taskTypes.isEmpty()){
			throw new IllegalArgumentException("Invalid task: no task type.");
		}
		
		return TasksCore.scheduleTask(task);
	}
	
	/**
	 * 
	 * Task identifier, if given is ignored and a new identifier is generated.
	 * 
	 * @param authenticatedUser
	 * @param task
	 * @return identifier for the created task or null on failure (permission denied)
	 * @throws IllegalArgumentException on invalid task
	 */
	public static String modifyTask(UserIdentity authenticatedUser, SensorTask task) throws IllegalArgumentException {
		List<String> taskIds = task.getTaskIds();
		if(taskIds == null || taskIds.size() != 1){
			throw new IllegalArgumentException("Invalid task: the task must have exactly one task identifier.");
		}
		
		String taskId = taskIds.iterator().next();
		TaskPermissions taskPermissions = task.getTaskDao().getTaskPermissions(false, taskId, authenticatedUser);
		if(!taskPermissions.isTaskExists()){
			throw new IllegalArgumentException("Invalid task: the task does not exist, id: "+taskId);
		}else if(!taskPermissions.isTaskOwner()){
			LOGGER.warn("User, id: "+authenticatedUser.getUserId()+" is not allowed to modify task, id: "+taskId);
			return null;
		}
		
		List<TaskBackend> backends = task.getBackends();
		if(backends == null){
			throw new IllegalArgumentException("Invalid task: no back ends.");
		}
		
		BackendDAO backendDAO = ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class);
		for(TaskBackend backend : backends){ // reset all status information to not started before task validation
			Long backendId = backend.getBackendId();
			Set<UserPermission> permissions = backendDAO.getBackendPermissions(backendId, authenticatedUser);
			if(permissions == null || !permissions.contains(UserPermission.TASKS)){
				LOGGER.warn("User, id: "+authenticatedUser.getUserId()+" is not allowed to use tasks for back end, id: "+backendId);
				return null;
			}
		}
		
		if(!SensorTask.isValid(task) || task.getConditions() == null){ // it is enough to check that either conditions or output exists as the validity is checked through isValid()
			throw new IllegalArgumentException("Invalid task.");
		}
		
		UserIdentity target = task.getUserId();
		if(!UserIdentity.isValid(target)){
			throw new IllegalArgumentException("Invalid task: task must have a user.");
		}
		
		if(!ServiceInitializer.getDAOHandler().getDAO(UserDAO.class).hasPermission(authenticatedUser, target, Permission.MODIFY_USERS)){
			LOGGER.warn("User, id: "+authenticatedUser.getUserId()+" attempted to modify user, id: "+target.getUserId()+" without permission "+Permission.MODIFY_USERS.name());
			return null;
		}
		
		Set<String> taskTypes = task.getTaskTypes();
		if(taskTypes == null || taskTypes.isEmpty()){
			throw new IllegalArgumentException("Invalid task: no task type.");
		}
		
		return TasksCore.scheduleTask(task);
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param backendId 
	 * @param file
	 * @return details for the created file or null on failure (permission denied)
	 * @throws IllegalArgumentException on bad data
	 */
	public static FileDetails createFile(UserIdentity authenticatedUser, Long backendId, InputStream file) throws IllegalArgumentException {
		Set<UserPermission> permissions = ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class).getBackendPermissions(backendId, authenticatedUser);
		if(permissions == null || !permissions.contains(UserPermission.AUTH_BACKENDS)){
			LOGGER.warn("User, id: "+authenticatedUser.getUserId()+" does not have permission "+UserPermission.AUTH_BACKENDS.name());
			return null;
		}
		
		FileDetails details = FilesCore.createFile(file);
		if(!FileDetails.isValid(details)){
			throw new IllegalArgumentException("Failed to create file from the given data.");
		}
		
		String guid = details.getGUID();
		LOGGER.debug("Associating file, guid: "+guid+" with back end, id: "+backendId);
		ServiceInitializer.getDAOHandler().getDAO(SensorsDAO.class).addFile(backendId, guid);
		
		return details;
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param backendIdFilter
	 * @param createdFilter
	 * @param dataGroups valid data groups are: {@value DataGroups#DATA_GROUP_ALL}, {@value DataGroups#DATA_GROUP_BASIC} (only the measurement details are given without data points, this is the default), {@value Definitions#DATA_GROUP_DATA_POINTS} (data points included in the response)
	 * @param limits
	 * @param measurementIdFilter
	 * @param taskIds
	 * @return list of measurements or null if none
	 * @throws IllegalArgumentException on invalid parameters
	 */
	public static MeasurementList getMeasurements(UserIdentity authenticatedUser, long[] backendIdFilter, Set<Interval> createdFilter, DataGroups dataGroups, Limits limits, List<String> measurementIdFilter, List<String> taskIds) throws IllegalArgumentException {
		TaskDAO taskDAO = ServiceInitializer.getDAOHandler().getDAO(TaskDAO.class);
		for(String taskId : taskIds){
			TaskPermissions permissions = taskDAO.getTaskPermissions(false, taskId, authenticatedUser);
			if(!permissions.canAccessData()){
				throw new IllegalArgumentException("Task, id: "+taskId+" was not found or permission was denied.");
			}
		}
		
		MeasurementList measurementList =  ServiceInitializer.getDAOHandler().getDAO(SensorsDAO.class).getMeasurements(backendIdFilter, createdFilter, dataGroups, limits, measurementIdFilter, taskIds);
		resolveFileUrls(measurementList);
		return measurementList;
	}
	
	/**
	 * resolve proper url's for all applicable data points in the measurement list
	 * 
	 * @param measurementList
	 */
	private static void resolveFileUrls(MeasurementList measurementList) {
		if(MeasurementList.isEmpty(measurementList)){
			LOGGER.debug("Empty measurement list.");
		}else{
			LOGGER.debug("Resolving data point values: "+Definitions.DATA_POINT_KEY_FILE_GUID+" to "+Definitions.DATA_POINT_KEY_FILE_DETAILS_URL);
			
			for(Measurement m : measurementList.getMeasurements()){
				List<DataPoint> dataPoints = m.getDataPoints();
				if(dataPoints == null){
					LOGGER.debug("No data points for measurement, id: "+m.getMeasurementId());
				}else{
					for(DataPoint dp : m.getDataPoints()){
						if(Definitions.DATA_POINT_KEY_FILE_GUID.equals(dp.getKey())){
							dp.setKey(Definitions.DATA_POINT_KEY_FILE_DETAILS_URL);
							dp.setValue(FilesCore.generateTemporaryUrl(dp.getValue()));
						} // if guid key
					} // for data points
				} // else has data points
			} // for measurements
		} // else
	}
	
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
				LOGGER.debug("Detected event of type "+type.name()+", removing all data generated by back end, id: "+backendId);
				
				SensorsDAO dao = ServiceInitializer.getDAOHandler().getDAO(SensorsDAO.class);
				dao.deleteMeasurements(backendId);
				
				List<String> guids = dao.getFileGUIDs(backendId);
				if(guids != null){
					LOGGER.debug("Removing files associated with back end, id: "+backendId);
					dao.deleteFiles(backendId);
					for(String guid : guids){
						FilesCore.removeFile(guid);
					}
				} // if
				
				LOGGER.debug("Back end data removed, back end id: "+backendId);
			}
		}
	} // class BackendListener
}
