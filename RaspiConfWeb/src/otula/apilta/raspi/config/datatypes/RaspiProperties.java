/**
 * Copyright 2018 Tampere University of Technology, Pori Department
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
package otula.apilta.raspi.config.datatypes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import otula.apilta.raspi.ServiceInitializer;

/**
 * Properties for the raspi
 * 
 * Note: this needs ServiceInitializer to have been called. That is, this object should not be initialized in static context.
 * 
 */
public class RaspiProperties {
	/** filename for raspi properties */
	protected static final String PROPERTY_FILE = "raspi.properties";
	private static final String PROPERTY_RASPI = "otula.raspi";
	private static final String PROPERTY_RASPI_INI_FILE = PROPERTY_RASPI+".ini_file";
	private static final String PROPERTY_RASPI_PHOTO_PATH = PROPERTY_RASPI+".photo_path";
	private static final Logger LOGGER = Logger.getLogger(RaspiProperties.class);
	private String _iniFile = null;
	private String _photoPath = null;
	
	/**
	 * @return the iniFile
	 */
	public String getIniFile() {
		return _iniFile;
	}
	
	/**
	 * @return the photoPath
	 */
	public String getPhotoPath() {
		return _photoPath;
	}

	/**
	 * 
	 */
	public RaspiProperties() {
		initialize();
	}

	/**
	 * Initializes the property object
	 * @throws IllegalArgumentException 
	 */
	private void initialize() throws IllegalArgumentException {
		String path = ServiceInitializer.getConfigHandler().getPropertyFilePath()+PROPERTY_FILE;
		try(InputStream in = getClass().getClassLoader().getResourceAsStream(path)){
			if(in == null){
				throw new IllegalArgumentException("Failed to open property file, path: "+path);
			}
			Properties properties = new Properties();
			properties.load(in);
			
			_iniFile = properties.getProperty(PROPERTY_RASPI_INI_FILE);
			if(StringUtils.isBlank(_iniFile)){
				throw new IllegalArgumentException("Invalid property "+PROPERTY_RASPI_INI_FILE);
			}
			
			_photoPath = properties.getProperty(PROPERTY_RASPI_PHOTO_PATH);
			if(StringUtils.isBlank(_photoPath)){
				throw new IllegalArgumentException("Invalid property "+PROPERTY_RASPI_PHOTO_PATH);
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to read property file, path: "+path);
		}
	}
}
