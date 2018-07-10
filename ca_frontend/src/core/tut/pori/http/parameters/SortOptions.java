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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import core.tut.pori.dao.SQLSelectBuilder.OrderDirection;
import core.tut.pori.http.Definitions;


/**
 * <p>The default parser for sort options.</p>
 * 
 * <p>The basic syntax is: ?PARAMETER=OrderDirection, e.g. ?sort=&lt;</p>
 * 
 * <p>It is also possible to give type specific order clauses, e.g. ?sort=TYPE1;&lt;,TYPE2;&gt;</p>
 * 
 * <p>Also, you can provide specific element name, e.g. ?sort=&lt;ELEMENT_NAME or ?sort=TYPE1;&gt;ELEMENT_NAME</p>
 * 
 * <p>These to variations can be combined, e.g. ?sort=&lt;,TYPE;&gt; to provide different sort option for a specific type</p>
 * 
 * <p>Note: the parses has no way of knowing if the given TYPE or ELEMENT_NAME value is valid, you should check the values yourself</p>
 *
 * <p>Any number of sort clauses (SortOption) can be given, and they will be processed in-order by the default SQL and SOLR query builders.</p>
 */
public final class SortOptions extends HTTPParameter{
	/** String for ascending sort */
	public static final String ORDER_DIRECTION_ASCENDING = "<";
	/** String for descending sort */
	public static final String ORDER_DIRECTION_DESCENDING = ">";
	/** Recommended name for the sort parameter */
	public static final String PARAMETER_DEFAULT_NAME = "sort_by";
	private static final Logger LOGGER = Logger.getLogger(SortOptions.class);
	private static final int ORDER_DIRECTION_ASCENDING_LENGTH = ORDER_DIRECTION_ASCENDING.length();
	private static final int ORDER_DIRECTION_DESCENDING_LENGTH = ORDER_DIRECTION_DESCENDING.length();
	private Map<String, Set<Option>> _typeOrderDirections = null;	// type-name, sort option map
	
	/**
	 * 
	 * @param options
	 * @param typeName
	 * @return the sort options or null if none
	 */
	public static Set<Option> getSortOptions(SortOptions options, String typeName){
		if(options == null){
			return null;
		}else{
			return options.getSortOptions(typeName);
		}
	}
	
	/**
	 * @param typeName
	 * @return set of sort options, the iteration order of the map is predictable, i.e. the values are iterated in the order given by the user. Or, null if none provided by the user.
	 */
	public Set<Option> getSortOptions(String typeName){
		if(_typeOrderDirections == null){
			return null;
		}else{
			Set<Option> o = _typeOrderDirections.get(typeName);
			if(o == null){
				return getDefaultSortOptions();
			}else{
				return o;
			}
		}
	}
	
	/**
	 * 
	 * @param option
	 */
	public void addSortOption(Option option){
		if(option == null){
			LOGGER.debug("Ignored null option.");
			return;
		}
		Set<Option> options = null;
		String typename = option.getTypeName();
		if(_typeOrderDirections == null){
			_typeOrderDirections = new HashMap<>();
			_typeOrderDirections.put(typename, (options = new LinkedHashSet<>()));
		}else{
			options = _typeOrderDirections.get(typename);
			if(options == null){
				_typeOrderDirections.put(typename, (options = new LinkedHashSet<>()));
			}
		}
		options.add(option);
	}
	
	/**
	 * 
	 * @return non-typed sort options, or null if not given by the user
	 */
	public Set<Option> getDefaultSortOptions(){
		return (_typeOrderDirections == null ? null : _typeOrderDirections.get(null));
	}

	@Override
	public void initialize(List<String> parameterValues) throws IllegalArgumentException {
		for(Iterator<String> iter = parameterValues.iterator();iter.hasNext();){
			initializeSort(iter.next());
		}
	}

	@Override
	public void initialize(String parameterValue) throws IllegalArgumentException {
		if(StringUtils.isBlank(parameterValue)){
			_typeOrderDirections = null;
			return;
		}
		initializeSort(parameterValue);
	}
	
	/**
	 * 
	 * @param valueString
	 * @throws IllegalArgumentException on bad valueString
	 */
	private void initializeSort(String valueString) throws IllegalArgumentException{
		if(StringUtils.isBlank(valueString)){
			LOGGER.debug("Detected null or empty value for parameter: "+getParameterName());
			return;
		}
		String[] parts = StringUtils.split(valueString, Definitions.SEPARATOR_URI_QUERY_TYPE_VALUE);
		Option option = null;
		if(parts.length > 2){	// bad input
			throw new IllegalArgumentException("Invalid value "+valueString+" for paramater "+getParameterName());
		}else if(parts.length == 2){	//TYPE;ELEMENT, TYPE;<ELEMENT, TYPE;>ELEMENT, TYPE;> or TYPE;<
			option = parseOption(parts[1]);
			option._typeName = parts[0];
		}else{	// <, >, <ELEMENT, >ELEMENT or ELEMENT
			option = parseOption(parts[0]);
		}
		
		Set<Option> set = null;
		if(_typeOrderDirections == null){
			_typeOrderDirections = new LinkedHashMap<>();
		}else{
			set = _typeOrderDirections.get(option._typeName);
		}
		
		if(set == null){
			set = new LinkedHashSet<>();
			_typeOrderDirections.put(option._typeName, set);
		}
		
		set.add(option);
	}
	
	/**
	 * helper method for parsin the sort string
	 * 
	 * @param sortString ELEMENT, <ELEMENT, >ELEMENT, > or <
	 * @return the parsed option
	 * @throws IllegalArgumentException on bad input
	 */
	private Option parseOption(String sortString) throws IllegalArgumentException{
		if(StringUtils.isBlank(sortString)){
			throw new IllegalArgumentException("Invalid value "+sortString+" for paramater "+getParameterName());
		}
		
		OrderDirection od = null;
		String sortElementName = null;
		if(sortString.startsWith(ORDER_DIRECTION_ASCENDING)){
			od = OrderDirection.ASCENDING;
			if(sortString.length() > ORDER_DIRECTION_ASCENDING_LENGTH){	// <ELEMENT_NAME
				sortElementName = sortString.substring(ORDER_DIRECTION_ASCENDING_LENGTH);
			}	// only <
		}else if(sortString.startsWith(ORDER_DIRECTION_DESCENDING)){
			od = OrderDirection.DESCENDING;
			if(sortString.length() > ORDER_DIRECTION_DESCENDING_LENGTH){	//> ELEMENT_NAME
				sortElementName = sortString.substring(ORDER_DIRECTION_DESCENDING_LENGTH);
			}	// only >
		}else{	// must be the element name
			sortElementName = sortString;
		}
		return new Option(sortElementName,od, null);
	}

	/**
	 * Note: getValue() may return null even though this method returns null, if user has provided a specific
	 * sorting option, which is NOT a default option
	 * 
	 * @return true if user has provided values, but not if only default value is given
	 */
	@Override
	public boolean hasValues() {
		return (_typeOrderDirections != null);
	}

	/**
	 * @return the general sort order
	 */
	@Override
	public Set<Option> getValue() {
		return getDefaultSortOptions();
	}
	
	/**
	 * A single sort option.
	 */
	public static class Option{
		private String _elementName = null;
		private OrderDirection _orderDirection = OrderDirection.DESCENDING;
		private String _typeName = null;
		
		/**
		 * 
		 * @param elementName
		 * @param direction
		 * @param typeName
		 */
		public Option(String elementName, OrderDirection direction, String typeName){
			_elementName = elementName;
			_typeName = typeName;
			if(direction == null){
				LOGGER.debug("null "+OrderDirection.class.toString()+", using default: "+_orderDirection.name());
			}else{
				_orderDirection = direction;
			}
		}

		/**
		 * @return the elementName
		 */
		public String getElementName() {
			return _elementName;
		}

		/**
		 * @return the orderDirection
		 */
		public OrderDirection getOrderDirection() {
			return _orderDirection;
		}

		/**
		 * @return the typeName
		 */
		public String getTypeName() {
			return _typeName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((_elementName == null) ? 0 : _elementName.hashCode());
			result = prime * result + ((_orderDirection == null) ? 0 : _orderDirection.hashCode());
			result = prime * result + ((_typeName == null) ? 0 : _typeName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Option other = (Option) obj;
			if (_elementName == null) {
				if (other._elementName != null)
					return false;
			} else if (!_elementName.equals(other._elementName))
				return false;
			if (_orderDirection != other._orderDirection)
				return false;
			if (_typeName == null) {
				if (other._typeName != null)
					return false;
			} else if (!_typeName.equals(other._typeName))
				return false;
			return true;
		}
	} //  class Option
}
