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
package service.tut.pori.fuzzysensors.frontend;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.util.ReflectionUtils;

import service.tut.pori.apilta.sensors.datatypes.SensorTask;
import service.tut.pori.apilta.sensors.reference.SensorsReferenceCore;
import service.tut.pori.fuzzysensors.FuzzyTask;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;

/**
 * fuzzy core methods for the fuzzy front end
 *
 */
public final class FuzzyFrontendCore {
	private static final Logger LOGGER = Logger.getLogger(FuzzyFrontendCore.class);
	
	/**
	 * 
	 */
	private FuzzyFrontendCore() {
		// nothing needed
	}

	/**
	 * 
	 * @param authenticatedUser 
	 * @param backendId
	 * @param taskId
	 * @param dataGroups
	 * @param limits
	 * @return fuzzy task
	 */
	public static FuzzyTask queryTaskDetails(UserIdentity authenticatedUser, Long backendId, String taskId, DataGroups dataGroups, Limits limits) {
		SensorTask sTask = SensorsReferenceCore.generateTaskDetails(authenticatedUser, backendId, dataGroups, limits, service.tut.pori.apilta.sensors.Definitions.TASK_TYPE_DATA_COLLECT); // create a simple sensor task
		FuzzyTask fTask = new FuzzyTask();
		ReflectionUtils.shallowCopyFieldState(sTask, fTask); //TODO check that this really works!!!
		return fTask;
	}

	/**
	 * 
	 * @param authenticatedUser 
	 * @param task
	 * @return id for the generated task
	 */
	public static String createTask(UserIdentity authenticatedUser, FuzzyTask task) {
		if(UserIdentity.isValid(authenticatedUser)){
			LOGGER.debug("Authenticated user, id: "+authenticatedUser.getUserId()); // simply log the user id for debug
		}
		return UUID.randomUUID().toString();
	}
}
