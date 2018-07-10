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
package fi.tut.pori.otula.roadroamer.datatypes;

import android.util.Log;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class DataPoint {
    private static final String CLASS_NAME = DataPoint.class.toString();
	private Date _created = null;
	private String _dataPointId = null;
	private String _description = null;
	private String _key = null;
	private Long _measurementId = null; // This field is not serialized to xml
	private String _value = null;
	
	

	/**
	 * @return the dataPointId
	 * @see #setDataPointId(String)
	 */
	public String getDataPointId() {
		return _dataPointId;
	}

	/**
	 * @param dataPointId the dataPointId to set
	 * @see #getDataPointId()
	 */
	public void setDataPointId(String dataPointId) {
		_dataPointId = dataPointId;
	}

	/**
	 * @return the measurementId
	 * @see #setMeasurementId(Long)
	 */
	public Long getMeasurementId() {
		return _measurementId;
	}

	/**
	 * @param measurementId the measurementId to set
	 * @see #getMeasurementId()
	 */
	public void setMeasurementId(Long measurementId) {
		_measurementId = measurementId;
	}

	/**
	 * @return the value
	 * @see #setValue(String)
	 */
	public String getValue() {
		return _value;
	}

	/**
	 * @param value the value to set
	 * @see #getValue()
	 */
	public void setValue(String value) {
		_value = value;
	}
	
	/**
	 * @return the description
	 * @see #setDescription(String)
	 */
	public String getDescription() {
		return _description;
	}

	/**
	 * @param description the description to set
	 * @see #getDescription()
	 */
	public void setDescription(String description) {
		_description = description;
	}

	/**
	 * @return the key
	 * @see #setKey(String)
	 */
	public String getKey() {
		return _key;
	}

	/**
	 * @param key the key to set
	 * @see #getKey()
	 */
	public void setKey(String key) {
		_key = key;
	}
	
	

	/**
	 * @return the created
	 * @see #setCreated(Date)
	 */
	public Date getCreated() {
		return _created;
	}

	/**
	 * @param created the created to set
	 * @see #getCreated()
	 */
	public void setCreated(Date created) {
		_created = created;
	}

	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if this data point is valid
	 * @see #isValid(DataPoint)
	 */
	protected boolean isValid() {
		if(StringUtils.isBlank(_key)){
            Log.d(CLASS_NAME, "Invalid key.");
			return false;
		}else if(StringUtils.isBlank(_value)){
            Log.d(CLASS_NAME, "Invalid value.");
			return false;
		}else{
			return true;
		}
	}

	/**
	 * 
	 * @param dataPoint
	 * @return false if the data point is null or invalid
	 */
	public static boolean isValid(DataPoint dataPoint) {
		if(dataPoint == null){
			return false;
		}else{
			return dataPoint.isValid();
		}
	}
}
