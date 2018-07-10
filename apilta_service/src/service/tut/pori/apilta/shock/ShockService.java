/**
 * Copyright 2018 Tampere University of Technology, Pori Department
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
package service.tut.pori.apilta.shock;

import service.tut.pori.apilta.shock.GroupCalculator.GroupMethod;
import service.tut.pori.apilta.shock.datatypes.LocationLimits;
import service.tut.pori.apilta.shock.datatypes.ShockMeasurementList;
import core.tut.pori.http.Response;
import core.tut.pori.http.annotations.HTTPAuthenticationParameter;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.AuthenticationParameter;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.DateIntervalParameter;
import core.tut.pori.http.parameters.DoubleParameter;
import core.tut.pori.http.parameters.InputStreamParameter;
import core.tut.pori.http.parameters.IntegerParameter;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.LongParameter;
import core.tut.pori.http.parameters.StringParameter;
import core.tut.pori.utils.XMLFormatter;

/**
 * 
 */
@HTTPService(name = Definitions.SERVICE_SHOCK)
public class ShockService {
	private XMLFormatter _formatter = new XMLFormatter();
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param xml {@link ShockMeasurementList}
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_CREATE_MEASUREMENT, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void createMeasurement (
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter = true) InputStreamParameter xml
			) 
	{
		ShockCore.createMeasurement(authenticatedUser.getUserIdentity(), _formatter.toObject(xml.getValue(), ShockMeasurementList.class));
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param locationLimits
	 * @param dataGroups
	 * @param levelFilter 
	 * @param dateInterval 
	 * @param limits
	 * @param groupMethod 
	 * @param groupRange 
	 * @param userIdFilter 
	 * @return see {@link service.tut.pori.apilta.shock.datatypes.ShockMeasurementList}
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_GET_MEASUREMENTS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response getMeasurements (
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = LocationLimits.PARAMETER_DEFAULT_NAME, required = false) LocationLimits locationLimits,
			@HTTPMethodParameter(name = DataGroups.PARAMETER_DEFAULT_NAME, required = false) DataGroups dataGroups,
			@HTTPMethodParameter(name = Definitions.PARAMETER_LEVEL, required = false) IntegerParameter levelFilter,
			@HTTPMethodParameter(name = Definitions.PARAMETER_TIMESTAMP, required = false) DateIntervalParameter dateInterval,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits,
			@HTTPMethodParameter(name = Definitions.PARAMETER_GROUP_METHOD, required = false) StringParameter groupMethod,
			@HTTPMethodParameter(name = Definitions.PARAMETER_GROUP_RANGE, required = false) IntegerParameter groupRange,
			@HTTPMethodParameter(name = service.tut.pori.users.Definitions.PARAMETER_USER_ID, required = false) LongParameter userIdFilter
			) 
	{
		return new Response(ShockCore.getMeasurements(authenticatedUser.getUserIdentity(), locationLimits, dataGroups, dateInterval, levelFilter.getValues(), limits, (groupMethod.hasValues() ? GroupMethod.fromString(groupMethod.getValue()) : null), groupRange.getValue(), userIdFilter.getValues()));
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param minMeasurements 
	 * @param range 
	 * @param locationLimits
	 * @param levelFilter 
	 * @param dateInterval 
	 * @param limits
	 * @param userIdFilter 
	 * @return see {@link service.tut.pori.apilta.shock.datatypes.ShockHighlightList}
	 */
	@HTTPServiceMethod(name = Definitions.METHOD_GET_HIGHLIGHTS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response getHighlights (
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_MIN_MEASUREMENTS) IntegerParameter minMeasurements,
			@HTTPMethodParameter(name = Definitions.PARAMETER_RANGE) DoubleParameter range,
			@HTTPMethodParameter(name = LocationLimits.PARAMETER_DEFAULT_NAME, required = false) LocationLimits locationLimits,
			@HTTPMethodParameter(name = Definitions.PARAMETER_LEVEL, required = false) IntegerParameter levelFilter,
			@HTTPMethodParameter(name = Definitions.PARAMETER_TIMESTAMP, required = false) DateIntervalParameter dateInterval,
			@HTTPMethodParameter(name = Limits.PARAMETER_DEFAULT_NAME, required = false) Limits limits,
			@HTTPMethodParameter(name = service.tut.pori.users.Definitions.PARAMETER_USER_ID, required = false) LongParameter userIdFilter
			) 
	{
		return new Response(ShockCore.getHighlights(authenticatedUser.getUserIdentity(), minMeasurements.getValue(), range.getValue(), locationLimits, dateInterval, levelFilter.getValues(), limits, userIdFilter.getValues()));
	}
}
