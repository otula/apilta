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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;

import core.tut.pori.dao.SolrQueryBuilder;

/**
 * Basic and filter with OR relation to other query filters, the values themselves have OR relation with one another.
 *
 * Comparison to null (non-existent) value is accepted.
 */
public class OrQueryFilter implements AbstractQueryFilter {
	private static final char SOLR_NEGATIVE_QUERY = '-';
	private String _fieldName = null;
	private Set<String> _values = null;
	private boolean _hasNullValue = false;
	
	/**
	 * 
	 * @param fieldName
	 * @param values
	 * @throws IllegalArgumentException on bad field name
	 */
	public OrQueryFilter(String fieldName, Collection<? extends Object> values) throws IllegalArgumentException{
		addFilter(fieldName, values);			
	}
	
	/**
	 * 
	 * @param fieldName
	 * @param value
	 * @throws IllegalArgumentException on bad field name
	 */
	public OrQueryFilter(String fieldName, Object value) throws IllegalArgumentException{
		if(StringUtils.isBlank(fieldName)){
			throw new IllegalArgumentException("No field name.");
		}
		_fieldName = fieldName;
		if(value == null){
			_hasNullValue = true;
		}else{
			_values = new HashSet<>(1);
			_values.add(ClientUtils.escapeQueryChars(String.valueOf(value)));
		}
	}
	
	/**
	 * 
	 * @param fieldName
	 * @param values
	 * @throws IllegalArgumentException on bad field name
	 */
	public OrQueryFilter(String fieldName, long[] values) throws IllegalArgumentException{
		List<String> v = null;
		if(values != null){
			v = new ArrayList<>(values.length);
			for(int i=0;i<values.length;++i){
				v.add(String.valueOf(values[i]));
			}
		}
		addFilter(fieldName, v);
	}
	
	/**
	 * 
	 * @param fieldName
	 * @param values
	 * @throws IllegalArgumentException on bad field name
	 */
	public OrQueryFilter(String fieldName, int[] values) throws IllegalArgumentException{
		List<String> v = null;
		if(values != null){
			v = new ArrayList<>(values.length);
			for(int i=0;i<values.length;++i){
				v.add(String.valueOf(values[i]));
			}
		}
		addFilter(fieldName, v);
	}
	
	/**
	 * 
	 * @param fieldName
	 * @param values
	 * @throws IllegalArgumentException on bad field name
	 */
	private void addFilter(String fieldName, Collection<? extends Object> values) throws IllegalArgumentException{
		if(StringUtils.isBlank(fieldName)){
			throw new IllegalArgumentException("No field name.");
		}
		
		_fieldName = fieldName;
		if(values == null || values.isEmpty()){
			_hasNullValue = true;
			return;
		}
		
		_values = new HashSet<>(values.size());
		for(Iterator<? extends Object> iter = values.iterator(); iter.hasNext();){
			Object value = iter.next();
			if(value == null){
				_hasNullValue = true;
			}else{
				_values.add(ClientUtils.escapeQueryChars(String.valueOf(value)));
			}	
		}
		
		if(_values.isEmpty()){	// if there was only the null value
			_values = null;
		}
	}

	@Override
	public void toFilterString(StringBuilder fq) {
		fq.append('(');
		if(_values != null){
			fq.append(_fieldName);
			fq.append(SolrQueryBuilder.SEPARATOR_SOLR_FIELD_VALUE);
			fq.append('(');
			Iterator<String> iter = _values.iterator();
			fq.append(iter.next());
			while(iter.hasNext()){
				fq.append(SOLR_OR);
				fq.append(iter.next());
			}
			
			if(_hasNullValue){
				fq.append(") OR (");
			}else{
				fq.append(')');
			}
		}
		
		if(_hasNullValue){
			fq.append("("+SolrQueryBuilder.QUERY_ALL+SOLR_AND+" "+SOLR_NEGATIVE_QUERY);
			fq.append(_fieldName);
			fq.append(SolrQueryBuilder.SEPARATOR_SOLR_FIELD_VALUE+"["+SolrQueryBuilder.SOLR_WILD_CARD+" TO "+SolrQueryBuilder.SOLR_WILD_CARD+"])");
		}
		
		fq.append(')');
	}

	@Override
	public QueryType getQueryType() {
		return QueryType.OR;
	}
}
