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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

/**
 * Class for creating AND sub queries.
 * 
 * The inner operation of the queries AND (Q1 [OR] Q2) depends on the type of the given query. In the aforementioned example the later query would have been an OR query.
 *
 */
public class AndSubQueryFilter implements AbstractQueryFilter {
	private static final Logger LOGGER = Logger.getLogger(AndSubQueryFilter.class);
	private List<AbstractQueryFilter> _filters = new ArrayList<>();

	/**
	 * 
	 * @param filters
	 * @throws IllegalArgumentException on empty filter list
	 */
	public AndSubQueryFilter(AbstractQueryFilter[] filters) throws IllegalArgumentException{
		if(ArrayUtils.isEmpty(filters)){
			throw new IllegalArgumentException("Empty filter list.");
		}
		for(int i=0;i<filters.length;++i){
			_filters.add(filters[i]);
		}
	}
	
	/**
	 * 
	 * @param filters
	 * @throws IllegalArgumentException on empty filter list
	 */
	public AndSubQueryFilter(Collection<AbstractQueryFilter> filters) throws IllegalArgumentException{
		if(filters == null || filters.isEmpty()){
			throw new IllegalArgumentException("Empty filter list.");
		}
		_filters.addAll(filters);
	}
	
	/**
	 * initialize and empty sub query filter
	 */
	public AndSubQueryFilter() {
		// nothing needed
	}
	
	/**
	 * 
	 * @param filter filter to be added to this query
	 */
	public void addFilter(AbstractQueryFilter filter) {
		if(filter == null){
			LOGGER.warn("Ignored null filter.");
		}else{
			_filters.add(filter);
		}
	}

	/**
	 * @throws IllegalStateException if the builder has not been initialized with sub-queries
	 */
	@Override
	public void toFilterString(StringBuilder fq) throws IllegalStateException {
		if(_filters.isEmpty()){
			throw new IllegalStateException("Attempted to convert to string, but no sub-queries are given.");
		}
		fq.append('(');
		Iterator<AbstractQueryFilter> iter = _filters.iterator();
		iter.next().toFilterString(fq);
		while(iter.hasNext()){
			AbstractQueryFilter f = iter.next();
			fq.append(f.getQueryType().toTypeString());
			f.toFilterString(fq);
		}
		fq.append(')');
	}

	@Override
	public QueryType getQueryType() {
		return QueryType.AND;
	}
	
} // class AndSubQueryFilter
