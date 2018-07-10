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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import core.tut.pori.dao.clause.SQLClause.SQLType;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import service.tut.pori.apilta.sensors.datatypes.Condition;
import service.tut.pori.apilta.sensors.datatypes.Output;
import service.tut.pori.apilta.sensors.datatypes.SensorTask;
import service.tut.pori.tasks.TaskDAO;
import service.tut.pori.tasks.datatypes.Task;

/**
 * task dao for the sensors service
 *
 */
public class SensorTaskDAO extends TaskDAO {
	private static final Logger LOGGER = Logger.getLogger(SensorTaskDAO.class);
	/* tables */
	private static final String TABLE_MEASUREMENTS_TASKS_CONDITIONS = DATABASE+".measurements_tasks_conditions";
	private static final String TABLE_MEASUREMENTS_TASKS_OUTPUTS = DATABASE+".measurements_tasks_outputs";
	/* columns */
	private static final String COLUMN_CONDITION_ID = "condition_id";
	private static final String COLUMN_CONDITION_KEY = "condition_key";
	private static final String COLUMN_CONDITION_VALUE = "condition_value";
	private static final String COLUMN_FEATURE = "feature";
	/* sql */
	private static final String SQL_DELETE_CONDITIONS = "DELETE FROM "+TABLE_MEASUREMENTS_TASKS_CONDITIONS+" WHERE "+Definitions.COLUMN_TASK_ID+"=?";
	private static final String SQL_DELETE_OUTPUT = "DELETE FROM "+TABLE_MEASUREMENTS_TASKS_OUTPUTS+" WHERE "+Definitions.COLUMN_TASK_ID+"=?";
	private static final String SQL_INSERT_CONDITION = "INSERT INTO "+TABLE_MEASUREMENTS_TASKS_CONDITIONS+" ("+Definitions.COLUMN_TASK_ID+", "+COLUMN_CONDITION_ID+", "+COLUMN_CONDITION_KEY+", "+COLUMN_CONDITION_VALUE+", "+COLUMN_ROW_CREATED+") VALUES (?,?,?,?,NOW())";
	private static final int[] SQL_INSERT_CONDITION_SQL_TYPES = {SQLType.STRING.toInt(), SQLType.STRING.toInt(), SQLType.STRING.toInt(), SQLType.STRING.toInt()};
	private static final String SQL_INSERT_OUTPUT = "INSERT INTO "+TABLE_MEASUREMENTS_TASKS_OUTPUTS+" ("+Definitions.COLUMN_TASK_ID+", "+COLUMN_FEATURE+", "+COLUMN_ROW_CREATED+") VALUES (?,?,NOW())";
	private static final int[] SQL_INSERT_OUTPUT_SQL_TYPES = {SQLType.STRING.toInt(), SQLType.STRING.toInt()};
	private static final String SQL_GET_CONDITIONS = "SELECT "+COLUMN_CONDITION_ID+", "+COLUMN_CONDITION_KEY+", "+COLUMN_CONDITION_VALUE+" FROM "+TABLE_MEASUREMENTS_TASKS_CONDITIONS+" WHERE "+Definitions.COLUMN_TASK_ID+"=? ORDER BY "+COLUMN_CONDITION_ID;
	private static final String SQL_GET_OUTPUT = "SELECT "+COLUMN_FEATURE+" FROM "+TABLE_MEASUREMENTS_TASKS_OUTPUTS+" WHERE "+Definitions.COLUMN_TASK_ID+"=?";
	private static final int[] SQL_TYPE_TASK_ID = {SQLType.STRING.toInt()};
	
	@Override
	public String createTask(Task task) {
		if(task instanceof SensorTask){
			return createTask((SensorTask) task);
		}else{
			LOGGER.debug("Using super class for task insertion...");
			return super.createTask(task);
		}
	}
	
	/**
	 * 
	 * @param task
	 * @return identifier of the created task
	 */
	public String createTask(SensorTask task) {
		return getTransactionTemplate().execute(new TransactionCallback<String>() {
			@Override
			public String doInTransaction(TransactionStatus arg0) {
				String taskId = SensorTaskDAO.super.createTask(task);
				if(taskId == null){
					LOGGER.warn("Failed to create base task.");
					return null;
				}
				
				addConditions(task.getConditions(), taskId);
				addOutput(task.getOutput(), taskId);
				
				return taskId;
			}
		});
	}

	/**
	 * 
	 * @param output
	 * @param taskId
	 */
	private void addOutput(List<Output> output, String taskId) {
		if(output == null || output.isEmpty()){
			LOGGER.warn("No output for task, id: "+taskId);
			return;
		}
		
		JdbcTemplate t = getJdbcTemplate();
		Object[] ob = {taskId, null};
		for(Output o : output) {
			ob[1] = o.getFeature();
			t.update(SQL_INSERT_OUTPUT, ob, SQL_INSERT_OUTPUT_SQL_TYPES);
		}
	}

	/**
	 * 
	 * @param conditions
	 * @param taskId
	 */
	private void addConditions(List<Condition> conditions, String taskId) {
		if(conditions == null || conditions.isEmpty()){
			LOGGER.warn("No conditions for task, id: "+taskId);
			return;
		}
		
		JdbcTemplate t = getJdbcTemplate();
		Object[] ob = {taskId, null, null, null};
		for(Condition condition : conditions){
			ob[1] = UUID.randomUUID().toString(); // generate random identifier for this condition
			for(Entry<String, String> entry : condition.getConditions().entrySet()){
				ob[2] = entry.getKey();
				ob[3] = entry.getValue();
				t.update(SQL_INSERT_CONDITION, ob, SQL_INSERT_CONDITION_SQL_TYPES);
			}
		}
	}

	@Override
	public boolean updateTask(Task task) throws IllegalArgumentException {
		if(task instanceof SensorTask){
			return updateTask((SensorTask) task);
		}else{
			LOGGER.debug("Using super class for task update...");
			return super.updateTask(task);
		}
	}
	
	/**
	 * 
	 * @param task
	 * @return true on success
	 */
	public boolean updateTask(SensorTask task) {
		return getTransactionTemplate().execute(new TransactionCallback<Boolean>() {
			@Override
			public Boolean doInTransaction(TransactionStatus status) {
				if(!SensorTaskDAO.super.updateTask(task)){
					LOGGER.warn("Base task update failed, aborting update...");
					return Boolean.FALSE;
				}
				
				for(String taskId : task.getTaskIds()){
					removeConditions(taskId);
					addConditions(task.getConditions(), taskId);
					removeOutput(taskId);
					addOutput(task.getOutput(), taskId);
				}
				
				return Boolean.TRUE;
			}
		});
	}

	/**
	 * 
	 * @param taskId
	 */
	private void removeOutput(String taskId) {
		getJdbcTemplate().update(SQL_DELETE_OUTPUT, new Object[]{taskId}, SQL_TYPE_TASK_ID);
	}

	/**
	 * 
	 * @param taskId
	 */
	private void removeConditions(String taskId) {
		getJdbcTemplate().update(SQL_DELETE_CONDITIONS, new Object[]{taskId}, SQL_TYPE_TASK_ID);
	}

	@Override
	public SensorTask getTask(Long backendId, DataGroups dataGroups, Limits limits, String taskId) {
		Task task = super.getTask(backendId, dataGroups, limits, taskId);
		if(task == null) {
			LOGGER.debug("Task not found, id: "+taskId);
			return null;
		}
		
		SensorTask sTask = new SensorTask(task);
		sTask.setConditions(getConditions(taskId));
		sTask.setOutput(getOuput(taskId));
		return sTask;
	}

	/**
	 * 
	 * @param taskId
	 * @return list of output or null if none was found
	 */
	private List<Output> getOuput(String taskId) {
		List<String> features = getJdbcTemplate().queryForList(SQL_GET_OUTPUT, new Object[]{taskId}, SQL_TYPE_TASK_ID, String.class);
		if(features.isEmpty()){
			LOGGER.warn("No output for task, id: "+taskId);
			return null;
		}
		
		ArrayList<Output> output = new ArrayList<>(features.size());
		for(String feature : features) {
			Output o = new Output();
			o.setFeature(feature);
			output.add(o);
		}
		
		return output;
	}

	/**
	 * 
	 * @param taskId
	 * @return list of conditions or null if none was found
	 */
	private List<Condition> getConditions(String taskId) {
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(SQL_GET_CONDITIONS, new Object[]{taskId}, SQL_TYPE_TASK_ID);
		if(rows.isEmpty()){
			LOGGER.warn("No conditions for task, id: "+taskId);
			return null;
		}
		
		ArrayList<Condition> conditions = new ArrayList<>();
		String conditionId = null;
		Condition condition = null;
		for(Map<String, Object> row : rows){
			Object tempId = row.get(COLUMN_CONDITION_ID);
			if(!tempId.equals(conditionId)){ // the result list is sorted by condition id, so the condition collection changes when the id changes
				condition = new Condition();
				condition.setConditions(new HashMap<>());
				conditions.add(condition);
				conditionId = (String) tempId;
			}	
			condition.getConditions().put((String) row.get(COLUMN_CONDITION_KEY), (String) row.get(COLUMN_CONDITION_VALUE));		
		} // for rows
		return conditions;
	}

	@Override
	public void removeTask(String taskId) {
		getTransactionTemplate().execute(new TransactionCallback<Void>() {

			@Override
			public Void doInTransaction(TransactionStatus status) {
				removeConditions(taskId);
				removeOutput(taskId);
				SensorTaskDAO.super.removeTask(taskId);
				return null;
			}
		});
	}
}
	