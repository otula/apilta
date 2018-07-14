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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import core.tut.pori.dao.SQLSelectBuilder.OrderDirection;
import core.tut.pori.dao.clause.SQLClause;
import core.tut.pori.dao.clause.SQLClause.SQLType;
import core.tut.pori.dao.clause.UpdateClause;
import core.tut.pori.dao.clause.WhereClause;
import core.tut.pori.datatypes.IntPrimitiveList;
import core.tut.pori.http.parameters.Limits;

/**
 * Helper class, which can be used to build an SQL UPDATE clause.
 *
 */
public class SQLUpdateBuilder {
	private Limits _limits = null;
	private LinkedHashMap<String, OrderDirection> _orderBy = null;
	private String _tableName = null;
	private ArrayList<UpdateClause> _updateClauses = new ArrayList<>();
	private ArrayList<WhereClause> _whereClauses = null;
	
	/**
	 * 
	 * @param table
	 */
	public SQLUpdateBuilder(String table){
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
	 * Note that limit offset is not allowed, and providing startItem != 0 will cause exception to be thrown when SQL script is generated (in {@link #toSQLString()} or {@link #toSQLString(String)})
	 * 
	 * @param limits if null, this will clear the previously set limits (if any)
	 */
	public void setLimits(Limits limits){
		_limits = limits;
	}
	
	/**
	 * 
	 * @return number of added update clauses
	 */
	public int getUpdateClauseCount() {
		return _updateClauses.size();
	}
	
	/**
	 * null clause will be silently ignored
	 * 
	 * @param clause
	 */
	public void addUpdateClause(UpdateClause clause){
		if(clause != null){
			_updateClauses.add(clause);
		}
	}
	
	/**
	 * clear the list of update clauses if any
	 */
	public void clearUpdateClauses(){
		_updateClauses.clear();
	}
	
	/**
	 * 
	 * @param column
	 * @param direction
	 * @throws IllegalArgumentException on empty or blank column name
	 */
	public void addOrderBy(String column, OrderDirection direction) throws IllegalArgumentException{
		if(StringUtils.isBlank(column)) {
			throw new IllegalArgumentException("Invalid column name: "+column);
		}
		if(_orderBy == null){
			_orderBy = new LinkedHashMap<>();
		}
		_orderBy.put(column,direction);
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
		List<Object> objects = new ArrayList<>();
		for(Iterator<? extends SQLClause> iter = _updateClauses.iterator();iter.hasNext();){
			SQLClause c = iter.next();
			Object[] o = c.getValues();
			if(o != null){
				for(int i=0;i<o.length;++i){
					objects.add(o[i]);
				}
			}
		}
		
		if(_whereClauses != null){
			for(Iterator<? extends SQLClause> iter = _whereClauses.iterator();iter.hasNext();){
				SQLClause c = iter.next();
				Object[] o = c.getValues();
				if(o != null){
					for(int i=0;i<o.length;++i){
						objects.add(o[i]);
					}
				}
			}
		}
		
		if(objects.isEmpty()){
			return null;
		}else{
			return objects.toArray();
		}
	}
	
	/**
	 * 
	 * @return array of parameter types
	 */
	public int[] getValueTypes(){
		IntPrimitiveList list = new IntPrimitiveList();
		for(Iterator<? extends SQLClause> iter = _updateClauses.iterator();iter.hasNext();){
			SQLClause c = iter.next();
			SQLType[] t = c.getValueTypes();
			if(t != null){
				for(int i=0;i<t.length;++i){
					list.add(t[i].toInt());
				}
			}
		}
		
		if(_whereClauses != null){
			for(Iterator<? extends SQLClause> iter = _whereClauses.iterator();iter.hasNext();){
				SQLClause c = iter.next();
				SQLType[] t = c.getValueTypes();
				if(t != null){
					for(int i=0;i<t.length;++i){
						list.add(t[i].toInt());
					}
				}
			}
		}
		
		return list.toArray();
	}
	
	/**
	 * 
	 * @return the query as a SQL string
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
		StringBuilder sql = new StringBuilder("UPDATE ");
		sql.append(_tableName);
		sql.append(" SET ");
		
		/* update clauses */
		Iterator<UpdateClause> iter = _updateClauses.iterator();
		iter.next().toSQLString(sql);	// let it throw out-of-bounds if there are no update clauses
		while(iter.hasNext()){
			sql.append(',');
			iter.next().toSQLString(sql);
		}

		/* create where clauses */
		if(_whereClauses != null){
			sql.append(" WHERE ");
			Iterator<WhereClause> witer = _whereClauses.iterator();
			witer.next().toSQLString(sql);
			while(witer.hasNext()){
				WhereClause clause = witer.next();
				sql.append(clause.getClauseType().toClauseString());
				clause.toSQLString(sql);
			}
		}
		
		/* create order by */
		if(_orderBy != null){
			sql.append(" ORDER BY ");
			for(Entry<String, OrderDirection> e : _orderBy.entrySet()){
				sql.append(e.getKey());
				sql.append(e.getValue().toOrderDirectionString());
				sql.append(',');
			}
			sql.setLength(sql.length()-1);  // chop the tailing ,
		}
		
		/* create limit */
		if(_limits != null){
			int startItem = _limits.getStartItem(type);
			if(startItem != 0) {
				throw new UnsupportedOperationException("Limit offset not supported for DELETE: startItem was != 0");
			}
			sql.append(" LIMIT ");
			sql.append(_limits.getMaxItems(type));
		}
		
		return sql.toString();
	}
}
