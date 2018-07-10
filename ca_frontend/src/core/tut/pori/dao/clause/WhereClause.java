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
 * Base class for SQL WHERE clauses.
 */
public abstract class WhereClause implements SQLClause{		
	/**
	 * Type of the WhereClause
	 */
	public enum ClauseType{
		/** sql OR clause */
		OR(" OR "),
		/** sql AND clause */
		AND(" AND ");

		private String _value;

		/**
		 * 
		 * @param value
		 */
		private ClauseType(String value){
			_value = value;
		}

		/**
		 * 
		 * @return the clause type as a string
		 */
		public String toClauseString(){
			return _value;
		}
	} // enum ClauseType

	/**
	 * 
	 * @return type of the clause
	 */
	public abstract ClauseType getClauseType();
}
