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

/**
 * definitions for sensor service
 * 
 */
public final class Definitions {
	/* sql columns */
	/** task identifier */
	protected static final String COLUMN_TASK_ID = "task_id";
	
	/* data groups */
	/** data group for retrieving data point details */
	public static final String DATA_GROUP_DATA_POINTS = "data_points";
	
	/* data point keys */
	/** data point key */
	public static final String DATA_POINT_KEY_FILE_GUID = "file/guid";
	/** data point key */
	public static final String DATA_POINT_KEY_FILE_DETAILS_URL = "file/details/url";
	
	/* methods */
	/** implemented by front end */
	public static final String METHOD_CREATE_FILE = "createFile";
	/** implemented by front end */
	public static final String METHOD_CREATE_TASK = "createTask";
	/** implemented by front end */
	public static final String METHOD_MODIFY_TASK = "modifyTask";
	/** implemented by front end */
	public static final String METHOD_GET_MEASUREMENTS = "getMeasurements";
	
	/* parameters */
	/** service method parameter declaration */
	public static final String PARAMETER_MEASUREMENT_ID = "measurement_id";
	
	/* services */
	/** service name declaration */
	public static final String SERVICE_SENSORS = "sensors";
	
	/* task types */
	/** task type for generic data collection */
	public static final String TASK_TYPE_DATA_COLLECT = "gather";

	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
