/**
 * Copyright 2016 Tampere University of Technology, Pori Department
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
package service.tut.pori.apilta.alerts.datatypes;

import core.tut.pori.dao.SolrDAO;

/**
 * definitions for alerts package data types
 * 
 */
public final class Definitions {
	/* elements */
	/** xml element declaration */
	public static final String ELEMENT_ALERT = "alert";
	/** xml element declaration */
	public static final String ELEMENT_ALERT_GROUP = "alertGroup";
	/** xml element declaration */
	public static final String ELEMENT_ALERT_GROUP_ID = "alertGroupId";
	/** xml element declaration */
	public static final String ELEMENT_ALERT_ID = "alertId";
	/** xml element declaration */
	public static final String ELEMENT_ALERT_LIST = "alertList";
	/** xml element declaration */
	public static final String ELEMENT_ALERT_TYPE = "alertType";
	/** xml element declaration */
	public static final String ELEMENT_CREATED_TIMESTAMP = "createdTimestamp";
	/** xml element declaration */
	public static final String ELEMENT_DESCRIPTION = "description";
	/** xml element declaration */
	public static final String ELEMENT_HEADING = "heading";
	/** xml element declaration */
	public static final String ELEMENT_LATITUDE = "latitude";
	/** xml element declaration */
	public static final String ELEMENT_LOCATION = "location";
	/** xml element declaration */
	public static final String ELEMENT_LONGITUDE = "longitude";
	/** xml element declaration */
	public static final String ELEMENT_NAME = "name";
	/** xml element declaration */
	public static final String ELEMENT_PERMISSION = "permission";
	/** xml element declaration */
	public static final String ELEMENT_PERMISSION_LIST = "permissionList";
	/** xml element declaration */
	public static final String ELEMENT_RANGE = "range";
	/** xml element declaration */
	public static final String ELEMENT_VALID_TIMESTAMP = "validTimestamp";
	
	/* solr fields */
	/* solr dynamic field datatypes */
	/** SOLR field declaration */
	public static final String SOLR_FIELD_ALERT_GROUP_ID = ELEMENT_ALERT_GROUP_ID+"_ll";
	/** SOLR field declaration */
	public static final String SOLR_FIELD_ALERT_TYPE = ELEMENT_ALERT_TYPE+SolrDAO.SOLR_STRING;
	/** SOLR field declaration */
	public static final String SOLR_FIELD_DESCRIPTION = ELEMENT_DESCRIPTION+SolrDAO.SOLR_STRING;
	/** SOLR field declaration */
	public static final String SOLR_FIELD_FILE_GUIDS = "fileGUIDs"+SolrDAO.SOLR_STRING_LIST;
	/** SOLR field declaration */
	public static final String SOLR_FIELD_LOCATION = "location";
	/** SOLR field declaration */
	public static final String SOLR_FIELD_RANGE = ELEMENT_RANGE+SolrDAO.SOLR_INTEGER;
	/** SOLR field declaration */
	public static final String SOLR_FIELD_USER_ID = core.tut.pori.users.Definitions.ELEMENT_USER_ID+SolrDAO.SOLR_LONG;
	/** SOLR field declaration */
	public static final String SOLR_FIELD_VALID = ELEMENT_VALID_TIMESTAMP+SolrDAO.SOLR_DATE;

	/**
	 * 
	 */
	private Definitions() {
		// nothing needed
	}
}
