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
package otula.backend.tasks.datatypes;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.quartz.JobBuilder;

import service.tut.pori.apilta.sensors.datatypes.Condition;
import service.tut.pori.apilta.sensors.datatypes.Measurement;
import service.tut.pori.apilta.sensors.datatypes.MeasurementList;
import service.tut.pori.apilta.sensors.datatypes.Output;
import service.tut.pori.tasks.TaskDAO;
import service.tut.pori.tasks.datatypes.Task;
import service.tut.pori.tasks.datatypes.TaskBackend;
import service.tut.pori.tasks.datatypes.TaskBackend.Status;

/**
 * Represents a single sensor task
 * 
 */
@XmlRootElement(name=service.tut.pori.tasks.Definitions.ELEMENT_TASK)
@XmlAccessorType(XmlAccessType.NONE)
public class SensorTask extends Task{
	private static final Logger LOGGER = Logger.getLogger(SensorTask.class);
	private String _callbackUri = null;
	@XmlElementWrapper(name = service.tut.pori.apilta.sensors.datatypes.Definitions.ELEMENT_WHEN)
	@XmlElementRef(type = Condition.class)
	private List<Condition> _conditions = null;
	@XmlElementWrapper(name = service.tut.pori.apilta.sensors.datatypes.Definitions.ELEMENT_WHAT)
	@XmlElementRef(type = Output.class)
	private List<Output> _output = null;
	/** Optional element for containing the task results */
	@XmlElementRef(type = MeasurementList.class)
	private MeasurementList _data = null;

	@Override
	public String getCallbackUri() {
		return _callbackUri;
	}
	
	/**
	 * 
	 * @param uri
	 */
	@Override
	public void setCallbackUri(String uri) {
		_callbackUri = uri;
	}

	@Override
	public TaskDAO getTaskDao() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Not implemented.");
	}

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
	public MeasurementList getMeasurements() {
		return _data;
	}

	/**
	 * @param data the data to set
	 */
	public void setMeasurements(MeasurementList data) {
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

	/**
	 * @return true if the task is valid, a valid task must have valid output+condition lists OR a valid measurement list (NOT both)
	 */
	@Override
	protected boolean isValid() {
		if(!super.isValid()){
			LOGGER.debug("Invalid base task.");
			return false;
		}else if(MeasurementList.isValid(_data)){
			if(_conditions != null || _output != null){
				LOGGER.debug("Giving measurement list in combination with output or condition list is not allowed.");
				return false;
			}
			
			List<TaskBackend> backends = getBackends();
			for(Measurement measurement : _data.getMeasurements()){ // check that the measurements do not contain back end identifiers that are not present in the task back end list
				Long backendId = measurement.getBackendId();
				boolean found = false;
				for(TaskBackend backend : backends){
					if(backend.getBackendId().equals(backendId)){
						found = true;
						break;
					}
				} // for back end
				if(!found){
					LOGGER.debug("Back end, id: "+backendId+" is not present in the task back end list.");
					return false;
				}
			} // for measurement
		}else{
			if(_conditions == null || _output == null || _conditions.isEmpty() || _output.isEmpty()){
				LOGGER.debug("No measurement list or conditions, output and back ends.");
				return false;
			}
			
			for(Condition condition : _conditions){
				if(!Condition.isValid(condition)){
					LOGGER.debug("Invalid condition.");
					return false;
				}
			}
			
			for(Output o : _output) {
				if(!Output.isValid(o)){
					LOGGER.debug("Invalid output.");
					return false;
				}
			}
		}
		
		return true;
	}

	@Override
	public JobBuilder getBuilder() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Not implemented.");
	}
	
	/**
	 * 
	 * @param backendId
	 * @param status
	 */
	public void addBackend(Long backendId, Status status){
		List<TaskBackend> backends = getBackends();
		if(backends == null){
			backends = new ArrayList<>();
			setBackends(backends);
		}
		TaskBackend backend = new TaskBackend();
		backend.setBackendId(backendId);
		backend.setStatus(status);
		backends.add(backend);
	}
	
	/**
	 * 
	 * @param measurement
	 */
	public void addMeasurement(Measurement measurement) {
		if(_data == null){
			_data = new MeasurementList();
		}
		_data.addMeasurement(measurement);
	}
}
