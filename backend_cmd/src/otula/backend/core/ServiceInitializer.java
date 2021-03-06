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
package otula.backend.core;

import org.apache.commons.lang3.NotImplementedException;

/**
 * This class has been modified from the original version:
 * 
 * Unimplemented ServiceInitializer.
 * 
 * This class only exists to comply with the interface described in {@link otula.backend.tasks.datatypes.Configuration} 
 *
 */
public abstract class ServiceInitializer {

	/**
	 * 
	 * @return null
	 * @throws NotImplementedException when called
	 */
	public static ConfigHandler getConfigHandler() throws NotImplementedException {
		throw new NotImplementedException("Not Implemented.");
	}

	/**
	 * Interface for ConfigHandler
	 */
	public interface ConfigHandler {
		/**
		 * 
		 * @return property file path
		 */
		public String getPropertyFilePath();
	} // interface ConfigHandler
}
