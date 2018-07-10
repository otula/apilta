/**
 * Copyright 2018 Tampere University of Technology, Pori Department
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
package service.tut.pori.apilta.shock;

import service.tut.pori.tasks.datatypes.Task.Visibility;

/**
 * 
 *
 */
public final class Definitions {
	/* services */
	/** service name declaration */
	public static final String SERVICE_SHOCK = "shock"; 
	
	/* methods */
	/** service method declaration */
	public static final String METHOD_CREATE_MEASUREMENT = "createMeasurement";
	/** service method declaration */
	public static final String METHOD_GET_MEASUREMENTS = "getMeasurements";
	/** service method declaration */
	public static final String METHOD_GET_HIGHLIGHTS = "getHighlights";
	
	/* parameters */
	/** service method parameter declaration */
	public static final String PARAMETER_GROUP_METHOD = "group_method";
	/** service method parameter declaration */
	public static final String PARAMETER_GROUP_RANGE = "group_range";
	/** service method parameter declaration */
	public static final String PARAMETER_LEVEL = "level";
	/** service method parameter declaration */
	public static final String PARAMETER_RANGE = "range";
	/** service method parameter declaration */
	public static final String PARAMETER_MIN_MEASUREMENTS = "min_measurements";
	/** service method parameter declaration */
	public static final String PARAMETER_TIMESTAMP = "timestamp";
	
	/* common */
	/** default measurement visibility */
	public static final Visibility DEFAULT_VISIBILITY = Visibility.PRIVATE;
	
	/* data groups */
	/** data group for selecting accelerometer data */
	public static final String DATA_GROUP_ACCELEROMETER_DATA = "accelerometer_data";
	/** data group for selecting location data */
	public static final String DATA_GROUP_LOCATION_DATA = "location_data";
}
