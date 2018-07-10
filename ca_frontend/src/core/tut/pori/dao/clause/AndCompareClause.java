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
 * A simple AND compare clause for comparing column value in an SQL where clause. 
 * 
 * For example, AND COLUMN_NAME<=VALUE
 * 
 * This class will not accept null as an acceptable value, an attempt to pass null will result in an exception to be thrown.
 *
 * Note that if you want to use a simple equals comparison (=), you can use the basic AND clause {@link core.tut.pori.dao.clause.AndClause}.
 */
public class AndCompareClause extends WhereClause {
	private String _column = null;
	private CompareType _cType = null;
	private SQLType[] _sType = null;
	private Object[] _value = null;
	
	/**
	 * type of the comparison
	 *
	 */
	public enum CompareType {
		/**
		 * the column value is lesser than the given value.
		 * 
		 * COLUMN_VALUE<VALUE
		 */
		LESSER("<"),
		/**
		 * the column value is greater than the given value.
		 * 
		 * COLUMN_VALUE>VALUE
		 */
		GREATER(">"),
		/**
		 * the column value is less or equal than the given value.
		 * 
		 * COLUMN_VALUE<=VALUE
		 */
		LESS_OR_EQUAL("<="),
		/**
		 * the column value is greater or equal than the given value.
		 * 
		 * COLUMN_VALUE=>VALUE
		 */
		GREATER_OR_EQUAL(">=");
		
		private String _value = null;
		
		/**
		 * 
		 * @param value
		 */
		private CompareType(String value) {
			_value = value;
		}
		
		/**
		 * returning the value of this type
		 */
		@Override
		public String toString() {
			return _value;
		}
	} // enum CompareType
	
	/**
	 * 
	 * @param column
	 * @param value
	 * @param cType
	 * @param sType
	 * @throws IllegalArgumentException on invalid value
	 */
	public AndCompareClause(String column, Object value, CompareType cType, SQLType sType) throws IllegalArgumentException {
		_column = column;
		if(value == null) { // comparing <,>,<= or >= against NULL in SQL will most likely result in something not intended, so do not allow it.
			throw new IllegalArgumentException("Value cannot be null");
		}
		_column = column;
		_value = new Object[]{value};
		_cType = cType;
		_sType = new SQLType[]{sType};
	}

	@Override
	public SQLType[] getValueTypes() {
		return _sType;
	}

	@Override
	public Object[] getValues() {
		return _value;
	}

	@Override
	public void toSQLString(StringBuilder sql) {
		sql.append('(');
		sql.append(_column);
		sql.append(_cType);
		sql.append("?)");
	}

	@Override
	public ClauseType getClauseType() {
		return ClauseType.AND;
	}
}
