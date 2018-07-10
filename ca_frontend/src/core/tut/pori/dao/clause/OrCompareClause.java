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

/**
 * A simple OR compare clause for comparing column value in an SQL where clause. 
 * 
 * For example, OR COLUMN_NAME<=VALUE
 * 
 * This class will not accept null as an acceptable value, an attempt to pass null will result in an exception to be thrown.
 *
 * Note that if you want to use a simple equals comparison (=), you can use the basic OR clause {@link core.tut.pori.dao.clause.OrClause}.
 */
public class OrCompareClause extends AndCompareClause {

	/**
	 * 
	 * @param column
	 * @param value
	 * @param cType
	 * @param sType
	 * @throws IllegalArgumentException on invalid value
	 */
	public OrCompareClause(String column, Object value, CompareType cType, SQLType sType) throws IllegalArgumentException {
		super(column, value, cType, sType);
	}

	@Override
	public ClauseType getClauseType() {
		return ClauseType.OR;
	}
}
