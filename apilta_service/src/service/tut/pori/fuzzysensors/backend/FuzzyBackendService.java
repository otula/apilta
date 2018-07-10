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
package service.tut.pori.fuzzysensors.backend;

import service.tut.pori.fuzzysensors.Definitions;
import service.tut.pori.fuzzysensors.FuzzyTask;
import core.tut.pori.http.annotations.HTTPAuthenticationParameter;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.AuthenticationParameter;
import core.tut.pori.http.parameters.InputStreamParameter;
import core.tut.pori.utils.XMLFormatter;

/**
 * Fuzzy service declaration for the fuzzy sensors test back end
 * 
 */
@HTTPService(name=Definitions.SERVICE_FUZZY_SENSORS_BACKEND)
public class FuzzyBackendService {
	private XMLFormatter _formatter = new XMLFormatter();

	/**
	 * Implementation of add task for fuzzy sensors
	 * 
	 * @param authenticatedUser 
	 * @param xml Only the workload data should be in the body. See {@link service.tut.pori.fuzzysensors.FuzzyTask}
	 */
	@HTTPServiceMethod(name = service.tut.pori.tasks.Definitions.METHOD_ADD_TASK, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void addTask (
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser, // require authentication, but accept any known user
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml
			) 
	{
		FuzzyBackendCore.addTask(authenticatedUser.getUserIdentity(), _formatter.toObject(xml.getValue(), FuzzyTask.class));
	}
}
