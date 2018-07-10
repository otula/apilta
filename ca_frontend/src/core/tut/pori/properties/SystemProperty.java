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

import java.util.Properties;

/**
 * Base class for property files.
 * 
 * All classes sub-classed from this base class will automatically be loaded and initialized by the system property handler.
 */
public abstract class SystemProperty {
	/* core */
	/** property prefix for core */
	protected static final String PROPERTY_CORE_PORI = "core.tut.pori";
	/** property prefix for core/utils */
	protected static final String PROPERTY_CORE_PORI_UTILS = PROPERTY_CORE_PORI+".utils";
	/** property prefix for core/executor */
	protected static final String PROPERTY_CORE_PORI_EXECUTOR = PROPERTY_CORE_PORI+".executor";
	/* services */
	/** property prefix for service */
	protected static final String PROPERTY_SERVICE_PORI = "service.tut.pori";
	/** property prefix for service/properties */
	protected static final String PROPERTY_SERVICE_PORI_PROPERTIES = PROPERTY_CORE_PORI+".properties";
	
	/* parameters */
	/** property bind address */
	protected static final String PROPERTY_SERVICE_PORI_PROPERTIES_BIND_ADDRESS = PROPERTY_SERVICE_PORI_PROPERTIES+".bind_address";
	
	/**
	 * Initialize the system property.
	 * 
	 * Note that when using the default file path, the passed object will be of type UnmodifiableProperties, 
	 * and when using a custom file path, the object will be of type Properties
	 * 
	 * @param properties
	 * @throws IllegalArgumentException
	 */
	public abstract void initialize(Properties properties) throws IllegalArgumentException;
	
	/**
	 * By default, this returns null. On null value, the default system property file will be used. If non-null value is provided, the path will be used to load the requested properties.
	 * 
	 * @return the path of the configuration file for this Property
	 */
	public String getPropertyFilePath(){
		return null;
	}
}
