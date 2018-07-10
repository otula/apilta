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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import core.tut.pori.dao.clause.JoinClause;
import core.tut.pori.dao.clause.SQLClause;
import core.tut.pori.dao.clause.SQLClause.SQLType;
import core.tut.pori.dao.clause.WhereClause;
import core.tut.pori.datatypes.IntPrimitiveList;
import core.tut.pori.http.parameters.Limits;


/**
 * Helper class, which can be used to build an SQL SELECT clause.
 */
public class SQLSelectBuilder {
	private static final Logger LOGGER = Logger.getLogger(SQLSelectBuilder.class);
	private static final String SORT_ORDER_RANDOM = " RAND()";
	private ArrayList<String> _groupBy = null;
	private ArrayList<JoinClause> _joins = null;
	private Limits _limits = null;
	private LinkedHashMap<String, OrderDirection> _orderBy = null;
	private HashSet<String> _selectColumns = new HashSet<>();
	private String _tableName = null;
	private ArrayList<WhereClause> _whereClauses = null;

	/**
	 * The direction of an sort order in SQL order by clause.
	 */
	public enum OrderDirection{
		/** ascending order */
		ASCENDING(" ASC"),
		/** descending order */
		DESCENDING(" DESC");

		private String _value;

		/**
		 * 
		 * @param value
		 */
		private OrderDirection(String value){
			_value = value;
		}

		/**
		 * 
		 * @return order direction as a string
		 */
		public String toOrderDirectionString(){
			return _value;
		}
	} // enum OrderDirection

	/**
	 * 
	 * @param table
	 */
	public SQLSelectBuilder(String table){
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
	 * 
	 * @param clause
	 */
	public void addJoin(JoinClause clause){
		if(_joins == null){
			_joins = new ArrayList<>();
		}
		_joins.add(clause);
	}
	
	/**
	 * 
	 * @param column
	 */
	public void addGroupBy(String column){
		if(_groupBy == null){
			_groupBy = new ArrayList<>();
		}
		_groupBy.add(column);
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
	 * @param template to use for retrieval
	 * @return record count for the given sql query or &lt; 0 on error
	 */
	public long getRecordCount(JdbcTemplate template){
		return getRecordCount(template, null);	
	}
	
	/**
	 * 
	 * @param template
	 * @param type
	 * @return record count (SQL COUNT(*)) for the given select query parameters
	 */
	public long getRecordCount(JdbcTemplate template, String type){
		try{
			Long count = template.queryForObject(toSQLString(true, type), getValues(), getValueTypes(), Long.class);
			return (count == null ? 0 : count);
		} catch (DataAccessException ex){
			LOGGER.error(ex, ex);
			return -1;
		}	
	}
	
	/**
	 * 
	 * @return value types or null if none
	 */
	public int[] getValueTypes(){
		return getValueTypes(_whereClauses);
	}
	
	/**
	 * 
	 * @param clauses
	 * @return value types or null if none
	 */
	protected static int[] getValueTypes(List<? extends SQLClause> clauses){
		if(clauses == null || clauses.isEmpty()){
			return null;
		}
		
		IntPrimitiveList list = new IntPrimitiveList();
		for(Iterator<? extends SQLClause> iter = clauses.iterator();iter.hasNext();){
			SQLClause c = iter.next();
			SQLType[] t = c.getValueTypes();
			if(t != null){
				for(int i=0;i<t.length;++i){
					list.add(t[i].toInt());
				}
			}
		}
		return list.toArray();
	}
	
	/**
	 * 
	 * @return values in an array or null if none
	 */
	public Object[] getValues(){
		return getValues(_whereClauses);
	}
	
	/**
	 * 
	 * @param clauses
	 * @return array of values or null if none
	 */
	protected static Object[] getValues(List<? extends SQLClause> clauses){
		if(clauses == null || clauses.isEmpty()){
			return null;
		}
		
		List<Object> objects = new ArrayList<>();
		for(Iterator<? extends SQLClause> iter = clauses.iterator();iter.hasNext();){
			SQLClause c = iter.next();
			Object[] o = c.getValues();
			if(o != null){
				for(int i=0;i<o.length;++i){
					objects.add(o[i]);
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
	 * @return the query as a SQL string
	 */
	public String toSQLString(){
		return toSQLString(false, null);
	}
	
	/**
	 * 
	 * @param type
	 * @return the query as a SQL string using type information for parameters (e.g. Limits). See {@link core.tut.pori.http.parameters.Limits}
	 */
	public String toSQLString(String type){
		return toSQLString(false, type);
	}
	
	/**
	 * helper method for generating SQL query string
	 * 
	 * @param onlyCount
	 * @param type
	 * @return the query as a SQL string
	 */
	private String toSQLString(boolean onlyCount, String type){
		StringBuilder sql = new StringBuilder("SELECT ");
		
		/* create select columns */
		if(onlyCount){
			sql.append("COUNT (*) FROM ");
		}else if(_selectColumns.isEmpty()){
			sql.append("* FROM ");
		}else{
			Iterator<String> iter = _selectColumns.iterator();
			sql.append(iter.next());
			while(iter.hasNext()){
				sql.append(',');
				sql.append(iter.next());
			}
			sql.append(" FROM ");
		}
		sql.append(_tableName);
		
		/* create joins */
		if(_joins != null){
			for(Iterator<JoinClause> iter = _joins.iterator();iter.hasNext();){
				iter.next().toSQLString(sql);
			}
		}

		/* create where clauses */
		if(_whereClauses != null){
			sql.append(" WHERE ");
			Iterator<WhereClause> iter = _whereClauses.iterator();
			iter.next().toSQLString(sql);
			while(iter.hasNext()){
				WhereClause clause = iter.next();
				sql.append(clause.getClauseType().toClauseString());
				clause.toSQLString(sql);
			}
		}
		
		/* create group by */
		if(_groupBy != null){
			sql.append(" GROUP BY ");
			Iterator<String> gIter = _groupBy.iterator();
			sql.append(gIter.next());
			while(gIter.hasNext()){
				sql.append(',');
				sql.append(gIter.next());
			}
		}

		/* create order by */
		if(_orderBy != null){
			sql.append(" ORDER BY ");
			for(Entry<String, OrderDirection> e : _orderBy.entrySet()){
				String column = e.getKey();
				if(StringUtils.isBlank(column)){
					sql.append(SORT_ORDER_RANDOM);
				}else{
					sql.append(column);
					sql.append(e.getValue().toOrderDirectionString());
				}
				sql.append(',');
			}
			sql.setLength(sql.length()-1);  // chop the tailing ,
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

	/**
	 * 
	 * @param column null column name is accepted, though it equals to random sort order
	 * @param direction
	 */
	public void addOrderBy(String column, OrderDirection direction){
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
	 * 
	 * @param column
	 */
	public void addSelectColumn(String column){
		_selectColumns.add(column);
	}

	/**
	 * 
	 * @param columns
	 */
	public void addSelectColumns(String[] columns){
		for(int i=0;i<columns.length;++i){
			_selectColumns.add(columns[i]);
		}
	}
}
