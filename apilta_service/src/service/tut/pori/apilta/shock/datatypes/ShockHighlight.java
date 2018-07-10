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
 * Single shock highlight
 *
 */
@XmlRootElement(name=Definitions.ELEMENT_SHOCK_HIGHLIGHT)
@XmlAccessorType(XmlAccessType.NONE)
public class ShockHighlight {
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	@XmlElement(name=Definitions.ELEMENT_FROM_TIMESTAMP)
	private Date _from = null;
	@XmlElement(name=Definitions.ELEMENT_LATITUDE)
	private Double _latitude = null;
	@XmlElement(name=Definitions.ELEMENT_LONGITUDE)
	private Double _longitude = null;
	@XmlElement(name=Definitions.ELEMENT_MAX_LEVEL)
	private Integer _maxLevel = null;
	@XmlElement(name=Definitions.ELEMENT_MAX_RANGE)
	private Double _maxRange = null;
	@XmlElement(name=Definitions.ELEMENT_MIN_LEVEL)
	private Integer _minLevel = null;
	@XmlElement(name=Definitions.ELEMENT_MEASUREMENT_COUNT)
	private int _measurementCount = 0;
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	@XmlElement(name=Definitions.ELEMENT_TO_TIMESTAMP)
	private Date _to = null;
	@XmlElement(name=Definitions.ELEMENT_USER_COUNT)
	private int _userCount = 0;
	
	/**
	 * @return the from
	 */
	public Date getFrom() {
		return _from;
	}
	
	/**
	 * @param from the from to set
	 */
	public void setFrom(Date from) {
		_from = from;
	}
	
	/**
	 * @return the maxLevel
	 */
	public Integer getMaxLevel() {
		return _maxLevel;
	}
	
	/**
	 * @param maxLevel the maxLevel to set
	 */
	public void setMaxLevel(Integer maxLevel) {
		_maxLevel = maxLevel;
	}
	
	/**
	 * @return the maxRange
	 */
	public Double getMaxRange() {
		return _maxRange;
	}
	
	/**
	 * @param maxRange the maxRange to set
	 */
	public void setMaxRange(Double maxRange) {
		_maxRange = maxRange;
	}
	
	/**
	 * @return the minLevel
	 */
	public Integer getMinLevel() {
		return _minLevel;
	}
	
	/**
	 * @param minLevel the minLevel to set
	 */
	public void setMinLevel(Integer minLevel) {
		_minLevel = minLevel;
	}
	
	/**
	 * @return the measurementCount
	 */
	public int getMeasurementCount() {
		return _measurementCount;
	}
	
	/**
	 * @param measurementCount the measurementCount to set
	 */
	public void setMeasurementCount(int measurementCount) {
		_measurementCount = measurementCount;
	}
	
	/**
	 * @return the to
	 */
	public Date getTo() {
		return _to;
	}
	
	/**
	 * @param to the to to set
	 */
	public void setTo(Date to) {
		_to = to;
	}
	
	/**
	 * @return the userCount
	 */
	public int getUserCount() {
		return _userCount;
	}
	
	/**
	 * @param userCount the userCount to set
	 */
	public void setUserCount(int userCount) {
		_userCount = userCount;
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
}
