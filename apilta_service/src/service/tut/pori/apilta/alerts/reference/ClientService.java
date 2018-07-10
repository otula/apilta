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
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

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
import service.tut.pori.apilta.files.datatypes.FileDetails;
import service.tut.pori.apilta.files.datatypes.FileDetailsList;

/**
 * Reference implementation for client API methods.
 * 
 * <h1>Implementation Service path {@value service.tut.pori.apilta.alerts.Definitions#SERVICE_ALERTS}</h1>
 * 
 * @see service.tut.pori.apilta.alerts.AlertService
 *
 */
@HTTPService(name = Definitions.SERVICE_ALERTS_REFERENCE_CLIENT)
public class ClientService {
	private static final Logger LOGGER = Logger.getLogger(ClientService.class);
	private XMLFormatter _formatter = new XMLFormatter();
	
	/**
	 * This method can be used to retrieve alerts based on the given filters
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * GET /rest/{@value service.tut.pori.apilta.alerts.reference.Definitions#SERVICE_ALERTS_REFERENCE_CLIENT}/{@value service.tut.pori.apilta.alerts.Definitions#METHOD_GET_ALERTS}?{@value service.tut.pori.apilta.alerts.Definitions#PARAMETER_ALERT_GROUP_ID}=1<br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.apilta.alerts.reference.Definitions#SERVICE_ALERTS_REFERENCE_CLIENT]" method="[service.tut.pori.apilta.alerts.Definitions#METHOD_GET_ALERTS]" type="GET" query="[service.tut.pori.apilta.alerts.Definitions#PARAMETER_ALERT_GROUP_ID]=1" body_uri=""}
	 * 
	 * Either alertGroupIdFilter or location must be given. Range cannot be given without location.
	 * 
	 * @param authenticatedUser
	 * @param alertGroupIdFilter
	 * @param alertTypeFilter
	 * @param createdFilter 
	 * @param dataGroups valid groups are {@value DataGroups#DATA_GROUP_BASIC} (default) and {@value DataGroups#DATA_GROUP_ALL}. The basic group does not contain file details or other optional elements.
	 * @param location
	 * @param range in km, if not given, the default range is used ({@link service.tut.pori.apilta.alerts.Definitions#DEFAULT_RANGE})
	 * @param limits
	 * @return see {@link service.tut.pori.apilta.alerts.datatypes.AlertList}
	 */
	@HTTPServiceMethod(name = service.tut.pori.apilta.alerts.Definitions.METHOD_GET_ALERTS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response getAlerts(
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = service.tut.pori.apilta.alerts.Definitions.PARAMETER_ALERT_GROUP_ID, required = false) LongParameter alertGroupIdFilter,
			@HTTPMethodParameter(name = service.tut.pori.apilta.alerts.Definitions.PARAMETER_ALERT_TYPE, required = false) StringParameter alertTypeFilter,
			@HTTPMethodParameter(name = service.tut.pori.apilta.alerts.Definitions.PARAMETER_CREATED, required = false) DateIntervalParameter createdFilter,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = LocationParameter.PARAMETER_DEFAULT_NAME, required = false) LocationParameter location,
			@HTTPMethodParameter(name = service.tut.pori.apilta.alerts.Definitions.PARAMETER_RANGE, required = false) DoubleParameter range,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits
			)
	{
		return new Response(AlertsReferenceCore.retrieveAlerts(alertGroupIdFilter.getValues(), alertTypeFilter.getValues(), authenticatedUser.getUserIdentity(), createdFilter.getValues(), dataGroups, location.getValue(), range.getValue(), limits));
	}

	/**
	 * The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * POST /rest/{@value service.tut.pori.apilta.alerts.reference.Definitions#SERVICE_ALERTS_REFERENCE_CLIENT}/{@value service.tut.pori.apilta.alerts.Definitions#METHOD_ADD_ALERT}?{@value service.tut.pori.apilta.alerts.Definitions#PARAMETER_ALERT_GROUP_ID}=1<br>
	 * Content-Type: text/xml; charset=UTF-8<br><br>
	 *
	 * <b>[HTTP BODY STARTS]</b><br>
	 * 
	 * {@doc.restlet service="[service.tut.pori.apilta.alerts.reference.Definitions#SERVICE_ALERTS_REFERENCE_EXAMPLE]" method="[service.tut.pori.apilta.alerts.datatypes.Definitions#ELEMENT_ALERT]" type="GET" query="" body_uri=""} <br>
	 * 
	 * <b>[HTTP BODY ENDS]</b><br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.apilta.alerts.reference.Definitions#SERVICE_ALERTS_REFERENCE_CLIENT]" method="[service.tut.pori.apilta.alerts.Definitions#METHOD_ADD_ALERT]" type="POST" query="[service.tut.pori.apilta.alerts.Definitions#PARAMETER_ALERT_GROUP_ID]=1" body_uri="[service.tut.pori.apilta.alerts.reference.Definitions#SERVICE_ALERTS_REFERENCE_EXAMPLE]/[service.tut.pori.apilta.alerts.datatypes.Definitions#ELEMENT_ALERT]"}
	 * 
	 * 
	 * @param authenticatedUser
	 * @param alertGroupId the group which the alert should be associated with
	 * @param xml Only the workload data should be in the body. See {@link service.tut.pori.apilta.alerts.datatypes.Alert}
	 * @return response See {@link service.tut.pori.apilta.alerts.datatypes.AlertList}
	 */
	@HTTPServiceMethod(name = service.tut.pori.apilta.alerts.Definitions.METHOD_ADD_ALERT, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response addAlert (
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = service.tut.pori.apilta.alerts.Definitions.PARAMETER_ALERT_GROUP_ID) LongParameter alertGroupId,
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml
			) 
	{
		try {
			String body = IOUtils.toString(xml.getValue(), core.tut.pori.http.Definitions.CHARSET_UTF8); // read the body
			LOGGER.debug(body); // print to debug
			try(InputStream input = IOUtils.toInputStream(body, core.tut.pori.http.Definitions.CHARSET_UTF8)){ // convert back to stream for unmarshal
				String alertId = AlertsReferenceCore.addAlert(_formatter.toObject(input, Alert.class), alertGroupId.getValues(), authenticatedUser.getUserIdentity());
				AlertList alerts = new AlertList();
				ArrayList<Alert> list = new ArrayList<>(1);
				alerts.setAlerts(list);
				Alert alert = new Alert();
				alert.setAlertId(alertId);
				list.add(alert);
				return new Response(alerts);
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			return new Response(Status.BAD_REQUEST);
		}
	}
	
	/**
	 * The request is to be sent in the body of POST method.
	 * 
	 * <h2>Example Query:</h2>
	 *
	 * POST /rest/{@value service.tut.pori.apilta.alerts.reference.Definitions#SERVICE_ALERTS_REFERENCE_CLIENT}/{@value service.tut.pori.apilta.alerts.Definitions#METHOD_CREATE_FILE}<br>
	 * Content-Type: text/xml; charset=UTF-8<br><br>
	 *
	 * <b>[HTTP BODY STARTS]</b><br>
	 * 
	 * [ANY FILE CONTENT] <br>
	 * 
	 * <b>[HTTP BODY ENDS]</b><br>
	 *
	 * <h2>Example Result:</h2>
	 * 
	 * {@doc.restlet service="[service.tut.pori.apilta.alerts.reference.Definitions#SERVICE_ALERTS_REFERENCE_CLIENT]" method="[service.tut.pori.apilta.alerts.Definitions#METHOD_CREATE_FILE]" type="POST" query="" body_uri="[service.tut.pori.apilta.alerts.reference.Definitions#SERVICE_ALERTS_REFERENCE_EXAMPLE]/[service.tut.pori.apilta.alerts.datatypes.Definitions#ELEMENT_ALERT]"}
	 * 
	 * @param authenticatedUser
	 * @param file only the file contents should be in the body.
	 * @return {@link service.tut.pori.apilta.files.datatypes.FileDetailsList} with identifier for the generated file
	 */
	@HTTPServiceMethod(name = service.tut.pori.apilta.alerts.Definitions.METHOD_CREATE_FILE, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response createFile (
			@HTTPAuthenticationParameter(required = false) AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter file
			) 
	{
		FileDetailsList list = new FileDetailsList();
		FileDetails details = new FileDetails();
		details.setGUID(AlertsReferenceCore.createFile(authenticatedUser.getUserIdentity(), file.getValue()));
		list.addFile(details);
		return new Response(list);
	}
}
