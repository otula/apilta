/**
 * Copyright 2017 Tampere University of Technology, Pori Department
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

import org.apache.commons.lang3.StringUtils;

import core.tut.pori.dao.filter.AbstractQueryFilter;
import core.tut.pori.http.Definitions;

/**
 * Basic filter for searching documents based on coordinate data.
 * 
 * The default filter type for this filter is {@link service.tut.pori.apilta.alerts.datatypes.AndGeoFilter#DEFAULT_FILTER_TYPE}.
 */
public class AndGeoFilter implements AbstractQueryFilter {
	/** the default filter type */
	public static final FilterType DEFAULT_FILTER_TYPE = FilterType.bbox;
	private static final String PARAMETER_FIELD_NAME = "sfield";
	private static final String PARAMETER_POINT = "pt";
	private static final String PARAMETER_RANGE = "d";
	private static final char PARAMETER_SEPARATOR = ' ';
	private String _fieldName = null;
	private FilterType _filterType = DEFAULT_FILTER_TYPE;
	private Location _location = null;
	private Double _range = null;
	
	/**
	 * Solr spatial filter type as defined in <a href="https://cwiki.apache.org/confluence/display/solr/Spatial+Search">Solr documentation</a>
	 * 
	 */
	public enum FilterType {
		/** filter of type geofilt */
		geofilt,
		/** filter of type bounding box */
		bbox
	} // enum FilterType
	
	/**
	 * 
	 * @param fieldName 
	 * @param location
	 * @param range
	 * @throws IllegalArgumentException
	 */
	public AndGeoFilter(String fieldName, Location location, Double range) throws IllegalArgumentException {
		if(range == null || range <= 0){
			throw new IllegalArgumentException("Invalid range: "+range);
		}
		if(StringUtils.isBlank(fieldName)){
			throw new IllegalArgumentException("Invalid field name: "+fieldName);
		}
		if(!Location.isValid(location)){
			throw new IllegalArgumentException("Invalid location.");
		}
		_location = location;
		_range = range;
		_fieldName = fieldName;
	}
	
	/**
	 * 
	 * @param fieldName 
	 * @param location
	 * @param filterType 
	 * @param range
	 * @throws IllegalArgumentException
	 */
	public AndGeoFilter(String fieldName, Location location, FilterType filterType, Double range) throws IllegalArgumentException {
		this(fieldName, location, range);
		if(filterType == null){
			throw new IllegalArgumentException("Invalid filter type: "+filterType);
		}
		_filterType = filterType;
	}
	
	
	@Override
	public void toFilterString(StringBuilder fq) {
		fq.append("{!");
		fq.append(_filterType);
		
		fq.append(PARAMETER_SEPARATOR);		
		fq.append(PARAMETER_POINT);
		fq.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE);
		fq.append(_location.getLatitude());
		fq.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
		fq.append(_location.getLongitude());
		
		fq.append(PARAMETER_SEPARATOR);
		fq.append(PARAMETER_FIELD_NAME);
		fq.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE);
		fq.append(_fieldName);
		
		fq.append(PARAMETER_SEPARATOR);
		fq.append(PARAMETER_RANGE);
		fq.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE);
		fq.append(_range);
		
		fq.append('}');
	}

	@Override
	public QueryType getQueryType() {
		return QueryType.AND;
	}

	/**
	 * @return the filterType
	 */
	public FilterType getFilterType() {
		return _filterType;
	}
}
