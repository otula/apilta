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

import java.util.List;

/**
 * Represents a single sensor task
 * 
 */
public class SensorTask extends Task{
	private List<Condition> _conditions = null;     //when
	private List<Output> _output = null;            //what
	/** Optional element for containing the task results */
	private List<Measurement> _data = null;         //measurementList

	/**
	 * 
	 * @return list of {@link Condition}s
	 */
	public List<Condition> getConditions() {
		return _conditions;
	}

	/**
	 * 
	 * @param conditions
	 */
	public void setConditions(List<Condition> conditions) {
		_conditions = conditions;
	}

	/**
	 * @return the output
	 */
	public List<Output> getOutput() {
		return _output;
	}

	/**
	 * @param output the output to set
	 */
	public void setOutput(List<Output> output) {
		_output = output;
	}

	/**
	 * @return the data
	 */
	public List<Measurement> getMeasurements() {
		return _data;
	}

	/**
	 * @param data the data to set
	 */
	public void setMeasurements(List<Measurement> data) {
		_data = data;
	}
	
	/**
	 * 
	 */
	public SensorTask() {
		super();
	}
	
	/**
	 * Create new sensor task based on the details of the base class task
	 * 
	 * @param task
	 */
	public SensorTask(Task task) {
		super(task);
	}
}
