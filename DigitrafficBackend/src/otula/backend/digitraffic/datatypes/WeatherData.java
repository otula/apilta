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
 * Weather data.
 * 
 * As defined in <a href="https://tie.digitraffic.fi/api/v1/data/documentation/swagger-ui.html#!/data/weatherDataUsingGET">Digitraffic documentation</a>
 * 
 */
public class WeatherData {
	@SerializedName(Definitions.JSON_NAME_DATA_UPDATED)
	private Date _dataUpdatedTime = null;
	@SerializedName(Definitions.JSON_NAME_WEATHER_STATIONS)
	private List<WeatherStationData> _weatherStations = null;
	
	/**
	 * @return the dataUpdatedTime
	 */
	public Date getDataUpdatedTime() {
		return _dataUpdatedTime;
	}
	
	/**
	 * @return the weatherStations
	 */
	public List<WeatherStationData> getWeatherStations() {
		return _weatherStations;
	}
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if this data contains no sensor values
	 * @see #isEmpty()
	 */
	protected boolean isEmpty(){
		if(_weatherStations != null && !_weatherStations.isEmpty()){
			for(WeatherStationData data : _weatherStations){
				List<SensorValue> values = data.getSensorValues();
				if(values != null && !values.isEmpty()){
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @param data
	 * @return true if the data is null or contains no sensor values
	 */
	public static boolean isEmpty(WeatherData data){
		return (data == null || data.isEmpty());
	}
}
