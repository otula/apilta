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
package fi.tut.pori.otula.roadroamer.utils;

/**
 * definitions for sensor data types
 * 
 */
public final class Definitions {
	/* elements */
    /** xml element declaration */
    public static final String ELEMENT_BACKEND = "backend";
    /** xml element declaration */
    public static final String ELEMENT_BACKEND_LIST = "backendList";
	/** xml element declaration */
	public static final String ELEMENT_BACKEND_ID = "backendId";
    /** xml element declaration */
    public static final String ELEMENT_CALLBACK_URI = "callbackUri";
	/** xml element declaration */
	public static final String ELEMENT_CONDITION = "condition";
    /** xml element declaration */
    public static final String ELEMENT_CREATED = "created";
	/** xml element declaration */
	public static final String ELEMENT_CREATED_TIMESTAMP = "createdTimestamp";
	/** xml element declaration */
	public static final String ELEMENT_DATAPOINT = "dataPoint";
	/** xml element declaration */
	public static final String ELEMENT_DATAPOINT_ID = "dataPointId";
	/** xml element declaration */
	public static final String ELEMENT_DATAPOINT_LIST = "dataPointList";
	/** xml element declaration */
	public static final String ELEMENT_DATAPOINT_TYPE = "dataPointType";
	/** xml element declaration */
	public static final String ELEMENT_DESCRIPTION = "description";
    /** xml element declaration */
    public static final String ELEMENT_ENABLED = "enabled";
    /** xml element declaration */
    public static final String ELEMENT_ENTRY = "entry";
	/** xml element declaration */
	public static final String ELEMENT_FEATURE = "feature";
    /** xml element name */
    public static final String ELEMENT_FILE_DETAILS = "fileDetails";
    /** xml element name */
    public static final String ELEMENT_FILE_DETAILS_LIST = "fileDetailsList";
    /** xml element declaration */
    public static final String ELEMENT_GUID = "GUID";
	/** xml element declaration */
	public static final String ELEMENT_KEY = "key";
	/** xml element declaration */
	public static final String ELEMENT_MEASUREMENT = "measurement";
	/** xml element declaration */
	public static final String ELEMENT_MEASUREMENT_ID = "measurementId";
	/** xml element declaration */
	public static final String ELEMENT_MEASUREMENT_LIST = "measurementList";
	/** xml element declaration */
	public static final String ELEMENT_NAME = "name";
	/** xml element declaration */
	public static final String ELEMENT_OUTPUT = "output";
    /** xml element declaration */
    public static final String ELEMENT_TASK = "task";
    /** xml element declaration */
    public static final String ELEMENT_TASK_LIST = "taskList";
    /** xml element declaration */
    public static final String ELEMENT_TASK_ID = "taskId";
    /** xml element declaration */
    public static final String ELEMENT_TASK_ID_LIST = "taskIdList";
    /** xml element declaration */
    public static final String ELEMENT_TASK_STATUS = "taskStatus";
	/** xml element declaration */
	public static final String ELEMENT_TERMS = "terms";
    /** xml element declaration */
    public static final String ELEMENT_UPDATED = "updated";
    /** xml element declaration */
    public static final String ELEMENT_UPDATED_TIMESTAMP = "updatedTimestamp";
    /** xml element name */
    public static final String ELEMENT_USER_IDENTITY = "userDetails";
    /** xml element name */
    public static final String ELEMENT_USER_IDENTITY_LIST = "userDetailsList";
    /** xml element name */
    public static final String ELEMENT_USER_ID = "userId";
	/** xml element declaration */
	public static final String ELEMENT_VALUE = "value";
	/** xml element declaration */
	public static final String ELEMENT_WHAT = "what";
	/** xml element declaration */
	public static final String ELEMENT_WHEN = "when";

    /* Feature element contents */
    /** Sensor Location (e.g. GPS, network)*/
    public static final String FEATURE_SENSOR_LOCATION = "sensor/location";
    /** Sensor Camera*/
    public static final String FEATURE_SENSOR_CAMERA = "sensor/camera";
    /** Sensor Location (e.g. calculation based on GPS)*/
    public static final String FEATURE_SENSOR_VELOCITY = "sensor/velocity";

    /* Term element contents */
    /** Location based condition: area (multiple coordinates, i.e. geofence)*/
    public static final String TERM_LOCATION_AREA = "location/area";
    /** Location based condition: point (e.g. coordinate)*/
    public static final String TERM_LOCATION_POINT = "location/point";
    /** Sensor/location based condition: velocity*/
    public static final String TERM_SENSOR_VELOCITY = FEATURE_SENSOR_VELOCITY;
    /** Text based information of the condition as a whole */
    public static final String TERM_TEXT_DESCRIPTION = "text/description";
    /** Time based condition: datetime range*/
    public static final String TERM_TIME_VALIDITY_RANGE = "time/validFromToRange";

    /* Data Point keys */
    /** Data point key file guid */
    public static final String DATA_POINT_KEY_FILE_GUID = "file/guid";
    /** Data point key file temp (this will be resolved as the file has been sent to the server)*/
    public static final String DATA_POINT_KEY_FILE_TEMP = "file/temp";
    /** Data point key sensor location */
    public static final String DATA_POINT_KEY_SENSOR_LOCATION = "sensor/location";

    /* services */
    /** service declaration */
    public static final String SERVICE_SENSORS = "sensors";
    /** service declaration */
    public static final String SERVICE_TASKS = "tasks";
    /** service declaration */
    public static final String SERVICE_USER = "user";

    /* methods */
    /** service method declaration */
    public static final String METHOD_CREATE_FILE = "createFile";
    /** service method declaration */
    public static final String METHOD_GET_TASKS = "getTasks";
    /** service method declaration */
    public static final String METHOD_GET_TASK_DETAILS = "queryTaskDetails";
    /** service method declaration */
    public static final String METHOD_GET_USER_DETAILS = "getUserDetails";
    /** service method declaration */
    public static final String METHOD_TASK_FINISHED= "taskFinished";

    /* parameters */
    /** method parameter declaration */
    public static final String PARAMETER_BACKEND_ID = "backend_id";
    /** method parameter declaration */
    public static final String PARAMETER_TASK_ID = "task_id";

    /* common */
    /** HTTP content type for XML */
    public static final String CONTENT_TYPE_XML = "text/xml";
    /** default encoding */
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String REST_PATH = "/rest/";
    /** separator used in the service uri path i.e. www.domain.fiSEPARATOR_URI_PATHsomething */
    public static final String SEPARATOR_URI_PATH = "/";
    /** distance to trigger location point in meters*/
    public static final float MINIMUM_DISTANCE_TO_ACTIVATE = 25;
    public static final String PREFIX_PHOTO = "apilta_";
    public static final String SUFFIX_PHOTO = ".jpg";
    public static final long TIME_BETWEEN_CONNECTION_ATTEMPTS = 60 * 1000;
    public static final String TASKSTATUS_COMPLETED = "COMPLETED";

	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
