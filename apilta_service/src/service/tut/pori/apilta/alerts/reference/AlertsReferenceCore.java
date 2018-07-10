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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.DateIntervalParameter.Interval;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;
import service.tut.pori.apilta.alerts.Definitions;
import service.tut.pori.apilta.alerts.datatypes.Alert;
import service.tut.pori.apilta.alerts.datatypes.AlertList;
import service.tut.pori.apilta.alerts.datatypes.Location;
import service.tut.pori.apilta.alerts.datatypes.LocationParameter;

/**
 * The reference implementations for Content Analysis Service.
 *
 */
public final class AlertsReferenceCore {
	private static final UserIdentity ALERT_USER_ID = new UserIdentity(22222L);
	private static final int BUFFER_SIZE = 256;
	private static final AlertsXMLObjectCreator CREATOR = new AlertsXMLObjectCreator(null);
	private static final Logger LOGGER = Logger.getLogger(AlertsReferenceCore.class);
	
	/**
	 * 
	 */
	private AlertsReferenceCore(){
		// nothing needed
	}

	/**
	 * 
	 * @param dataGroups 
	 * @param limits
	 * @return random alert list
	 */
	public static AlertList generateAlertList(DataGroups dataGroups, Limits limits) {
		return CREATOR.generateAlertList(null, null, null, dataGroups, limits, null, null);
	}

	/**
	 * 
	 * @param dataGroups 
	 * @return random alert
	 */
	public static Alert generateAlert(DataGroups dataGroups) {
		return CREATOR.generateAlert(null, null, null, dataGroups, null, null, null);
	}

	/**
	 * @param alertGroupIdFilter 
	 * @param alertTypeFilter 
	 * @param authenticatedUser 
	 * @param createdFilter 
	 * @param dataGroups 
	 * @param location 
	 * @param range 
	 * @param limits 
	 * @return randomly generated list of alerts matching the given terms
	 */
	public static AlertList retrieveAlerts(long[] alertGroupIdFilter, List<String> alertTypeFilter, UserIdentity authenticatedUser, Set<Interval> createdFilter, DataGroups dataGroups, Location location, Double range, Limits limits) {
		if(UserIdentity.isValid(authenticatedUser)){
			LOGGER.debug("Authenticated user, id: "+authenticatedUser.getUserId()); // simply log the user id for debug
		}
		
		if(!Location.isValid(location)){
			if(ArrayUtils.isEmpty(alertGroupIdFilter)){
				throw new IllegalArgumentException(LocationParameter.PARAMETER_DEFAULT_NAME+" or "+Definitions.PARAMETER_ALERT_GROUP_ID+" must be given.");
			}else if(range != null){
				throw new IllegalArgumentException(Definitions.PARAMETER_RANGE+" cannot be given without "+LocationParameter.PARAMETER_DEFAULT_NAME);
			}
		}else if(range == null){
			LOGGER.debug("Using default range: "+Definitions.DEFAULT_RANGE);
			range = Definitions.DEFAULT_RANGE;
		}else if(range <= 0){
			throw new IllegalArgumentException("Invalid range: "+range);
		}
		
		return CREATOR.generateAlertList(alertGroupIdFilter, alertTypeFilter, createdFilter, dataGroups, limits, location, range);
	}

	/**
	 * 
	 * @param alert
	 * @param alertGroupIds 
	 * @param authenticatedUser
	 * @return randomly generated id for the alert
	 * @throws IllegalArgumentException on bad input
	 */
	public static String addAlert(Alert alert, long[] alertGroupIds, UserIdentity authenticatedUser) throws IllegalArgumentException{
		if(ArrayUtils.isEmpty(alertGroupIds)){
			throw new IllegalArgumentException("Alert group ids must be given.");
		}
		
		if(UserIdentity.isValid(authenticatedUser)){
			LOGGER.debug("Authenticated user, id: "+authenticatedUser.getUserId()); // simply log the user id for debug
			alert.setUserId(authenticatedUser);
		}else{
			LOGGER.debug("No authenticated user, using default user id for validation.");
			alert.setUserId(ALERT_USER_ID);
		}
		
		if(!Alert.isValid(alert)){
			throw new IllegalArgumentException("Invalid alert.");
		}
		
		return UUID.randomUUID().toString();
	}

	/**
	 * 
	 * @param authenticatedUser 
	 * @param file the contents of the file are simply iterated as raw byte data (all content is discarded)
	 * @return randomly generated guid for the file
	 * @throws IllegalArgumentException on bad data
	 */
	public static String createFile(UserIdentity authenticatedUser, InputStream file) throws IllegalArgumentException {
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			while(file.read(buffer) > 0){
				// simply discard all data
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to read file.");
		}
		if(UserIdentity.isValid(authenticatedUser)){
			LOGGER.debug("Authenticated user, id: "+authenticatedUser.getUserId()); // simply log the user id for debug
		}
		return CREATOR.createGUID(); // accept any file, return random GUID
	}
}
