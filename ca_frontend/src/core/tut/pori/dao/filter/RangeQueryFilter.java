/**
 * Copyright 2015 Tampere University of Technology, Pori Department
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
package core.tut.pori.dao.filter;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;

import core.tut.pori.dao.SolrQueryBuilder;

/**
 * Filter that supports range operations (field:[* TO *]) for Solr queries
 * 
 */
public class RangeQueryFilter implements AbstractQueryFilter {
	private String _fieldName = null;
	private Object _from = null;
	private Object _to = null;
	private QueryType _type = null;
	
	/**
	 * Both from and to can be of any object type, which can be converted to string using java.lang.String.valueOf()
	 * 
	 * @param fieldName solr field to target the search on
	 * @param from start range (inclusive), if null all values &lt;= end will be returned
	 * @param to end range clause (inclusive), if null all values &gt;= start will be returned
	 * @param type type of the query
	 * @throws IllegalArgumentException on bad field name, or if both from and to are null
	 */
	public RangeQueryFilter(String fieldName, Object from, Object to, QueryType type) throws IllegalArgumentException{
		if(StringUtils.isBlank(fieldName)){
			throw new IllegalArgumentException("Invalid field name : "+fieldName);
		}
		if(from == null && to == null){
			throw new IllegalArgumentException("From and to cannot both be null.");
		}
		if(type == null){
			throw new IllegalArgumentException("Invalid query type: "+type);
		}
		_fieldName = fieldName;
		_from = from;
		_to = to;
		_type = type;
	}
	
	/**
	 * overloaded convenience method for using dates
	 * 
	 * @param fieldName
	 * @param from
	 * @param to
	 * @param type
	 * @see #RangeQueryFilter(String, Object, Object, core.tut.pori.dao.filter.AbstractQueryFilter.QueryType)
	 */
	public RangeQueryFilter(String fieldName, Date from, Date to, QueryType type) {
		this(fieldName, (from == null ? (Object) null : core.tut.pori.utils.StringUtils.dateToISOString(from)), (to == null ? (Object) null : core.tut.pori.utils.StringUtils.dateToISOString(to)), type); // for the range query to work the dates must be in ISODate format, make it so
	}

	@Override
	public void toFilterString(StringBuilder fq) {
		fq.append('(');
		fq.append(_fieldName);
		fq.append(SolrQueryBuilder.SEPARATOR_SOLR_FIELD_VALUE);
		fq.append('[');
		if(_from != null){
			fq.append(ClientUtils.escapeQueryChars(String.valueOf(_from)));
			fq.append(" TO ");
		}else{ // everything < _to
			fq.append("* TO ");
		}
		if(_to != null){
			fq.append(ClientUtils.escapeQueryChars(String.valueOf(_to)));
			fq.append("]");
		}else{ // everything > _from
			fq.append("*]");
		} // else
		
		fq.append(')');
	}

	@Override
	public QueryType getQueryType() {
		return _type;
	}
}
