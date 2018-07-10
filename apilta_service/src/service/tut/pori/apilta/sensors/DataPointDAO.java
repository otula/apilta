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
package service.tut.pori.apilta.sensors;

import java.util.Collection;
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
import core.tut.pori.http.parameters.DateIntervalParameter.Interval;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.SortOptions;
import service.tut.pori.apilta.sensors.datatypes.DataPoint;
import service.tut.pori.apilta.sensors.datatypes.Definitions;

/**
 * DAO for accessing data points
 * 
 */
public class DataPointDAO extends SolrDAO {
	private static final String BEAN_ID_SOLR_SERVER = "solrServerSensorsDataPoints";
	private static final SortOptions DEFAULT_SORT_OPTIONS;
	static{
		DEFAULT_SORT_OPTIONS = new SortOptions();
		DEFAULT_SORT_OPTIONS.addSortOption(new SortOptions.Option(SOLR_FIELD_ID, OrderDirection.ASCENDING, null));
	}
	private static final Logger LOGGER = Logger.getLogger(DataPointDAO.class);
	
	/**
	 * 
	 * @param createdFilter optional filter for created timestamps
	 * @param limits 
	 * @param measurementId
	 * @return list of data points or null if none was found
	 */
	public List<DataPoint> getDataPoints(Set<Interval> createdFilter, Limits limits, String measurementId) {
		SolrQueryBuilder solr = new SolrQueryBuilder();
		solr.setLimits(limits);
		solr.setSortOptions(DEFAULT_SORT_OPTIONS);
		solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_MEASUREMENT_ID, measurementId));

		if(createdFilter != null && !createdFilter.isEmpty()){
			LOGGER.debug("Using created filter...");
			AndSubQueryFilter cf = new AndSubQueryFilter();
			for(Interval interval : createdFilter){
				cf.addFilter(new RangeQueryFilter(SolrDAO.SOLR_FIELD_CREATED, interval.getStart(), interval.getEnd(), QueryType.OR));
			}
			solr.addCustomFilter(cf);
		}
		
		return getSolrTemplate(BEAN_ID_SOLR_SERVER).queryForList(solr.toSolrQuery(Definitions.ELEMENT_DATAPOINT_LIST), DataPoint.class);
	}
	
	/**
	 * create points for the given measurement id
	 * 
	 * @param dataPoints
	 * @param measurementId
	 */
	public void createDataPoints(List<DataPoint> dataPoints, String measurementId) {
		for(DataPoint dp : dataPoints) { // populate id and timestamps
			dp.setDataPointId(UUID.randomUUID().toString());
			dp.setMeasurementId(measurementId);
			Date created = dp.getCreated();
			if(created == null){
				LOGGER.debug("No created timestamp, using current timestamp for data point, id: "+dp.getDataPointId());
				created = new Date();
				dp.setCreated(created);
			}
		}
		
		int status = getSolrTemplate(BEAN_ID_SOLR_SERVER).addBeans(dataPoints).getStatus();
		if(status != SolrException.ErrorCode.UNKNOWN.code){
			LOGGER.warn("Failed to create data points for measurement, id: "+measurementId+", status: "+status);
		}
	}
	
	/**
	 * 
	 * @param measurementIds
	 */
	public void deleteDataPoints(Collection<Long> measurementIds) {
		SolrQueryBuilder solr = new SolrQueryBuilder();
		solr.addCustomFilter(new AndQueryFilter(Definitions.SOLR_FIELD_MEASUREMENT_ID, measurementIds));
		int status = getSolrTemplate(BEAN_ID_SOLR_SERVER).deleteByQuery(solr.toSolrQuery()).getStatus();
		if(status != SolrException.ErrorCode.UNKNOWN.code){
			LOGGER.warn("Failed to delete data points, status: "+status);
		}
	}
}
