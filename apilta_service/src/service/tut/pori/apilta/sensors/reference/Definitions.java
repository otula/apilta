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

/**
 * reference definitions
 * 
 */
public final class Definitions {
	/* elements */
	/** xml element declaration */
	protected static final String ELEMENT_EXAMPLE = "example";
	
	/* methods */
	/** service method declaration */
	public static final String METHOD_TASK_DETAILS = service.tut.pori.tasks.Definitions.ELEMENT_TASK+"/details";
	/** service method declaration */
	public static final String METHOD_TASK_RESULTS = service.tut.pori.tasks.Definitions.ELEMENT_TASK+"/results";
	
	/* services */
	/** service name declaration */
	public static final String SERVICE_SENSORS_REFERENCE_BACKEND = "cserb";
	/** service name declaration */
	public static final String SERVICE_SENSORS_REFERENCE_CLIENT = "cserc";
	/** service name declaration */
	public static final String SERVICE_SENSORS_REFERENCE_EXAMPLE = "csere";
	/** service name declaration */
	public static final String SERVICE_SENSORS_REFERENCE_SERVER = "csers";
	
	/* parameters */
	/** parameter declaration */
	protected static final String PARAMETER_TASK_TYPE = "task_type";

	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
