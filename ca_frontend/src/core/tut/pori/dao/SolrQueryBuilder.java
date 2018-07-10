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
package core.tut.pori.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.CommonParams;

import core.tut.pori.dao.SQLSelectBuilder.OrderDirection;
import core.tut.pori.dao.filter.AbstractQueryFilter;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.QueryParameter;
import core.tut.pori.http.parameters.SortOptions;
import core.tut.pori.http.parameters.SortOptions.Option;

/**
 * Helper class, which can be used to build a SOLR query.
 *
 */
public class SolrQueryBuilder {
	/** Separator for solr fields and values */
	public static final char SEPARATOR_SOLR_FIELD_VALUE = ':';
	/** Solr query selector for all items */
	public static final String QUERY_ALL = "*:*";
	/** Solr wild card */
	public static final char SOLR_WILD_CARD = '*';
	private static final Limits DEFAULT_MAX_LIMITS = new Limits(0, SolrDAO.MAX_DOCUMENT_COUNT);
	private static final Logger LOGGER = Logger.getLogger(SolrQueryBuilder.class);
	private static final String SOLR_SELECT = "/select";
	private static final String SOLR_QUERY = "/query";
	private static final String SOLR_SUGGEST = "/suggest";
	private List<AbstractQueryFilter> _customFilters = null;
	private Map<String, String> _elementFieldMap = null;
	private Limits _limits = null;
	private QueryParameter _queryParameter = null;
	private SortOptions _sortOptions = null;
	private boolean _useExtendedDismax = false;
	private Set<String> _fieldList = null;

	/**
	 * Type of the request handler.
	 */
	public enum RequestHandlerType{
		/** select */
		SELECT(SOLR_SELECT),
		/** query */
		QUERY(SOLR_QUERY),
		/** suggest */
		SUGGEST(SOLR_SUGGEST);
		
		private String _handler;
		
		/**
		 * 
		 * @param handler
		 */
		private RequestHandlerType(String handler){
			_handler = handler;
		}
		
		/**
		 * 
		 * @return the type as a string
		 */
		public String toHandlerString(){
			return _handler;
		}
	} // enum RequestHandlerType
	
	/**
	 * Note: the given map is ASSUMED to be escaped and to contain only valid element names and field names
	 * 
	 * @param elementFieldMap map of element-solr field relations
	 */
	public SolrQueryBuilder(Map<String,String> elementFieldMap){
		_elementFieldMap = elementFieldMap;
	}
	
	/**
	 * same as calling the overloaded constructor with parameter null (without relation map)
	 */
	public SolrQueryBuilder(){
		// nothing needed
	}

	/**
	 * @param limits the limits to set
	 */
	public void setLimits(Limits limits) {
		_limits = limits;
	}
	
	/**
	 * 
	 * @param option
	 */
	public void addSortOption(Option option){
		if(_sortOptions == null){
			_sortOptions = new SortOptions();
		}
		_sortOptions.addSortOption(option);
	}
	
	/**
	 * Add field to be selected in the response.
	 * 
	 * @param field
	 */
	public void addField(String field){
		if(_fieldList == null){
			_fieldList = new HashSet<>();
		}
		_fieldList.add(ClientUtils.escapeQueryChars(field));
	}
	
	/**
	 * Add a list of fields to be selected in the response
	 * 
	 * @param fields
	 */
	public void addFields(String ...fields){
		for(int i=0;i<fields.length; ++i){
			addField(fields[i]);
		}
	}

	/**
	 * @param queryParameter the query to set
	 */
	public void setQueryParameter(QueryParameter queryParameter) {
		_queryParameter = queryParameter;
	}

	/**
	 * @param sortOptions the sortOptions to set
	 */
	public void setSortOptions(SortOptions sortOptions) {
		_sortOptions = sortOptions;
	}

	/**
	 * 
	 * @return true if extended dismax is enabled
	 */
	public boolean isUseExtendedDismax() {
		return _useExtendedDismax;
	}

	/**
	 * Setter for the use of enabling Extended Query Parser (edismax). Setting this true will be using the more user friendly query parser.
	 * @param useExtendedDismax
	 */
	public void setUseExtendedDismax(boolean useExtendedDismax) {
		_useExtendedDismax = useExtendedDismax;
	}

	/**
	 * Add new filter query.
	 * 
	 * @param filter
	 */
	public void addCustomFilter(AbstractQueryFilter filter){
		if(filter == null){
			LOGGER.warn("Ignored null filter.");
			return;
		}
		if(_customFilters == null){
			_customFilters = new ArrayList<>();
		}
		_customFilters.add(filter);
	}
	
	/**
	 * clear the list of currently set custom filters
	 */
	public void clearCustomFilters(){
		_customFilters = null;
	}

	/**
	 * Get the query using a specified type parameter for sort and limit operations.
	 * 
	 * @param type the type or null
	 * @return new query created with the given parameters using type information
	 * @throws IllegalArgumentException on bad query value
	 */
	public SolrQuery toSolrQuery(String type) throws IllegalArgumentException{
		SolrQuery query = new SolrQuery();

		if(_customFilters != null){
			Iterator<AbstractQueryFilter> pIter = _customFilters.iterator();
			StringBuilder filter = new StringBuilder();
			pIter.next().toFilterString(filter);
			while(pIter.hasNext()){
				AbstractQueryFilter fq = pIter.next();
				filter.append(fq.getQueryType().toTypeString());
				fq.toFilterString(filter);
			}
			query.addFilterQuery(filter.toString());
		}

		boolean querySet = false;
		Set<String> queryParams = QueryParameter.getValues(_queryParameter, type);
		if(queryParams != null){
			int paramCount = queryParams.size();
			if(paramCount != 1){
				throw new IllegalArgumentException("Only a single query parameter is accepted, found: "+paramCount);
			}
			querySet = true;

			String q = queryParams.iterator().next();
			if(_useExtendedDismax){
				LOGGER.debug("Using edismax query parser and raw query");
				query.set("defType", "edismax");	//activate edismax query parser
				query.set("q.alt", QUERY_ALL);	//setter for alternative query to retrieve everything if the regular query is not given.
				query.setQuery(q);
			}else if(_elementFieldMap == null){
				LOGGER.debug("No element-field map provided, using the raw query.");
				query.setQuery(q);
			}else{
				StringBuilder solr = new StringBuilder(q.length()); // estimate size to minimize re-allocations
				boolean inParenthesis = false;
				boolean inElement = false;
				StringBuilder elementName = new StringBuilder();
				for(int i = q.length()-1;i>=0;--i){	// go in reverse
					char c = q.charAt(i);
					if(c == '"'){
						inParenthesis = !inParenthesis;
					}else if(!inParenthesis && c == SEPARATOR_SOLR_FIELD_VALUE){	// not in parenthesis, and this is the start of a new element name
						if(inElement){	// an error, cannot start a new element and already be inside an element
							throw  new IllegalArgumentException("Bad query string, duplicate field separator at: "+i);
						}
						inElement = true;
					}else if(inElement){
						if(c == SOLR_WILD_CARD){
							if(elementName.length() > 0){	// there is a wild card in the element name
								LOGGER.debug("Wild card detected inside element name. Skipping element-field mapping...");
								solr.append(elementName);	// dump whatever is in the element name in back into the query string
								elementName.setLength(0);	// clear the element
							}else{	// probably *:SOMETHING
								inElement = false;
							}
						}else if(c < 'A' || c > 'z'){	// end of element
							solr.append(getReversedFieldName(elementName));
							elementName.setLength(0);
							inElement = false;
						}else{
							elementName.append(c);
							continue;
						}
					}
					solr.append(c);	// append in the query string
				}	// for
				if(inElement){
					solr.append(getReversedFieldName(elementName));
				}
				query.setQuery(solr.reverse().toString());	// the query was processed from end to beginning, we need to reverse this first
			}	// else
		}

		if(!querySet){
			if(_useExtendedDismax){
				LOGGER.debug("Extended Dismax enabled, query parameter can be null");
				query.set("defType", "edismax");	//activate edismax query parser
				query.set("q.alt", QUERY_ALL);	//setter for alternative query to retrieve everything if the regular query is not given.
			}else{
				query.setQuery(QUERY_ALL);
			}
		}

		if(_limits == null){
			query.setStart(DEFAULT_MAX_LIMITS.getStartItem());
			query.setRows(DEFAULT_MAX_LIMITS.getMaxItems());
		}else{
			int start = _limits.getStartItem(type);
			query.setStart(_limits.getStartItem(type));
			int rows = _limits.getMaxItems(type);
			long total = (long)start+(long)rows;
			if(total > DEFAULT_MAX_LIMITS.getMaxItems()){ // solr server throws array out of bounds exception if total row count (start+rows) exceeds INTEGER.MAX
				rows = DEFAULT_MAX_LIMITS.getMaxItems()-start;
				LOGGER.debug("Max items exceeds the maximum document count, capping to: "+rows);
			}
			query.setRows(rows);
		}

		Set<Option> options = SortOptions.getSortOptions(_sortOptions, type);
		if(options != null){
			for(Option o : options){
				if(_elementFieldMap == null){
					LOGGER.debug("No element-field map provided, using the element name directly.");
					query.addSort(o.getElementName(), fromOrderDirection(o.getOrderDirection()));
				}else{
					String field = _elementFieldMap.get(o.getElementName());
					if(field == null){
						LOGGER.warn("Ignored unknown sort element: "+o.getElementName());
					}else{
						query.addSort(field, fromOrderDirection(o.getOrderDirection()));
					}
				}
			}
		}
		if(_fieldList != null){
			query.setFields(_fieldList.toArray(new String[_fieldList.size()]));
		}

		return query;
	}
	
	/**
	 * Helper method to reverse element name
	 * @param reversedElementName
	 * @return reversed field name mapped to the given reversed element name
	 */
	private String getReversedFieldName(StringBuilder reversedElementName){
		String eName = reversedElementName.reverse().toString(); // the query is processed from end-to-beginning, so we need to reverse the element name
		String fieldName = _elementFieldMap.get(eName);	// check if it matches a known field name
		if(fieldName == null){
			throw new IllegalArgumentException("Bad element name: "+eName);
		}
		return StringUtils.reverse(fieldName);
	}

	/**
	 * get an executable SolrQuery from the builder
	 * 
	 * @return new query created with the given parameters
	 */
	public SolrQuery toSolrQuery(){
		return toSolrQuery(null);
	}

	/**
	 * Can be used to convert SQL OrderDirection to SOLR ORDER clause.
	 * 
	 * @param direction
	 * @return the given sql order direction converted to solr order direction
	 * @throws UnsupportedOperationException 
	 */
	public static ORDER fromOrderDirection(OrderDirection direction) throws UnsupportedOperationException{
		switch(direction){
			case ASCENDING:
				return ORDER.asc;
			case DESCENDING:
				return ORDER.desc;
			default:
				throw new UnsupportedOperationException("Unhandeled direction: "+direction.name());
		}
	}
	
	/**
	 * Helper method to set the correct path for different kinds of request handlers (mainly for suggest-handler)
	 * @param query
	 * @param type
	 * @return the passed query
	 */
	public static SolrQuery setRequestHandler(SolrQuery query, RequestHandlerType type) {
		if(query != null){
			switch(type){
				case SUGGEST: 
					query.setParam(CommonParams.QT, type.toHandlerString());
					break;
				case QUERY: // default is OK
				case SELECT: // default is OK
					LOGGER.debug("Using default query handler.");
					break;
				default:
					throw new UnsupportedOperationException("Unhandeled "+RequestHandlerType.class.toString()+" : "+type.name());
			}
		}
		return query;
	}
}
