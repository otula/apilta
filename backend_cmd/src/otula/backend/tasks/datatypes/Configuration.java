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
package otula.backend.tasks.datatypes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import otula.backend.core.ServiceInitializer;

/**
 * class for common configuration options
 *
 * Note: this class uses the ConfigHandler {@link otula.backend.core.ServiceInitializer#getConfigHandler()}, which is initialized on service startup.
 * Thus, using this class in statically initialized way may not work as intended.
 */
public class Configuration {
	/** filename for back end properties */
	protected static final String PROPERTY_FILE = "backend.properties";
	/** property path for basic back end properties */
	protected static final String PROPERTY_BACKEND = "otula.backend";
	/** property path for basic back end task properties */
	protected static final String PROPERTY_BACKEND_TASKS = PROPERTY_BACKEND+".tasks";
	private static final String PROPERTY_BACKEND_ID = PROPERTY_BACKEND+".id";
	private static final String PROPERTY_BACKEND_SERVICE_URI = PROPERTY_BACKEND+".service_uri";
	private static final String PROPERTY_BACKEND_TASKS_PASSWORD = PROPERTY_BACKEND_TASKS+".password";
	private static final String PROPERTY_BACKEND_TASKS_USERNAME = PROPERTY_BACKEND_TASKS+".username";
	private static final Logger LOGGER = Logger.getLogger(Configuration.class);
	private Long _backendId = null; 
	private String _password = null;
	private String _serviceAddress = null;
	private String _username = null;
	
	/**
	 * 
	 */
	public Configuration() {
		// nothing needed
	}
	
	/**
	 * Initializes the property object and calls {@link #initialize(Properties)}
	 * @return this object
	 * @throws IllegalArgumentException 
	 */
	public Configuration initialize() throws IllegalArgumentException {
		String path = ServiceInitializer.getConfigHandler().getPropertyFilePath()+PROPERTY_FILE;
		try(InputStream in = getClass().getClassLoader().getResourceAsStream(path)){
			if(in == null){
				throw new IllegalArgumentException("Failed to open property file, path: "+path);
			}
			Properties properties = new Properties();
			properties.load(in);
			initialize(properties);
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to read property file, path: "+path);
		}
		return this;
	}
	
	/**
	 * Called automatically when the configuration object is initialized
	 * 
	 * @param properties
	 * @throws IllegalArgumentException on invalid or missing data
	 */
	protected void initialize(Properties properties) throws IllegalArgumentException {
		_username = properties.getProperty(PROPERTY_BACKEND_TASKS_USERNAME);
		if(StringUtils.isBlank(_username)){
			throw new IllegalArgumentException("Invalid property "+PROPERTY_BACKEND_TASKS_USERNAME);
		}
		_password = properties.getProperty(PROPERTY_BACKEND_TASKS_PASSWORD);
		if(StringUtils.isBlank(_password)){
			throw new IllegalArgumentException("Invalid property "+PROPERTY_BACKEND_TASKS_PASSWORD);
		}
		_serviceAddress = properties.getProperty(PROPERTY_BACKEND_SERVICE_URI);
		if(StringUtils.isBlank(_serviceAddress)){
			throw new IllegalArgumentException("Invalid property "+PROPERTY_BACKEND_SERVICE_URI);
		}
		
		try{
			_backendId = Long.valueOf(properties.getProperty(PROPERTY_BACKEND_ID));
		} catch (NumberFormatException ex){
			Logger.getLogger(getClass()).error(ex, ex);
			throw new IllegalArgumentException("Invalid property "+PROPERTY_BACKEND_ID);
		}
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return _password;
	}
	
	/**
	 * @return the service address, e.g. http://127.0.0.1:8080/TestApp/rest/
	 */
	public String getServiceAddress() {
		return _serviceAddress;
	}
	
	/**
	 * @return the username
	 */
	public String getUsername() {
		return _username;
	}
	
	/**
	 * 
	 * @return the back end id
	 */
	public Long getBackendId() {
		return _backendId;
	}
}
