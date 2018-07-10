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
package core.tut.pori.dao.clause;

import java.sql.Types;

/**
 * The interface for implementing SQL Clauses.
 *
 */
public interface SQLClause {
	/**
	 * Supported SQL types for the clause
	 */
	public enum SQLType{
		/** signed integer */
		INTEGER(Types.INTEGER),
		/** signed long integer */
		LONG(Types.BIGINT),
		/** double */
		DOUBLE(Types.DOUBLE),
		/** text string */
		STRING(Types.VARCHAR),
		/** boolean */
		BOOLEAN(Types.BOOLEAN),
		/** date/timestamp */
		TIMESTAMP(Types.TIMESTAMP);
		
		private int _value;
		
		private SQLType(int value){
			_value = value;
		}
		
		/**
		 * 
		 * @return this SQLType as java.sql.Types.*
		 */
		public int toInt(){
			return _value;
		}
	} // enum SQLType
	
	/**
	 * 
	 * @return value types
	 */
	public SQLType[] getValueTypes();
	
	/**
	 * Note: null value is accepted IF NULL is also set on the valueType map, otherwise behavior is undefined
	 * 
	 * The values will be added in-order in places designated by ? in the sql string
	 * 
	 * @return values
	 */
	public Object[] getValues();
	
	/**
	 * print this clause using the given builder, the output should NOT include AND or OR keyword, this will be automatically added when needed
	 * @param sql
	 */
	public void toSQLString(StringBuilder sql);
}
