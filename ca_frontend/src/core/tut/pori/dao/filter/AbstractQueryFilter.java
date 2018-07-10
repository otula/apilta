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
package core.tut.pori.dao.filter;

/**
 * Interface for creating SOLR Query filters
 *
 */
public interface AbstractQueryFilter {
	/** Solr AND operator definition */
	public static final String SOLR_AND = " AND ";
	/** Solr OR operator definition */
	public static final String SOLR_OR = " OR ";
	
	/**
	 * Type of the query
	 * 
	 * <a href="https://wiki.apache.org/solr/SolrQuerySyntax">Solr Query Syntax</a>
	 */
	public enum QueryType{
		/** solr AND query */
		AND(SOLR_AND),
		/** solr OR query */
		OR(SOLR_OR);
		
		private String _type;
		
		/**
		 * 
		 * @param type
		 */
		private QueryType(String type){
			_type = type;
		}
		
		/**
		 * 
		 * @return type as a string
		 */
		public String toTypeString(){
			return _type;
		}
	} // enum QueryType
	
	/**
	 * The appended contents should NOT contain the fq= parameter, and should contain ( ) when necessary to contain the query contents.
	 * 
	 * @param fq append this query's contents to the given filter query
	 */
	public void toFilterString(StringBuilder fq);
	
	/**
	 * 
	 * @return type of the query
	 */
	public QueryType getQueryType();
}
