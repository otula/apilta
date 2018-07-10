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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.util.ArrayUtils;

/**
 * 
 * List of coordinates
 *
 */
@XmlRootElement(name=Definitions.ELEMENT_MULTIGEOMETRY, namespace = Definitions.NAMESPACE_KML)
@XmlAccessorType(XmlAccessType.NONE)
public class Coordinates {
	/** Coordinate separator */
	public static final char SEPARATOR_COORDINATES = ' ';
	private List<Coordinate> _coordinates = null;
	
	/**
	 * @return the value
	 */
	public String getValue() {
		if(_coordinates == null || _coordinates.isEmpty()){
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		for(Coordinate coordinate : _coordinates){
			sb.append(coordinate.toLngLatString());
			sb.append(SEPARATOR_COORDINATES);
		}
		
		sb.setLength(sb.length()-1); // chop the last separator
		return sb.toString();
	}

	/**
	 * @param value the value to set
	 */
	@XmlValue
	public void setValue(String value) {
		String[] pairs = StringUtils.split(value, SEPARATOR_COORDINATES);
		int count = ArrayUtils.getLength(pairs);
		if(count < 1){
			return;
		}
		
		_coordinates = new ArrayList<>(count);
		for(String pair : pairs){
			_coordinates.add(Coordinate.fromLngLat(pair));
		}
	}

	/**
	 * @return the coordinates
	 */
	public List<Coordinate> getCoordinates() {
		return _coordinates;
	}

	/**
	 * @param coordinates the coordinates to set
	 */
	public void setCoordinates(List<Coordinate> coordinates) {
		_coordinates = coordinates;
	}
	
	/**
	 * 
	 * @return calculate and return the average (lat/lng average) coordinate of the coordinate series or null if no coordinates in the series
	 */
	public Coordinate calculateAverageCoordinate() {
		if(_coordinates == null){
			return null;
		}
		int count = _coordinates.size();
		if(count == 0){
			return null;
		}
		
		Iterator<Coordinate> iter = _coordinates.iterator();
		if(count == 1){
			return iter.next();
		}
		
		double latSum = 0;
		double lngSum = 0;
		while(iter.hasNext()){
			Coordinate coordinate = iter.next();
			latSum += coordinate.getLatitude();
			lngSum += coordinate.getLongitude();
		}
		
		return new Coordinate(latSum/count, lngSum/count);
	}
}
