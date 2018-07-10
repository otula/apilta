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
package service.tut.pori.apilta.alerts;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.DateIntervalParameter.Interval;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.ListUtils;
import service.tut.pori.apilta.ApiltaProperties;
import service.tut.pori.apilta.alerts.datatypes.Alert;
import service.tut.pori.apilta.alerts.datatypes.AlertList;
import service.tut.pori.apilta.alerts.datatypes.AlertUserIdentity.UserPermission;
import service.tut.pori.apilta.alerts.datatypes.Location;
import service.tut.pori.apilta.alerts.datatypes.LocationParameter;
import service.tut.pori.apilta.files.FilesCore;
import service.tut.pori.apilta.files.datatypes.FileDetails;
import service.tut.pori.apilta.files.datatypes.FileDetailsList;

/**
 * 
 * core methods for alerts service
 */
public final class AlertsCore {
	private static final Logger LOGGER = Logger.getLogger(AlertsCore.class);
	
	/**
	 * 
	 */
	private AlertsCore() {
		// nothing needed
	}

	/**
	 * 
	 * @param alertGroupIdFilter
	 * @param alertTypeFilter
	 * @param authenticatedUser 
	 * @param createdFilter 
	 * @param dataGroups 
	 * @param limits
	 * @param location
	 * @param range
	 * @return list of alerts or null if none was found
	 */
	public static AlertList retrieveAlerts(long[] alertGroupIdFilter, List<String> alertTypeFilter, UserIdentity authenticatedUser, Set<Interval> createdFilter, DataGroups dataGroups, Limits limits, Location location, Double range) {
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
		
		List<Long> validGroupIds = ServiceInitializer.getDAOHandler().getDAO(AlertGroupsDAO.class).getAlertGroupIds(alertGroupIdFilter, UserPermission.READ_ALERTS, authenticatedUser);
		if(validGroupIds == null){
			LOGGER.debug("No valid alert groups for user, id: "+authenticatedUser.getUserId());
			return null;
		}
		
		AlertList alertList = ServiceInitializer.getDAOHandler().getDAO(AlertsDAO.class).getAlerts(validGroupIds, alertTypeFilter, createdFilter, dataGroups, limits, location, range);
		if(!AlertList.isEmpty(alertList)){
			for(Alert alert : alertList.getAlerts()){
				FileDetailsList fileDetailsList = alert.getFiles();
				if(!FileDetailsList.isEmpty(fileDetailsList)){ // alert has file details
					List<FileDetails> GUIDList = fileDetailsList.getFiles(); // the basic list only contains GUIDs
					ArrayList<FileDetails> resolved = new ArrayList<>(GUIDList.size());
					for(FileDetails GUIDDetails : GUIDList){ // resolve file details
						String guid = GUIDDetails.getGUID();
						FileDetails resolvedDetails = FilesCore.getFileDetails(guid);
						if(resolvedDetails == null){
							LOGGER.warn("Could not resovle details for file, GUID: "+guid);
						}else{
							resolved.add(resolvedDetails);
						}
					}
					fileDetailsList.setFiles((resolved.isEmpty() ? null : resolved)); // replace the original list
				}
			}
		}
		
		return alertList;
	}

	/**
	 * 
	 * @param alert
	 * @param alertGroupIds 
	 * @param authenticatedUser
	 * @return identifier for the added alert or null on failure (permission denied)
	 * @throws IllegalArgumentException on bad input data
	 */
	public static String addAlert(Alert alert, long[] alertGroupIds, UserIdentity authenticatedUser) throws IllegalArgumentException {
		if(ArrayUtils.isEmpty(alertGroupIds)){
			throw new IllegalArgumentException("Invalid or missing alert group ids.");
		}
		alert.setUserId(authenticatedUser); // make sure the authenticated user is the owner of this alert
		if(!Alert.isValid(alert)){
			throw new IllegalArgumentException("Invalid alert.");
		}
		
		List<Long> validGroupIds = ServiceInitializer.getDAOHandler().getDAO(AlertGroupsDAO.class).getAlertGroupIds(alertGroupIds, UserPermission.POST_ALERT, authenticatedUser);
		if(!ListUtils.containsAll(validGroupIds, alertGroupIds)){
			LOGGER.warn("User, id: "+authenticatedUser.getUserId()+" attempted to post alert to invaliding alert group id.");
			return null;
		}
		
		Date created = alert.getCreated();
		if(created == null){
			LOGGER.debug("No created date, using current timestamp.");
			created = new Date();
			alert.setCreated(created);
		}

		if(alert.getValidUntil() == null){ //TODO check from database if a specific validity time is available for the alert, also, create the database tables
			alert.setValidUntil(new Date(created.getTime()+ServiceInitializer.getPropertyHandler().getSystemProperties(ApiltaProperties.class).getAlertValidityTime()*60000));
		}
		
		return ServiceInitializer.getDAOHandler().getDAO(AlertsDAO.class).addAlert(alert, validGroupIds); // use valid group ids list to remove possible duplicates in the id list
		
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param file
	 * @return details for the created file or null on failure (permission denied)
	 * @throws IllegalArgumentException on bad data
	 */
	public static FileDetails createFile(UserIdentity authenticatedUser, InputStream file) throws IllegalArgumentException {
		FileDetails details = FilesCore.createFile(file);
		if(!FileDetails.isValid(details)){
			throw new IllegalArgumentException("Failed to create file from the given data.");
		}
		
		return details;
	}
	
	//TODO "floating" rating system for alerts? possibility to confirm/reject an existing alert? (implement on web page? do not allow on the mobile app?)
	// i.e. "alert feedback", also allow users to remove their own alerts directly?
}
