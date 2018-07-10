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
package service.tut.pori.apilta.alerts.reference;

import core.tut.pori.http.Response;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;

/**
 * Generates example XML output
 * 
 */
@HTTPService(name = Definitions.SERVICE_ALERTS_REFERENCE_EXAMPLE)
public class ExampleService {
	/**
	 * Generates example alert list
	 * 
	 * @param dataGroups 
	 * @param limits 
	 * @return alert list
	 * @see service.tut.pori.apilta.alerts.datatypes.AlertList
	 */
	@HTTPServiceMethod(name = service.tut.pori.apilta.alerts.datatypes.Definitions.ELEMENT_ALERT_LIST)
	public Response alertList(
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required=false, defaultValue="0-0") Limits limits
			) 
	{
		Example example = new Example();
		example.setAlertList(AlertsReferenceCore.generateAlertList(dataGroups, limits));
		return new Response(example);
	}
	
	/**
	 * Generates example alert
	 * 
	 * @param dataGroups 
	 * @return alert
	 * @see service.tut.pori.apilta.alerts.datatypes.Alert
	 */
	@HTTPServiceMethod(name = service.tut.pori.apilta.alerts.datatypes.Definitions.ELEMENT_ALERT)
	public Response alert(
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups
			)	
	{
		Example example = new Example();
		example.setAlert(AlertsReferenceCore.generateAlert(dataGroups));
		return new Response(example);
	}
}
