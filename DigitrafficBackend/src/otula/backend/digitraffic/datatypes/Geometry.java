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

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Geometry details.
 * 
 * As defined in <a href="https://tie.digitraffic.fi/api/v1/data/documentation/swagger-ui.html#!/metadata/weatherStationsUsingGET">Digitraffic documentation</a>
 * 
 */
public class Geometry {
	@SerializedName(Definitions.JSON_NAME_COORDINATES)
	private Double[] _coordinateData = null;
	@SerializedName(Definitions.JSON_NAME_TYPE)
	private String _type = null;
	
	/**
	 * @return the type
	 */
	public String getType() {
		return _type;
	}
	
	/**
	 * 
	 * @return the longitude
	 */
	public Double getLongitude() {
		return (ArrayUtils.getLength(_coordinateData) < 2 ? null : _coordinateData[0]); // valid array must have atleast longitude and latitude
	}
	
	/**
	 * 
	 * @return the latitude
	 */
	public Double getLatitude() {
		return (ArrayUtils.getLength(_coordinateData) < 2 ? null : _coordinateData[1]); // valid array must have atleast longitude and latitude
	}
	
	/**
	 * 
	 * @return the altitude
	 */
	public Double getAltitude() {
		return (ArrayUtils.getLength(_coordinateData) < 3 ? null : _coordinateData[2]);
	}
}
