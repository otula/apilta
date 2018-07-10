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
package service.tut.pori.apilta.alerts;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrException;

import core.tut.pori.dao.SQLSelectBuilder.OrderDirection;
import core.tut.pori.dao.SolrDAO;
import core.tut.pori.dao.SolrQueryBuilder;
import core.tut.pori.dao.filter.AbstractQueryFilter.QueryType;
import core.tut.pori.dao.filter.AndQueryFilter;
import core.tut.pori.dao.filter.AndSubQueryFilter;
import core.tut.pori.dao.filter.RangeQueryFilter;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.DateIntervalParameter.Interval;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.SortOptions;
import service.tut.pori.apilta.alerts.datatypes.Alert;
import service.tut.pori.apilta.alerts.datatypes.AlertList;
import service.tut.pori.apilta.alerts.datatypes.AndGeoFilter;
import service.tut.pori.apilta.alerts.datatypes.Definitions;
import service.tut.pori.apilta.alerts.datatypes.Location;

/**
 * dao for accessing alert data
 * 
 */
public class AlertsDAO extends SolrDAO {
	private static final SortOptions DEFAULT_SORT_OPTIONS;
	static{
		DEFAULT_SORT_OPTIONS = new SortOptions();
		DEFAULT_SORT_OPTIONS.addSortOption(new SortOptions.Option(SOLR_FIELD_ID, OrderDirection.ASCENDING, null));
	}
	private static final Logger LOGGER = Logger.getLogger(AlertsDAO.class);
	private static final String[] SOLR_FIELDS_BASIC = {SOLR_FIELD_ID, Definitions.SOLR_FIELD_ALERT_TYPE, SOLR_FIELD_CREATED, Definitions.SOLR_FIELD_DESCRIPTION, Definitions.SOLR_FIELD_LOCATION};
	private static final String SOLR_SERVER_BEAN_ID = "solrServerAlerts";

	/**
	 * 
	 * @param alertGroupIdFilter optional filter
	 * @param alertTypeFilter optional filter
	 * @param createdFilter 
	 * @param dataGroups 
	 * @param limits
	 * @param location
	 * @param range
	 * @return list of alerts or null if none was found
	 */
	public AlertList getAlerts(List<Long> alertGroupIdFilter, List<String> alertTypeFilter, Set<Interval> createdFilter, DataGroups dataGroups, Limits limits, Location location, Double range) {
		SolrQueryBuilder solr = new SolrQueryBuilder();
		solr.setLimits(limits);
		solr.setSortOptions(DEFAULT_SORT_OPTIONS);
						
		if(!DataGroups.hasDataGroup(DataGroups.DATA_GROUP_ALL, dataGroups)){ // if all not present...
			solr.addFields(SOLR_FIELDS_BASIC); // ... limit to basic details
		}
		
		if(alertGroupIdFilter != null && !alertGroupIdFilter.isEmpty()){
			LOGGER.debug("Using alert group id filter...");
			solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_ALERT_GROUP_ID, alertGroupIdFilter));
		}
		
		if(alertTypeFilter != null && !alertTypeFilter.isEmpty()){
			LOGGER.debug("Using alert type filter...");
			solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_ALERT_TYPE, alertTypeFilter));
		}
		
		if(location != null){
			LOGGER.debug("Using location filter...");	
			Double heading = location.getHeading();
			if(heading != null){
				LOGGER.warn("Ignored heading..."); // TODO calculate new bounding box/location based on heading ?
			}
			
			solr.addCustomFilter(new AndGeoFilter(Definitions.SOLR_FIELD_LOCATION, location, range));
		}
		
		if(createdFilter != null && !createdFilter.isEmpty()){
			LOGGER.debug("Using created filter...");
			AndSubQueryFilter cf = new AndSubQueryFilter();
			for(Interval interval : createdFilter){
				cf.addFilter(new RangeQueryFilter(SolrDAO.SOLR_FIELD_CREATED, interval.getStart(), interval.getEnd(), QueryType.OR));
			}
			solr.addCustomFilter(cf);
		}
		
		solr.addCustomFilter(new RangeQueryFilter(Definitions.SOLR_FIELD_VALID, new Date(), null, QueryType.AND)); // check that the valid until timestamp is in the future (after current time)
		
		List<Alert> alerts = getSolrTemplate(SOLR_SERVER_BEAN_ID).queryForList(solr.toSolrQuery(Definitions.ELEMENT_ALERT_LIST), Alert.class);
		if(alerts == null){
			return null;
		}else{
			AlertList list = new AlertList();
			list.setAlerts(alerts);
			return list;
		}
	}

	/**
	 * 
	 * @param alert
	 * @param alertGroupIds 
	 * @return id for the alert
	 */
	public String addAlert(Alert alert, List<Long> alertGroupIds) {
		String alertId = UUID.randomUUID().toString();
		alert.setAlertId(alertId);
		alert.setAlertGroupIds(alertGroupIds);
			
		int status = getSolrTemplate(SOLR_SERVER_BEAN_ID).addBean(alert).getStatus();
		if(status != SolrException.ErrorCode.UNKNOWN.code){
			LOGGER.warn("Failed to add alert, status: "+status);
			alert.setAlertId(null);
			return null;
		}else{
			return alertId;
		}
	}
}
