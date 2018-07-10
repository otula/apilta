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
package otula.backend.parking;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;

import otula.backend.core.ServiceInitializer;
import otula.backend.digitraffic.utils.GeometryUtils;
import otula.backend.parking.ParkingClient.City;
import otula.backend.parking.datatypes.Coordinate;
import otula.backend.parking.datatypes.Coordinates;
import otula.backend.parking.datatypes.ParkingConfiguration;
import otula.backend.parking.datatypes.ParkingPlace;
import otula.backend.tasks.TaskClient;
import otula.backend.tasks.datatypes.SensorTask;
import service.tut.pori.apilta.sensors.datatypes.DataPoint;
import service.tut.pori.apilta.sensors.datatypes.Measurement;
import service.tut.pori.tasks.datatypes.TaskBackend.Status;

/**
 * Performs parking task job.
 * 
 */
public class ParkingDataJob implements Job {
	private static final String KEY_BACKEND_ID = "backendIds";
	private static final String KEY_CALLBACK_URI = "callbackUri";
	private static final String KEY_COORDINATES = "coordinates";
	private static final String KEY_FEATURES = "features";
	private static final String KEY_TASK_ID = "taskId";
	private static final char SEPARATOR_VALUES = ',';
	private static final Logger LOGGER = Logger.getLogger(ParkingDataJob.class);
	private static ParkingConfiguration _configuration = null;
	private static List<ParkingPlace> _parkingPlaces = new ArrayList<>();
	private static volatile long _parkingPlacesUpdated = Long.MIN_VALUE; // unix UTC timestamp of the previous update of parking station list

	/**
	 * 
	 * @return lazy-initialized configuration object
	 */
	private static ParkingConfiguration getConfiguration() {
		if(_configuration == null){
			_configuration = new ParkingConfiguration().initialize(); // in a race condition this will simply create an extra object
		}
		return _configuration;
	}

	/**
	 * 
	 * @param backendId
	 * @param callbackUri
	 * @param coordinates coordinates to use for the search
	 * @param features
	 * @param taskId
	 */
	public static void schedule(Long backendId, String callbackUri, List<Coordinate> coordinates, Set<String> features, String taskId){
		JobDataMap map = new JobDataMap();

		map.put(KEY_BACKEND_ID, backendId);

		map.put(KEY_CALLBACK_URI, callbackUri);

		Coordinates c = new Coordinates();
		c.setCoordinates(coordinates);
		map.put(KEY_COORDINATES, c.getValue());

		StringBuilder sb = new StringBuilder();
		for(String feature : features){
			sb.append(feature);
			sb.append(SEPARATOR_VALUES);
		}
		sb.setLength(sb.length()-1);
		map.put(KEY_FEATURES, sb.toString());

		map.put(KEY_TASK_ID, taskId);

		LOGGER.debug("Scheduling task, id: "+taskId);
		try {
			ServiceInitializer.getExecutorHandler().getScheduler().scheduleJob(JobBuilder.newJob(ParkingDataJob.class).setJobData(map).build(), TriggerBuilder.newTrigger().startNow().build());
		} catch (UnsupportedOperationException | SchedulerException ex) {
			LOGGER.error(ex, ex);
		}
	}

	/**
	 * initialize the parking places list
	 */
	private void initializeParkingPlaces() {
		LOGGER.debug("Initializing parking places list...");

		try(ParkingClient client = new ParkingClient(getConfiguration())){
			List<ParkingPlace> places = client.getParkingPlaces(EnumSet.allOf(City.class));
			if(places == null) {
				places = new ArrayList<>(0);
			}
			_parkingPlaces = places; // replace the old list
			_parkingPlacesUpdated = System.currentTimeMillis();
			LOGGER.debug("Initialized "+places.size()+" parking places...");
		}
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOGGER.debug("Executing parking data job...");

		ParkingConfiguration configuration = getConfiguration();
		if(configuration.getParkingCheckInterval()*1000+_parkingPlacesUpdated <= System.currentTimeMillis()){
			LOGGER.debug("Parking places list has expired, retrieving a new one...");
			initializeParkingPlaces(); // in a race condition this can load the list for an extra time, let it do so.
		}

		JobDataMap map = context.getMergedJobDataMap();

		SensorTask task = new SensorTask();

		ArrayList<String> taskIds = new ArrayList<>(1);
		String taskId = map.getString(KEY_TASK_ID);
		taskIds.add(taskId);
		task.setTaskIds(taskIds);

		task.setCallbackUri(map.getString(KEY_CALLBACK_URI));

		Long backendId = map.getLong(KEY_BACKEND_ID);
		task.addBackend(backendId, Status.COMPLETED);

		HashSet<ParkingPlace> matching = new HashSet<>();
		Coordinates c = new Coordinates();
		c.setValue(map.getString(KEY_COORDINATES));
		Date measured = new Date(_parkingPlacesUpdated);
		for(Coordinate coordinate : c.getCoordinates()){
			double lat = coordinate.getLatitude();
			double lon = coordinate.getLongitude();
			double closestDistance = Double.MAX_VALUE;
			ParkingPlace closestPlace = null;
			for(ParkingPlace pp : _parkingPlaces){
				Coordinate location = pp.getLocation();
				double distance = GeometryUtils.haversine(lat, lon, location.getLatitude(), location.getLongitude());
				if(distance < closestDistance){
					closestDistance = distance;
					closestPlace = pp;
				} // if
			} // for
			if(closestPlace != null){
				matching.add(closestPlace);
			}
		}

		boolean includeDetails = false;
		boolean includeLocation = false;
		for(String feature : StringUtils.split(map.getString(KEY_FEATURES), SEPARATOR_VALUES)){
			switch(feature){
				case Definitions.FEATURE_DETAILS:
					includeDetails = true;
					break;
				case Definitions.FEATURE_LOCATION:
					includeLocation = true;
					break;
				default:
					LOGGER.warn("Ignored invalid feature: "+feature);
					break;
			}
		}

		for(ParkingPlace match : matching){
			Measurement measurement = new Measurement();
			measurement.setBackendId(backendId);
			if(includeDetails){
				String temp = match.getName();
				if(!StringUtils.isBlank(temp)){
					DataPoint dp = new DataPoint();
					dp.setCreated(measured);
					dp.setKey(Definitions.DATA_KEY_NAME);
					dp.setValue(temp);
					measurement.addDataPoint(dp);
				}

				temp = match.getNotice();
				if(!StringUtils.isBlank(temp)){
					DataPoint dp = new DataPoint();
					dp.setCreated(measured);
					dp.setKey(Definitions.DATA_KEY_NOTICE);
					dp.setValue(temp);
					measurement.addDataPoint(dp);
				}

				temp = match.getOwner();
				if(!StringUtils.isBlank(temp)){
					DataPoint dp = new DataPoint();
					dp.setCreated(measured);
					dp.setKey(Definitions.DATA_KEY_OWNER);
					dp.setValue(temp);
					measurement.addDataPoint(dp);
				}

				temp = match.getZone();
				if(!StringUtils.isBlank(temp)){
					DataPoint dp = new DataPoint();
					dp.setCreated(measured);
					dp.setKey(Definitions.DATA_KEY_ZONE);
					dp.setValue(temp);
					measurement.addDataPoint(dp);
				}

				Boolean boolTemp = match.getPaymentRequired();
				if(boolTemp != null){
					DataPoint dp = new DataPoint();
					dp.setCreated(measured);
					dp.setKey(Definitions.DATA_KEY_PAYMENT_REQUIRED);
					dp.setValue(boolTemp.toString());
					measurement.addDataPoint(dp);
				}

				Integer intTemp = match.getPlaceCount();
				if(intTemp != null){
					DataPoint dp = new DataPoint();
					dp.setCreated(measured);
					dp.setKey(Definitions.DATA_KEY_PLACE_COUNT);
					dp.setValue(intTemp.toString());
					measurement.addDataPoint(dp);
				}

				intTemp = match.getTimeLimit();
				if(intTemp != null){
					DataPoint dp = new DataPoint();
					dp.setCreated(measured);
					dp.setKey(Definitions.DATA_KEY_TIME_LIMIT);
					dp.setValue(intTemp.toString());
					measurement.addDataPoint(dp);
				}
			} // if include details

			if(includeLocation){
				DataPoint dp = new DataPoint();
				dp.setCreated(measured);
				dp.setKey(Definitions.DATA_KEY_LOCATION);
				dp.setValue(match.getLocation().toLatLngString());
				measurement.addDataPoint(dp);
			} // if include location
			task.addMeasurement(measurement);
		} // for

		try(TaskClient client = new TaskClient(configuration)){
			if(!client.taskFinished(task)){
				LOGGER.warn("Failed to send task finished for task, id: "+taskId);
			}
		}
	}
}
