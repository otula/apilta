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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

/**
 * representation of measurement data
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_MEASUREMENT)
@XmlAccessorType(XmlAccessType.NONE)
public class Measurement {
	private static final Logger LOGGER = Logger.getLogger(Measurement.class);
	@XmlElement(name = Definitions.ELEMENT_BACKEND_ID)
	private Long _backendId = null;
	@XmlElementWrapper(name = Definitions.ELEMENT_DATAPOINT_LIST)
	@XmlElementRef(type = DataPoint.class)
	private List<DataPoint> _dataPoints = null;
	@XmlElement(name = Definitions.ELEMENT_MEASUREMENT_ID)
	private String _measurementId = null; // generated id
	
	/**
	 * @return the backendId
	 * @see #setBackendId(Long)
	 */
	public Long getBackendId() {
		return _backendId;
	}

	/**
	 * @param backendId the backendId to set
	 * @see #getBackendId()
	 */
	public void setBackendId(Long backendId) {
		_backendId = backendId;
	}

	/**
	 * @return the data points
	 * @see #setDataPoints(List)
	 */
	public List<DataPoint> getDataPoints() {
		return _dataPoints;
	}

	/**
	 * @param dataPoints the dataPoints to set
	 * @see #getDataPoints()
	 */
	public void setDataPoints(List<DataPoint> dataPoints) {
		_dataPoints = dataPoints;
	}

	/**
	 * @return the measurementId
	 * @see #setMeasurementId(String)
	 */
	public String getMeasurementId() {
		return _measurementId;
	}
	
	/**
	 * @param measurementId the measurementId to set
	 * @see #getMeasurementId()
	 */
	public void setMeasurementId(String measurementId) {
		_measurementId = measurementId;
	}
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if valid
	 * @see #isValid(Measurement)
	 */
	protected boolean isValid() {
		if(_backendId == null) {
			LOGGER.debug("Invalid back end id.");
			return false;
		}
		
		if(_dataPoints == null || _dataPoints.isEmpty()) {
			LOGGER.debug("No data points.");
			return false;
		}
		
		for(DataPoint dp : _dataPoints){
			if(!DataPoint.isValid(dp)){
				LOGGER.debug("Measurement for back end, id: "+_backendId+" contained an invalid data point.");
				return false;
			}
		}
		
		return true;
	}

	/**
	 * 
	 * @param measurement
	 * @return false if the measurement is null or invalid
	 */
	public static boolean isValid(Measurement measurement) {
		if(measurement == null){
			return false;
		}else{
			return measurement.isValid();
		}
	}
	
	/**
	 * 
	 * @param dataPoint
	 * @see #getDataPoints()
	 */
	public void addDataPoint(DataPoint dataPoint){
		if(_dataPoints == null){
			_dataPoints = new ArrayList<>();
		}
		_dataPoints.add(dataPoint);
	}
}
