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
import java.util.List;

import org.apache.log4j.Logger;

import otula.backend.tasks.datatypes.SensorTask;
import service.tut.pori.apilta.sensors.datatypes.Output;

/**
 * Core methods for Digitraffic back end
 */
public final class DigitrafficCore {
	private static final Logger LOGGER = Logger.getLogger(DigitrafficCore.class);
	
	/**
	 * 
	 */
	private DigitrafficCore(){
		// nothing needed
	}
	
	/**
	 * 
	 * @param task
	 * @throws IllegalArgumentException on bad values
	 */
	public static void scheduleTask(SensorTask task) throws IllegalArgumentException{
		boolean includeLocation = false;
		List<String> weatherFeatures = new ArrayList<>();
		List<Output> output = task.getOutput();
		for(Output o : output){
			String feature = o.getFeature();
			if(Definitions.FEATURE_LOCATION.equals(feature)){
				includeLocation = true;
			}else if(feature.startsWith(Definitions.FEATURE_WEATHER_DATA)){
				weatherFeatures.add(feature);
			}
		}
		
		String taskId = task.getTaskIds().iterator().next();
		if(!weatherFeatures.isEmpty()){
			LOGGER.debug("Received task, id: "+taskId+" with weather features.");
			WeatherDataJob.addWeatherDataTask(task.getBackends().iterator().next().getBackendId(), task.getCallbackUri(), task.getConditions(), includeLocation, taskId, weatherFeatures);
		}else{
			LOGGER.warn("Ignored task without compatible features, id: "+taskId);
		}
	}

	/**
	 * unschedule the task (if scheduled)
	 * 
	 * @param task
	 */
	public static void unscheduleTask(SensorTask task) {
		String taskId = task.getTaskIds().iterator().next();
		LOGGER.debug("Removing task, id: "+taskId+" from schedule, if present.");
		WeatherDataJob.removeWeatherDataTask(taskId);
	}
}