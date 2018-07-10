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
 * Create and SQL sub-clause with OR prefix. WHERE OR (CLAUSE [OR] CLAUSE [OR] ...), 
 * the inner operation ([OR]) depends on the type of the clause provided as a sub-clause.
 * 
 * This class is provided for convenience. In some cases the same functionality could be achieved by a combination of multiple OR clauses without sub-clause.
 * 
 * Note: not adding any clauses will make this clause a no-op.
 */
public class OrSubClause extends AndSubClause{
	/**
	 * 
	 * @param clauses
	 */
	public OrSubClause(WhereClause[] clauses) {
		super(clauses);
	}

	@Override
	public ClauseType getClauseType() {
		return ClauseType.OR;
	}
	
	/**
	 * 
	 */
	public OrSubClause(){
		super();
	}
} // class OrSubClause