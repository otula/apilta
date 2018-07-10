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
 * <p>Create a raw sql WhereClause, the insertable sql string (such as COLUMN=? or COLUMN&lt;9) can be added manually.</p>
 * 
 * <p>This class can be used to create SQL WHERE clauses not supported by the built-in WhereClause implementations,
 * without the need to implement a new WhereClause class.</p>
 *
 */
public class RawClause extends WhereClause{
	private String _sqlClause = null;
	private Object[] _values = null;
	private SQLType[] _types = null;
	private ClauseType _type = null;
	
	/**
	 * 
	 * @param sqlClause the raw clause
	 * @param values optional list of values
	 * @param types optional list of types
	 * @param type type of this clause
	 */
	public RawClause(String sqlClause, Object[] values, SQLType[] types, ClauseType type){
		_sqlClause = sqlClause;
		_values = values;
		_type = type;
		_types = types;
	}

	@Override
	public SQLType[] getValueTypes() {
		return _types;
	}

	@Override
	public Object[] getValues() {
		return _values;
	}

	@Override
	public void toSQLString(StringBuilder sql) {
		sql.append(_sqlClause);
	}

	@Override
	public ClauseType getClauseType() {
		return _type;
	}
}
