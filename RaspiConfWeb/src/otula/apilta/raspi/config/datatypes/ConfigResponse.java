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

import com.google.gson.annotations.SerializedName;

/**
 * 
 * wrapper for config responses
 */
public class ConfigResponse extends Response {
	@SerializedName(value=Definitions.JSON_RASPIINI)
	private RaspiIni _raspiIni = null;

	/**
	 * @return the raspiIni
	 */
	public RaspiIni getRaspiIni() {
		return _raspiIni;
	}

	/**
	 * @param raspiIni the raspiIni to set
	 */
	public void setRaspiIni(RaspiIni raspiIni) {
		_raspiIni = raspiIni;
	}
}
