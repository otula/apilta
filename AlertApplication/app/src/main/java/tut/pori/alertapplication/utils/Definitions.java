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
package tut.pori.alertapplication.utils;

/**
 * definitions for utilities
 */
public final class Definitions {
	/* xm elements */
	/** xml element name */
	public static final String ELEMENT_ALERT = "alert";
	/** xml element name */
	public static final String ELEMENT_ALERT_ID = "alertId";
	/** xml element name */
	public static final String ELEMENT_ALERT_LIST = "alertList";
	/** xml element name */
	public static final String ELEMENT_ALERT_TYPE = "alertType";
	/** xml element name */
	public static final String ELEMENT_CREATED_TIMESTAMP = "createdTimestamp";
	/** xml element name */
	public static final String ELEMENT_DESCRIPTION = "description";
	/** xml element name */
	public static final String ELEMENT_GUID = "GUID";
	/** xml element name */
	public static final String ELEMENT_FILE_DETAILS = "fileDetails";
	/** xml element name */
	public static final String ELEMENT_FILE_DETAILS_LIST = "fileDetailsList";
	/** xml element name */
	public static final String ELEMENT_LATITUDE = "latitude";
	/** xml element name */
	public static final String ELEMENT_LOCATION = "location";
	/** xml element name */
	public static final String ELEMENT_LONGITUDE = "longitude";
	/** xml element name */
	public static final String ELEMENT_RANGE = "range";
	/** xml element name */
	public static final String ELEMENT_USER_IDENTITY = "userDetails";
    /** xml element name */
    public static final String ELEMENT_USER_IDENTITY_LIST = "userDetailsList";
	/** xml element name */
	public static final String ELEMENT_USER_ID = "userId";

	/* methods */
	/** service method declaration */
	public static final String METHOD_ADD_ALERT = "addAlert";
	/** service method declaration */
	public static final String METHOD_CREATE_FILE = "createFile";
	/** service method declaration */
	public static final String METHOD_GET_ALERTS = "getAlerts";
    /** service method declaration */
    public static final String METHOD_GET_USER_DETAILS = "getUserDetails";

	/* parameters */
	/** service method parameter declaration */
	public static final String PARAMETER_ALERT_GROUP_ID = "alert_group_id";
	/** service method parameter declaration */
	public static final String PARAMETER_ALERT_TYPE = "alert_type";
	/** service method parameter declaration */
	public static final String PARAMETER_CREATED = "created";
    /** service method parameter declaration */
    public static final String PARAMETER_DATA_GROUPS = "data_groups";
	/** service method parameter declaration */
	public static final String PARAMETER_LOCATION = "location";
	/** location parameter for a coordinate value */
	public static final String PARAMETER_LOCATION_COORDINATE = "coordinate";
	/** location parameter for a heading value */
	public static final String PARAMETER_LOCATION_HEADING = "heading";
	/** service method parameter declaration */
	public static final String PARAMETER_RANGE = "range";

	/* uri separators */
	/** separator for coordinate values */
	public static final String SEPARATOR_COORDINATES = "%20";
	/** separator used in the service uri path to separate methods from parameters i.e. www.domain.fi/somethingSEPARATOR_URI_METHOD_PARAMSparam=value */
	public static final String SEPARATOR_URI_METHOD_PARAMS = "?";
	/** separator used in the service uri path i.e. www.domain.fiSEPARATOR_URI_PATHsomething */
	public static final String SEPARATOR_URI_PATH = "/";
	/** separator used in query string to separate parameter values ({@value tut.pori.alertapplication.utils.Definitions#SEPARATOR_URI_QUERY_PARAM_VALUES}) */
	public static final String SEPARATOR_URI_QUERY_PARAM_VALUES = ",";
	/** separator used in query string to separate query parameters ({@value tut.pori.alertapplication.utils.Definitions#SEPARATOR_URI_QUERY_PARAMS}) */
	public static final String SEPARATOR_URI_QUERY_PARAMS = "&";
	/** separator used in query string to separate parameters from values ({@value tut.pori.alertapplication.utils.Definitions#SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR}) */
	public static final String SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR = "=";
	/** separates types from values. e.g. ?param=type[SEPARATOR_URI_QUERY_TYPE_VALUE]value */
	public static final String SEPARATOR_URI_QUERY_TYPE_VALUE = ";";

    /* services */
    /** service declaration */
    public static final String SERVICE_ALERTS = "alerts";
    /** service declaration */
    public static final String SERVICE_USER = "user";

	/* common */
	/** HTTP content type for XML */
	public static final String CONTENT_TYPE_XML = "text/xml";
    /** Data group all */
    public static final String DATA_GROUPS_ALL = "all";
	/** default encoding */
	public static final String DEFAULT_ENCODING = "UTF-8";
	/** location provider for locations retrieved from a web service */
	public static final String LOCATION_PROVIDER_SERVICE = "alertService";
	/** location provider for locations retrieved from settings, the location generally only contains latitude and longitude */
	public static final String LOCATION_PROVIDER_SETTINGS = "settings";

	/**
	 *
	 */
	private Definitions(){
		// nothing needed
	}
}
