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
package otula.backend.sampo.datatypes;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import otula.backend.tasks.datatypes.Configuration;

/**
 * Configuration options for the sampo back end
 * 
 */
public class SampoConfiguration extends Configuration {
	/** property path for sampo back end task properties */
	protected static final String PROPERTY_BACKEND_SAMPO = "otula.backend.sampo";
	private static final String PROPERTY_BACKEND_SAMPO_TASK_CHECK_INTERVAL = PROPERTY_BACKEND_SAMPO+".task_check_interval";
	private static final String PROPERTY_BACKEND_SAMPO_TEMP_DIRECTORY = PROPERTY_BACKEND_SAMPO+".temp_directory";
	private int _taskCheckInterval = 600; // in seconds
	private String _temporaryDirectory = null;

	@Override
	public SampoConfiguration initialize() throws IllegalArgumentException {
		return (SampoConfiguration) super.initialize();
	}

	@Override
	protected void initialize(Properties properties) throws IllegalArgumentException {
		super.initialize(properties);
		_taskCheckInterval = Integer.parseInt(properties.getProperty(PROPERTY_BACKEND_SAMPO_TASK_CHECK_INTERVAL));
		_temporaryDirectory = properties.getProperty(PROPERTY_BACKEND_SAMPO_TEMP_DIRECTORY);
		if(StringUtils.isBlank(_temporaryDirectory)){
			throw new IllegalArgumentException("Invalid property "+PROPERTY_BACKEND_SAMPO_TEMP_DIRECTORY);
		}
	}

	/**
	 * @return the taskCheckInterval
	 */
	public int getTaskCheckInterval() {
		return _taskCheckInterval;
	}

	/**
	 * @return the temporaryDirectory
	 */
	public String getTemporaryDirectory() {
		return _temporaryDirectory;
	}
}
