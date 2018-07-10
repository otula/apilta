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

import org.apache.commons.lang3.StringUtils;

/**
 * This class defines a single SQL update clause.
 * 
 * Passing null equals to creating an update clause with SQL NULL.
 *
 */
public class UpdateClause implements SQLClause{
	private String _column = null;
	private Object[] _value = null;
	private SQLType[] _type = null;
	
	/**
	 * 
	 * @param column
	 * @param value a single object or null, passing an array will create undefined behavior
	 * @param type
	 */
	public UpdateClause(String column, Object value, SQLType type){
		_column = column;
		_value = new Object[]{value};
		_type = new SQLType[]{type};
	}
	
	/**
	 * 
	 * @param column
	 * @param value a single object or null, passing an array will have undefined behavior
	 * @param type the correct type of the value parameter
	 * @return new update clause or null if the given value was null, empty or blank
	 */
	public static UpdateClause getIfNotBlankValue(String column, Object value, SQLType type){
		if(value == null){
			return null;
		}else if(type == SQLType.STRING && StringUtils.isBlank((CharSequence) value)){
			return null;
		}else{
			return new UpdateClause(column, value, type);
		}
	}
	
	@Override
	public void toSQLString(StringBuilder sql){
		sql.append(_column);
		sql.append("=?");
	}

	@Override
	public SQLType[] getValueTypes() {
		return _type;
	}

	@Override
	public Object[] getValues() {
		return _value;
	}
}
