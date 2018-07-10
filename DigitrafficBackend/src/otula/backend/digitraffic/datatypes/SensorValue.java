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
 * A sensor value.
 * 
 * As defined in <a href="https://tie.digitraffic.fi/api/v1/data/documentation/swagger-ui.html#!/data/weatherDataUsingGET">Digitraffic documentation</a>
 * 
 */
public class SensorValue {
	@SerializedName(Definitions.JSON_NAME_ID)
	private Integer _id = null;
	@SerializedName(Definitions.JSON_NAME_NAME)
	private String _name = null;
	@SerializedName(Definitions.JSON_NAME_ROAD_STATION_ID)
	private Integer _roadStationId = null;
	@SerializedName(Definitions.JSON_NAME_SENSOR_VALUE)
	private String _sensorValue = null;
	@SerializedName(Definitions.JSON_NAME_SENSOR_VALUE_DESCRIPTION_EN)
	private String _sensorValueDescriptionEn = null;
	@SerializedName(Definitions.JSON_NAME_SENSOR_VALUE_DESCRIPTION_FI)
	private String _sensorValueDescriptionFi = null;
	@SerializedName(Definitions.JSON_NAME_SENSOR_UNIT)
	private String _sensorUnit = null;
	@SerializedName(Definitions.JSON_NAME_SHORT_NAME)
	private String _shortName = null;
	@SerializedName(Definitions.JSON_NAME_TIME_WINDOW_END)
	private String _timeWindowEnd = null;
	@SerializedName(Definitions.JSON_NAME_TIME_WINDOW_START)
	private String _timeWindowStart = null;
	
	/**
	 * @return the id
	 */
	public Integer getId() {
		return _id;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * @return the roadStationId
	 */
	public Integer getRoadStationId() {
		return _roadStationId;
	}
	
	/**
	 * @return the sensorValue
	 */
	public String getSensorValue() {
		return _sensorValue;
	}
	
	/**
	 * @return the sensorValueDescriptionEn
	 */
	public String getSensorValueDescriptionEn() {
		return _sensorValueDescriptionEn;
	}
	
	/**
	 * @return the sensorValueDescriptionFi
	 */
	public String getSensorValueDescriptionFi() {
		return _sensorValueDescriptionFi;
	}
	
	/**
	 * @return the sensorUnit
	 */
	public String getSensorUnit() {
		return _sensorUnit;
	}
	
	/**
	 * @return the shortName
	 */
	public String getShortName() {
		return _shortName;
	}
	
	/**
	 * @return the timeWindowEnd
	 */
	public String getTimeWindowEnd() {
		return _timeWindowEnd;
	}
	
	/**
	 * @return the timeWindowStart
	 */
	public String getTimeWindowStart() {
		return _timeWindowStart;
	}
}
