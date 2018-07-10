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
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import otula.backend.parking.datatypes.Coordinate;
import otula.backend.tasks.datatypes.SensorTask;
import service.tut.pori.apilta.sensors.datatypes.Condition;
import service.tut.pori.apilta.sensors.datatypes.Output;

/**
 * Core methods for the parking back end
 * 
 */
public final class ParkingCore {
	private static final Logger LOGGER = Logger.getLogger(ParkingCore.class);
	
	/**
	 * 
	 */
	private ParkingCore() {
		// nothing needed
	}
	
	/**
	 * 
	 * @param task
	 */
	public static void scheduleTask(SensorTask task) {
		List<Output> output = task.getOutput();
		HashSet<String> features = new HashSet<>(output.size());
		for(Output o : output){
			String feature = o.getFeature();
			switch(feature){
				case Definitions.FEATURE_DETAILS:
				case Definitions.FEATURE_LOCATION:
					features.add(feature);
					break;
				default: // simply ignore unknown features
					break;
			}
		}
		
		String taskId = task.getTaskIds().iterator().next();
		if(features.isEmpty()){
			LOGGER.warn("No features for task, id: "+taskId);
			return; // got a task that would return nothing
		}
		
		List<Condition> conditions = task.getConditions();
		ArrayList<Coordinate> coordinates = new ArrayList<>(conditions.size());
		for(Condition condition : conditions){
			for(Entry<String, String> entry : condition.getConditions().entrySet()){
				if(Definitions.CONDITION_ENTRY_KEY_LOCATION.equals(entry.getKey())){
					coordinates.add(Coordinate.fromLatLng(entry.getValue()));
				}
			}
		}
		
		ParkingDataJob.schedule(task.getBackends().iterator().next().getBackendId(), task.getCallbackUri(), coordinates, features, taskId);
	}
}
