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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import service.tut.pori.apilta.sensors.datatypes.Measurement;
import service.tut.pori.apilta.sensors.datatypes.MeasurementList;
import core.tut.pori.dao.SQLDAO;
import core.tut.pori.dao.SQLDeleteBuilder;
import core.tut.pori.dao.SQLSelectBuilder;
import core.tut.pori.dao.SQLSelectBuilder.OrderDirection;
import core.tut.pori.dao.clause.AndClause;
import core.tut.pori.dao.clause.AndCompareClause;
import core.tut.pori.dao.clause.AndCompareClause.CompareType;
import core.tut.pori.dao.clause.AndSubClause;
import core.tut.pori.dao.clause.JoinClause;
import core.tut.pori.dao.clause.OrSubClause;
import core.tut.pori.dao.clause.SQLClause.SQLType;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.DateIntervalParameter.Interval;
import core.tut.pori.http.parameters.Limits;

/**
 * dao for accessing sensor data
 * 
 */
public class SensorsDAO extends SQLDAO {
	private static final Logger LOGGER = Logger.getLogger(SensorsDAO.class);
	/* tables */
	private static final String TABLE_MEASUREMENTS = DATABASE+".measurements";
	private static final String TABLE_MEASUREMENTS_FILES = DATABASE+".measurements_files";
	private static final String TABLE_MEASUREMENTS_TASKS = DATABASE+".measurements_tasks";
	/* columns */
	private static final String COLUMN_BACKEND_ID = "backend_id";
	private static final String COLUMN_MEASUREMENT_ID = "measurement_id";
	private static final String COLUMN_TABLE_MEASUREMENTS_ROW_CREATED = TABLE_MEASUREMENTS+"."+COLUMN_ROW_CREATED;
	private static final String COLUMN_TASK_ID_WITH_TABLE_NAME = TABLE_MEASUREMENTS_TASKS+"."+Definitions.COLUMN_TASK_ID;
	/* sql */
	private static final String SQL_BACKEND_HAS_GUID = "SELECT "+COLUMN_COUNT+" FROM "+TABLE_MEASUREMENTS_FILES+" WHERE "+COLUMN_BACKEND_ID+"=? AND "+COLUMN_GUID+"=? LIMIT 1";
	private static final int[] SQL_BACKEND_HAS_GUID_SQL_TYPES = {SQLType.LONG.toInt(), SQLType.STRING.toInt()};
	private static final String[] SQL_COLUMNS_GET_MEASUREMENTS = {TABLE_MEASUREMENTS+"."+COLUMN_MEASUREMENT_ID, TABLE_MEASUREMENTS+"."+COLUMN_BACKEND_ID}; // also used in the where filters of getMeasurements(), check before making modifications
	private static final String[] SQL_COLUMNS_INSERT_MEASUREMENT = {COLUMN_BACKEND_ID, COLUMN_MEASUREMENT_ID, COLUMN_ROW_CREATED};
	private static final String SQL_DELETE_FILES = "DELETE FROM "+TABLE_MEASUREMENTS_FILES+" WHERE "+COLUMN_BACKEND_ID+"=?";
	private static final String SQL_DELETE_MEASUREMENTS = "DELETE FROM "+TABLE_MEASUREMENTS+" WHERE "+COLUMN_BACKEND_ID+"=?";
	private static final String SQL_GET_BACKEND_ID = "SELECT "+COLUMN_BACKEND_ID+", "+COLUMN_COUNT+" FROM "+TABLE_MEASUREMENTS_FILES+" WHERE "+COLUMN_GUID+"=? LIMIT 1";
	private static final int[] SQL_GET_BACKEND_ID_SQL_TYPES = {SQLType.STRING.toInt()};
	private static final String SQL_GET_FILE_GUIDS = "SELECT "+COLUMN_GUID+" FROM "+TABLE_MEASUREMENTS_FILES+" WHERE "+COLUMN_BACKEND_ID+"=?";
	private static final String SQL_GET_MEASUREMENT_IDS = "SELECT "+COLUMN_MEASUREMENT_ID+" FROM "+TABLE_MEASUREMENTS+" WHERE "+COLUMN_BACKEND_ID+"=?";
	private static final String SQL_INSERT_FILE = "INSERT INTO "+TABLE_MEASUREMENTS_FILES+" ("+COLUMN_BACKEND_ID+", "+COLUMN_GUID+", "+COLUMN_ROW_CREATED+") VALUES (?,?,NOW())";
	private static final int[] SQL_INSERT_FILE_SQL_TYPES = {SQLType.LONG.toInt(), SQLType.STRING.toInt()};
	private static final String SQL_INSERT_MEASUREMENT_TASK = "INSERT INTO "+TABLE_MEASUREMENTS_TASKS+" ("+COLUMN_MEASUREMENT_ID+", "+Definitions.COLUMN_TASK_ID+", "+COLUMN_ROW_CREATED+") VALUES (?,?,NOW())";
	private static final int[] SQL_INSERT_MEASUREMENT_TASK_SQL_TYPES =  {SQLType.STRING.toInt(), SQLType.STRING.toInt()};
	private static final JoinClause SQL_JOIN_TASKS = new JoinClause("INNER JOIN "+TABLE_MEASUREMENTS_TASKS+" ON "+TABLE_MEASUREMENTS+"."+COLUMN_MEASUREMENT_ID+"="+TABLE_MEASUREMENTS_TASKS+"."+COLUMN_MEASUREMENT_ID);
	private static final int[] SQL_TYPE_BACKEND_ID = {SQLType.LONG.toInt()};
	@Autowired
	private DataPointDAO _dataPointDAO = null;
	
	/**
	 * 
	 * @param backendIdFilter optional filter
	 * @param createdFilter optional filter, this is targeted to the data points (if requested)
	 * @param dataGroups
	 * @param limits
	 * @param measurementIdFilter optional filter
	 * @param taskIds
	 * @return list of measurements or null if none was found
	 */
	public MeasurementList getMeasurements(long[] backendIdFilter, Set<Interval> createdFilter, DataGroups dataGroups, Limits limits, List<String> measurementIdFilter, List<String> taskIds) {
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_MEASUREMENTS);
		sql.addSelectColumns(SQL_COLUMNS_GET_MEASUREMENTS);
		sql.addOrderBy(COLUMN_TABLE_MEASUREMENTS_ROW_CREATED, OrderDirection.DESCENDING);
		sql.setLimits(limits);
		
		sql.addWhereClause(new AndClause(COLUMN_TASK_ID_WITH_TABLE_NAME, taskIds, SQLType.STRING));
		sql.addJoin(SQL_JOIN_TASKS);
		
		if(!ArrayUtils.isEmpty(backendIdFilter)){
			LOGGER.debug("Using back end id filter...");
			sql.addWhereClause(new AndClause(SQL_COLUMNS_GET_MEASUREMENTS[1], backendIdFilter));
		}
		
		if(measurementIdFilter != null && !measurementIdFilter.isEmpty()){
			LOGGER.debug("Using measurement id filter...");
			sql.addWhereClause(new AndClause(SQL_COLUMNS_GET_MEASUREMENTS[0], measurementIdFilter, SQLType.STRING));		
		}
		
		if(createdFilter != null && !createdFilter.isEmpty()){
			LOGGER.debug("Using created filter...");
			AndSubClause asc = new AndSubClause();
			for(Interval interval : createdFilter){
				asc.addWhereClause(new OrSubClause(new AndCompareClause[]{new AndCompareClause(COLUMN_TABLE_MEASUREMENTS_ROW_CREATED, interval.getStart(), CompareType.GREATER_OR_EQUAL, SQLType.TIMESTAMP), new AndCompareClause(COLUMN_TABLE_MEASUREMENTS_ROW_CREATED, interval.getEnd(), CompareType.LESS_OR_EQUAL, SQLType.TIMESTAMP)}));
			}
			sql.addWhereClause(asc);
		}
		
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql.toSQLString(service.tut.pori.apilta.sensors.datatypes.Definitions.ELEMENT_MEASUREMENT_LIST), sql.getValues(), sql.getValueTypes());
		if(rows.isEmpty()){
			LOGGER.debug("No measurements found.");
			return null;
		}
		
		boolean getDataPoints = (DataGroups.hasDataGroup(DataGroups.DATA_GROUP_ALL, dataGroups) || DataGroups.hasDataGroup(Definitions.DATA_GROUP_DATA_POINTS, dataGroups));
		ArrayList<Measurement> measurements = new ArrayList<>(rows.size());
		for(Map<String, Object> row : rows){
			measurements.add(extractMeasurement(row, createdFilter, getDataPoints, limits));
		}
		
		MeasurementList list = new MeasurementList();
		list.setMeasurements(measurements);
		return list;
	}
	
	/**
	 * 
	 * @param row
	 * @param createdFilter 
	 * @param getDataPoints if true, data points for the measuremnt will be retrieved
	 * @param limits 
	 * @return measurement extracted from the given row
	 */
	private Measurement extractMeasurement(Map<String, Object> row, Set<Interval> createdFilter, boolean getDataPoints, Limits limits) {
		Measurement m = new Measurement();
		String measurementId = null;
		for(Entry<String, Object> entry : row.entrySet()){
			switch(entry.getKey()){
				case COLUMN_MEASUREMENT_ID:
					measurementId = (String) entry.getValue();
					m.setMeasurementId(measurementId);
					break;
				case COLUMN_BACKEND_ID:
					m.setBackendId((Long) entry.getValue());
					break;
				default:
					if(checkCountColumn(entry.getKey(), entry.getValue()) < 1){	// this should be count(*)
						LOGGER.warn("Unknown column name, or no results, column: "+entry.getKey());
						return null;
					}
					break;
			}
		}
		if(getDataPoints){
			m.setDataPoints(_dataPointDAO.getDataPoints(createdFilter, limits, measurementId));
		}
		return m;
	}
	
	/**
	 * 
	 * Add measurements, the generated measurement identifiers are set to the passed objects
	 * 
	 * @param measurements the measurements to add
	 * @param taskIds the tasks which are to be associated with the measurements
	 */
	public void addMeasurements(Collection<Measurement> measurements, Collection<String> taskIds) {
		getTransactionTemplate().execute(new TransactionCallback<Void>() {

			@Override
			public Void doInTransaction(TransactionStatus status) {
				JdbcTemplate t = getJdbcTemplate();
				SimpleJdbcInsert sql = new SimpleJdbcInsert(t);
				sql.withTableName(TABLE_MEASUREMENTS);
				sql.withoutTableColumnMetaDataAccess();
				sql.usingColumns(SQL_COLUMNS_INSERT_MEASUREMENT);
				
				HashMap<String, Object> params = new HashMap<>(SQL_COLUMNS_INSERT_MEASUREMENT.length);
				params.put(COLUMN_ROW_CREATED, null);
				Object[] ob = new Object[2];
				for(Measurement m : measurements){
					params.put(COLUMN_BACKEND_ID, m.getBackendId());
					String measurementId = UUID.randomUUID().toString();
					params.put(COLUMN_MEASUREMENT_ID, measurementId);
					sql.execute(params);
					
					ob[0] = measurementId;
					for(String taskId : taskIds) {
						ob[1] = taskId;
						t.update(SQL_INSERT_MEASUREMENT_TASK, ob, SQL_INSERT_MEASUREMENT_TASK_SQL_TYPES);
					}
					
					m.setMeasurementId(measurementId);
					_dataPointDAO.createDataPoints(m.getDataPoints(), measurementId);
				}
				return null;
			}
		});
	}
	
	/**
	 * delete all measurements taken by the back end
	 * 
	 * @param backendId
	 */
	public void deleteMeasurements(Long backendId) {
		getTransactionTemplate().execute(new TransactionCallback<Void>() {

			@Override
			public Void doInTransaction(TransactionStatus status) {
				JdbcTemplate t = getJdbcTemplate();
				Object[] ob = {backendId};
				List<Long> measurementIds = t.queryForList(SQL_GET_MEASUREMENT_IDS, ob, SQL_TYPE_BACKEND_ID, Long.class);
				if(measurementIds.isEmpty()){
					LOGGER.debug("No measurements for back end, id: "+backendId);
					return null;
				}
				
				_dataPointDAO.deleteDataPoints(measurementIds);
				
				t.update(SQL_DELETE_MEASUREMENTS, ob, SQL_TYPE_BACKEND_ID);
				
				SQLDeleteBuilder sql = new SQLDeleteBuilder(TABLE_MEASUREMENTS_TASKS);
				sql.addWhereClause(new AndClause(COLUMN_MEASUREMENT_ID, measurementIds, SQLType.LONG));
				sql.execute(getJdbcTemplate());
				return null;
			}
		});
	}
	
	/**
	 * 
	 * @param backendId
	 * @param guid
	 */
	protected void addFile(Long backendId, String guid) {
		getJdbcTemplate().update(SQL_INSERT_FILE, new Object[]{backendId, guid}, SQL_INSERT_FILE_SQL_TYPES);
	}
	
	/**
	 * 
	 * @param guid
	 * @return back end identifier associated with the given file GUID or null if not found
	 */
	protected Long getFileBackendId(String guid) {
		return (Long) getJdbcTemplate().queryForMap(SQL_GET_BACKEND_ID, new Object[]{guid}, SQL_GET_BACKEND_ID_SQL_TYPES).get(COLUMN_BACKEND_ID);
	}
	
	/**
	 * 
	 * @param backendId
	 * @return list of file GUIDs associated with the given back end id or null if none was found
	 */
	protected List<String> getFileGUIDs(Long backendId) {
		List<String> guids = getJdbcTemplate().queryForList(SQL_GET_FILE_GUIDS, new Object[]{backendId}, SQL_GET_BACKEND_ID_SQL_TYPES, String.class);
		return (guids.isEmpty() ? null : guids);
	}
	
	/**
	 * 
	 * @param backendId
	 * @param guid
	 * @return true if the given GUID is associated with the given back end
	 */
	protected boolean backendHasGUID(Long backendId, String guid) {
		return (getJdbcTemplate().queryForObject(SQL_BACKEND_HAS_GUID, new Object[]{backendId, guid}, SQL_BACKEND_HAS_GUID_SQL_TYPES, Integer.class) > 0);
	}
	
	/**
	 * delete all file GUID associations for the given back end id
	 * 
	 * @param backendId
	 */
	protected void deleteFiles(Long backendId) {
		getJdbcTemplate().update(SQL_DELETE_FILES, new Object[]{backendId}, SQL_GET_BACKEND_ID_SQL_TYPES);
	}
}
