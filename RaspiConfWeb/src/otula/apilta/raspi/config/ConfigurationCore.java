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
package otula.apilta.raspi.config;

import java.io.File;

import otula.apilta.raspi.config.datatypes.RaspiIni;
import otula.apilta.raspi.config.datatypes.RaspiProperties;
import otula.apilta.raspi.config.utils.IniHandler;

/**
 * 
 * 
 */
public final class ConfigurationCore {
	private static final IniHandler INI_HANDLER = new IniHandler();
	private static RaspiProperties PROPERTIES = null;
	
	/**
	 * 
	 * @param config
	 * @throws IllegalArgumentException 
	 */
	public static void saveConfig(RaspiIni config) throws IllegalArgumentException {
		if(!RaspiIni.isValid(config)){
			throw new IllegalArgumentException("Invalid configuration.");
		}
		
		synchronized (INI_HANDLER) {
			INI_HANDLER.save(config, getProperties().getIniFile());
		}
	}
	
	/**
	 * 
	 * @return the currently active configuration
	 */
	public static RaspiIni loadConfig() {
		synchronized (INI_HANDLER) {
			return INI_HANDLER.load(getProperties().getIniFile());
		}
	}
	
	/**
	 * 
	 * @return file
	 */
	public static File getPhotoFile() {
		String path = getProperties().getPhotoPath();
		File file = null;
		long newest = 0;
		for(File f : new File(path).listFiles()){
			long time = f.lastModified();
			if(time > newest){
				newest = time;
				file = f;
			}
		}
		return file;
	}
	
	/**
	 * 
	 * @return the properties
	 */
	private static RaspiProperties getProperties() {
		if(PROPERTIES == null){
			PROPERTIES = new RaspiProperties(); // note: this has potential race condition, but possibly creating the object twice is cheaper than synchronizing
		}
		return PROPERTIES;
	}
	
	/**
	 * 
	 */
	private ConfigurationCore() {
		// nothing needed
	}
}
