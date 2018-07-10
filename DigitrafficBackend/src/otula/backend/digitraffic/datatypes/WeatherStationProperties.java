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
package otula.backend.digitraffic.datatypes;

import com.google.gson.annotations.SerializedName;

/**
 * A minimal implementation of properties for a weather station.
 * 
 * As defined in <a href="https://tie.digitraffic.fi/api/v1/data/documentation/swagger-ui.html#!/metadata/weatherStationsUsingGET">Digitraffic documentation</a>
 * 
 */
public class WeatherStationProperties implements Properties {
	@SerializedName(Definitions.JSON_NAME_ROAD_STATION_ID)
	private Integer _roadStationId = null;

	/**
	 * @return the roadStationId
	 */
	public Integer getRoadStationId() {
		return _roadStationId;
	}
}
