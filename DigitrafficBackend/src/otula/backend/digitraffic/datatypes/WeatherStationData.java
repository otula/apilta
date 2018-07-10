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

import java.util.Date;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Weather station data.
 * 
 * As defined in <a href="https://tie.digitraffic.fi/api/v1/data/documentation/swagger-ui.html#!/data/weatherDataUsingGET">Digitraffic documentation</a>
 * 
 */
public class WeatherStationData {
	@SerializedName(Definitions.JSON_NAME_ID)
	private Integer _id = null;
	@SerializedName(Definitions.JSON_NAME_MEASURED_TIME)
	private Date _measuredTime = null;
	@SerializedName(Definitions.JSON_NAME_SENSOR_VALUES)
	private List<SensorValue> _sensorValues = null;
	
	/**
	 * @return the id
	 */
	public Integer getId() {
		return _id;
	}
	
	/**
	 * @return the measuredTime
	 */
	public Date getMeasuredTime() {
		return _measuredTime;
	}
	
	/**
	 * @return the sensorValues
	 */
	public List<SensorValue> getSensorValues() {
		return _sensorValues;
	}
}
