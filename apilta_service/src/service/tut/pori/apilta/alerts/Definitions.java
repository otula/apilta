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
package service.tut.pori.apilta.alerts;

/**
 * definitions for alerts package
 * 
 */
public final class Definitions {
	/* common */
	/** default range for distance calculations, in km */
	public static final Double DEFAULT_RANGE = 1D;
	
	/* methods */
	/** service method declaration */
	public static final String METHOD_ADD_ALERT = "addAlert";
	/** service method declaration */
	public static final String METHOD_CREATE_FILE = "createFile";
	/** service method declaration */
	public static final String METHOD_GET_ALERTS = "getAlerts";
	
	/* parameters */
	/** service method parameter declaration */
	public static final String PARAMETER_ALERT_GROUP_ID = "alert_group_id";
	/** service method parameter declaration */
	public static final String PARAMETER_ALERT_TYPE = "alert_type";
	/** service method parameter declaration */
	public static final String PARAMETER_CREATED = "created";
	/** service method parameter declaration */
	public static final String PARAMETER_RANGE = "range";
		
	/* services */
	/** service name declaration */
	public static final String SERVICE_ALERTS = "alerts";

	/**
	 * 
	 */
	private Definitions() {
		// nothing needed
	}
}
