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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import core.tut.pori.http.ResponseData;
import service.tut.pori.apilta.sensors.datatypes.Definitions;

/**
 * 
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_MEASUREMENT_LIST)
@XmlAccessorType(XmlAccessType.NONE)
public class ShockMeasurementList extends ResponseData {
	@XmlElementRef
	private List<ShockMeasurement> _shockMeasurements = null;
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if valid
	 */
	protected boolean isValid() {
		if(isEmpty()){
			return false;
		}
		for(ShockMeasurement m : _shockMeasurements){
			if(!ShockMeasurement.isValid(m)){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @param list
	 * @return true if the list is not null, empty or contain invalid data
	 */
	public static boolean isValid(ShockMeasurementList list) {
		return (list != null && list.isValid());
	}

	/**
	 * @return the shockMeasurements
	 */
	public List<ShockMeasurement> getShockMeasurements() {
		return _shockMeasurements;
	}

	/**
	 * @param shockMeasurements the shockMeasurements to set
	 */
	public void setShockMeasurements(List<ShockMeasurement> shockMeasurements) {
		_shockMeasurements = shockMeasurements;
	}
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if list is empty
	 */
	protected boolean isEmpty() {
		return (_shockMeasurements == null || _shockMeasurements.isEmpty());
	}
	
	/**
	 * 
	 * @param list
	 * @return true if the list is null or empty
	 */
	public static boolean isEmpty(ShockMeasurementList list) {
		return (list == null || list.isEmpty());
	}
	
	/**
	 * 
	 * @param measurements
	 * @return return list object containing the given list or null if null or empty list was passed
	 */
	public static ShockMeasurementList getShockMeasurementList(List<ShockMeasurement> measurements) {
		if(measurements == null || measurements.isEmpty()) {
			return null;
		}
		ShockMeasurementList list = new ShockMeasurementList();
		list._shockMeasurements = measurements;
		return list;
	}
}
