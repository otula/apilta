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
package otula.backend.sampo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;

import otula.backend.core.ServiceInitializer;
import otula.backend.sampo.datatypes.SampoConfiguration;
import otula.backend.sampo.datatypes.SampoDataTask;
import otula.backend.sampo.datatypes.triggers.Interval;
import otula.backend.sampo.datatypes.triggers.Trigger;
import otula.backend.sampo.datatypes.triggers.ValidFromTo;
import otula.backend.tasks.TaskClient;
import otula.backend.tasks.datatypes.SensorTask;
import service.tut.pori.apilta.files.datatypes.FileDetails;
import service.tut.pori.apilta.sensors.datatypes.Condition;
import service.tut.pori.apilta.sensors.datatypes.DataPoint;
import service.tut.pori.apilta.sensors.datatypes.Measurement;
import service.tut.pori.apilta.sensors.datatypes.MeasurementList;
import service.tut.pori.tasks.datatypes.TaskBackend.Status;

/**
 * Sampo data job
 * 
 * Warning: you should NOT schedule this job manually, it will be automatically scheduled when {@link #addSampoDataTask(Long, String, List, boolean, String, List)} is called
 */
public class SampoDataJob implements Job {
	private static final Logger LOGGER = Logger.getLogger(SampoDataJob.class);
	private static SampoConfiguration _configuration = null;
	private static boolean _firstExecute = true;
	private static List<SampoDataTask> _tasks = Collections.synchronizedList(new ArrayList<>());
	private static AtomicBoolean _taskScheduled = new AtomicBoolean(false);

	/**
	 * Schedule the Sampo data job if one is not already running
	 * @return true if the task was successfully scheduled (or was already scheduled before)
	 */
	private static  boolean scheduleJob() {
		if(_taskScheduled.compareAndSet(false, true)){
			org.quartz.Trigger trigger = null;
			if(_firstExecute){
				_firstExecute = false;
				LOGGER.debug("Scheduling the data job...");
				trigger = TriggerBuilder.newTrigger().startNow().build(); // start immediately on first time
			}else{
				long interval = getConfiguration().getTaskCheckInterval();
				LOGGER.debug("Scheduling the data job in "+interval+" seconds...");
				trigger = TriggerBuilder.newTrigger().startAt(new Date(System.currentTimeMillis()+(interval*1000))).build();
			}
			try {
				ServiceInitializer.getExecutorHandler().getScheduler().scheduleJob(JobBuilder.newJob(SampoDataJob.class).build(), trigger);
			} catch (SchedulerException ex) {
				LOGGER.error(ex, ex);
				_taskScheduled.set(false);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @return lazy-initialize and return configuration
	 */
	private static SampoConfiguration getConfiguration() {
		if(_configuration == null){
			_configuration = new SampoConfiguration().initialize(); // the configuration cannot be a static initialization. Note: this can create potential race condition, but double-initialization and replacement of the previous one is not an issue
		}
		return _configuration;
	}
	
	/**
	 * This will also auto-schedule the job if one is not already running
	 * 
	 * @param backendId
	 * @param callbackUri
	 * @param conditions the trigger conditions for the task
	 * @param includeLocation if true, the location details will be added to the finished task
	 * @param taskId
	 * @param features
	 * @return true if task was added and the task was successfully scheduled (if needed)
	 */
	protected static boolean addSampoDataTask(Long backendId, String callbackUri, List<Condition> conditions, boolean includeLocation, String taskId, List<String> features) {
		List<Trigger> triggers = new ArrayList<>(conditions.size());
		for(Condition condition : conditions){
			ValidFromTo fromTo = null;
			String area = null;
			Interval interval = null;
			for(Entry<String, String> entry : condition.getConditions().entrySet()){
				String key = entry.getKey();
				switch(key){
					case Definitions.CONDITION_ENTRY_KEY_TIME_INTERVAL:
						interval = new Interval(Integer.valueOf(entry.getValue()));
						break;
					case Definitions.CONDITION_ENTRY_KEY_TIME_PERIOD:
						fromTo = new ValidFromTo(entry.getValue());
						break;
					case Definitions.CONDITION_ENTRY_KEY_LOCATION:
						area = entry.getValue();
						break;
					default:
						LOGGER.debug("Ignored unknown key: "+key);
						break;
				}
			}
			if(fromTo == null || interval == null){
				LOGGER.warn("Ignored invalid trigger condition for the task.");
			}else{
				if(StringUtils.isAllBlank(area)){
					LOGGER.debug("No area given, using default: "+Definitions.LOCATION_SODANKYLA);
					area = Definitions.LOCATION_SODANKYLA;
				}
				
				for(String feature : features){
					if(SampoClient.validate(area, feature)){ // check that at least one of the features is compatible with the given area
						Trigger trigger = new Trigger();
						trigger.setArea(area);
						trigger.setValidFromTo(fromTo);
						trigger.setInterval(interval);
						triggers.add(trigger);
						break;
					} // if
				} // for
			} // else
		} // for
		
		synchronized (_tasks) {
			for(Iterator<SampoDataTask> iter = _tasks.iterator(); iter.hasNext();){
				if(iter.next().getTaskId().equals(taskId)){
					LOGGER.debug("Removing existing task details for task, id: "+taskId);
					iter.remove();
					break;
				}
			}
			
			if(triggers.isEmpty()){
				LOGGER.warn("The given task has no valid triggers, id: "+taskId);
				return false;
			}
			
			SampoDataTask task = new SampoDataTask();
			task.setBackendId(backendId);
			task.setCallbackUri(callbackUri);
			task.setIncludeLocation(includeLocation);
			task.setTaskId(taskId);
			task.setTriggers(triggers);
			task.setFeatures(features);
			_tasks.add(task);
		} // synchronized
		
		LOGGER.debug("Scheduling weather data task...");
		return scheduleJob();
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		HashMap<SampoDataTask, HashSet<Pair<String, String>>> matching = new HashMap<>(); // task/areas
		HashSet<Pair<String, String>> combinedAreasFeatures = new HashSet<>(); // area-feature set
		HashSet<String> tempAreas = new HashSet<>();
		synchronized (_tasks) {
			for(Iterator<SampoDataTask> iter = _tasks.iterator(); iter.hasNext();){
				SampoDataTask task = iter.next();
				tempAreas.clear();
				long currentTime = System.currentTimeMillis();
				for(Trigger trigger : task.getTriggers()){
					ValidFromTo period = trigger.getValidFromTo();
					long to = period.getTo().getTime();
					if(to < currentTime){ // no longer valid
						iter.remove(); //TODO in principle, we could send taskFinished without data and with COMPLETED status at this point
						task = null; // make invalid
						break;
					}else if(period.getFrom().getTime() >= currentTime){
						Interval interval = trigger.getInterval();
						Date lastTriggered = interval.getLastTriggered();
						if(lastTriggered == null || (lastTriggered.getTime()+interval.getInterval()*1000) <= currentTime){
							interval.setLastTriggered(new Date());
						}else{ // interval time period has not yet passed
							task = null; // make invalid
							break;
						}
					} // else if
					
					tempAreas.add(trigger.getArea());
				} // for triggers
				
				if(task != null){ // if the task is valid for processing at this time
					HashSet<Pair<String, String>> tempAreasFeatures = new HashSet<>(); // area-feature set
					for(String area : tempAreas){ // sort the areas/features to minimize the amount of calls necessary to construct task responses
						for(String feature : task.getFeatures()){
							tempAreasFeatures.add(Pair.of(area, feature));
						}
					}
					matching.put(task, tempAreasFeatures);
					combinedAreasFeatures.addAll(tempAreasFeatures);
				} // if
			}
		} // synchronized
		
		if(!combinedAreasFeatures.isEmpty()){
			processTasks(combinedAreasFeatures, matching);
		}
	
		_taskScheduled.set(false);
		synchronized (_tasks) {
			if(!_tasks.isEmpty()){ // if there are tasks remaining, re-schedule the job
				scheduleJob();
			}
		}
	}

	/**
	 * 
	 * @param areas area/feature pairs
	 * @param matching tasks / area/feature pairs
	 */
	private void processTasks(HashSet<Pair<String, String>> areas, HashMap<SampoDataTask, HashSet<Pair<String, String>>> matching){
		SampoConfiguration configuration = getConfiguration();

		HashMap<Pair<String, String>, File> areaFileMap = new HashMap<>();
		Date measured = new Date();
		try(SampoClient sClient = new SampoClient()){
			String tempPath = configuration.getTemporaryDirectory();
			for(Pair<String, String> area : areas){
				File file = sClient.retrieveImage(area.getRight(), area.getLeft(), tempPath);
				if(file == null){
					LOGGER.warn("Failed to retrieve image for area: "+area.getLeft()+" feature: "+area.getRight());
				}else{
					areaFileMap.put(area, file);
				} // else
			} // for
		} // try

		if(areaFileMap.isEmpty()){
			LOGGER.debug("No new data...");
			return;
		} // if
		
		try(TaskClient client = new TaskClient(configuration)) {
			for(Entry<SampoDataTask, HashSet<Pair<String, String>>> entry : matching.entrySet()){
				SampoDataTask task = entry.getKey();
				Long backendId = task.getBackendId();
				String taskId = task.getTaskId();
				SensorTask finishedTask = new SensorTask();
				for(Pair<String, String> area : entry.getValue()){
					File file = areaFileMap.get(area);
					if(file != null){
						FileDetails details = client.createFile(backendId, file);
						if(details == null){
							LOGGER.warn("Failed to create new file for backend, id: "+backendId+", task, id: "+taskId);
						}else{
							Measurement measurement = new Measurement();
							measurement.setBackendId(backendId);
							DataPoint point = new DataPoint();
							point.setCreated(measured);
							point.setKey(service.tut.pori.apilta.sensors.Definitions.DATA_POINT_KEY_FILE_GUID);
							point.setValue(details.getGUID());
							point.setDescription(area.getRight());
							measurement.addDataPoint(point);
							
							if(task.isIncludeLocation()){
								point = new DataPoint();
								point.setCreated(measured);
								point.setKey(Definitions.FEATURE_LOCATION);
								point.setValue(area.getLeft());
								measurement.addDataPoint(point);
							}
							finishedTask.addMeasurement(measurement);
						} // else
					} // if
				} // for
				
				if(MeasurementList.isEmpty(finishedTask.getMeasurements())){
					LOGGER.debug("No new data for task, id: "+taskId);
				}else{
					finishedTask.setCallbackUri(task.getCallbackUri());
					finishedTask.addTaskId(taskId);
					finishedTask.addBackend(task.getBackendId(), Status.EXECUTING);
					if(!client.taskFinished(finishedTask)){
						LOGGER.warn("Failed to send task finished for task, id: "+taskId);
					}
				}
			} // for
		} // try
			
		LOGGER.debug("Clearing temporary files...");
		for(File file : areaFileMap.values()){
			if(!file.delete()){
				LOGGER.warn("Failed to delete file: "+file.getAbsolutePath());
			}
		} // for
	}

	/**
	 * 
	 * @param taskId
	 */
	protected static void removeSampoDataTask(String taskId) {
		synchronized(_tasks) {
			for(Iterator<SampoDataTask> iter = _tasks.iterator(); iter.hasNext();){
				if(iter.next().getTaskId().equals(taskId)){
					iter.remove(); // remove the task, no need to remove task scheduling, it will be automatically removed if no tasks are present
					break;
				} // if
			} // for
		} // synchronized
	}
}
