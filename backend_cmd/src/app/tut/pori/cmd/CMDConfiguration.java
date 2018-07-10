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
package app.tut.pori.cmd;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import otula.backend.tasks.datatypes.Configuration;

/**
 * class for common configuration options
 *
 */
public class CMDConfiguration extends Configuration {
	private static final String PROPERTY_CMD = "otula.cmd";
	private static final String PROPERTY_CMD_KEEP_FILES = PROPERTY_CMD+".keep_files";
	private static final String PROPERTY_CMD_KEEP_DATA = PROPERTY_CMD+".keep_data";
	private static final String PROPERTY_CMD_DATABASE_FILE = PROPERTY_CMD+".database_file";
	private static final Logger LOGGER = Logger.getLogger(CMDConfiguration.class);
	private String _databaseFilePath = null;
	private boolean _keepData = true;
	private boolean _keepFiles = true;
	
	/**
	 * 
	 */
	public CMDConfiguration() {
		// nothing needed
	}
	
	
	
	/**
	 * initialize the configuration using class loader
	 */
	@Override
	public CMDConfiguration initialize() throws IllegalArgumentException {
		return (CMDConfiguration) super.initialize();
	}



	/**
	 * Initializes the property object and calls {@link #initialize(Properties)}
	 * @param propertyFilePath 
	 * @return this object
	 * @throws IllegalArgumentException 
	 */
	public CMDConfiguration initialize(String propertyFilePath) throws IllegalArgumentException {
		try(FileInputStream in = new FileInputStream(propertyFilePath)){
			Properties properties = new Properties();
			properties.load(in);
			initialize(properties);
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to read property file, path: "+propertyFilePath);
		}
		return this;
	}
	
	@Override
	protected void initialize(Properties properties) throws IllegalArgumentException {
		super.initialize(properties);
		
		_databaseFilePath = properties.getProperty(PROPERTY_CMD_DATABASE_FILE);
		if(StringUtils.isBlank(_databaseFilePath)){
			throw new IllegalArgumentException("Invalid property "+PROPERTY_CMD_DATABASE_FILE);
		}
		
		String temp = properties.getProperty(PROPERTY_CMD_KEEP_FILES);
		if(StringUtils.isBlank(temp)){
			throw new IllegalArgumentException("Invalid property "+PROPERTY_CMD_KEEP_FILES);
		}
		_keepFiles = BooleanUtils.toBoolean(temp);
		
		temp = properties.getProperty(PROPERTY_CMD_KEEP_DATA);
		if(StringUtils.isBlank(temp)){
			throw new IllegalArgumentException("Invalid property "+PROPERTY_CMD_KEEP_DATA);
		}
		_keepData = BooleanUtils.toBoolean(temp);
	}

	/**
	 * @return the keepData
	 */
	public boolean isKeepData() {
		return _keepData;
	}

	/**
	 * @return the keepFiles
	 */
	public boolean isKeepFiles() {
		return _keepFiles;
	}

	/**
	 * @return the databaseFilePath
	 */
	public String getDatabaseFilePath() {
		return _databaseFilePath;
	}
}
