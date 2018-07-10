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
package core.tut.pori.context;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;

/**
 * One should not initialize this handler directly, as an instantiated version is available from ServiceInitializer.
 *
 */
public class ConfigHandler {
	private static final String LOG4J_CONFIGURATION_FILE = "log4j2.xml";
	private static final String LOG4J_SYSTEM_PROPERTY = "log4j.configurationFile";
	private static final String PARAM_CONFIG_FILE_PATH = "configFilePath";
	private static final String PARAM_PROPERTY_FILE_PATH = "propertyFilePath";
	private String _configFilePath = null;
	private String _propertyFilePath = null;
	
	/**
	 * @param context 
	 * 
	 */
	public ConfigHandler(ServletContext context){
		initialize(context);
	}
	
	/**
	 * 
	 */
	private void initialize(ServletContext context){
		_propertyFilePath = context.getInitParameter(PARAM_PROPERTY_FILE_PATH);
		if(StringUtils.isBlank(_propertyFilePath)){
			throw new IllegalArgumentException("Property value is not defined: " + PARAM_PROPERTY_FILE_PATH);
		}
		_configFilePath = context.getInitParameter(PARAM_CONFIG_FILE_PATH);
		if(StringUtils.isBlank(_configFilePath)){
			throw new IllegalArgumentException("Property value is not defined: " + PARAM_CONFIG_FILE_PATH);
		}
		System.setProperty(LOG4J_SYSTEM_PROPERTY, _configFilePath + LOG4J_CONFIGURATION_FILE);	// Load logger configuration
	}

	/**
	 * @return Root path for the the configuration files
	 */
	public String getConfigFilePath() {
		return _configFilePath;
	}

	/**
	 * @return Root path for the the property files
	 */
	public String getPropertyFilePath() {
		return _propertyFilePath;
	}
}
