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
 * Basic and filter with AND relation to other query filters, the values themselves have OR relation with one another.
 *
 */
public class AndQueryFilter extends OrQueryFilter {
	/**
	 * @param fieldName
	 * @param values
	 * @throws IllegalArgumentException
	 */
	public AndQueryFilter(String fieldName, Collection<? extends Object> values) throws IllegalArgumentException {
		super(fieldName, values);
	}

	/**
	 * @param fieldName
	 * @param values
	 * @throws IllegalArgumentException
	 */
	public AndQueryFilter(String fieldName, int[] values) throws IllegalArgumentException {
		super(fieldName, values);
	}

	/**
	 * @param fieldName
	 * @param values
	 * @throws IllegalArgumentException
	 */
	public AndQueryFilter(String fieldName, long[] values) throws IllegalArgumentException {
		super(fieldName, values);
	}

	/**
	 * @param fieldName
	 * @param value
	 * @throws IllegalArgumentException
	 */
	public AndQueryFilter(String fieldName, Object value) throws IllegalArgumentException {
		super(fieldName, value);
	}

	@Override
	public void toFilterString(StringBuilder fq) {
		super.toFilterString(fq);
	}

	@Override
	public QueryType getQueryType() {
		return QueryType.AND;
	}
}
