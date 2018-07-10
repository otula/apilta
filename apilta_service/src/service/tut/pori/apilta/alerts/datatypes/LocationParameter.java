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
package service.tut.pori.apilta.alerts.datatypes;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import core.tut.pori.http.Definitions;
import core.tut.pori.http.parameters.HTTPParameter;

/**
 * The default parser for location parameters.
 * 
 * The syntax being: ?location=LOCATION_PARAMETER{@value Definitions#SEPARATOR_URI_QUERY_TYPE_VALUE}LOCATION_VALUE{@value Definitions#SEPARATOR_URI_QUERY_TYPE_VALUE}LOCATION_PARAMETER{@value Definitions#SEPARATOR_URI_QUERY_TYPE_VALUE}LOCATION_VALUE
 * 
 * Possible LOCATION_PARAMETERs and corresponding LOCATION_VALUEs:
 * 
 * <ul>
 *  <li>"{@value service.tut.pori.apilta.alerts.datatypes.LocationParameter#LOCATION_PARAMETER_COORDINATE}" and "latitude longitude". 
 *  The datum for the coordinate depends on the service, but both latitude and longitude must be in numeric (Java double) format with decimal separator being period (.). 
 *  Latitude and longitude must be separated by white space.</li>
 *  <li>"{@value service.tut.pori.apilta.alerts.datatypes.LocationParameter#LOCATION_PARAMETER_HEADING}" and "heading_value". Heading value (generally, in degrees) must be a double with period (.) as decimal separator.</li>
 * </ul>
 * 
 * For example, the location: ?location={@value service.tut.pori.apilta.alerts.datatypes.LocationParameter#LOCATION_PARAMETER_COORDINATE}{@value Definitions#SEPARATOR_URI_QUERY_TYPE_VALUE}0 0{@value Definitions#SEPARATOR_URI_QUERY_TYPE_VALUE}{@value service.tut.pori.apilta.alerts.datatypes.LocationParameter#LOCATION_PARAMETER_HEADING}{@value Definitions#SEPARATOR_URI_QUERY_TYPE_VALUE}0 would be location in coordinate 0.0 and the heading would be 0
 *
 * The parser does not validate the given values, remember to check the returned {@link #getValue()} object with {@link service.tut.pori.apilta.alerts.datatypes.Location#isValid(Location)}.
 * Especially, the presence of heading value without coordinate values should be checked by the implementing service if checks are required.
 * 
 * If multiple values are given, the output is unspecified (most likely, the last value given in the query URL will be used for initialization).
 */
public class LocationParameter extends HTTPParameter {
	/** location parameter for a coordinate value */
	public static final String LOCATION_PARAMETER_COORDINATE = "coordinate";
	/** location parameter for a heading value */
	public static final String LOCATION_PARAMETER_HEADING = "heading";
	/** the default HTTP parameter name */
	public static final String PARAMETER_DEFAULT_NAME = "location";
	private static final Logger LOGGER = Logger.getLogger(LocationParameter.class);
	private Location _location = null;

	@Override
	public void initialize(List<String> parameterValues) throws IllegalArgumentException {
		for(String value : parameterValues){
			initialize(value);
		}
	}

	@Override
	public void initialize(String parameterValue) throws IllegalArgumentException {
		String[] valuePairs = StringUtils.split(parameterValue, Definitions.SEPARATOR_URI_QUERY_TYPE_VALUE);
		if(ArrayUtils.isEmpty(valuePairs) || valuePairs.length % 2 != 0){ // check that we have values and that the list contains pairs (param + value)
			throw new IllegalArgumentException("Invalid value string: "+parameterValue);
		}
		
		for(int i=0;i<valuePairs.length;++i){
			switch(valuePairs[i]){
				case LOCATION_PARAMETER_COORDINATE:
					parseCoordinate(valuePairs[++i]);
					break;
				case LOCATION_PARAMETER_HEADING:
					parseHeading(valuePairs[++i]); // the next string is the value parameter
					break;
				default:
					throw new IllegalArgumentException("Invalid value string: "+parameterValue);
			}
		}
	}
	
	/**
	 * 
	 * @param coordinate in the format LATITUDE LONGITUDE, with latitude and longitude fitting in double and being separated by a whitespace
	 * @throws IllegalArgumentException
	 */
	private void parseCoordinate(String coordinate) throws IllegalArgumentException {
		String[] latLon = StringUtils.split(coordinate);
		if(ArrayUtils.isEmpty(latLon) || latLon.length != 2){
			throw new IllegalArgumentException("Invalid value for "+LOCATION_PARAMETER_COORDINATE+" : "+coordinate);
		}
		
		Double latitude = null;
		Double longitude = null;
		try{
			latitude = Double.valueOf(latLon[0]);
			longitude = Double.valueOf(latLon[1]);
		} catch (NumberFormatException ex){
			LOGGER.warn(ex, ex);
			throw new IllegalArgumentException("Invalid value for "+LOCATION_PARAMETER_COORDINATE+" : "+coordinate);
		}
		
		if(_location == null){
			_location = new Location();
		}
		_location.setLatitude(latitude);
		_location.setLongitude(longitude);
	}
	
	/**
	 * 
	 * @param heading as an integer value
	 * @throws IllegalArgumentException
	 */
	private void parseHeading(String heading) throws IllegalArgumentException {
		Double h = null;
		try{
			h = Double.valueOf(heading);
		} catch (NumberFormatException ex){
			LOGGER.warn(ex, ex);
			throw new IllegalArgumentException("Invalid value for "+LOCATION_PARAMETER_HEADING+" : "+heading);
		}
		
		if(_location == null){
			_location = new Location();
		}
		_location.setHeading(h);
	}

	@Override
	public boolean hasValues() {
		return (_location != null);
	}

	@Override
	public Location getValue() {
		return _location;
	}
}
