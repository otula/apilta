/**
 * Copyright 2014 Tampere University of Technology, Pori Department
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
package core.tut.pori.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import core.tut.pori.context.RESTHandler;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.utils.StringUtils;

/**
 * The system property handler.
 * 
 * This class can be used to retrieve system property files known by the system.
 * 
 * One should not initialize this handler directly, as an instantiated version is available from ServiceInitializer.
 */
public final class PropertyHandler {
	private static final Logger LOGGER = Logger.getLogger(PropertyHandler.class);
	private static final String PROPERTIES_CONFIGURATION_FILE = "properties-context.xml";
	private static final String SYSTEM_PROPERTY_FILE = "system.properties";
	private String _bindContext = null;
	private ClassPathXmlApplicationContext _context = null;
	private Map<Class<?>,SystemProperty> _properties = null;
	private String _restBindContext = null;
	
	/**
	 * @param context 
	 * @throws IllegalArgumentException thrown on missing configuration value
	 */
	public PropertyHandler(ServletContext context) throws IllegalArgumentException{
		LOGGER.debug("Initializing handler...");
		Date started = new Date();
		
		_context = new ClassPathXmlApplicationContext(ServiceInitializer.getConfigHandler().getConfigFilePath()+PROPERTIES_CONFIGURATION_FILE);
		LOGGER.debug("Class Path XML Context initialized in "+StringUtils.getDurationString(started, new Date()));
		
		loadProperties(context);
		LOGGER.debug("Property Handler initialized in "+StringUtils.getDurationString(started, new Date()));
	}
	
	/**
	 * @param context
	 * @throws IllegalArgumentException
	 */
	private void loadProperties(ServletContext context) throws IllegalArgumentException{
		ClassLoader classLoader = getClass().getClassLoader();
		try (InputStream systemStream = classLoader.getResourceAsStream(ServiceInitializer.getConfigHandler().getPropertyFilePath()+SYSTEM_PROPERTY_FILE)) {
			if(systemStream == null){
				throw new IllegalArgumentException("Failed to load system property file: "+ServiceInitializer.getConfigHandler().getPropertyFilePath()+SYSTEM_PROPERTY_FILE);
			}
			
			Properties systemProperties = new Properties();
			systemProperties.load(systemStream);
			systemProperties = UnmodifiableProperties.unmodifiableProperties(systemProperties);
			_bindContext = systemProperties.getProperty(SystemProperty.PROPERTY_SERVICE_PORI_PROPERTIES_BIND_ADDRESS)+context.getContextPath()+"/";
			_restBindContext = _bindContext + RESTHandler.PATH_REST;
			
			Map<String, SystemProperty> propertyMap = _context.getBeansOfType(SystemProperty.class);
			if(propertyMap == null || propertyMap.isEmpty()){
				LOGGER.debug("No properties to initialize.");
				return;
			}
			int size = propertyMap.size();
			LOGGER.debug("Found "+size+" SystemProperties.");
			
			_properties = new HashMap<>(size);
			for(Iterator<SystemProperty> iter = propertyMap.values().iterator();iter.hasNext();){
				SystemProperty property = iter.next();
				String propertyFilePath = property.getPropertyFilePath();
				if(propertyFilePath != null && !propertyFilePath.equals(ServiceInitializer.getConfigHandler().getPropertyFilePath()+SYSTEM_PROPERTY_FILE)){	// check if custom property file location has been given
					LOGGER.debug("Found property with custom file path.");
					Properties customProperties = new Properties();
					try (InputStream customStream = classLoader.getResourceAsStream(propertyFilePath)) {
						if(customStream == null){
							throw new IllegalArgumentException("Failed to load custom property file: "+propertyFilePath+", for "+property.getClass().toString());
						}
						customProperties.load(customStream);
						property.initialize(customProperties);
					}
				}else{
					property.initialize(systemProperties);
				}
				_properties.put(property.getClass(), property);
			//	String confi
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to read property file.");
		}
	}
	
	/**
	 * Get the defined URL e.g. https://example.org/context/
	 * @return bind context of the service
	 */
	public String getBindContext() {
		return _bindContext;
	}
	
	/**
	 * Get the defined URL e.g. https://example.org/context/rest/
	 * @return REST bind context of the service
	 */
	public String getRESTBindContext() {
		return _restBindContext;
	}

	/**
	 * Do NOT close or cleanup the instances returned by this method, the initialization and destruction is handled automatically.
	 * 
	 * @param cls 
	 * @return system properties of the given class or null if not found
	 */
	@SuppressWarnings("unchecked")
	public <T extends SystemProperty> T getSystemProperties(Class<T> cls) {
		return (T) (_properties == null ? null : _properties.get(cls));
	}
	
	/**
	 * close this handler and release all resources
	 */
	public void close(){
		_bindContext = null;
		_restBindContext = null;
		_properties = null;
		_context.close();
		_context = null;
	}
}
