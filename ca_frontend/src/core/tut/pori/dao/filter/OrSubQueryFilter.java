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

import java.util.Collection;

/**
 * class for creation OR sub queries. 
 * 
 * The inner operation of the queries OR (Q1 [OR] Q2) depends on the type of the given query. In the aforementioned example the later query would have been an OR query.
 *
 */
public class OrSubQueryFilter extends AndSubQueryFilter {

	/**
	 * 
	 * @param filters
	 * @throws IllegalArgumentException
	 */
	public OrSubQueryFilter(AbstractQueryFilter[] filters) throws IllegalArgumentException {
		super(filters);
	}

	/**
	 * 
	 * @param filters
	 * @throws IllegalArgumentException
	 */
	public OrSubQueryFilter(Collection<AbstractQueryFilter> filters) throws IllegalArgumentException {
		super(filters);
	}

	/**
	 * initialize empty sub query filter
	 */
	public OrSubQueryFilter() {
		super();
	}

	@Override
	public void toFilterString(StringBuilder fq) {
		super.toFilterString(fq);
	}

	@Override
	public QueryType getQueryType() {
		return QueryType.OR;
	}
}
