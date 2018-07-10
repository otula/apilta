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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

/**
 * A simple SQL WHERE AND clause.
 * 
 * This class will always prefix the clause with AND, and giving multiple values equals to SQL AND IN (?,?,...)
 * 
 * Passing null object, null array or empty array equals to providing the clause with SQL NULL. An array can also have a null value.
 */
public class AndClause extends WhereClause{
	private String _column = null;
	private SQLType[] _types = null;
	private Object[] _values = null;
	private boolean _not = false;
	private boolean _hasNull = false;

	@Override
	public SQLType[] getValueTypes() {
		return _types;
	}

	@Override
	public Object[] getValues() {
		return _values;
	}
	
	/**
	 * 
	 * @param column
	 * @param values
	 */
	public AndClause(String column, int[] values){
		_column = column;
		setValues(ArrayUtils.toObject(values), SQLType.INTEGER);
	}
	
	/**
	 * 
	 * @param column
	 * @param values
	 */
	public AndClause(String column, long[] values){
		_column = column;
		setValues(ArrayUtils.toObject(values), SQLType.LONG);
	}
	
	/**
	 * 
	 * @param column
	 * @param values
	 */
	public AndClause(String column, double[] values){
		_column = column;
		setValues(ArrayUtils.toObject(values), SQLType.DOUBLE);
	}
	
	/**
	 * 
	 * @param column
	 * @param values
	 */
	public AndClause(String column, boolean[] values){
		_column = column;
		setValues(ArrayUtils.toObject(values), SQLType.BOOLEAN);
	}

	/**
	 * @param column
	 * @param value can be null
	 * @param type type of data
	 */
	public AndClause(String column, Object value, SQLType type){
		setValues(new Object[]{value}, type);
		_column = column;
	}
	
	/**
	 * 
	 * @param column
	 * @param values
	 * @param type
	 */
	public AndClause(String column, Collection<? extends Object> values, SQLType type){
		_column = column;
		Object[] o = null;
		if(values != null){
			o = values.toArray();
		}
		setValues(o, type);
	}
	
	/**
	 * Note: this does creates an IN () clause based on the list of values, NOT multiple AND clauses
	 * that include all of the given values. This is because no column can have more than a single value,
	 * and thus, creating AND for all of the given values would not make sense.
	 * 
	 * @param column
	 * @param values can be null
	 * @param type type of data
	 */
	public AndClause(String column, Object[] values, SQLType type){
		setValues(values, type);
		_column = column;
	}
	
	/**
	 * 
	 * @param values
	 * @param type
	 */
	public void setValues(Object[] values, SQLType type){
		_values = null;
		_types = null;
		if(!ArrayUtils.isEmpty(values)){
			List<SQLType> types = new ArrayList<>(values.length);
			for(int i=0;i<values.length;++i){
				if(values[i] == null){
					_hasNull = true;
					values = ArrayUtils.remove(values, i--); // remove the current value and decrease counter
					continue;
				}
				types.add(type);
			} // for
			if(!types.isEmpty()){
				_values = values;
				_types = types.toArray(new SQLType[types.size()]);
			}
		}else{
			_hasNull = true;
		}
	}

	@Override
	public void toSQLString(StringBuilder sql) {
		sql.append('(');
		if(_values != null){
			sql.append(_column);
			if(_not){
				sql.append(" NOT IN (");
			}else{
				sql.append(" IN (");
			}
			sql.append('?');
			for(int i=1;i<_values.length;++i){
				sql.append(",?");
			}
			if(_hasNull){
				sql.append(") OR ");
			}else{
				sql.append(')');
			}
		}
		if(_hasNull){
			sql.append(_column);
			if(_not){
				sql.append(" IS NOT NULL");
			}else{
				sql.append(" IS NULL");
			}
		}
		sql.append(')');
	}

	/**
	 * 
	 * @param not set this to be NOT clause on true, remove NOT declaration on false
	 * @return this
	 */
	public AndClause setNot(boolean not){
		_not = not;
		return this;
	}

	@Override
	public ClauseType getClauseType() {
		return ClauseType.AND;
	}
}
