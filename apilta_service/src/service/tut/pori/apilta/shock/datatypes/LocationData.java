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
package service.tut.pori.apilta.shock.datatypes;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import core.tut.pori.utils.ISODateAdapter;

/**
 * 
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_LOCATION_DATA)
@XmlAccessorType(XmlAccessType.NONE)
public class LocationData {
	@XmlElement(name = service.tut.pori.apilta.sensors.datatypes.Definitions.ELEMENT_MEASUREMENT_ID)
	private String _measurementId = null;
	@XmlElement(name=Definitions.ELEMENT_LATITUDE)
	private Double _latitude = null;
	@XmlElement(name=Definitions.ELEMENT_LONGITUDE)
	private Double _longitude = null;
	@XmlElement(name=Definitions.ELEMENT_HEADING)
	private Double _heading = null;
	@XmlElement(name=Definitions.ELEMENT_SPEED)
	private Double _speed = null;
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	@XmlElement(name=Definitions.ELEMENT_TIMESTAMP)
	private Date _timestamp = null;
	
	/***
	 * for sub-classing, use the static
	 * 
	 * @return true if valid
	 * @see #isValid(LocationData)
	 */
	protected boolean isValid() {
		if(_latitude == null || _longitude == null) {
			return false;
		}else if(_latitude < -90 || _latitude > 90){
			return false;
		}else if(_longitude < -180 || _longitude > 180){
			return false;
		}else if(_speed != null && _speed < 0){
			return false;
		}else if(_heading != null && (_heading < 0 || _heading > 360)) {
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * 
	 * @param data
	 * @return true if valid and not null
	 */
	public static boolean isValid(LocationData data) {
		return (data != null && data.isValid());
	}
	
	/**
	 * @return the measurementId
	 */
	public String getMeasurementId() {
		return _measurementId;
	}
	
	/**
	 * @param measurementId the measurementId to set
	 */
	public void setMeasurementId(String measurementId) {
		_measurementId = measurementId;
	}
	
	/**
	 * @return the latitude
	 */
	public Double getLatitude() {
		return _latitude;
	}
	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(Double latitude) {
		_latitude = latitude;
	}
	
	/**
	 * @return the longitude
	 */
	public Double getLongitude() {
		return _longitude;
	}
	
	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(Double longitude) {
		_longitude = longitude;
	}
	
	/**
	 * @return the heading
	 */
	public Double getHeading() {
		return _heading;
	}

	/**
	 * @param heading the heading to set
	 */
	public void setHeading(Double heading) {
		_heading = heading;
	}

	/**
	 * @return the speed
	 */
	public Double getSpeed() {
		return _speed;
	}
	
	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(Double speed) {
		_speed = speed;
	}
	
	/**
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return _timestamp;
	}
	
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(Date timestamp) {
		_timestamp = timestamp;
	}
}
