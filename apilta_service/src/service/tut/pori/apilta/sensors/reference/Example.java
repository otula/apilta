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
package service.tut.pori.apilta.sensors.reference;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import service.tut.pori.apilta.sensors.datatypes.SensorTask;
import service.tut.pori.apilta.sensors.datatypes.MeasurementList;
import core.tut.pori.http.ResponseData;

/**
 * Response example
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_EXAMPLE)
@XmlAccessorType(XmlAccessType.NONE)
public class Example extends ResponseData {
	@XmlElementRef
	private SensorTask _task = null;
	@XmlElementRef
	private MeasurementList _measurements = null;

	/**
	 * overridden to gather the real classes required for serialization
	 */
	@Override
	public Class<?>[] getDataClasses() {
		List<Class<?>> classes = new ArrayList<>();
		classes.add(getClass());
		if(_measurements != null){
			classes.add(_measurements.getClass());
		}
		if(_task != null){
			classes.add(_task.getClass());
		}
		return classes.toArray(new Class<?>[classes.size()]);
	}

	/**
	 * @return the measurements
	 */
	public MeasurementList getMeasurements() {
		return _measurements;
	}

	/**
	 * @param measurements the measurements to set
	 */
	public void setMeasurements(MeasurementList measurements) {
		_measurements = measurements;
	}

	/**
	 * @return the task
	 */
	public SensorTask getTask() {
		return _task;
	}

	/**
	 * @param task the task to set
	 */
	public void setTask(SensorTask task) {
		_task = task;
	}
}
