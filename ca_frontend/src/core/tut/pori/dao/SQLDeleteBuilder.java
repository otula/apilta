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
package core.tut.pori.dao;

import java.util.ArrayList;
import java.util.Iterator;

import org.springframework.jdbc.core.JdbcTemplate;

import core.tut.pori.dao.clause.SQLClause;
import core.tut.pori.dao.clause.WhereClause;
import core.tut.pori.http.parameters.Limits;


/**
 * Helper class, which can be used to build an SQL DELETE clause.
 * 
 * Note that the class has no implementations of its own of the SQLClause, 
 * but uses the same clauses as defined by SQLSelectBuilder (SQL WHERE).
 */
public class SQLDeleteBuilder {
	private Limits _limits = null;
	private String _tableName = null;
	private ArrayList<SQLClause> _whereClauses = null;
	
	/**
	 * 
	 * @param table
	 */
	public SQLDeleteBuilder(String table){
		_tableName = table;
	}
	
	
	/**
	 * clear the currently set list of where clauses
	 */
	public void clearWhereClauses(){
		_whereClauses = null;
	}
	
	/**
	 * 
	 * @return number of added where clauses
	 */
	public int getWhereClauseCount() {
		return (_whereClauses == null ? 0 : _whereClauses.size());
	}
	
	/**
	 * @param startItem the startItem to set, negative value disables limit
	 */
	public void setStartItem(int startItem) {
		if(_limits == null){
			_limits = new Limits(startItem, Limits.DEFAULT_MAX_ITEMS);
		}else{
			_limits.setTypeLimits(startItem, _limits.getEndItem(), null);
		}
	}

	/**
	 * @param maxItems the maxItems to set, negative value disables limit
	 */
	public void setMaxItems(int maxItems) {
		if(_limits == null){
			_limits = new Limits(0, maxItems-1);
		}else{
			_limits.setTypeLimits(_limits.getStartItem(), maxItems-1, null);
		}
	}
	
	/**
	 * 
	 * @param limits if null, this is a no-op
	 */
	public void setLimits(Limits limits){
		_limits = limits;
	}
	
	/**
	 * 
	 * @param clause
	 */
	public void addWhereClause(WhereClause clause){
		if(_whereClauses == null){
			_whereClauses = new ArrayList<>();
		}
		_whereClauses.add(clause);
	}
	
	/**
	 * execute the statement on the given template
	 * @param template
	 * @return number of rows affected
	 */
	public int execute(JdbcTemplate template){
		return template.update(toSQLString(), getValues(), getValueTypes());
	}
	
	/**
	 * execute the statement on the given template
	 * @param template
	 * @param type
	 * @return number of rows affected
	 */
	public int execute(JdbcTemplate template, String type){
		return template.update(toSQLString(type), getValues(), getValueTypes());
	}
	
	/**
	 * 
	 * @return array of parameter values
	 */
	public Object[] getValues(){
		return SQLSelectBuilder.getValues(_whereClauses);
	}
	
	/**
	 * 
	 * @return array of parameter types
	 */
	public int[] getValueTypes(){
		return SQLSelectBuilder.getValueTypes(_whereClauses);
	}
	
	/**
	 * 
	 * @return the query as an SQL string
	 */
	public String toSQLString(){
		return toSQLString(null);
	}

	/**
	 * 
	 * @param type
	 * @return the query as an SQL string using the given type of typed parameters (e.g. Limits). See {@link core.tut.pori.http.parameters.Limits}
	 */
	public String toSQLString(String type){
		StringBuilder sql = new StringBuilder("DELETE FROM ");
		sql.append(_tableName);

		/* create where clauses */
		if(_whereClauses != null){
			sql.append(" WHERE ");
			Iterator<SQLClause> iter = _whereClauses.iterator();
			iter.next().toSQLString(sql);
			while(iter.hasNext()){
				WhereClause clause = (WhereClause) iter.next();
				sql.append(clause.getClauseType().toClauseString());
				clause.toSQLString(sql);
			}
		}
		
		/* create limit */
		if(_limits != null){
			sql.append(" LIMIT ");
			sql.append(_limits.getStartItem(type));
			sql.append(',');
			sql.append(_limits.getMaxItems());
		}
		
		return sql.toString();
	}
}
