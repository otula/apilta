/**
 * Copyright 2014 Tampere University of Technology, Pori Department
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
package core.tut.pori.http.parameters;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import core.tut.pori.http.Definitions;

/**
 * The default parser for query parameters, 
 * the syntax being: ?query=VALUE
 * 
 * It is also possible to provide query for a specific type with ?query=TYPE:VALUE
 * 
 * The two variations can be combined: ?query=TYPE;VALUE,VALUE
 * 
 * The order of parameter values is preserved. Note that in all cases the order cannot be strictly preserved.
 * For example, ?query=VALUE,TYPE:VALUE2,VALUE3, in this case the non-typed VALUE and VALUE3 will be grouped, and thus,
 * both terms will appear before TYPE:VALUE2.
 */
public class QueryParameter extends HTTPParameter{
	/** Recommended name for the Query parameter */
	public static final String PARAMETER_DEFAULT_NAME = "query";
	private static final Logger LOGGER = Logger.getLogger(QueryParameter.class);
	private LinkedHashMap<String, Set<String>> _typeValueMap = null;	// map of types and their values, null key is the default, non-typed value set

	@Override
	public void initializeRaw(String parameterValue) throws IllegalArgumentException {
		parse(parameterValue);
	}

	@Override
	public void initializeRaw(List<String> parameterValues) throws IllegalArgumentException {
		for(Iterator<String> iter = parameterValues.iterator();iter.hasNext();){
			parse(iter.next());
		}
	}

	/**
	 * @param parameterValues list of URL decoded values, this will assume the whole string to be a single search term, WITHOUT type
	 */
	@Override
	public void initialize(List<String> parameterValues) throws IllegalArgumentException {
		for(Iterator<String> iter = parameterValues.iterator();iter.hasNext();){
			initialize(iter.next());
		}
	}

	/**
	 * @param parameterValue an URL decoded value, this will assume the whole string to be a single search term, WITHOUT type
	 */
	@Override
	public void initialize(String parameterValue) throws IllegalArgumentException {
		addQueryParameter(null, parameterValue);
	}
	
	/**
	 * 
	 */
	public QueryParameter(){
		super();
	}
	
	/**
	 * 
	 * @param queryString URL decoded query string
	 */
	public QueryParameter(String queryString) {
		if(queryString == null){
			LOGGER.debug("Null query string.");
		}else{
			initialize(queryString);
		}
	}

	/**
	 * 
	 * @param parameterValue in URL decoded form
	 * @throws IllegalArgumentException on bad input
	 */
	private void parse(String parameterValue) throws IllegalArgumentException {
		if(StringUtils.isBlank(parameterValue)){
			LOGGER.debug("Detected null or empty value for parameter: "+getParameterName());
			return;
		}
		String[] values = StringUtils.split(parameterValue, Definitions.SEPARATOR_URI_QUERY_TYPE_VALUE);
		try{
			if(values.length == 1){	// only value
				addQueryParameter(null, URLDecoder.decode(values[0], Definitions.ENCODING_UTF8));
			}else if(values.length == 2){ // type;value
				addQueryParameter( URLDecoder.decode(values[0], Definitions.ENCODING_UTF8),  URLDecoder.decode(values[1], Definitions.ENCODING_UTF8));
			}else{	// ;;;; or something
				throw new IllegalArgumentException("Invalid value "+parameterValue+" for paramater "+getParameterName());
			}
		} catch (UnsupportedEncodingException ex){	// this should never happen
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to decode "+parameterValue);	// but if it does happen, abort here
		}
	}

	/**
	 * 
	 * @param type type of the query term
	 * @param value the query term
	 */
	public void addQueryParameter(String type, String value){
		if(_typeValueMap == null){
			_typeValueMap = new LinkedHashMap<>();
		}
		Set<String> terms = _typeValueMap.get(type);
		if(terms == null){
			terms = new LinkedHashSet<>();
			_typeValueMap.put(type, terms);
		}
		terms.add(value);
	}

	@Override
	public boolean hasValues() {
		return (_typeValueMap != null);
	}

	/**
	 * @return the non-typed search string, or null if none available
	 */
	@Override
	public Set<String> getValue() {
		return getValues(null);
	}
	
	/**
	 * 
	 * @param typeName
	 * @return parameter values (queries)
	 */
	protected Set<String> getValues(String typeName){
		return (hasValues() ? _typeValueMap.get(typeName) : null);
	}
	
	/**
	 * 
	 * @param param
	 * @param typeName
	 * @return set for the type name or null if none
	 */
	public static Set<String> getValues(QueryParameter param, String typeName){
		if(param == null){
			return null;
		}else{
			return param.getValues(typeName);
		}
	}

	@Override
	public void initialize(InputStream parameterValue) throws IllegalArgumentException {
		try {
			initialize(IOUtils.toString(parameterValue, core.tut.pori.http.Definitions.CHARSET_UTF8));
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to read HTTP body.");
		}
	}
}
