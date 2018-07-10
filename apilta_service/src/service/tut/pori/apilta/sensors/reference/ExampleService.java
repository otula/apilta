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

import org.apache.commons.lang3.StringUtils;

import core.tut.pori.http.Response;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.StringParameter;

/**
 * Generates example XML output
 * 
 */
@HTTPService(name = Definitions.SERVICE_SENSORS_REFERENCE_EXAMPLE)
public class ExampleService {
	/**
	 * Generates example traffic data
	 * 
	 * @param limits 
	 * @return traffic data
	 * @see service.tut.pori.apilta.sensors.datatypes.MeasurementList
	 */
	@HTTPServiceMethod(name = service.tut.pori.apilta.sensors.datatypes.Definitions.ELEMENT_MEASUREMENT)
	public Response trafficData(
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits
			) 
	{
		Example example = new Example();
		example.setMeasurements(SensorsReferenceCore.generateTrafficData(limits));
		return new Response(example);
	}
	
	/**
	 * Generates example task details
	 * 
	 * @param dataGroups
	 * @param limits paging limits
	 * @param taskType Will default to {@link service.tut.pori.apilta.sensors.Definitions#TASK_TYPE_DATA_COLLECT}, if missing
	 * @return task details
	 * @see service.tut.pori.apilta.sensors.datatypes.SensorTask
	 * @see #taskDetails(DataGroups, Limits, StringParameter)
	 */
	@HTTPServiceMethod(name = service.tut.pori.tasks.Definitions.ELEMENT_TASK)
	public Response task(
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required=false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits,
			@HTTPMethodParameter(name = Definitions.PARAMETER_TASK_TYPE, required=false) StringParameter taskType
			) 
	{
		return taskDetails(dataGroups, limits, taskType);
	}
	
	/**
	 * Generates example task details
	 * 
	 * @param dataGroups
	 * @param limits paging limits
	 * @param taskType Will default to {@link service.tut.pori.apilta.sensors.Definitions#TASK_TYPE_DATA_COLLECT}, if missing
	 * @return task details
	 * @see service.tut.pori.apilta.sensors.datatypes.SensorTask
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_TASK_DETAILS)
	public Response taskDetails(
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required=false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits,
			@HTTPMethodParameter(name = Definitions.PARAMETER_TASK_TYPE, required=false) StringParameter taskType
			) 
	{
		Example example = new Example();
		String type = taskType.getValue();
		example.setTask(SensorsReferenceCore.generateTaskDetails(null, null, dataGroups, limits, (StringUtils.isBlank(type) ? service.tut.pori.apilta.sensors.Definitions.TASK_TYPE_DATA_COLLECT : type)));
		return new Response(example);
	}
	
	/**
	 * Generates example task results
	 * 
	 * @param dataGroups
	 * @param limits paging limits
	 * @param taskType Will default to {@link service.tut.pori.apilta.sensors.Definitions#TASK_TYPE_DATA_COLLECT}, if missing
	 * @return task details
	 * @see service.tut.pori.apilta.sensors.datatypes.SensorTask
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_TASK_RESULTS)
	public Response taskResults(
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required=false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits,
			@HTTPMethodParameter(name = Definitions.PARAMETER_TASK_TYPE, required=false, defaultValue=service.tut.pori.apilta.sensors.Definitions.TASK_TYPE_DATA_COLLECT) StringParameter taskType
			) 
	{
		Example example = new Example();
		example.setTask(SensorsReferenceCore.generateTaskResults(null, dataGroups, limits, taskType.getValue()));
		return new Response(example);
	}
}
