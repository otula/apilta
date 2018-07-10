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

import service.tut.pori.tasks.datatypes.Task.Visibility;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.ISODateAdapter;

/**
 * 
 * 
 */
@XmlRootElement(name=service.tut.pori.apilta.sensors.datatypes.Definitions.ELEMENT_MEASUREMENT)
@XmlAccessorType(XmlAccessType.NONE)
public class ShockMeasurement {
	@XmlElement(name=Definitions.ELEMENT_LEVEL)
	private Integer _level = null;
	@XmlElement(name = service.tut.pori.apilta.sensors.datatypes.Definitions.ELEMENT_MEASUREMENT_ID)
	private String _measurementId = null;
	@XmlElement(name = service.tut.pori.tasks.Definitions.ELEMENT_DATA_VISIBILITY)
	private Visibility _visibility = null;
	@XmlElement(name=core.tut.pori.users.Definitions.ELEMENT_USER_IDENTITY)
	private UserIdentity _userId = null;
	@XmlElement(name=Definitions.ELEMENT_ACCELEROMETER_DATA)
	private AccelerometerData _accelerometerData = null;
	@XmlElement(name=Definitions.ELEMENT_LOCATION_DATA)
	private LocationData _locationData = null;
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	@XmlElement(name=Definitions.ELEMENT_TIMESTAMP)
	private Date _timestamp = null;
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if valid
	 * @see #isValid(ShockMeasurement)
	 */
	protected boolean isValid() {
		if(_timestamp == null){
			return false;
		}else if(_accelerometerData != null && !AccelerometerData.isValid(_accelerometerData)){
			return false;
		}else if(_locationData != null && !LocationData.isValid(_locationData)){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * 
	 * @param measurement
	 * @return true if valid and not null
	 */
	public static boolean isValid(ShockMeasurement measurement) {
		return (measurement != null && measurement.isValid());
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
	 * @return the visibility
	 */
	public Visibility getVisibility() {
		return _visibility;
	}
	
	/**
	 * @param visibility the visibility to set
	 */
	public void setVisibility(Visibility visibility) {
		_visibility = visibility;
	}
	
	/**
	 * @return the userId
	 */
	public UserIdentity getUserId() {
		return _userId;
	}
	
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(UserIdentity userId) {
		_userId = userId;
	}
	
	/**
	 * @return the accelerometerData
	 */
	public AccelerometerData getAccelerometerData() {
		return _accelerometerData;
	}
	
	/**
	 * @param accelerometerData the accelerometerData to set
	 */
	public void setAccelerometerData(AccelerometerData accelerometerData) {
		_accelerometerData = accelerometerData;
	}
	
	/**
	 * @return the locationData
	 */
	public LocationData getLocationData() {
		return _locationData;
	}
	
	/**
	 * @param locationData the locationData to set
	 */
	public void setLocationData(LocationData locationData) {
		_locationData = locationData;
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

	/**
	 * @return the level
	 */
	public Integer getLevel() {
		return _level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(Integer level) {
		_level = level;
	}
}
