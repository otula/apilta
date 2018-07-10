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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import core.tut.pori.http.Definitions;


/**
 * The default parser for limit parameters.
 * 
 * The syntax being: ?limits=START_ITEM-END_ITEM with a possible open ended limit for END_ITEM, e.g. ?limits=START_ITEM in this case the END_ITEM is automatically assigned to START_ITEM+DEFAULT_MAX_ITEMS-1
 * 
 * It is also possible to provide limits for a specific type with ?limits=TYPE{@value Definitions#SEPARATOR_URI_QUERY_TYPE_VALUE}START_ITEM-END_ITEM or for types with ?limits=TYPE{@value Definitions#SEPARATOR_URI_QUERY_TYPE_VALUE}START_ITEM-END_ITEM{@value core.tut.pori.http.Definitions#SEPARATOR_URI_QUERY_PARAM_VALUES}TYPE{@value Definitions#SEPARATOR_URI_QUERY_TYPE_VALUE}START_ITEM-END_ITEM, 
 * the type limits can be retrieved using the parameterized getters, if no limits were provided for the type, default limits will be returned
 * 
 * It is also possible to provide both the default limit and type specific limits: ?limits=START_ITEM-END_ITEM,TYPE{@value Definitions#SEPARATOR_URI_QUERY_TYPE_VALUE}START_ITEM-END_ITEM
 * 
 * Providing only the - character as a limit should as ?limits=- will be assumed to mean max items = 0, i.e. return nothing, this can also be used with typed limits, e.g. ?limits=0-1{@value core.tut.pori.http.Definitions#SEPARATOR_URI_QUERY_PARAM_VALUES}TYPE{@value Definitions#SEPARATOR_URI_QUERY_TYPE_VALUE}- to return no result for a specific type.
 * 
 * The limits have no specific processing order, and can also be given in any order in the limits clause. The given order is generally internally preserved, though conceptually, no such order exists.
 */
public final class Limits extends HTTPParameter{
	/** the default HTTP parameter name */
	public static final String PARAMETER_DEFAULT_NAME = "limits";
	/** The default maximum amount of items if no last item is specified */
	public static final int DEFAULT_MAX_ITEMS = Integer.MAX_VALUE;
	private static final Logger LOGGER = Logger.getLogger(Limits.class);
	private static final char SEPARATOR_LIMITS = '-';
	private Map<String, TypeLimits> _typeLimits = new HashMap<>();	// typeName - typeLimits map

	/**
	 * Initialize limits
	 * 
	 * @param startItem if &lt; 0, 0 will be used
	 * @param endItem the last item index (inclusively), if &lt;= startItem, startItem+DEFAULT_MAX_ITEM-1 will be used
	 */
	public Limits(int startItem, int endItem){
		_typeLimits.put(null, new TypeLimits(startItem, endItem, null));
	}

	/**
	 * create default limits
	 */
	public Limits(){
		_typeLimits.put(null, new TypeLimits(0, DEFAULT_MAX_ITEMS, null));
	}
	
	/**
	 * 
	 * @return the limits as a limits string (query parameter value, without the parameter name and equals sign), or null if no limits
	 */
	public String toLimitString(){
		if(hasValues()){
			StringBuilder value = new StringBuilder();
			for(Iterator<Entry<String, TypeLimits>> iter = _typeLimits.entrySet().iterator();;){
				Entry<String, TypeLimits> e = iter.next();
				String type = e.getKey();
				if(type != null){
					value.append(type);
					value.append(Definitions.SEPARATOR_URI_QUERY_TYPE_VALUE);
				}
				TypeLimits limits = e.getValue();
				value.append(limits.getStartItem());
				value.append(SEPARATOR_LIMITS);
				value.append(limits.getEndItem());
				if(iter.hasNext()){
					value.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
				}else{
					break;
				}
			}
			return value.toString();
		}else{
			return null;
		}
	}

	/**
	 * 
	 * @param startItem
	 * @param endItem
	 * @param typeName setting to null will replace the default (global) limits
	 */
	public void setTypeLimits(int startItem, int endItem, String typeName){
		_typeLimits.put(typeName, new TypeLimits(startItem, endItem, typeName));
	}

	/**
	 * 
	 * @return start item index
	 */
	public int getStartItem(){
		return _typeLimits.get(null).getStartItem();
	}

	/**
	 * @return the endItem index
	 */
	public int getEndItem() {
		return _typeLimits.get(null).getEndItem();
	}

	/**
	 * 
	 * @return maximum number of items
	 */
	public int getMaxItems(){
		return _typeLimits.get(null).getMaxItems(); // add one to return at least one result
	}

	/**
	 * 
	 * @param typeName
	 * @return start item index for the given type or if the type does not exist, the general start item
	 */
	public int getStartItem(String typeName){
		if(_typeLimits == null){
			return getStartItem();
		}
		TypeLimits l = _typeLimits.get(typeName);
		if(l == null){
			return getStartItem();
		}else{
			return l.getStartItem();
		}
	}

	/**
	 * 
	 * @param typeName
	 * @return the end item index or if the type is not defined, the general end item
	 */
	public int getEndItem(String typeName){
		if(_typeLimits == null){
			return getEndItem();
		}
		TypeLimits l = _typeLimits.get(typeName);
		if(l == null){
			return getEndItem();
		}else{
			return l.getEndItem();
		}
	}
	
	/**
	 * Note: if no limits for the given typeName is found, this will return the default limits.
	 * Whether default limits were returned or not can be checked by callint TypeLimits.getTypeName(),
	 * which will return null on default limits.
	 * 
	 * @param typeName
	 * @return type limits for the requested type
	 */
	public TypeLimits getTypeLimits(String typeName){
		TypeLimits tl = _typeLimits.get(typeName);
		if(tl == null){
			return _typeLimits.get(null);	// return the default limits
		}else{
			return tl;
		}
	}

	/**
	 * 
	 * @param typeName
	 * @return maximum number of items for the given type or maximum number of items in general if the given type is not defined
	 */
	public int getMaxItems(String typeName){
		if(_typeLimits == null){
			return getMaxItems();
		}
		TypeLimits l = _typeLimits.get(typeName);
		if(l == null){
			LOGGER.debug("Using defaults: No limits found for type: "+typeName);
			return getMaxItems();
		}else{
			return l.getMaxItems(); // add one to return at least one result
		}
	}

	@Override
	public void initialize(List<String> parameterValues) throws IllegalArgumentException {
		for(Iterator<String> iter = parameterValues.iterator();iter.hasNext();){
			initializeLimits(iter.next());
		}
	}

	@Override
	public void initialize(String parameterValue) throws IllegalArgumentException {
		initializeLimits(parameterValue);
	}

	/**
	 * 
	 * @param valueString 0, 0- or 0-1
	 * @return limits parsed from the given string
	 * @throws IllegalArgumentException on bad value string
	 */
	private TypeLimits parseLimitValues(String valueString) throws IllegalArgumentException{
		if(StringUtils.isBlank(valueString)){
			LOGGER.debug("Detected null or empty value for parameter: "+getParameterName());
			return null;
		}
		String[] parts = StringUtils.split(valueString, SEPARATOR_LIMITS);
		try{
			if(parts == null || parts.length > 2){	// probably limits=0-1-2-3 or similar
				throw new IllegalArgumentException("Invalid "+getParameterName()+": "+valueString);
			}else if(parts.length < 1){ // limits=- or similar
				return new TypeLimits(-1, -1, null);
			}else if(parts.length == 2){	// 0-1
				return new TypeLimits(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), null);
			}else{	// 0 (or 0-)
				int item = Integer.parseInt(parts[0]);
				return new TypeLimits(item, item, null);
			}
		}catch(NumberFormatException ex){
			LOGGER.debug(ex, ex);
			throw new IllegalArgumentException("Invalid "+getParameterName()+": "+valueString);
		}
	}

	/**
	 * 
	 * @param limitString
	 * @throws IllegalArgumentException on bad input
	 */
	private void initializeLimits(String limitString) throws IllegalArgumentException{
		if(StringUtils.isBlank(limitString)){
			LOGGER.debug("Detected null or empty value for parameter: "+getParameterName());
			return;
		}
		String[] parts = StringUtils.split(limitString,Definitions.SEPARATOR_URI_QUERY_TYPE_VALUE);
		if(parts.length == 1){	// 0, 0- or 0-1
			TypeLimits limits = parseLimitValues(parts[0]);
			_typeLimits.put(null, limits);
		}else if(parts.length == 2){	// TYPE;0, TYPE;0- OR TYPE;0-1
			TypeLimits limits = parseLimitValues(parts[1]);
			limits._typeName = parts[0];
			_typeLimits.put(parts[0], limits);
		}else{
			throw new IllegalArgumentException("Invalid "+getParameterName()+": "+limitString);
		}
	}

	/**
	 * always true
	 */
	@Override
	public boolean hasValues() {
		return true;
	}
	
	/**
	 * @return there is no single value available, this always returns null
	 */
	@Override
	public Object getValue() {
		return null;
	}

	/**
	 * A type specific limit clause.
	 */
	public class TypeLimits{
		private int _startItem = -1;
		private int _endItem = -1;
		private String _typeName = null;
		
		/**
		 * Create new TypeLimits. The limit validity will be checked:
		 * <ul>
		 * 	<li>If end item is smaller than start item, this will throw an exception</li>
		 * 	<li>if start item is less than 0, it will be set to 0</li>
		 * 	<li>if end item is INTEGER MAX, it will be set to INTEGER_MAX - 1. This is because the end item is included in the limits, which would cause interval START - END cause limit overflow (INTEGER MAX + 1)</li>
		 * 	<li>if end item is &lt; 0, the maximum item count is assumed to be 0 and getMaxItems() will return 0 regardless of the given start item</li>
		 * </ul>
		 * @param startItem
		 * @param endItem
		 * @param typeName
		 * @throws IllegalArgumentException 
		 */
		public TypeLimits(int startItem, int endItem, String typeName) throws IllegalArgumentException{
			_typeName = typeName;
			if(startItem < 0){
				LOGGER.debug("Start item < 0, setting value to 0.");
				_startItem = 0;
			}else{
				_startItem = startItem;
			}
			if(endItem == Integer.MAX_VALUE){
				LOGGER.debug("End item = "+Integer.MAX_VALUE+" setting value to MAX-1.");
				_endItem = endItem-1;
			}else if(endItem < _startItem && endItem >= 0){
				throw new IllegalArgumentException("End item < start item.");
			}else{
				_endItem = endItem;
			}
		}

		/**
		 * @return the startItem
		 */
		public int getStartItem() {
			return _startItem;
		}

		/**
		 * @return the endItem
		 */
		public int getEndItem() {
			return _endItem;
		}

		/**
		 * @return the typeName
		 */
		public String getTypeName() {
			return _typeName;
		}
		
		/**
		 * 
		 * @return max item count
		 */
		public int getMaxItems() {
			if(_endItem < 0){
				return 0;
			}
			return _endItem-_startItem+1;
		}
	}	// class TypeLimits
}
