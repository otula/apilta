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

/**
 * Definitions for sampo package
 * 
 */
public final class Definitions {
	/* condition entry types */
	/** condition entry key */
	public static final String CONDITION_ENTRY_KEY_TIME_INTERVAL = "time/interval";
	/** condition entry key */
	public static final String CONDITION_ENTRY_KEY_TIME_PERIOD = "time/validFromToRange";
	/** condition entry key */
	public static final String CONDITION_ENTRY_KEY_LOCATION = "location/area";
	
	/* features */
	/** weather data service feature */
	public static final String FEATURE_AEROSOL = "aerosol";
	/** weather data service feature */
	public static final String FEATURE_CLOUD_COVER = "cloud_cover";
	/** feature separator */
	public static final String FEATURE_SEPARATOR = "/";
	/** weather data service feature */
	public static final String FEATURE_COMPOSITE = "composite";
	/** weather data service feature */
	public static final String FEATURE_INDIVIDUAL = "individual";
	/** location feature */
	public static final String FEATURE_LOCATION = "sensor"+FEATURE_SEPARATOR+"location"+FEATURE_SEPARATOR+"area";
	/** weather data service feature */
	public static final String FEATURE_O3 = "O3";
	/** weather data service feature */
	public static final String FEATURE_SO2 = "SO2";
	/** weather data service feature */
	public static final String FEATURE_UV_DAILY = "UV_daily";
	/** weather data service feature */
	public static final String FEATURE_UV_INDEX = "UV_index";
	
	/* locations */
	/** location Sodankylä */
	public static final String LOCATION_SODANKYLA = "Sodankylä";
	
	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
