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
package otula.backend.parking.datatypes;

import org.apache.commons.lang3.StringUtils;

/**
 * Latitude/Longitude coordinate
 * 
 */
public class Coordinate {
	/** Coordinate value separator */
	public static final char SEPARATOR_COORDINATE_VALUE = ',';
	private double _latitude = 0;
	private double _longitude = 0;
	
	/**
	 * 
	 * @param latitude
	 * @param longitude
	 */
	public Coordinate(double latitude, double longitude) {
		_latitude = latitude;
		_longitude = longitude;
	}
	
	/**
	 * 
	 */
	private Coordinate()  {
		// nothing needed
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return _latitude;
	}
	
	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return _longitude;
	}

	/**
	 * 
	 * @return The coordinate in longitude,latitude format
	 */
	public String toLngLatString() {
		StringBuilder sb = new StringBuilder();
		sb.append(_longitude);
		sb.append(SEPARATOR_COORDINATE_VALUE);
		sb.append(_latitude);
		return sb.toString();
	}
	
	/**
	 * 
	 * @return The coordinate in latitude,longitude format
	 */
	public String toLatLngString() {
		StringBuilder sb = new StringBuilder();
		sb.append(_latitude);
		sb.append(SEPARATOR_COORDINATE_VALUE);
		sb.append(_longitude);
		return sb.toString();
	}

	/**
	 * 
	 * @param coordinate in longitude,latitude format
	 * @return the coordinate
	 * @throws IllegalArgumentException on invalid coordinate string
	 */
	public static Coordinate fromLngLat(String coordinate) throws IllegalArgumentException {
		String[] values = StringUtils.split(coordinate, Coordinate.SEPARATOR_COORDINATE_VALUE);
		if(values.length < 2){
			throw new IllegalArgumentException("Invalid coordinate: "+coordinate);
		}
		
		Coordinate c = new Coordinate();
		c._latitude = Double.parseDouble(values[1]);
		c._longitude = Double.parseDouble(values[0]);
		return c;
	}
	
	/**
	 * 
	 * @param coordinate in latitude,longitude format
	 * @return the coordinate
	 * @throws IllegalArgumentException on invalid coordinate string
	 */
	public static Coordinate fromLatLng(String coordinate) throws IllegalArgumentException {
		String[] values = StringUtils.split(coordinate, Coordinate.SEPARATOR_COORDINATE_VALUE);
		if(values.length < 2){
			throw new IllegalArgumentException("Invalid coordinate: "+coordinate);
		}
		
		Coordinate c = new Coordinate();
		c._latitude = Double.parseDouble(values[0]);
		c._longitude = Double.parseDouble(values[1]);
		return c;
	}
}
