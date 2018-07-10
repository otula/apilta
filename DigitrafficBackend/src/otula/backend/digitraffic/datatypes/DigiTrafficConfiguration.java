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
package otula.backend.digitraffic.datatypes;

import java.util.Properties;

import otula.backend.tasks.datatypes.Configuration;

/**
 * configuration options for the Digitraffic service
 */
public class DigiTrafficConfiguration extends Configuration {
	/** property path for Digitraffic back end task properties */
	protected static final String PROPERTY_BACKEND_DIGITRAFFIC = "otula.backend.digitraffic";
	private static final String PROPERTY_BACKEND_DIGITRAFFIC_MAX_DISTANCE_THRESHOLD = PROPERTY_BACKEND_DIGITRAFFIC+".max_distance_threshold";
	private static final String PROPERTY_BACKEND_DIGITRAFFIC_STATION_CHECK_INTERVAL = PROPERTY_BACKEND_DIGITRAFFIC+".station_check_interval";
	private static final String PROPERTY_BACKEND_DIGITRAFFIC_TASK_CHECK_INTERVAL = PROPERTY_BACKEND_DIGITRAFFIC+".task_check_interval";
	private double _maxDistanceThreshold = 10; // in km
	private int _taskCheckInterval = 600; // in seconds
	private int _weatherStationCheckInterval = 31536000; // in seconds

	/**
	 * 
	 */
	public DigiTrafficConfiguration() {
		super();
	}
	
	@Override
	public DigiTrafficConfiguration initialize() throws IllegalArgumentException {
		return (DigiTrafficConfiguration) super.initialize();
	}

	@Override
	protected void initialize(Properties properties) throws IllegalArgumentException {
		super.initialize(properties);
		_maxDistanceThreshold = Double.parseDouble(properties.getProperty(PROPERTY_BACKEND_DIGITRAFFIC_MAX_DISTANCE_THRESHOLD));
		_taskCheckInterval = Integer.parseInt(properties.getProperty(PROPERTY_BACKEND_DIGITRAFFIC_TASK_CHECK_INTERVAL));
		_weatherStationCheckInterval = Integer.parseInt(properties.getProperty(PROPERTY_BACKEND_DIGITRAFFIC_STATION_CHECK_INTERVAL));
	}

	/**
	 * @return the maxDistanceThreshold in km
	 */
	public double getMaxDistanceThreshold() {
		return _maxDistanceThreshold;
	}

	/**
	 * @return the weatherStationCheckInterval
	 */
	public int getWeatherStationCheckInterval() {
		return _weatherStationCheckInterval;
	}

	/**
	 * @return the taskCheckInterval in seconds
	 */
	public int getTaskCheckInterval() {
		return _taskCheckInterval;
	}
}
