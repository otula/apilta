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
package otula.backend.digitraffic.datatypes.triggers;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * a location
 * 
 */
public class Location {
	private Double _latitude = null;
	private Double _longitude = null;
	
	/**
	 * 
	 * @param latitude
	 * @param longitude
	 */
	public Location(Double latitude, Double longitude){
		_latitude = latitude;
		_longitude = longitude;
	}
	
	/**
	 * Construct from coordinate string: latitude,longitude
	 * 
	 * @param latLng
	 * @throws IllegalArgumentException on invalid coordinate string
	 */
	public Location(String latLng) throws IllegalArgumentException{
		String[] parts = StringUtils.split(latLng, ',');
		if(ArrayUtils.getLength(parts) < 2){
			throw new IllegalArgumentException("Invalid coordinate string: "+latLng);
		}
		_latitude = Double.valueOf(parts[0]);
		_longitude = Double.valueOf(parts[1]);
	}

	/**
	 * @return the latitude
	 */
	public Double getLatitude() {
		return _latitude;
	}

	/**
	 * @return the longitude
	 */
	public Double getLongitude() {
		return _longitude;
	}
}
