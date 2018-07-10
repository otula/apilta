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
@XmlRootElement(name=Definitions.ELEMENT_ACCELEROMETER_DATA)
@XmlAccessorType(XmlAccessType.NONE)
public class AccelerometerData {
	@XmlElement(name = service.tut.pori.apilta.sensors.datatypes.Definitions.ELEMENT_MEASUREMENT_ID)
	private String _measurementId = null;
	@XmlElement(name=Definitions.ELEMENT_SYSTEMATIC_ERROR)
	private Double _systematicError = null;
	@XmlElement(name=Definitions.ELEMENT_X_ACCELERATION)
	private Double _xAcceleration = null;
	@XmlElement(name=Definitions.ELEMENT_Y_ACCELERATION)
	private Double _yAcceleration = null;
	@XmlElement(name=Definitions.ELEMENT_Z_ACCELERATION)
	private Double _zAcceleration = null;
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	@XmlElement(name=Definitions.ELEMENT_TIMESTAMP)
	private Date _timestamp = null;
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if valid
	 * @see #isValid(AccelerometerData)
	 */
	protected boolean isValid() {
		return (_xAcceleration != null && _xAcceleration != null && _yAcceleration != null);
	}
	
	/**
	 * 
	 * @param data
	 * @return true if valid and not null
	 */
	public static boolean isValid(AccelerometerData data) {
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
	 * @return the xAcceleration
	 */
	public Double getxAcceleration() {
		return _xAcceleration;
	}
	
	/**
	 * @param xAcceleration the xAcceleration to set
	 */
	public void setxAcceleration(Double xAcceleration) {
		_xAcceleration = xAcceleration;
	}
	
	/**
	 * @return the yAcceleration
	 */
	public Double getyAcceleration() {
		return _yAcceleration;
	}
	
	/**
	 * @param yAcceleration the yAcceleration to set
	 */
	public void setyAcceleration(Double yAcceleration) {
		_yAcceleration = yAcceleration;
	}
	
	/**
	 * @return the zAcceleration
	 */
	public Double getzAcceleration() {
		return _zAcceleration;
	}
	
	/**
	 * @param zAcceleration the zAcceleration to set
	 */
	public void setzAcceleration(Double zAcceleration) {
		_zAcceleration = zAcceleration;
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
	 * @return the systematicError
	 */
	public Double getSystematicError() {
		return _systematicError;
	}

	/**
	 * @param systematicError the systematicError to set
	 */
	public void setSystematicError(Double systematicError) {
		_systematicError = systematicError;
	}
}
