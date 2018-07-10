/**
 * Copyright 2018 Tampere University of Technology, Pori Department
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
package service.tut.pori.apilta.shock;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import core.tut.pori.dao.SQLDAO;
import core.tut.pori.dao.SQLSelectBuilder;
import core.tut.pori.dao.SQLSelectBuilder.OrderDirection;
import core.tut.pori.dao.clause.AndClause;
import core.tut.pori.dao.clause.AndCompareClause;
import core.tut.pori.dao.clause.AndCompareClause.CompareType;
import core.tut.pori.dao.clause.AndSubClause;
import core.tut.pori.dao.clause.JoinClause;
import core.tut.pori.dao.clause.OrClause;
import core.tut.pori.dao.clause.OrSubClause;
import core.tut.pori.dao.clause.SQLClause.SQLType;
import core.tut.pori.dao.clause.WhereClause;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.DateIntervalParameter;
import core.tut.pori.http.parameters.DateIntervalParameter.Interval;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;
import service.tut.pori.apilta.shock.datatypes.AccelerometerData;
import service.tut.pori.apilta.shock.datatypes.LocationData;
import service.tut.pori.apilta.shock.datatypes.LocationLimits;
import service.tut.pori.apilta.shock.datatypes.LocationLimits.LatLng;
import service.tut.pori.apilta.shock.datatypes.ShockMeasurement;
import service.tut.pori.apilta.shock.datatypes.ShockMeasurementList;
import service.tut.pori.tasks.datatypes.Task.Visibility;

/**
 * 
 *
 */
public class ShockDAO extends SQLDAO {
	private static final Logger LOGGER = Logger.getLogger(ShockDAO.class);
	/* tables */
	private static final String TABLE_SHOCK_ACCELEROMETER_DATA = DATABASE+".shock_accelerometer_data";
	private static final String TABLE_SHOCK_LOCATION_DATA = DATABASE+".shock_location_data";
	private static final String TABLE_SHOCK_MEASUREMENTS = DATABASE+".shock_measurements";
	/* columns */
	private static final String COLUMN_DATA_VISIBILITY = "data_visibility";
	private static final String COLUMN_LATITUDE = "latitude";
	private static final String COLUMN_LONGITUDE = "longitude";
	private static final String COLUMN_LEVEL = "level";
	private static final String COLUMN_HEADING = "heading";
	private static final String COLUMN_SPEED = "speed";
	private static final String COLUMN_MEASUREMENT_ID = "measurement_id";
	private static final String COLUMN_TIMESTAMP = "timestamp";
	private static final String COLUMN_MEASUREMENTS_LEVEL = TABLE_SHOCK_MEASUREMENTS+"."+COLUMN_LEVEL;
	private static final String COLUMN_MEASUREMENTS_TIMESTAMP = TABLE_SHOCK_MEASUREMENTS+"."+COLUMN_TIMESTAMP;
	private static final String COLUMN_SYSTEMATIC_ERROR = "systematic_error";
	private static final String COLUMN_X_ACCELERATION = "x_acc";
	private static final String COLUMN_Y_ACCELERATION = "y_acc";
	private static final String COLUMN_Z_ACCELERATION = "z_acc";
	
	private static final String[] COLUMNS_GET_MEASUREMENTS = {TABLE_SHOCK_MEASUREMENTS+"."+COLUMN_MEASUREMENT_ID, TABLE_SHOCK_MEASUREMENTS+"."+COLUMN_DATA_VISIBILITY, TABLE_SHOCK_MEASUREMENTS+"."+COLUMN_USER_ID, COLUMN_MEASUREMENTS_TIMESTAMP, COLUMN_MEASUREMENTS_LEVEL};
	/* sql strings */
	private static final String SQL_INSERT_MEASUREMENT = "INSERT INTO "+TABLE_SHOCK_MEASUREMENTS+" ("+COLUMN_MEASUREMENT_ID+", "+COLUMN_LEVEL+", "+COLUMN_DATA_VISIBILITY+", "+COLUMN_USER_ID+", "+COLUMN_TIMESTAMP+", "+COLUMN_ROW_CREATED+") VALUES (?,?,?,?,?,NOW())";
	private static final int[] SQL_INSERT_MEASUREMENT_VALUE_TYPES = {SQLType.STRING.toInt(), SQLType.INTEGER.toInt(), SQLType.INTEGER.toInt(), SQLType.LONG.toInt(), SQLType.TIMESTAMP.toInt()};
	
	private static final String SQL_INSERT_ACCELEROMETER_DATA = "INSERT INTO "+TABLE_SHOCK_ACCELEROMETER_DATA+" ("+COLUMN_MEASUREMENT_ID+", "+COLUMN_X_ACCELERATION+", "+COLUMN_Y_ACCELERATION+", "+COLUMN_Z_ACCELERATION+", "+COLUMN_SYSTEMATIC_ERROR+", "+COLUMN_TIMESTAMP+", "+COLUMN_ROW_CREATED+") VALUES (?,?,?,?,?,?,NOW())";
	private static final int[] SQL_INSERT_ACCELEROMETER_DATA_VALUE_TYPES = {SQLType.STRING.toInt(), SQLType.DOUBLE.toInt(), SQLType.DOUBLE.toInt(), SQLType.DOUBLE.toInt(), SQLType.DOUBLE.toInt(), SQLType.TIMESTAMP.toInt()};
	
	private static final String SQL_INSERT_LOCATION_DATA = "INSERT INTO "+TABLE_SHOCK_LOCATION_DATA+" ("+COLUMN_MEASUREMENT_ID+", "+COLUMN_HEADING+", "+COLUMN_LATITUDE+", "+COLUMN_LONGITUDE+", "+COLUMN_SPEED+", "+COLUMN_TIMESTAMP+", "+COLUMN_ROW_CREATED+") VALUES (?,?,?,?,?,?,NOW())";
	private static final int[] SQL_INSERT_LOCATION_DATA_VALUE_TYPES = {SQLType.STRING.toInt(), SQLType.DOUBLE.toInt(), SQLType.DOUBLE.toInt(), SQLType.DOUBLE.toInt(), SQLType.DOUBLE.toInt(), SQLType.TIMESTAMP.toInt()};
	
	private static final WhereClause SQL_DATA_VISIBILITY_PUBLIC = new OrClause(COLUMN_DATA_VISIBILITY, Visibility.PUBLIC.toInt(), SQLType.INTEGER);
	
	private static final String SQL_GET_ACCELEROMETER_DATA = "SELECT "+COLUMN_X_ACCELERATION+", "+COLUMN_Y_ACCELERATION+", "+COLUMN_Z_ACCELERATION+", "+COLUMN_TIMESTAMP+", "+COLUMN_SYSTEMATIC_ERROR+" FROM "+TABLE_SHOCK_ACCELEROMETER_DATA+" WHERE "+COLUMN_MEASUREMENT_ID+"=?";
	private static final int[] SQL_MEASUREMENT_ID_VALUE_TYPE = {SQLType.STRING.toInt()};
	
	private static final String SQL_GET_LOCATION_DATA = "SELECT "+COLUMN_LATITUDE+", "+COLUMN_LONGITUDE+", "+COLUMN_SPEED+", "+COLUMN_HEADING+", "+COLUMN_TIMESTAMP+" FROM "+TABLE_SHOCK_LOCATION_DATA+" WHERE "+COLUMN_MEASUREMENT_ID+"=?";
	
	private static final JoinClause SQL_JOIN_LOCATION_DATA = new JoinClause("LEFT JOIN "+TABLE_SHOCK_LOCATION_DATA+" ON "+TABLE_SHOCK_MEASUREMENTS+"."+COLUMN_MEASUREMENT_ID+"="+TABLE_SHOCK_LOCATION_DATA+"."+COLUMN_MEASUREMENT_ID);
	private static final AndClause SQL_CLAUSE_LEVEL_NOT_NULL = new AndClause(COLUMN_LEVEL, (Object) null, SQLType.INTEGER).setNot(true);
	
	
	/**
	 * 
	 * @param userIdentity
	 * @param locationLimits
	 * @param dataGroups
	 * @param dateInterval
	 * @param levelFilter 
	 * @param limits
	 * @param userIdFilter 
	 * @return list of measurements or null if none was found
	 */
	public ShockMeasurementList getMeasurements(UserIdentity userIdentity, LocationLimits locationLimits, DataGroups dataGroups, DateIntervalParameter dateInterval, int[] levelFilter, Limits limits, long[] userIdFilter) {
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_SHOCK_MEASUREMENTS);
		sql.setLimits(limits);
		sql.addWhereClause(new AndSubClause(new WhereClause[]{new OrClause(COLUMN_USER_ID, userIdentity.getUserId(), SQLType.LONG), SQL_DATA_VISIBILITY_PUBLIC}));
		sql.addSelectColumns(COLUMNS_GET_MEASUREMENTS);
		sql.addOrderBy(COLUMN_MEASUREMENTS_TIMESTAMP, OrderDirection.DESCENDING);
		
		if(dateInterval.hasValues()){
			LOGGER.debug("Using date filter...");
			AndSubClause asc = new AndSubClause();
			for(Interval interval : dateInterval.getValues()){
				asc.addWhereClause(new OrSubClause(new AndCompareClause[]{new AndCompareClause(COLUMN_MEASUREMENTS_TIMESTAMP, interval.getStart(), CompareType.GREATER_OR_EQUAL, SQLType.TIMESTAMP), new AndCompareClause(COLUMN_MEASUREMENTS_TIMESTAMP, interval.getEnd(), CompareType.LESS_OR_EQUAL, SQLType.TIMESTAMP)}));
			}
			sql.addWhereClause(asc);
		}
		
		if(!ArrayUtils.isEmpty(userIdFilter)) {
			LOGGER.debug("Using user id filter...");
			sql.addWhereClause(new AndClause(COLUMN_USER_ID, userIdFilter));
		}
		
		if(!ArrayUtils.isEmpty(levelFilter)) {
			LOGGER.debug("Using level filter...");
			sql.addWhereClause(new AndSubClause(new WhereClause[]{SQL_CLAUSE_LEVEL_NOT_NULL, new AndClause(COLUMN_LEVEL, levelFilter)}));
		}
		
		if(locationLimits.hasValues()) {
			LOGGER.debug("Using location limits filter...");
			sql.addJoin(SQL_JOIN_LOCATION_DATA); // note: we could also select all data from location table if location datagroup is given (same for accelerometer data)
			
			LatLng lowerLeft = locationLimits.getLowerLeft(); // column >= lat, column >= lon
			sql.addWhereClause(new AndCompareClause(COLUMN_LATITUDE, lowerLeft.getLatitude(), CompareType.GREATER_OR_EQUAL, SQLType.DOUBLE));
			sql.addWhereClause(new AndCompareClause(COLUMN_LONGITUDE, lowerLeft.getLongitude(), CompareType.GREATER_OR_EQUAL, SQLType.DOUBLE));
			
			LatLng upperRight = locationLimits.getUpperRight(); // column <= lat, column <= lon
			sql.addWhereClause(new AndCompareClause(COLUMN_LATITUDE, upperRight.getLatitude(), CompareType.LESS_OR_EQUAL, SQLType.DOUBLE));
			sql.addWhereClause(new AndCompareClause(COLUMN_LONGITUDE, upperRight.getLongitude(), CompareType.LESS_OR_EQUAL, SQLType.DOUBLE));
		}
		
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql.toSQLString(service.tut.pori.apilta.sensors.datatypes.Definitions.ELEMENT_MEASUREMENT_LIST), sql.getValues(), sql.getValueTypes());
		int size = rows.size();
		if(size < 1) {
			LOGGER.debug("No measurements founds.");
			return null;
		}
		
		boolean dataGroupAll = DataGroups.hasDataGroup(DataGroups.DATA_GROUP_ALL, dataGroups);
		boolean retrieveLocations = (dataGroupAll || DataGroups.hasDataGroup(Definitions.DATA_GROUP_LOCATION_DATA, dataGroups));
		boolean retrieveAccelerometer = (dataGroupAll || DataGroups.hasDataGroup(Definitions.DATA_GROUP_ACCELEROMETER_DATA, dataGroups));
		
		LinkedList<ShockMeasurement> measurements = new LinkedList<>();
		for(Map<String, Object> row : rows) {
			measurements.add(extractMeasurement(row, retrieveLocations, retrieveAccelerometer));
		}
		
		return ShockMeasurementList.getShockMeasurementList(measurements);
	}
	
	/**
	 * 
	 * @param row
	 * @param getLocations
	 * @param getAccelerometer
	 * @return the extracted measurement
	 */
	private ShockMeasurement extractMeasurement(Map<String, Object> row, boolean getLocations, boolean getAccelerometer) {
		ShockMeasurement m = new ShockMeasurement();
		Object[] measurementId = new Object[1];
		for(Entry<String, Object> e : row.entrySet()) {
			String column = e.getKey();
			switch(column) {
				case COLUMN_MEASUREMENT_ID:
					measurementId[0] = e.getValue();
					m.setMeasurementId((String) measurementId[0]);
					break;
				case COLUMN_DATA_VISIBILITY:
					m.setVisibility(Visibility.fromInt((int) e.getValue()));
					break;
				case COLUMN_USER_ID:
					m.setUserId(new UserIdentity((Long) e.getValue()));
					break;
				case COLUMN_TIMESTAMP:
					m.setTimestamp((Date) e.getValue());
					break;
				case COLUMN_LEVEL:
					m.setLevel((Integer) e.getValue());
					break;
				default:
					if(checkCountColumn(column, e.getValue()) < 1){	// this should be count(*)
						LOGGER.warn("Unknown column name, or no results, column: "+column);
						return null;
					}
					break;
			}
		}
		
		if(getAccelerometer) {
			List<Map<String, Object>> rows = getJdbcTemplate().queryForList(SQL_GET_ACCELEROMETER_DATA, measurementId, SQL_MEASUREMENT_ID_VALUE_TYPE);
			if(rows.isEmpty()) {
				LOGGER.debug("No accelerometer data for measurement, id: "+measurementId[0]);
			}else {
				m.setAccelerometerData(extractAccelerometerData(rows.iterator().next()));
			}
		}
		
		if(getLocations) {
			List<Map<String, Object>> rows = getJdbcTemplate().queryForList(SQL_GET_LOCATION_DATA, measurementId, SQL_MEASUREMENT_ID_VALUE_TYPE);
			if(rows.isEmpty()) {
				LOGGER.debug("No location data for measurement, id: "+measurementId[0]);
			}else {
				m.setLocationData(extractLocationData(rows.iterator().next()));
			}
		}
		
		return m;
	}
	
	/**
	 * 
	 * @param row
	 * @return the extracted data
	 */
	private AccelerometerData extractAccelerometerData(Map<String, Object> row) {
		AccelerometerData aData = new AccelerometerData();
		for(Entry<String, Object> e : row.entrySet()) {
			String column = e.getKey();
			switch(column) {
				case COLUMN_X_ACCELERATION:
					aData.setxAcceleration((Double) e.getValue());
					break;
				case COLUMN_Y_ACCELERATION:
					aData.setyAcceleration((Double) e.getValue());
					break;
				case COLUMN_Z_ACCELERATION:
					aData.setzAcceleration((Double) e.getValue());
					break;
				case COLUMN_TIMESTAMP:
					aData.setTimestamp((Date) e.getValue());
					break;
				case COLUMN_SYSTEMATIC_ERROR:
					aData.setSystematicError((Double) e.getValue());
					break;
				case COLUMN_MEASUREMENT_ID: // valid column, but not used in data object
					break;
				default:
					if(checkCountColumn(column, e.getValue()) < 1){	// this should be count(*)
						LOGGER.warn("Unknown column name, or no results, column: "+column);
						return null;
					}
					break;
			}
		}
		return aData;
	}
	
	/**
	 * 
	 * @param row
	 * @return the extracted data
	 */
	private LocationData extractLocationData(Map<String, Object> row) {
		LocationData lData = new LocationData();
		for(Entry<String, Object> e : row.entrySet()) {
			String column = e.getKey();
			switch(column) {
				case COLUMN_LATITUDE:
					lData.setLatitude((Double) e.getValue());
					break;
				case COLUMN_LONGITUDE:
					lData.setLongitude((Double) e.getValue());
					break;
				case COLUMN_HEADING:
					lData.setHeading((Double) e.getValue());
					break;
				case COLUMN_SPEED:
					lData.setSpeed((Double) e.getValue());
					break;
				case COLUMN_TIMESTAMP:
					lData.setTimestamp((Date) e.getValue());
					break;
				case COLUMN_MEASUREMENT_ID: // valid column, but not not used in the data object
					break;
				default:
					if(checkCountColumn(column, e.getValue()) < 1){	// this should be count(*)
						LOGGER.warn("Unknown column name, or no results, column: "+column);
						return null;
					}
					break;
			}
		}
		return lData;
	}

	/**
	 * 
	 * @param measurement
	 * @return measurement id for the created measurement
	 */
	public String createMeasurement(ShockMeasurement measurement) {
		return getTransactionTemplate().execute(new TransactionCallback<String>() {

			@Override
			public String doInTransaction(TransactionStatus status) {
				String measurementId = UUID.randomUUID().toString();
				getJdbcTemplate().update(SQL_INSERT_MEASUREMENT, new Object[]{measurementId, measurement.getLevel(), measurement.getVisibility().toInt(), measurement.getUserId().getUserId(), measurement.getTimestamp()}, SQL_INSERT_MEASUREMENT_VALUE_TYPES);
				
				AccelerometerData aData = measurement.getAccelerometerData();
				if(aData != null){
					insertData(measurementId, aData);
				}
				
				LocationData lData = measurement.getLocationData();
				if(lData != null){
					insertData(measurementId, lData);
				}
				
				measurement.setMeasurementId(measurementId);
				return measurementId;
			}
		});
	}
	
	/**
	 * 
	 * @param measurementId 
	 * @param data
	 */
	private void insertData(String measurementId, AccelerometerData data) {
		getJdbcTemplate().update(SQL_INSERT_ACCELEROMETER_DATA, new Object[]{measurementId, data.getxAcceleration(), data.getyAcceleration(), data.getzAcceleration(), data.getSystematicError(), data.getTimestamp()}, SQL_INSERT_ACCELEROMETER_DATA_VALUE_TYPES);
	}
	
	/**
	 * 
	 * @param measurementId
	 * @param data
	 */
	private void insertData(String measurementId, LocationData data) {
		getJdbcTemplate().update(SQL_INSERT_LOCATION_DATA, new Object[]{measurementId, data.getHeading(), data.getLatitude(), data.getLongitude(), data.getSpeed(), data.getTimestamp()}, SQL_INSERT_LOCATION_DATA_VALUE_TYPES);
	}
}
