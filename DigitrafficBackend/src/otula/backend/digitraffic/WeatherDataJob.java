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
package otula.backend.digitraffic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;

import otula.backend.core.ServiceInitializer;
import otula.backend.digitraffic.datatypes.DigiTrafficConfiguration;
import otula.backend.digitraffic.datatypes.Feature;
import otula.backend.digitraffic.datatypes.FeatureCollection;
import otula.backend.digitraffic.datatypes.Geometry;
import otula.backend.digitraffic.datatypes.SensorValue;
import otula.backend.digitraffic.datatypes.WeatherData;
import otula.backend.digitraffic.datatypes.WeatherDataTask;
import otula.backend.digitraffic.datatypes.WeatherStationData;
import otula.backend.digitraffic.datatypes.WeatherStationProperties;
import otula.backend.digitraffic.datatypes.triggers.Interval;
import otula.backend.digitraffic.datatypes.triggers.Location;
import otula.backend.digitraffic.datatypes.triggers.RoadStationLocation;
import otula.backend.digitraffic.datatypes.triggers.Trigger;
import otula.backend.digitraffic.datatypes.triggers.ValidFromTo;
import otula.backend.digitraffic.utils.GeometryUtils;
import otula.backend.tasks.TaskClient;
import otula.backend.tasks.datatypes.SensorTask;
import service.tut.pori.apilta.sensors.datatypes.Condition;
import service.tut.pori.apilta.sensors.datatypes.DataPoint;
import service.tut.pori.apilta.sensors.datatypes.Measurement;
import service.tut.pori.tasks.datatypes.TaskBackend.Status;

/**
 * weather data job
 * 
 * Warning: you should NOT schedule this job manually, it will be automatically scheduled when {@link #addWeatherDataTask(Long, String, List, boolean, String, List)} is called
 */
public class WeatherDataJob implements Job {
	private static final Logger LOGGER = Logger.getLogger(WeatherDataJob.class);
	private static DigiTrafficConfiguration _configuration = null;
	private static boolean _firstExecute = true;
	private static List<WeatherDataTask> _weatherDataTasks = Collections.synchronizedList(new ArrayList<>());
	private static HashMap<Integer, Geometry> _weatherStationMap = new HashMap<>();
	private static long _weatherStationMapUpdated = Long.MIN_VALUE; // unix UTC timestamp of the previous update of weather station map
	private static AtomicBoolean _weatherTaskScheduled = new AtomicBoolean(false);
	
	/**
	 * Schedule the weather data job if one is not already running
	 * @return true if the task was successfully scheduled (or was already scheduled before)
	 */
	private static  boolean scheduleJob() {
		if(_weatherTaskScheduled.compareAndSet(false, true)){
			org.quartz.Trigger trigger = null;
			if(_firstExecute){
				_firstExecute = false;
				LOGGER.debug("Scheduling the Weather data job...");
				trigger = TriggerBuilder.newTrigger().startNow().build(); // start immediately on first time
			}else{
				long interval = getConfiguration().getTaskCheckInterval();
				LOGGER.debug("Scheduling the Weather data job in "+interval+" seconds...");
				trigger = TriggerBuilder.newTrigger().startAt(new Date(System.currentTimeMillis()+(interval*1000))).build();
			}
			try {
				ServiceInitializer.getExecutorHandler().getScheduler().scheduleJob(JobBuilder.newJob(WeatherDataJob.class).build(), trigger);
			} catch (SchedulerException ex) {
				LOGGER.error(ex, ex);
				_weatherTaskScheduled.set(false);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @return lazy-initialize and return configuration
	 */
	private static DigiTrafficConfiguration getConfiguration() {
		if(_configuration == null){
			_configuration = new DigiTrafficConfiguration().initialize(); // the configuration cannot be a static initialization. Note: this can create potential race condition, but double-initialization and replacement of the previous one is not an issue
		}
		return _configuration;
	}
	
	/**
	 * 
	 * @throws IllegalStateException if the map could not be properly initialized
	 */
	private static void initializeWeatherStationMap() throws IllegalStateException{
		LOGGER.debug("Initializing weather station map...");
		synchronized(_weatherStationMap){
			_weatherStationMap.clear(); // clear the old data if any
			
			FeatureCollection collection = null;
			try(DigiTrafficClient client = new DigiTrafficClient()){
				collection = client.getWeatherStations();
			}
			if(FeatureCollection.isEmpty(collection)){
				LOGGER.debug("No stations retrieved...");
				return;
			}
			
			for(Feature feature : collection.getFeatures()){
				_weatherStationMap.put(((WeatherStationProperties) feature.getProperties()).getRoadStationId(), feature.getGeometry());
			}
			
			_weatherStationMapUpdated = System.currentTimeMillis();
			LOGGER.debug("Initialized "+_weatherStationMap.size()+" stations.");
		}
	}
	
	/**
	 * 
	 * @param location
	 * @return road station location for the given location or null if no matching road station found
	 */
	private static RoadStationLocation resolveRoadStation(Location location){
		synchronized (_weatherStationMap) {
			if(_weatherStationMap.isEmpty()){
				LOGGER.debug("No known stations...");
				return null;
			}
			
			double lat = location.getLatitude();
			double lon = location.getLongitude();
			RoadStationLocation closest = null;
			double closestDistance = 30000; // any number higher than max coordinate distance (in km) is OK as initial value
			DigiTrafficConfiguration configuration = getConfiguration();
			for(Entry<Integer, Geometry> entry : _weatherStationMap.entrySet()){
				Geometry geometry = entry.getValue();
				double distance = GeometryUtils.haversine(lat, lon, geometry.getLatitude(), geometry.getLongitude());
				if(distance < configuration.getMaxDistanceThreshold() && distance < closestDistance){
					closestDistance = distance;
					closest = new RoadStationLocation(geometry, entry.getKey());
				}
			}
			return closest;
		}
	}
	
	/**
	 * This will also auto-schedule the job if one is not already running
	 * 
	 * @param backendId
	 * @param callbackUri
	 * @param conditions the trigger conditions for the task
	 * @param includeLocation if true, the location details will be added to the finished task
	 * @param taskId
	 * @param weatherFeatures
	 * @return true if task was added and the task was successfully scheduled (if needed)
	 */
	protected static boolean addWeatherDataTask(Long backendId, String callbackUri, List<Condition> conditions, boolean includeLocation, String taskId, List<String> weatherFeatures) {
		if(_weatherStationMapUpdated+getConfiguration().getWeatherStationCheckInterval()*1000 <= System.currentTimeMillis()){ // if the map has expired
			LOGGER.debug("Weather station list has expired, retrieving a new one...");
			initializeWeatherStationMap();
		}
		
		List<Trigger> triggers = new ArrayList<>(conditions.size());
		for(Condition condition : conditions){
			ValidFromTo fromTo = null;
			RoadStationLocation location = null;
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
						location = resolveRoadStation(new Location(entry.getValue()));
						break;
					default:
						LOGGER.debug("Ignored unknown key: "+key);
						break;
				}
			}
			if(location == null || fromTo == null || interval == null){
				LOGGER.warn("Ignored invalid trigger condition for weather task.");
			}else{
				Trigger trigger = new Trigger();
				trigger.setLocation(location);
				trigger.setValidFromTo(fromTo);
				trigger.setInterval(interval);
				triggers.add(trigger);
			}
		}
		
		synchronized (_weatherDataTasks) {
			for(Iterator<WeatherDataTask> iter = _weatherDataTasks.iterator(); iter.hasNext();){
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
			
			WeatherDataTask task = new WeatherDataTask();
			task.setBackendId(backendId);
			task.setCallbackUri(callbackUri);
			task.setFeatures(weatherFeatures);
			task.setIncludeLocation(includeLocation);
			task.setTaskId(taskId);
			task.setTriggers(triggers);
			_weatherDataTasks.add(task);
		} // synchronized
		
		LOGGER.debug("Scheduling weather data task...");
		return scheduleJob();
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOGGER.debug("Executing weather data job...");
		
		HashMap<WeatherDataTask, HashSet<Integer>> matching = new HashMap<>();
		HashSet<Integer> combinedIds = new HashSet<>();
		synchronized (_weatherDataTasks) {
			for(Iterator<WeatherDataTask> iter = _weatherDataTasks.iterator(); iter.hasNext();){
				WeatherDataTask task = iter.next();
				long currentTime = System.currentTimeMillis();
				HashSet<Integer> tempIds = new HashSet<>();
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
					tempIds.add(((RoadStationLocation) trigger.getLocation()).getRoadStationId());
				} // for triggers
				
				if(task != null){ // if the task is valid for processing at this time
					matching.put(task, tempIds);
					combinedIds.addAll(tempIds);
				} // if
			}
		}
		
		if(!combinedIds.isEmpty()){
			processTasks(combinedIds, matching);
		}
		
		if(!_weatherDataTasks.isEmpty()){ // if there are tasks remaining, re-schedule the job
			_weatherTaskScheduled.set(false);
			scheduleJob();
		}
	}
	
	/**
	 * 
	 * @param combinedIds
	 * @param tasks map of tasks and the related road station ids
	 */
	private void processTasks(HashSet<Integer> combinedIds, HashMap<WeatherDataTask, HashSet<Integer>> tasks){
		ArrayList<WeatherStationData> dataList = new ArrayList<>();
		try(DigiTrafficClient dClient = new DigiTrafficClient()){
			for(Integer roadStationId : combinedIds){
				WeatherData data = dClient.getWeatherData(roadStationId); // there is no way to filter the data, so retrieve everything
				if(data == null){
					LOGGER.debug("No data for road station, id: "+roadStationId);
				}else{
					dataList.addAll(data.getWeatherStations());
				} // else
			} // for
		} // try
		
		if(dataList.isEmpty()){
			LOGGER.debug("No new data...");
			return;
		}
		
		try(TaskClient client = new TaskClient(getConfiguration())) {
			synchronized (_weatherStationMap) {
				for(Entry<WeatherDataTask, HashSet<Integer>> entry : tasks.entrySet()){
					WeatherDataTask task = entry.getKey();
					SensorTask finishedTask = new SensorTask();
					
					boolean hasData = false;
					List<String> features = task.getFeatures();
					for(WeatherStationData data : dataList){
						Integer roadStationId = data.getId();
						if(entry.getValue().contains(roadStationId)){
							Measurement measurement = new Measurement();
							measurement.setBackendId(task.getBackendId());
							Date measured = data.getMeasuredTime();
							for(SensorValue value : data.getSensorValues()){
								String featureName = Definitions.FEATURE_WEATHER_DATA+Definitions.FEATURE_SEPARATOR+value.getName(); // the values are without the weather prefix
								if(features.contains(featureName)){
									DataPoint point = new DataPoint();
									point.setCreated(measured);
									point.setKey(featureName);
									point.setValue(value.getSensorValue());
									measurement.addDataPoint(point);
									hasData = true; // there are actual sensor values to report
								}
							}
							
							if(task.isIncludeLocation()){
								DataPoint point = new DataPoint();
								point.setCreated(measured);
								point.setKey(Definitions.FEATURE_LOCATION);
								Geometry geometry = _weatherStationMap.get(roadStationId);
								if(geometry != null){
									point.setValue(geometry.getLatitude()+","+geometry.getLongitude());
									measurement.addDataPoint(point);
								}
							}
							finishedTask.addMeasurement(measurement);
						} // if
					} // for station data
					
					String taskId = task.getTaskId();
					if(hasData){
						finishedTask.setCallbackUri(task.getCallbackUri());
						finishedTask.addTaskId(taskId);
						finishedTask.addBackend(task.getBackendId(), Status.EXECUTING);
						if(!client.taskFinished(finishedTask)){
							LOGGER.warn("Failed to send task finished for task, id: "+taskId);
						}
					}else{
						LOGGER.debug("No new data for task, id: "+taskId);
					}
				} // for
			}
		} // try
	}

	/**
	 * 
	 * @param taskId
	 */
	protected static void removeWeatherDataTask(String taskId) {
		synchronized (_weatherDataTasks) {
			for(Iterator<WeatherDataTask> iter = _weatherDataTasks.iterator(); iter.hasNext();){
				if(iter.next().getTaskId().equals(taskId)){
					iter.remove(); // remove the task, no need to remove task scheduling, it will be automatically removed if no tasks are present
					break;
				}
			}
		}
	}
}
