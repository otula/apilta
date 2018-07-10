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
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import core.tut.pori.http.ResponseData;

/**
 * lists a list of Measurement Data
 * 
 * @see service.tut.pori.apilta.sensors.datatypes.Measurement
 */
@XmlRootElement(name=Definitions.ELEMENT_MEASUREMENT_LIST)
@XmlAccessorType(XmlAccessType.NONE)
public class MeasurementList extends ResponseData {
	@XmlElementRef
	private List<Measurement> _measurements = null;

	/**
	 * @return the measurements
	 * @see #setMeasurements(List)
	 */
	public List<Measurement> getMeasurements() {
		return _measurements;
	}

	/**
	 * @param measurements the measurements to set
	 * @see #getMeasurements()
	 */
	public void setMeasurements(List<Measurement> measurements) {
		_measurements = measurements;
	}
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if the measurements in this list are valid
	 * @see #isValid(MeasurementList)
	 */
	protected boolean isValid() {
		if(_measurements == null || _measurements.isEmpty()){
			return false;
		}
		for(Measurement m : _measurements){
			if(!Measurement.isValid(m)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @param list
	 * @return false if the list is null or invalid
	 */
	public static boolean isValid(MeasurementList list) {
		if(list == null){
			return false;
		}else{
			return list.isValid();
		}
	}
	
	/**
	 * for sub-classing, use the static
	 * @return true if empty
	 * @see #isEmpty(MeasurementList)
	 */
	protected boolean isEmpty() {
		return (_measurements == null || _measurements.isEmpty());
	}
	
	/**
	 * 
	 * @param list
	 * @return true if the given list is null or empty
	 */
	public static boolean isEmpty(MeasurementList list) {
		return (list == null || list.isEmpty());
	}
	
	/**
	 * 
	 * @param measurement
	 * @see #getMeasurements()
	 */
	public void addMeasurement(Measurement measurement) {
		if(_measurements == null){
			_measurements = new ArrayList<>();
		}
		_measurements.add(measurement);
	}
}
