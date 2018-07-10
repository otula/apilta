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
 * A class, which implements a simple SQL JOIN clause.
 *
 */
public class JoinClause{
	private String _joinClause = null;
	
	/**
	 * 
	 * @param joinClause e.g. "LEFT JOIN srcTable ON srcTable.sourceColumn=targetTable.targetColumn"
	 */
	public JoinClause(String joinClause){
		_joinClause = joinClause;
	}
	
	/**
	 * 
	 * @param sql
	 */
	public void toSQLString(StringBuilder sql){
		sql.append(' ');
		sql.append(_joinClause);
	}
}
