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
package service.tut.pori.apilta.sensors.datatypes;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import core.tut.pori.utils.ISODateAdapter;

/**
 * data point data to be used for solr storage
 *
 * The class has been modified from the original version:
 * - SolrJ dependencies have been removed (@Field annotation and core.tut.pori.dao.SolrDAO class)
 * - non-serialized field measurementId has been removed
 */
@XmlRootElement(name=Definitions.ELEMENT_DATAPOINT)
@XmlAccessorType(value=XmlAccessType.NONE)
public class DataPoint {
	private static final Logger LOGGER = Logger.getLogger(DataPoint.class);
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	@XmlElement(name = Definitions.ELEMENT_CREATED_TIMESTAMP)
	private Date _created = null;
	@XmlElement(name = Definitions.ELEMENT_DATAPOINT_ID)
	private String _dataPointId = null;
	@XmlElement(name = Definitions.ELEMENT_DESCRIPTION)
	private String _description = null;
	@XmlElement(name = Definitions.ELEMENT_KEY)
	private String _key = null;
	@XmlElement(name = Definitions.ELEMENT_VALUE)
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
			LOGGER.debug("Invalid key.");
			return false;
		}else if(StringUtils.isBlank(_value)){
			LOGGER.debug("Invalid value.");
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
