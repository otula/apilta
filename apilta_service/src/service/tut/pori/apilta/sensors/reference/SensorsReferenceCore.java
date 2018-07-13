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
package service.tut.pori.apilta.sensors.reference;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import service.tut.pori.apilta.sensors.datatypes.Measurement;
import service.tut.pori.apilta.sensors.datatypes.MeasurementList;
import service.tut.pori.apilta.sensors.datatypes.SensorTask;
import service.tut.pori.tasks.Definitions;
import service.tut.pori.tasks.datatypes.TaskBackend;
import service.tut.pori.tasks.datatypes.TaskBackend.Status;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.DateIntervalParameter.Interval;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;

/**
 * The reference implementations for Content Analysis Service.
 *
 */
public final class SensorsReferenceCore {
	private static final int BUFFER_SIZE = 256;
	private static final SensorsXMLObjectCreator CREATOR = new SensorsXMLObjectCreator(null);
	private static final DataGroups DATA_GROUPS_TRAFFIC_DATA = new DataGroups(DataGroups.DATA_GROUP_ALL);
	private static final Logger LOGGER = Logger.getLogger(SensorsReferenceCore.class);
	
	/**
	 * 
	 */
	private SensorsReferenceCore(){
		// nothing needed
	}

	/**
	 * 
	 * @param limits
	 * @return measurements
	 */
	public static MeasurementList generateTrafficData(Limits limits) {
		MeasurementList datalist = new MeasurementList();
		datalist.setMeasurements(CREATOR.generateMeasurementList(null, null, DATA_GROUPS_TRAFFIC_DATA, limits, null));
		return datalist;
	}

	/**
	 * 
	 * @param authenticatedUser 
	 * @param backendId if null, value is automatically generated
	 * @param taskIds optional ids, one random id will be generated if none is given
	 * @param taskType
	 * @return task details for a submitted task
	 */
	public static SensorTask generateTaskDetails(UserIdentity authenticatedUser, Long backendId, Collection<String> taskIds, String taskType) {
		if(UserIdentity.isValid(authenticatedUser)){
			LOGGER.debug("Authenticated user, id: "+authenticatedUser.getUserId()); // simply log the user id for debug
		}
		return CREATOR.generateTaskDetails((backendId == null ? Math.abs(CREATOR.getRandom().nextLong()) : backendId), taskIds, taskType);
	}
	
	/**
	 * 
	 * @param authenticatedUser 
	 * @param dataGroups
	 * @param limits
	 * @param taskId 
	 * @param taskType
	 * @return task details for a finished task
	 */
	public static SensorTask generateTaskResults(UserIdentity authenticatedUser, DataGroups dataGroups, Limits limits, String taskId, String taskType) {
		if(UserIdentity.isValid(authenticatedUser)){
			LOGGER.debug("Authenticated user, id: "+authenticatedUser.getUserId()); // simply log the user id for debug
		}
		return CREATOR.generateTaskResults(null, dataGroups, limits, (StringUtils.isBlank(taskId) ? null : Arrays.asList(taskId)), taskType);
	}

	/**
	 * reference implementation of the method for adding a task to a back end
	 * 
	 * Note: this will not call the callback URI with task finished
	 *  
	 * @param authenticatedUser 
	 * @param task
	 * @throws IllegalArgumentException on invalid task
	 */
	public static void addTask(UserIdentity authenticatedUser, SensorTask task) throws IllegalArgumentException {
		if(UserIdentity.isValid(authenticatedUser)){
			LOGGER.debug("Authenticated user, id: "+authenticatedUser.getUserId()); // simply log the user id for debug
		}
		
		if(createTask(authenticatedUser, task) == null){
			throw new IllegalArgumentException("Invalid task.");
		}
		
		List<String> taskId = task.getTaskIds();
		if(taskId == null || taskId.size() != 1){
			throw new IllegalArgumentException("Task must have exactly one identifier.");
		}
		
		if(task.getTaskTypes().contains(Definitions.TASK_TYPE_VIRTUAL)){
			throw new IllegalArgumentException("This back end cannot process tasks of type: "+Definitions.TASK_TYPE_VIRTUAL);
		}
		
		List<TaskBackend> backends = task.getBackends();
		if(backends.size() != 1){
			throw new IllegalArgumentException("Only a single back end can be given for add task."); // add task always targeted to a single back end
		}
	}

	/**
	 * 
	 * @param authenticatedUser 
	 * @param task
	 * @throws IllegalArgumentException on invalid task
	 */
	public static void taskFinished(UserIdentity authenticatedUser, SensorTask task) throws IllegalArgumentException {
		if(UserIdentity.isValid(authenticatedUser)){
			LOGGER.debug("Authenticated user, id: "+authenticatedUser.getUserId()); // simply log the user id for debug
		}
		
		List<String> taskIds = task.getTaskIds();
		if(taskIds == null || taskIds.isEmpty()){
			throw new IllegalArgumentException("Invalid task: task identifier missing.");
		}
		
		if(!SensorTask.isValid(task) || task.getConditions() != null){ // it is enough to check that either conditions or output is not present as the validity is checked through isValid()
			throw new IllegalArgumentException("Invalid task.");
		}
	}

	/**
	 * Note: this will not schedule back end calls even if the task contains back ends (is not virtual)
	 * 
	 * @param authenticatedUser 
	 * @param task
	 * @return id for the created task
	 * @throws IllegalArgumentException on invalid task
	 */
	public static String createTask(UserIdentity authenticatedUser, SensorTask task) throws IllegalArgumentException {
		Set<String> taskTypes = task.getTaskTypes();
		if(taskTypes == null || taskTypes.isEmpty()){
			throw new IllegalArgumentException("Invalid task: no task type.");
		}
		
		List<TaskBackend> backends = task.getBackends();
		if(backends == null){
			throw new IllegalArgumentException("Invalid task: no back ends.");
		}
		for(TaskBackend backend : backends){ // reset all status information to not started before task validation
			backend.setStatus(Status.NOT_STARTED);
		}
		
		if(UserIdentity.isValid(authenticatedUser)){
			LOGGER.debug("Authenticated user, id: "+authenticatedUser.getUserId()); // simply log the user id for debug
		}
		
		if(!SensorTask.isValid(task) || task.getConditions() == null || !UserIdentity.isValid(task.getUserId())){ // it is enough to check that either conditions or output exists as the validity is checked through isValid()
			throw new IllegalArgumentException("Invalid task.");
		}
		
		return UUID.randomUUID().toString();
	}
	
	/**
	 * Note: this will not schedule back end calls even if the task contains back ends (is not virtual)
	 * 
	 * @param authenticatedUser 
	 * @param task
	 * @return id for the created task
	 * @throws IllegalArgumentException on invalid task
	 */
	public static String modifyTask(UserIdentity authenticatedUser, SensorTask task) throws IllegalArgumentException {
		List<String> taskIds = task.getTaskIds();
		if(taskIds == null || taskIds.size() != 1){
			throw new IllegalArgumentException("The modified task must have exactly one task identifier.");
		}
		createTask(authenticatedUser, task); // use create task to validate
		return taskIds.iterator().next();
	}

	/**
	 * 
	 * @param authenticatedUser 
	 * @param backendId 
	 * @param file the contents of the file are simply iterated as raw byte data (all content is discarded)
	 * @return randomly generated guid for the file
	 * @throws IllegalArgumentException on bad data
	 */
	@SuppressWarnings("unused")
	public static String createFile(UserIdentity authenticatedUser, Long backendId, InputStream file) throws IllegalArgumentException {
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			while(file.read(buffer) > 0){
				// simply discard all data
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to read file.");
		}
		if(UserIdentity.isValid(authenticatedUser)){
			LOGGER.debug("Authenticated user, id: "+authenticatedUser.getUserId()); // simply log the user id for debug
		}
		return CREATOR.createGUID(); // accept any file, return random GUID
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param backendIdFilter
	 * @param createdFilter
	 * @param dataGroups 
	 * @param limits
	 * @param measurementIdFilter
	 * @param taskIds 
	 * @return randomly generated measurement id list
	 */
	public static MeasurementList getMeasurements(UserIdentity authenticatedUser, long[] backendIdFilter, Set<Interval> createdFilter, DataGroups dataGroups, Limits limits, List<String> measurementIdFilter, Collection<String> taskIds) {
		if(UserIdentity.isValid(authenticatedUser)){
			LOGGER.debug("Authenticated user, id: "+authenticatedUser.getUserId()); // simply log the user id for debug
		}
		
		if(taskIds != null && !taskIds.isEmpty()) {
			LOGGER.debug("Ignored task identifiers.");
		}
		
		List<Measurement> measurements = CREATOR.generateMeasurementList(backendIdFilter, createdFilter, dataGroups, limits, measurementIdFilter);
		if(measurements == null){
			LOGGER.debug("Empty measurement list generated based on the given parameters.");
			return null;
		}
		MeasurementList list = new MeasurementList();
		list.setMeasurements(measurements);
		return list;
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param backendId
	 * @param dataGroups
	 * @param limits
	 * @param taskIds
	 * @param taskType
	 * @return pseudo-randomly generated task
	 */
	@SuppressWarnings("unused")
	public static SensorTask queryTaskDetails(UserIdentity authenticatedUser, Long backendId, DataGroups dataGroups, Limits limits, List<String> taskIds, String taskType) {
		if(!DataGroups.isEmpty(dataGroups)) {
			LOGGER.debug("Ignored data groups.");
		}
		return generateTaskDetails(authenticatedUser, backendId, taskIds, taskType);
	}
}
