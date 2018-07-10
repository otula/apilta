/**
 * Copyright 2017 Tampere University of Technology, Pori Department
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
package app.tut.pori.cmd;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import otula.backend.tasks.datatypes.SensorTask;
import service.tut.pori.apilta.sensors.datatypes.Condition;
import service.tut.pori.apilta.sensors.datatypes.DataPoint;
import service.tut.pori.apilta.sensors.datatypes.Measurement;
import service.tut.pori.apilta.sensors.datatypes.Output;
import app.tut.pori.cmd.datatypes.SqliteFile;

/**
 * handler class for accessing sqlite
 * 
 * Note: this class is NOT thread-safe or reentrant
 * 
 * Tables:
 * 
 * {@value app.tut.pori.cmd.SQLiteHandler#TABLE_MEASUREMENTS}
 * ------------
 * {@value app.tut.pori.cmd.SQLiteHandler#COLUMN_MEASUREMENT_ID} TEXT (unique 1/2)
 * {@value app.tut.pori.cmd.SQLiteHandler#COLUMN_TASK_ID} TEXT (unique 2/2)
 * {@value app.tut.pori.cmd.SQLiteHandler#COLUMN_SENT} INTEGER (boolean 0/1)
 * 
 * {@value app.tut.pori.cmd.SQLiteHandler#TABLE_MEASUREMENT_DATA}
 * ------------
 * {@value app.tut.pori.cmd.SQLiteHandler#COLUMN_MEASUREMENT_ID} TEXT
 * {@value app.tut.pori.cmd.SQLiteHandler#COLUMN_KEY} TEXT
 * {@value app.tut.pori.cmd.SQLiteHandler#COLUMN_VALUE} TEXT
 * {@value app.tut.pori.cmd.SQLiteHandler#COLUMN_TIMESTAMP} INTEGER
 * 
 * {@value app.tut.pori.cmd.SQLiteHandler#TABLE_MEASUREMENT_FILES}
 * ------------
 * {@value app.tut.pori.cmd.SQLiteHandler#COLUMN_MEASUREMENT_ID} TEXT (unique 1/2)
 * {@value app.tut.pori.cmd.SQLiteHandler#COLUMN_PATH} TEXT (unique 2/2)
 * {@value app.tut.pori.cmd.SQLiteHandler#COLUMN_GUID} TEXT the GUID for the uploaded file
 * {@value app.tut.pori.cmd.SQLiteHandler#COLUMN_TIMESTAMP} INTEGER
 * 
 * {@value app.tut.pori.cmd.SQLiteHandler#TABLE_TASKS}
 * ------------
 * {@value app.tut.pori.cmd.SQLiteHandler#COLUMN_TASK_ID} TEXT
 * {@value app.tut.pori.cmd.SQLiteHandler#COLUMN_CALLBACK_URI} TEXT
 * 
 * {@value app.tut.pori.cmd.SQLiteHandler#TABLE_TASK_CONDITIONS}
 * ------------
 * {@value app.tut.pori.cmd.SQLiteHandler#COLUMN_TASK_ID} TEXT
 * {@value app.tut.pori.cmd.SQLiteHandler#COLUMN_CONDITION_ID} TEXT Note: there is no condition_id in the condition list, this is used to track the condition relations internally
 * {@value app.tut.pori.cmd.SQLiteHandler#COLUMN_KEY} TEXT
 * {@value app.tut.pori.cmd.SQLiteHandler#COLUMN_VALUE} TEXT
 * 
 * {@value app.tut.pori.cmd.SQLiteHandler#TABLE_TASK_OUTPUTS}
 * ------------
 * {@value app.tut.pori.cmd.SQLiteHandler#COLUMN_TASK_ID} TEXT
 * {@value app.tut.pori.cmd.SQLiteHandler#COLUMN_FEATURE} TEXT
 */
public class SQLiteHandler implements Closeable {
	private static final Logger LOGGER = Logger.getLogger(SQLiteHandler.class);
	/* sqlite columns */
	private static final String COLUMN_CALLBACK_URI = "callback_uri";
	private static final String COLUMN_CONDITION_ID = "condition_id";
	private static final String COLUMN_FEATURE = "feature";
	private static final String COLUMN_GUID = "guid";
	private static final String COLUMN_KEY = "key";
	private static final String COLUMN_MEASUREMENT_ID = "measurement_id";
	private static final String COLUMN_PATH = "path";
	private static final String COLUMN_SENT = "sent";
	private static final String COLUMN_TASK_ID = "task_id";
	private static final String COLUMN_TIMESTAMP = "timestamp";
	private static final String COLUMN_VALUE = "value";
	/* sqlite tables */
	private static final String TABLE_MEASUREMENT_DATA = "measurement_data";
	private static final String TABLE_MEASUREMENT_FILES = "measurement_files";
	private static final String TABLE_MEASUREMENTS = "measurements";
	private static final String TABLE_TASK_CONDITIONS = "task_conditions";
	private static final String TABLE_TASK_OUTPUTS = "task_outputs";
	private static final String TABLE_TASKS = "tasks";
	/* sqlite scripts */
	private static final String SQL_BEGIN_TRANSACTION = "BEGIN TRANSACTION";
	private static final String SQL_COMMIT = "COMMIT";
	private static final String SQL_CREATE_INDEX_TABLE_MEASUREMENT_FILES = "CREATE UNIQUE INDEX "+COLUMN_MEASUREMENT_ID+"_"+COLUMN_PATH+"_UNIQUE ON "+TABLE_MEASUREMENT_FILES+" ("+COLUMN_MEASUREMENT_ID+", "+COLUMN_PATH+")";
	private static final String SQL_CREATE_INDEX_TABLE_MEASUREMENTS = "CREATE UNIQUE INDEX "+COLUMN_MEASUREMENT_ID+"_"+COLUMN_TASK_ID+"_UNIQUE ON "+TABLE_MEASUREMENTS+" ("+COLUMN_MEASUREMENT_ID+", "+COLUMN_TASK_ID+")";
	private static final String SQL_CREATE_TABLE_MEASUREMENTS = "CREATE TABLE "+TABLE_MEASUREMENTS+" ("+COLUMN_MEASUREMENT_ID+" TEXT, "+COLUMN_TASK_ID+" TEXT, "+COLUMN_SENT+" INTEGER)";
	private static final String SQL_CREATE_TABLE_MEASUREMENT_DATA = "CREATE TABLE "+TABLE_MEASUREMENT_DATA+" ("+COLUMN_MEASUREMENT_ID+" TEXT, "+COLUMN_KEY+" TEXT, "+COLUMN_VALUE+" TEXT, "+COLUMN_TIMESTAMP+" INTEGER)";
	private static final String SQL_CREATE_TABLE_MEASUREMENT_FILES = "CREATE TABLE "+TABLE_MEASUREMENT_FILES+" ("+COLUMN_MEASUREMENT_ID+" TEXT, "+COLUMN_PATH+" TEXT, "+COLUMN_GUID+" TEXT, "+COLUMN_TIMESTAMP+" INTEGER)";
	private static final String SQL_CREATE_TABLE_TASK_CONDITIONS = "CREATE TABLE "+TABLE_TASK_CONDITIONS+" ("+COLUMN_TASK_ID+" TEXT, "+COLUMN_CONDITION_ID+" TEXT, "+COLUMN_KEY+" TEXT, "+COLUMN_VALUE+" TEXT)";
	private static final String SQL_CREATE_TABLE_TASK_OUTPUTS = "CREATE TABLE "+TABLE_TASK_OUTPUTS+" ("+COLUMN_TASK_ID+" TEXT, "+COLUMN_FEATURE+" TEXT)";
	private static final String SQL_CREATE_TABLE_TASKS = "CREATE TABLE "+TABLE_TASKS+" ("+COLUMN_TASK_ID+" TEXT PRIMARY KEY, "+COLUMN_CALLBACK_URI+" TEXT)";
	private static final String SQL_DELETE_DATA = "DELETE FROM "+TABLE_MEASUREMENT_DATA+" WHERE "+COLUMN_MEASUREMENT_ID+"=?";
	private static final String SQL_DELETE_FILE = "DELETE FROM "+TABLE_MEASUREMENT_FILES+" WHERE "+COLUMN_MEASUREMENT_ID+"=?";
	private static final String SQL_DELETE_MEASUREMENT = "DELETE FROM "+TABLE_MEASUREMENTS+" WHERE "+COLUMN_MEASUREMENT_ID+"=?";
	private static final String SQL_DELETE_TASK_CONDITIONS = "DELETE FROM "+TABLE_TASK_CONDITIONS;
	private static final String SQL_DELETE_TASK_OUTPUT = "DELETE FROM "+TABLE_TASK_OUTPUTS;
	private static final String SQL_DELETE_TASKS = "DELETE FROM "+TABLE_TASKS;
	private static final String SQL_GET_TASK_CALLBACK_URI = "SELECT "+COLUMN_CALLBACK_URI+" FROM "+TABLE_TASKS+" WHERE "+COLUMN_TASK_ID+"=? LIMIT 1";
	private static final String SQL_GET_TASK_CONDITIONS = "SELECT "+COLUMN_CONDITION_ID+", "+COLUMN_KEY+", "+COLUMN_VALUE+" FROM "+TABLE_TASK_CONDITIONS+" WHERE "+COLUMN_TASK_ID+"=? ORDER BY "+COLUMN_CONDITION_ID+" ASC";
	private static final String SQL_GET_TASK_OUTPUTS = "SELECT "+COLUMN_FEATURE+" FROM "+TABLE_TASK_OUTPUTS+" WHERE "+COLUMN_TASK_ID+"=?";
	private static final String SQL_GET_UNSENT_MEASUREMENTS = "SELECT "+COLUMN_TASK_ID+", "+COLUMN_MEASUREMENT_ID+" FROM "+TABLE_MEASUREMENTS+" WHERE "+COLUMN_SENT+"=0 ORDER BY "+COLUMN_TASK_ID+" ASC";
	private static final String SQL_INSERT_TASK_CALLBACK = "INSERT INTO "+TABLE_TASKS+" ("+COLUMN_TASK_ID+", "+COLUMN_CALLBACK_URI+") VALUES (?,?)";
	private static final String SQL_INSERT_TASK_CONDITION = "INSERT INTO "+TABLE_TASK_CONDITIONS+" ("+COLUMN_TASK_ID+", "+COLUMN_CONDITION_ID+", "+COLUMN_KEY+", "+COLUMN_VALUE+") VALUES (?,?,?,?)";
	private static final String SQL_INSERT_TASK_OUTPUT = "INSERT INTO "+TABLE_TASK_OUTPUTS+" ("+COLUMN_TASK_ID+", "+COLUMN_FEATURE+") VALUES (?,?)";
	private static final String SQL_ROLLBACK = "ROLLBACK";
	private static final String SQL_SET_SENT = "UPDATE "+TABLE_MEASUREMENTS+" SET "+COLUMN_SENT+"=1 WHERE "+COLUMN_MEASUREMENT_ID+"=? AND "+COLUMN_TASK_ID+"=?";
	private static final String SQL_UPDATE_FILE_GUID = "UPDATE "+TABLE_MEASUREMENT_FILES+" SET "+COLUMN_GUID+"=? WHERE "+COLUMN_PATH+"=?";
	private Connection _connection = null;
	
	/**
	 * 
	 * @param databaseFilePath
	 * @param create if true the database file and the database will be created on initialization
	 * @return failure if reading the given database file failed
	 */
	public boolean initialize(String databaseFilePath, boolean create) {
		if(create && !createDatabase(databaseFilePath)){
			return false;
		}
		
		try {
			_connection = DriverManager.getConnection("jdbc:sqlite:"+databaseFilePath);
		} catch (SQLException ex) {
			LOGGER.error(ex, ex);
			return false;
		}
		
		if(create && !createTables()){
			deleteDatabase(databaseFilePath);
			return false;
		}
		
		return true;
	}

	/**
	 * 
	 * @param databaseFilePath
	 */
	private void deleteDatabase(String databaseFilePath) {
		if(!new File(databaseFilePath).delete()){
			LOGGER.error("Failed to delete database file: "+databaseFilePath);
		}
	}
	
	/**
	 * 
	 * @param databaseFilePath
	 * @return true on success
	 */
	private boolean createDatabase(String databaseFilePath) {
		File file = new File(databaseFilePath);
		try {
			if(!file.createNewFile()){
				LOGGER.error("Failed to create new database file: "+databaseFilePath+", file already exists?");
				return false;
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 
	 * @return true on success
	 */
	private boolean createTables() {
		try (Statement stmnt = _connection.createStatement()) {
			stmnt.executeUpdate(SQL_CREATE_TABLE_MEASUREMENT_FILES);
			stmnt.executeUpdate(SQL_CREATE_INDEX_TABLE_MEASUREMENT_FILES);
			stmnt.executeUpdate(SQL_CREATE_TABLE_MEASUREMENT_DATA);
			stmnt.executeUpdate(SQL_CREATE_TABLE_MEASUREMENTS);
			stmnt.executeUpdate(SQL_CREATE_INDEX_TABLE_MEASUREMENTS);
			stmnt.executeUpdate(SQL_CREATE_TABLE_TASKS);
			stmnt.executeUpdate(SQL_CREATE_TABLE_TASK_CONDITIONS);
			stmnt.executeUpdate(SQL_CREATE_TABLE_TASK_OUTPUTS);
		} catch (SQLException ex) {
			LOGGER.error(ex, ex);
			return false;
		}
		
		return true;
	}

	@Override
	public void close() {
		if(_connection != null){
			try {
				_connection.close();
			} catch (SQLException ex) {
				LOGGER.error(ex, ex);
			}
			_connection = null;
		}
	}
	
	/**
	 * delete the given measurements from file and measurement tables
	 * 
	 * @param measurementIds
	 * @return false on failure (generally, a database error, non-existing ids are ignored)
	 */
	public boolean deleteMeasurements(Collection<String> measurementIds){
		try (PreparedStatement fstmnt = _connection.prepareStatement(SQL_DELETE_FILE); PreparedStatement dstmnt = _connection.prepareStatement(SQL_DELETE_DATA); PreparedStatement mstmnt = _connection.prepareStatement(SQL_DELETE_MEASUREMENT)) {
			for(String measurementId : measurementIds) {
				fstmnt.setString(1, measurementId);
				if(fstmnt.executeUpdate() < 1){
					LOGGER.warn("No file data deleted, measurement id: "+measurementId);
				}
				
				dstmnt.setString(1, measurementId);
				if(dstmnt.executeUpdate() < 1){
					LOGGER.warn("No measurement data deleted, measurement id: "+measurementId);
				}
				
				mstmnt.setString(1, measurementId);
				if(mstmnt.executeUpdate() < 1){
					LOGGER.warn("No measurements deleted, measurement id: "+measurementId);
				}
			}
		} catch (SQLException ex) {
			LOGGER.error(ex, ex);
			return false;
		}
		return true;
	}
	
	/**
	 * set the given measurements (and files) to send condition
	 * 
	 * Note: on failure the database will be left unmodified
	 * 
	 * @param measurementIds
	 * @param taskId 
	 * @return true on success
	 */
	public boolean setMeasurementsSent(Collection<String> measurementIds, String taskId){
		try(Statement stmnt = _connection.createStatement()){
			stmnt.execute(SQL_BEGIN_TRANSACTION);
			try(PreparedStatement pstmnt = _connection.prepareStatement(SQL_SET_SENT)) {
				for(String measurementId : measurementIds) {
					pstmnt.setString(1, measurementId);
					pstmnt.setString(2, taskId);
					if(pstmnt.executeUpdate() < 1){
						LOGGER.error("No measurements set to sent for measurement id: "+measurementId);
						stmnt.execute(SQL_ROLLBACK);
						return false;
					}
				}
				stmnt.execute(SQL_COMMIT);
			} catch (SQLException ex) {
				LOGGER.error(ex, ex);
				stmnt.execute(SQL_ROLLBACK);
				return false;
			}
		} catch (SQLException ex) {
			LOGGER.error(ex, ex);
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param filePath
	 * @param GUID
	 * @return true if successfully updated
	 */
	public boolean setFileGUID(String filePath, String GUID) {
		try(PreparedStatement stmnt = _connection.prepareStatement(SQL_UPDATE_FILE_GUID)) {
			stmnt.setString(1, GUID);
			stmnt.setString(2, filePath);
			if(stmnt.executeUpdate() < 1){
				LOGGER.error("No rows updated.");
				return false;
			}
		} catch (SQLException ex) {
			LOGGER.error(ex, ex);
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param hasGUID the file has a GUID
	 * @param measurementIds optional list of measurement ids used as a filter
	 * @return list of matching files or null if none was found
	 */
	public List<SqliteFile> getFiles(boolean hasGUID, Collection<String> measurementIds) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(TABLE_MEASUREMENT_FILES);
		sql.append(" WHERE ");
		sql.append(COLUMN_GUID);
		if(hasGUID){
			sql.append(" IS NOT NULL");
		}else{
			sql.append(" IS NULL");
		}
		
		boolean hasIds = (measurementIds != null && !measurementIds.isEmpty());
		if(hasIds){
			sql.append(" AND ");
			sql.append(COLUMN_MEASUREMENT_ID);
			sql.append(" IN (?");
			for(int i=1, size=measurementIds.size(); i<size; ++i){
				sql.append(",?");
			}
			sql.append(')');
		}
		
		ArrayList<SqliteFile> files = new ArrayList<>();
		try (PreparedStatement stmnt = _connection.prepareStatement(sql.toString())) {
			if(hasIds){
				int index = 0;
				for(String measurementId : measurementIds){
					stmnt.setString(++index, measurementId);
				}
			}
			
			try (ResultSet set = stmnt.executeQuery()) {
				files = new ArrayList<>();
				while(set.next()){
					SqliteFile file = new SqliteFile();
					file.setGUID(set.getString(COLUMN_GUID));
					file.setMeasurementId(set.getString(COLUMN_MEASUREMENT_ID));
					file.setPath(set.getString(COLUMN_PATH));
					file.setTimestamp(new Date(set.getLong(COLUMN_TIMESTAMP)));
					files.add(file);
				}
			}
		} catch (SQLException ex) {
			LOGGER.error(ex, ex);
		}
		
		return (files.isEmpty() ? null : files);
	}
	
	/**
	 * 
	 * @return taskId-measurement_id map of unsent measurements or null if nothing found
	 */
	public Map<String, List<String>> getUnsentMeasurementIds() {
		HashMap<String, List<String>> map = new HashMap<>();
		try (Statement stmnt = _connection.createStatement(); ResultSet set = stmnt.executeQuery(SQL_GET_UNSENT_MEASUREMENTS)) {
			List<String> measurementIds = null;
			String currentTaskId = null;
			while(set.next()){
				String taskId = set.getString(COLUMN_TASK_ID);
				if(!taskId.equals(currentTaskId)){
					currentTaskId = taskId;
					measurementIds = new ArrayList<>();
					map.put(taskId, measurementIds);
				}
				measurementIds.add(set.getString(COLUMN_MEASUREMENT_ID));
			}
		} catch (SQLException ex) {
			LOGGER.error(ex, ex);
		}

		return (map.isEmpty() ? null : map);
	}
	
	/**
	 * 
	 * @param measurementIds
	 * @return list of matching measurements with all related data points or null if nothing was found
	 */
	public List<Measurement> getMeasurements(Collection<String> measurementIds){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ");
		sql.append(TABLE_MEASUREMENT_DATA);
		sql.append(" WHERE ");
		sql.append(COLUMN_MEASUREMENT_ID);
		sql.append(" IN (?");
		for(int i=1, size = measurementIds.size(); i<size; ++i){
			sql.append(", ?");
		}
		sql.append(") ORDER BY ");
		sql.append(COLUMN_MEASUREMENT_ID);
		sql.append(" ASC");
		
		ArrayList<Measurement> measurements = new ArrayList<>();
		try(PreparedStatement stmnt = _connection.prepareStatement(sql.toString())){
			int index = 0;
			for(String measurementId : measurementIds){
				stmnt.setString(++index, measurementId);
			}
			try(ResultSet set = stmnt.executeQuery()) {
				String currentMeasurementId = null;
				Measurement m = null;
				while(set.next()){
					String measurementId = set.getString(COLUMN_MEASUREMENT_ID);
					if(!measurementId.equals(currentMeasurementId)){
						currentMeasurementId = measurementId;
						m = new Measurement();
						m.setMeasurementId(measurementId);
						measurements.add(m);
					}
					DataPoint dp = new DataPoint();
					dp.setKey(set.getString(COLUMN_KEY));
					dp.setValue(set.getString(COLUMN_VALUE));
					dp.setCreated(new Date(set.getLong(COLUMN_TIMESTAMP)));
					m.addDataPoint(dp);
				}
			}
		} catch (SQLException ex) {
			LOGGER.error(ex, ex);
		}
		return (measurements.isEmpty() ? null : measurements);
	}
	
	/**
	 * 
	 * @param taskId
	 * @return callback URI for the task
	 */
	public String getCallbackURI(String taskId) {
		try (PreparedStatement stmnt = _connection.prepareStatement(SQL_GET_TASK_CALLBACK_URI)){
			stmnt.setString(1, taskId);
			try(ResultSet set = stmnt.executeQuery()) {
				if(set.next()){
					return set.getString(1);
				}
			}
		} catch (SQLException ex) {
			LOGGER.error(ex, ex);
		}
		return null;
	}
	
	/**
	 * clear all task details
	 * @return false on failure (generally, a database error)
	 */
	public boolean deleteTasks() {
		try (Statement stmnt = _connection.createStatement()) {
			stmnt.executeUpdate(SQL_DELETE_TASKS);
			stmnt.executeUpdate(SQL_DELETE_TASK_CONDITIONS);
			stmnt.executeUpdate(SQL_DELETE_TASK_OUTPUT);
		} catch (SQLException ex) {
			LOGGER.error(ex, ex);
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param task
	 * @return true on success
	 */
	public boolean saveTaskDetails(SensorTask task) {
		try(Statement stmnt = _connection.createStatement()){
			stmnt.execute(SQL_BEGIN_TRANSACTION);
			try (PreparedStatement callbackStmnt = _connection.prepareStatement(SQL_INSERT_TASK_CALLBACK); PreparedStatement conditionStmnt = _connection.prepareStatement(SQL_INSERT_TASK_CONDITION); PreparedStatement outputStmnt = _connection.prepareStatement(SQL_INSERT_TASK_OUTPUT)) {
				String taskId = task.getTaskIds().iterator().next();
				callbackStmnt.setString(1, taskId);
				callbackStmnt.setString(2, task.getCallbackUri());
				callbackStmnt.executeUpdate();
				
				conditionStmnt.setString(1, taskId);
				for(Condition condition : task.getConditions()){
					conditionStmnt.setString(2, UUID.randomUUID().toString());
					for(Entry<String, String> e : condition.getConditions().entrySet()){
						conditionStmnt.setString(3, e.getKey());
						conditionStmnt.setString(4, e.getValue());
						conditionStmnt.executeUpdate();
					}
				}
				
				outputStmnt.setString(1, taskId);
				for(Output output : task.getOutput()){
					outputStmnt.setString(2, output.getFeature());
					outputStmnt.executeUpdate();
				}
				
				stmnt.execute(SQL_COMMIT);
			} catch (SQLException ex) {
				LOGGER.error(ex, ex);
				stmnt.execute(SQL_ROLLBACK);
				return false;
			} 
		} catch (SQLException ex) {
			LOGGER.error(ex, ex);
			return false;
		}
		return true;
	}
	
	/**
	 * Note: this will only return the details stored with {@link #saveTaskDetails(SensorTask)}, the related measurements will not be returned.
	 * 
	 * @param taskId
	 * @return the stored task details matching the given task id or null if nothing was found
	 */
	public SensorTask getTaskDetails(String taskId) {
		String callbackUri = getCallbackURI(taskId);
		if(StringUtils.isBlank(callbackUri)){
			LOGGER.error("Callback URI not found for task, id: "+taskId);
			return null;
		}
		
		SensorTask task = new SensorTask();
		task.setCallbackUri(callbackUri);
		
		try (PreparedStatement cstmnt = _connection.prepareStatement(SQL_GET_TASK_CONDITIONS); PreparedStatement ostmnt = _connection.prepareStatement(SQL_GET_TASK_OUTPUTS)) {
			cstmnt.setString(1, taskId);
			try(ResultSet set = cstmnt.executeQuery()) {
				ArrayList<Condition> conditions = new ArrayList<>();
				String currentConditionId = null;
				HashMap<String, String> conditionMap = new HashMap<>();
				while(set.next()){
					String conditionId = set.getString(COLUMN_CONDITION_ID);
					if(!conditionId.equals(currentConditionId)){
						currentConditionId = conditionId;
						Condition condition = new Condition();
						condition.setConditions(conditionMap);
						conditions.add(condition);
					}
					conditionMap.put(set.getString(COLUMN_KEY), set.getString(COLUMN_VALUE));
				}
				
				if(conditions.isEmpty()){
					LOGGER.warn("No conditions found for task, id: "+taskId);
				}else{
					task.setConditions(conditions);
				}
			}
			
			try(ResultSet set = ostmnt.executeQuery()) {
				ArrayList<Output> outputs = new ArrayList<>();
				while(set.next()){
					Output output = new Output();
					output.setFeature(set.getString(COLUMN_FEATURE));
					outputs.add(output);
				}
				
				if(outputs.isEmpty()){
					LOGGER.warn("No outputs found for task, id: "+taskId);
				}else{
					task.setOutput(outputs);
				}
			}
		} catch (SQLException ex) {
			LOGGER.error(ex, ex);
			return null;
		}
		
		return task;
	}
}
