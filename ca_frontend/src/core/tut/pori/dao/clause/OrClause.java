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
package core.tut.pori.dao.clause;

import java.util.Collection;

/**
 * A simple SQL WHERE OR clause.
 * 
 * This class will always prefix the clause with OR, and giving multiple values equals to SQL OR IN (?,?,...)
 * 
 * Passing null object, null array or empty array equals to providing the clause with SQL NULL. An array can also have a null value.
 */
public class OrClause extends AndClause{
	/**
	 * 
	 * @param column
	 * @param value
	 * @param type
	 */
	public OrClause(String column, Object value, SQLType type){
		super(column, value, type);
	}
	
	/**
	 * 
	 * @param column
	 * @param values
	 * @param type
	 */
	public OrClause(String column, Object[] values, SQLType type){
		super(column, values, type);
	}
	
	/**
	 * @param column
	 * @param values
	 * @param type
	 */
	public OrClause(String column, Collection<? extends Object> values, SQLType type) {
		super(column, values, type);
	}

	/**
	 * 
	 * @param column
	 * @param values
	 */
	public OrClause(String column, int[] values){
		super(column,values);
	}
	
	/**
	 * 
	 * @param column
	 * @param values
	 */
	public OrClause(String column, long[] values){
		super(column,values);
	}
	
	/**
	 * 
	 * @param column
	 * @param values
	 */
	public OrClause(String column, double[] values){
		super(column,values);
	}
	
	/**
	 * 
	 * @param column
	 * @param values
	 */
	public OrClause(String column, boolean[] values){
		super(column,values);
	}

	@Override
	public ClauseType getClauseType() {
		return ClauseType.OR;
	}
}
