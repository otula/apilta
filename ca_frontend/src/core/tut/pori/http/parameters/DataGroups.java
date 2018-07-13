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

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import core.tut.pori.http.Definitions;

/**
 * Default dataGroups parser.
 * 
 * Syntax: ?data_groups=DATA_GROUP_VALUE or with type ?data_groups=TYPE;DATA_GROUP_VALUE, these can be combined: ?data_groups=DATA_GROUP_VALUE,TYPE;DATA_GROUP_VALUE
 * 
 * If you want to give multiple data groups for a single type, you can achieve it by duplicating the type declaration, i.e: ?data_groups=TYPE1;DATA_GROUP_VALUE1,TYPE1;DATA_GROUP_VALUE2
 * 
 * Note: this has no way of knowing if the given string are valid for the required context, you should check the values manually
 *
 * There is no concept of order for the data groups, and they can be provided in any order for the data_groups parameter.
 */
public final class DataGroups extends HTTPParameter {
	/** If present in a request, all available content should be returned in the response */
	public static final String DATA_GROUP_ALL = "all";
	/** If present in a request, the basic should be returned in the response. Note that the definition of "basic" is service specific. */
	public static final String DATA_GROUP_BASIC = "basic";
	/** If present in a request, the default content should be returned in the response. This in general equals to not providing a data group parameter. */
	public static final String DATA_GROUP_DEFAULTS = "defaults";
	/** the default HTTP parameter name */
	public static final String PARAMETER_DEFAULT_NAME = "data_groups";
	private static final Logger LOGGER = Logger.getLogger(DataGroups.class);
	private HashMap<String, Set<String>> _dataGroups = null;	// type-datagroup map, null = default, non-typed datagroups

	@Override
	public void initialize(List<String> parameterValues) throws IllegalArgumentException {
		for(Iterator<String> iter = parameterValues.iterator();iter.hasNext();){
			parse(iter.next());
		}
	}

	@Override
	public void initialize(String parameterValue) throws IllegalArgumentException{
		parse(parameterValue);
	}
	
	/**
	 * 
	 * @param parameterValue
	 */
	private void parse(String parameterValue){
		if(StringUtils.isBlank(parameterValue)){
			LOGGER.debug("Detected null or empty value for parameter: "+getParameterName());
			return;
		}
		
		String[] parts = StringUtils.split(parameterValue, Definitions.SEPARATOR_URI_QUERY_TYPE_VALUE);
		if(parts.length == 2){	// type;value
			addDataGroup(parts[1], parts[0]);
		}else if(parts.length == 1){	// only value
			addDataGroup(parts[0]);
		}else{
			throw new IllegalArgumentException("Invalid value "+parameterValue+" for paramater "+getParameterName());
		}
	}

	@Override
	public boolean hasValues() {
		return (_dataGroups != null);
	}
	
	/**
	 * varargs constructor for data groups
	 * 
	 * @param dataGroups
	 */
	public DataGroups(String ...dataGroups) {
		initialize(Arrays.asList(dataGroups));
	}
	
	/**
	 * required for serialization
	 */
	public DataGroups(){
		// nothing needed
	}
	
	/**
	 * Create a new copy based on the given dataGroup, this will create a deep copy of the given object.
	 * 
	 * @param dataGroups not-null
	 */
	public DataGroups(DataGroups dataGroups){
		if(dataGroups._dataGroups != null){
			_dataGroups = new HashMap<>(dataGroups._dataGroups.size());
			for(Entry<String,Set<String>> e : dataGroups._dataGroups.entrySet()){
				_dataGroups.put(e.getKey(), new HashSet<>(e.getValue()));
			}
		}
	}

	/**
	 * for sub-classing, use the static
	 * 
	 * @param dataGroup
	 * @param type
	 * @return true if the given data group is given for the given type. If the type is not found, returns true if the data group is given by default (non-typed).
	 */
	protected boolean hasDataGroup(String dataGroup, String type){
		if(_dataGroups == null){
			LOGGER.debug("No datagroups.");
			return false;
		}
		Set<String> dataGroups = _dataGroups.get(type);
		if(dataGroups == null && type != null){	// did not find for the type, try if there are defaults
			dataGroups = _dataGroups.get(null);
		}
		return (dataGroups == null ? false : dataGroups.contains(dataGroup));
	}

	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if this group contains no data groups
	 */
	protected boolean isEmpty(){
		return (_dataGroups == null ? true : _dataGroups.isEmpty());
	}

	/**
	 * 
	 * @param dataGroup
	 * @param dataGroups can be null
	 * @return true if the data group contains the given group
	 */
	public static boolean hasDataGroup(String dataGroup, DataGroups dataGroups){
		if(dataGroups == null){
			return false;
		}else{
			return dataGroups.hasDataGroup(dataGroup, (String)null);
		}
	}
	
	/**
	 * 
	 * @param dataGroup
	 * @param dataGroups can be null
	 * @param type
	 * @return true if the given data group is given for the given type and data group. If the type is not found, returns true if the data group is given by default (non-typed).
	 */
	public static boolean hasDataGroup(String dataGroup, DataGroups dataGroups, String type){
		if(dataGroups == null){
			return false;
		}else{
			return dataGroups.hasDataGroup(dataGroup, type);
		}
	}

	/**
	 * Remove the data group from the default type
	 * 
	 * @param dataGroup
	 * @return true if the dataGroup was removed
	 */
	public boolean removeDataGroup(String dataGroup) {
		return removeDataGroup(dataGroup, null);
	}
	
	/**
	 * Remove data group from the given type
	 * 
	 * @param dataGroup
	 * @param type
	 * @return true if the group was removed (existed in this group)
	 */
	public boolean removeDataGroup(String dataGroup, String type){
		if(_dataGroups == null){
			return false;
		}
		Set<String> dataGroups = _dataGroups.get(type);
		if(dataGroups == null){
			return false;
		}
		return dataGroups.remove(dataGroup);
	}

	/**
	 * Add datagroup to the default, non-typed datagroup list
	 * 
	 * @param dataGroup
	 */
	public void addDataGroup(String dataGroup) {
		addDataGroup(dataGroup, null);
	}
	
	/**
	 * Add the given dataGroup for the given type
	 * 
	 * @param dataGroup
	 * @param type
	 */
	public void addDataGroup(String dataGroup, String type) {
		if(_dataGroups == null){
			_dataGroups = new HashMap<>();
		}
		Set<String> dataGroups = _dataGroups.get(type);
		if(dataGroups == null){
			dataGroups = new HashSet<>();
			_dataGroups.put(type, dataGroups);
		}
		dataGroups.add(dataGroup);
	}
	
	/**
	 * 
	 * @param dataGroups
	 * @return true if this group is empty
	 */
	public static boolean isEmpty(DataGroups dataGroups) {
		if(dataGroups == null){
			return true;
		}else{
			return !dataGroups.hasValues();
		}
	}
	
	/**
	 * 
	 * @return this group as data group uri string
	 */
	public String toDataGroupString() {
		if(!hasValues()){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for(Iterator<Entry<String, Set<String>>> iter = _dataGroups.entrySet().iterator();;){
			Entry<String, Set<String>> e = iter.next();
			String type = e.getKey();
			Iterator<String> vIter = e.getValue().iterator();
			if(type != null){	// there is a type, we must separate all values to different parts: TYPE:VALUE,TYPE:VALUE,TYPE:VALUE
				do{
					sb.append(type);
					sb.append(Definitions.SEPARATOR_URI_QUERY_TYPE_VALUE);
					sb.append(vIter.next());	// there should always be at least one data group per type	
				}while(vIter.hasNext());
			}else{	// no type, we can just add the values: VALUE,VALUE,VALUE
				sb.append(vIter.next());	// there should always be at least one data group per type
				while(vIter.hasNext()){
					sb.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
					sb.append(vIter.next());
				}
			}
			
			if(iter.hasNext()){
				sb.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
			}else{
				break;
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * 
	 * @param dataGroupString
	 * @return data groups based on the string
	 * @throws IllegalArgumentException on bad data group string
	 */
	public static DataGroups fromDataGroupString(String dataGroupString) throws IllegalArgumentException {
		String[] groups = StringUtils.split(dataGroupString, Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
		if(ArrayUtils.isEmpty(groups)){
			throw new IllegalArgumentException("Bad data group string: "+dataGroupString);
		}
		return new DataGroups(groups);
	}

	/**
	 * @return the datagroups as datagroup String or null if no datagroups
	 */
	@Override
	public String getValue() {
		return toDataGroupString();
	}
	
	@Override
	public void initialize(InputStream parameterValue) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("The use of HTTP Body is not implemented for this parameter.");
	}
}
