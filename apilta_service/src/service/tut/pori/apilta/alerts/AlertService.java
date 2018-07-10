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

import java.util.ArrayList;

import core.tut.pori.http.Response;
import core.tut.pori.http.Response.Status;
import core.tut.pori.http.annotations.HTTPAuthenticationParameter;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.AuthenticationParameter;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.DateIntervalParameter;
import core.tut.pori.http.parameters.DoubleParameter;
import core.tut.pori.http.parameters.InputStreamParameter;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.LongParameter;
import core.tut.pori.http.parameters.StringParameter;
import core.tut.pori.utils.XMLFormatter;
import service.tut.pori.apilta.alerts.datatypes.Alert;
import service.tut.pori.apilta.alerts.datatypes.AlertList;
import service.tut.pori.apilta.alerts.datatypes.LocationParameter;
import service.tut.pori.apilta.files.datatypes.FileDetailsList;

/**
 * Service declaration for the alerts service
 * 
 */
@HTTPService(name = Definitions.SERVICE_ALERTS)
public class AlertService {
	private XMLFormatter _formatter = new XMLFormatter();
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param alertGroupIdFilter
	 * @param alertTypeFilter
	 * @param createdFilter 
	 * @param dataGroups 
	 * @param location
	 * @param range
	 * @param limits
	 * @return see {@link service.tut.pori.apilta.alerts.datatypes.AlertList}
	 * @see service.tut.pori.apilta.alerts.reference.ClientService#getAlerts(AuthenticationParameter, LongParameter, StringParameter, DateIntervalParameter, DataGroups, LocationParameter, DoubleParameter, Limits)
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_GET_ALERTS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response getAlerts(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_ALERT_GROUP_ID, required = false) LongParameter alertGroupIdFilter,
			@HTTPMethodParameter(name = Definitions.PARAMETER_ALERT_TYPE, required = false) StringParameter alertTypeFilter,
			@HTTPMethodParameter(name = Definitions.PARAMETER_CREATED, required = false) DateIntervalParameter createdFilter,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = LocationParameter.PARAMETER_DEFAULT_NAME, required = false) LocationParameter location,
			@HTTPMethodParameter(name = Definitions.PARAMETER_RANGE, required = false) DoubleParameter range,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits
			)
	{
		return new Response(AlertsCore.retrieveAlerts(alertGroupIdFilter.getValues(), alertTypeFilter.getValues(), authenticatedUser.getUserIdentity(), createdFilter.getValues(), dataGroups, limits, location.getValue(), range.getValue()));
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param alertGroupId 
	 * @param xml Only the workload data should be in the body. See {@link service.tut.pori.apilta.alerts.datatypes.Alert}
	 * @return response See {@link service.tut.pori.apilta.alerts.datatypes.AlertList}
	 * @see service.tut.pori.apilta.alerts.reference.ClientService#addAlert(AuthenticationParameter, LongParameter, InputStreamParameter)
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_ADD_ALERT, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response addAlert (
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_ALERT_GROUP_ID) LongParameter alertGroupId,
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml
			) 
	{
		Response r = new Response();
		String alertId = AlertsCore.addAlert(_formatter.toObject(xml.getValue(), Alert.class), alertGroupId.getValues(), authenticatedUser.getUserIdentity());
		if(alertId == null){
			r.setStatus(Status.FORBIDDEN);
		}else{
			Alert alert = new Alert();
			alert.setAlertId(alertId);
			ArrayList<Alert> list = new ArrayList<>(1);
			list.add(alert);
			AlertList alerts = new AlertList();
			alerts.setAlerts(list);
			r.setResponseData(alerts);
		}
		return r;
	}
	
	/**
	 * @param authenticatedUser
	 * @param file only the file contents should be in the body.
	 * @return {@link service.tut.pori.apilta.files.datatypes.FileDetailsList} with identifier for the generated file
	 * @see service.tut.pori.apilta.alerts.reference.ClientService#createFile(AuthenticationParameter, InputStreamParameter)
	 */
	@HTTPServiceMethod(name = service.tut.pori.apilta.alerts.Definitions.METHOD_CREATE_FILE, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response createFile (
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter file
			) 
	{
		FileDetailsList list = new FileDetailsList();
		list.addFile(AlertsCore.createFile(authenticatedUser.getUserIdentity(), file.getValue()));
		return new Response(list);
	}
}
