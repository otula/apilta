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
package otula.apilta.raspi.config.utils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import otula.apilta.raspi.config.datatypes.RaspiIni;

/**
 * handles reading/writing the configuration ini file
 * 
 */
public class IniHandler {
	private static final Logger LOGGER = Logger.getLogger(IniHandler.class);
	private static final String SECTION_LINES = "lines";
	private static final String SECTION_AREA = "area";
	private static final String SECTION_TASKS = "tasks";
	private static final String PROPERTY_CROP_LIMITS = SECTION_LINES+".crop_limits";
	private static final String PROPERTY_LINES_DOWN = SECTION_LINES+".down";
	private static final String PROPERTY_LINES_DOWN_LIMIT = SECTION_LINES+".down_limit";
	private static final String PROPERTY_LINES_LEFT_LIMIT = SECTION_LINES+".left_limit";
	private static final String PROPERTY_LINES_RIGHT_LIMIT = SECTION_LINES+".right_limit";
	private static final String PROPERTY_LINES_UP = SECTION_LINES+".up";
	private static final String PROPERTY_LINES_UP_LIMIT = SECTION_LINES+".up_limit";
	private static final String PROPERTY_AREA_THRESHOLD_MIN = SECTION_AREA+".threshold_min";
	private static final String PROPERTY_AREA_THRESHOLD_MAX = SECTION_AREA+".threshold_max";
	private static final String PROPERTY_TASKS_TASK_ID = SECTION_TASKS+".task_id";
	
	/**
	 * 
	 * @param filePath 
	 * @return the configuration
	 */
	public RaspiIni load(String filePath) {
		RaspiIni ini = new RaspiIni();
		INIConfiguration configuration = new INIConfiguration();
		try (FileReader reader = new FileReader(filePath)) {
			configuration.read(reader);
			ini.setLineDown(configuration.getInteger(PROPERTY_LINES_DOWN, null));
			ini.setLineDownLimit(configuration.getInteger(PROPERTY_LINES_DOWN_LIMIT, null));
			ini.setLineUp(configuration.getInteger(PROPERTY_LINES_UP, null));
			ini.setLineUpLimit(configuration.getInteger(PROPERTY_LINES_UP_LIMIT, null));
			
			ini.setLineLeftLimit(configuration.getInteger(PROPERTY_LINES_LEFT_LIMIT, null));
			ini.setLineRightLimit(configuration.getInteger(PROPERTY_LINES_RIGHT_LIMIT, null));
			
			ini.setCropLimits(configuration.getBoolean(PROPERTY_CROP_LIMITS));
			
			ini.setAreaThresholdMin(configuration.getInteger(PROPERTY_AREA_THRESHOLD_MIN, null));
			ini.setAreaThresholdMax(configuration.getInteger(PROPERTY_AREA_THRESHOLD_MAX, null));

			ini.setTaskId(configuration.getString(PROPERTY_TASKS_TASK_ID));
		} catch (IOException | ConfigurationException ex) { // should not happen
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Could not read configuration.");
		}
		
		return ini;
	}
	
	/**
	 * 
	 * @param ini
	 * @param filePath
	 */
	public void save(RaspiIni ini, String filePath) {
		INIConfiguration configuration = new INIConfiguration();
		try (FileReader reader = new FileReader(filePath)) {
			configuration.read(reader); // read to get existing values
		} catch (IOException | ConfigurationException ex) { // should not happen
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Could not read configuration.");
		}
		
		try (FileWriter writer = new FileWriter(filePath)) {
			Integer value = ini.getLineDown();
			if(value != null){
				configuration.setProperty(PROPERTY_LINES_DOWN, value);
			}
			value = ini.getLineDownLimit();
			if(value != null){
				configuration.setProperty(PROPERTY_LINES_DOWN_LIMIT, value);
			}
			value = ini.getLineUp();
			if(value != null){
				configuration.setProperty(PROPERTY_LINES_UP, value);
			}
			value = ini.getLineUpLimit();
			if(value != null){
				configuration.setProperty(PROPERTY_LINES_UP_LIMIT, value);
			}
			
			value = ini.getLineLeftLimit();
			if(value != null){
				configuration.setProperty(PROPERTY_LINES_LEFT_LIMIT, value);
			}
			value = ini.getLineRightLimit();
			if(value != null){
				configuration.setProperty(PROPERTY_LINES_RIGHT_LIMIT, value);
			}
			
			value = ini.getAreaThresholdMax();
			if(value != null){
				configuration.setProperty(PROPERTY_AREA_THRESHOLD_MAX, value);
			}
			value = ini.getAreaThresholdMin();
			if(value != null){
				configuration.setProperty(PROPERTY_AREA_THRESHOLD_MIN, value);
			}
			
			String taskId = ini.getTaskId();
			if(!StringUtils.isBlank(taskId)){
				configuration.setProperty(PROPERTY_TASKS_TASK_ID, taskId);
			}
			
			Boolean cropLimits = ini.getCropLimits();
			if(cropLimits != null) {
				configuration.setProperty(PROPERTY_CROP_LIMITS, cropLimits);
			}
			
			configuration.write(writer);
		} catch (IOException | ConfigurationException ex) { // should not happen
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Could not read configuration.");
		}
	}
}
