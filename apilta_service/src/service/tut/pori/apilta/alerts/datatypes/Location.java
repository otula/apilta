/**
 * Copyright 2016 Tampere University of Technology, Pori Department
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
package service.tut.pori.apilta.alerts.datatypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

/**
 * class for representing location
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_LOCATION)
@XmlAccessorType(XmlAccessType.NONE)
public class Location {
	private static final Logger LOGGER = Logger.getLogger(Location.class);
	@XmlElement(name=Definitions.ELEMENT_HEADING)
	private Double _heading = null;
	@XmlElement(name=Definitions.ELEMENT_LATITUDE)
	private Double _latitude = null;
	@XmlElement(name=Definitions.ELEMENT_LONGITUDE)
	private Double _longitude = null;
	
	/**
	 * @return the latitude
	 * @see #setLatitude(Double)
	 */
	public Double getLatitude() {
		return _latitude;
	}
	
	/**
	 * @param latitude the latitude to set
	 * @see #getLatitude()
	 */
	public void setLatitude(Double latitude) {
		_latitude = latitude;
	}
	
	/**
	 * @return the longitude
	 * @see #setLongitude(Double)
	 */
	public Double getLongitude() {
		return _longitude;
	}
	
	/**
	 * @param longitude the longitude to set
	 * @see #getLongitude()
	 */
	public void setLongitude(Double longitude) {
		_longitude = longitude;
	}

	/**
	 * @return the heading
	 * @see #setHeading(Double)
	 */
	public Double getHeading() {
		return _heading;
	}

	/**
	 * @param heading the heading to set
	 * @see #getHeading()
	 */
	public void setHeading(Double heading) {
		_heading = heading;
	}
	
	/**
	 * 
	 * @return true if valid
	 */
	protected boolean isValid() {
		if(_heading != null && (_heading < 0 || _heading > 360)) {
			LOGGER.debug("Invalid heading.");
			return false;
		}else if(_longitude < -180 || _longitude > 180){
			LOGGER.debug("Invalid longitude.");
			return false;
		}else if(_latitude < -90 || _latitude > 90){
			LOGGER.debug("Invalid latitude.");
			return false;
		}else{
			return true;
		}
	}

	/**
	 * 
	 * @param location
	 * @return false if location is null or invalid
	 */
	public static boolean isValid(Location location) {
		return (location != null && location.isValid());
	}
}
