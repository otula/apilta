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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Create and SQL sub-clause with AND prefix. WHERE AND (CLAUSE [OR] CLAUSE [OR] ...), 
 * the inner operation ([OR]) depends on the type of the clause provided as a sub-clause.
 * 
 * Note: not adding any clauses will make this clause a no-op.
 */
public class AndSubClause extends WhereClause{
	private List<WhereClause> _whereClauses = null;
	private SQLType[] _types = null;
	private Object[] _values = null;
	private int _valueCount = 0;

	/**
	 * 
	 * @param clauses
	 */
	public AndSubClause(WhereClause[] clauses){
		_whereClauses = new ArrayList<>(clauses.length);
		if(!ArrayUtils.isEmpty(clauses)){
			for(int i=0;i<clauses.length;++i){
				addWhereClause(clauses[i]);
			}
		}
	}
	
	/**
	 * 
	 */
	public AndSubClause(){
		_whereClauses = new ArrayList<>();
	}
	
	/**
	 * 
	 * @param clause
	 */
	public void addWhereClause(WhereClause clause){
		Object[] values = clause.getValues();
		if(values != null){
			_values = null;	// not valid anymore
			_types = null;	// not valid anymore
			_valueCount+=values.length;
		}
		_whereClauses.add(clause);
	}

	@Override
	public SQLType[] getValueTypes() {
		createArrays();
		return _types;
	}

	@Override
	public Object[] getValues() {
		createArrays();
		return _values;
	}
	
	/**
	 * helper for creating (combining sub-clause) arrays if needed
	 */
	protected void createArrays(){
		if(_values == null && _valueCount > 0){
			_values = new Object[_valueCount];
			_types = new SQLType[_valueCount];
			int index = 0;
			for(Iterator<WhereClause> iter = _whereClauses.iterator();iter.hasNext();){
				WhereClause clause = iter.next();
				Object[] values = clause.getValues();
				if(values != null){
					SQLType[] types = clause.getValueTypes();
					for(int i=0;i<values.length;++i){
						_values[index] = values[i];
						_types[index++] = types[i];	//set and increase for next addition
					}	// for values
				}	// if values
			}	// for clauses
		}	// if
	}

	@Override
	public void toSQLString(StringBuilder sql) {
		int size = _whereClauses.size();
		if(size > 0){
			sql.append('(');
			_whereClauses.get(0).toSQLString(sql);
			for(int i=1;i<size;++i){
				WhereClause clause = _whereClauses.get(i);
				sql.append(clause.getClauseType().toClauseString());
				clause.toSQLString(sql);
			}
			sql.append(')');
		}
	}

	@Override
	public ClauseType getClauseType() {
		return ClauseType.AND;
	}		
}
