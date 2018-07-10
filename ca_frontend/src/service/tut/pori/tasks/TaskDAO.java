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
package service.tut.pori.tasks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.quartz.JobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.dao.SQLDAO;
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
import core.tut.pori.users.UserIdentity;
import service.tut.pori.backends.BackendDAO;
import service.tut.pori.backends.datatypes.Backend;
import service.tut.pori.backends.datatypes.BackendList;
import service.tut.pori.backends.datatypes.BackendUserIdentity.UserPermission;
import service.tut.pori.tasks.datatypes.Task;
import service.tut.pori.tasks.datatypes.Task.State;
import service.tut.pori.tasks.datatypes.TaskBackend.Status;
import service.tut.pori.tasks.datatypes.Task.Visibility;
import service.tut.pori.tasks.datatypes.TaskBackend;
import service.tut.pori.tasks.datatypes.TaskList;
import service.tut.pori.tasks.datatypes.TaskPermissions;
import service.tut.pori.tasks.utils.TaskUtils;

/**
 * base implementation for a task dao
 * 
 */
public class TaskDAO extends SQLDAO {
	private static final Logger LOGGER = Logger.getLogger(TaskDAO.class);
	/* table names */
	private static final String TABLE_TASKS = DATABASE+".tasks";
	private static final String TABLE_TASKS_BACKENDS = DATABASE+".tasks_backends";
	private static final String TABLE_TASKS_TYPES = DATABASE+".tasks_types";
	/* columns */
	private static final String COLUMN_BACKEND_ID = "backend_id";
	private static final String COLUMN_DAO_CLASS = "dao_class";
	private static final String COLUMN_DATA_VISIBILITY = "data_visibility";
	private static final String COLUMN_DESCRIPTION = "description";
	private static final String COLUMN_MESSAGE = "message";
	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_ROW_CREATED_WITH_TABLE_NAME = TABLE_TASKS+"."+COLUMN_ROW_CREATED;
	private static final String COLUMN_STATE = "state";
	private static final String COLUMN_STATUS = "status";
	private static final String COLUMN_TASK_ID = "task_id";
	private static final String COLUMN_TASK_ID_WITH_TABLE_NAME = TABLE_TASKS+"."+COLUMN_TASK_ID;
	private static final String COLUMN_TASK_TYPE = "task_type";
	/* sql */
	private static final String[] SQL_COLUMNS_TASK = {COLUMN_USER_ID, COLUMN_TASK_ID, COLUMN_DAO_CLASS, COLUMN_NAME, COLUMN_DESCRIPTION, COLUMN_DATA_VISIBILITY, COLUMN_STATE, COLUMN_ROW_CREATED};
	private static final String[] SQL_COLUMNS_TASK_BACKEND = {COLUMN_BACKEND_ID, COLUMN_STATUS, COLUMN_MESSAGE};
	private static final String[] SQL_COLUMNS_TASK_LIST = {COLUMN_TASK_ID_WITH_TABLE_NAME, TABLE_TASKS+"."+COLUMN_USER_ID, TABLE_TASKS+"."+COLUMN_STATE, TABLE_TASKS+"."+COLUMN_NAME, TABLE_TASKS+"."+COLUMN_DESCRIPTION, TABLE_TASKS+"."+COLUMN_ROW_CREATED, TABLE_TASKS+"."+COLUMN_ROW_UPDATED};
	
	private static final String SQL_DELETE_TASK = "DELETE FROM "+TABLE_TASKS+" WHERE "+COLUMN_TASK_ID+"=?";
	
	private static final String SQL_DELETE_TASK_BACKEND = "DELETE FROM "+TABLE_TASKS_BACKENDS+" WHERE "+COLUMN_BACKEND_ID+"=?";
	private static final int[] SQL_DELETE_TASK_BACKEND_SQL_TYPES = {SQLType.LONG.toInt()};
	
	private static final String SQL_DELETE_TASK_BACKENDS = "DELETE FROM "+TABLE_TASKS_BACKENDS+" WHERE "+COLUMN_TASK_ID+"=?";
	
	private static final String SQL_DELETE_TASK_TYPES = "DELETE FROM "+TABLE_TASKS_TYPES+" WHERE "+COLUMN_TASK_ID+"=?";
	
	private static final String SQL_GET_BACKEND_IDS = "SELECT "+COLUMN_BACKEND_ID+" FROM "+TABLE_TASKS_BACKENDS+" WHERE "+COLUMN_TASK_ID+"=?";
	
	private static final String SQL_GET_DAO_CLASS = "SELECT "+COLUMN_DAO_CLASS+" FROM "+TABLE_TASKS+" WHERE "+COLUMN_TASK_ID+"=?";
	
	private static final String SQL_GET_TASK = "SELECT "+TABLE_TASKS+"."+COLUMN_USER_ID+", "+TABLE_TASKS+"."+COLUMN_NAME+", "+TABLE_TASKS+"."+COLUMN_DESCRIPTION+", "+TABLE_TASKS+"."+COLUMN_DATA_VISIBILITY+", "+TABLE_TASKS+"."+COLUMN_STATE+", "+TABLE_TASKS+"."+COLUMN_ROW_UPDATED+", "+TABLE_TASKS+"."+COLUMN_ROW_CREATED+" FROM "+TABLE_TASKS+" LEFT JOIN "+TABLE_TASKS_BACKENDS+" ON "+TABLE_TASKS_BACKENDS+"."+COLUMN_TASK_ID+"="+COLUMN_TASK_ID_WITH_TABLE_NAME+" WHERE "+TABLE_TASKS_BACKENDS+"."+COLUMN_TASK_ID+"=? AND "+TABLE_TASKS_BACKENDS+"."+COLUMN_BACKEND_ID+"=?";
	private static final int[] SQL_GET_TASK_SQL_TYPES = {SQLType.STRING.toInt(), SQLType.LONG.toInt()};
	
	private static final String SQL_GET_TASK_IDS_FOR_USER_ID = "SELECT "+COLUMN_TASK_ID+" FROM "+TABLE_TASKS+" WHERE "+COLUMN_USER_ID+"=?";
	private static final int[] SQL_GET_TASK_IDS_FOR_USER_ID_SQL_TYPES = {SQLType.LONG.toInt()};
	
	private static final String SQL_GET_TASK_TYPES = "SELECT "+COLUMN_TASK_TYPE+" FROM "+TABLE_TASKS_TYPES+" WHERE "+COLUMN_TASK_ID+"=?";
	
	private static final String SQL_INSERT_TASK_BACKEND = "INSERT INTO "+TABLE_TASKS_BACKENDS+" ("+COLUMN_TASK_ID+", "+COLUMN_BACKEND_ID+", "+COLUMN_STATUS+", "+COLUMN_MESSAGE+", "+COLUMN_ROW_CREATED+") VALUES (?, ?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE "+COLUMN_STATUS+"=VALUES("+COLUMN_STATUS+"), "+COLUMN_MESSAGE+"=VALUES("+COLUMN_MESSAGE+")";
	private static final int[] SQL_INSERT_TASK_BACKEND_SQL_TYPES = {SQLType.STRING.toInt(), SQLType.LONG.toInt(), SQLType.INTEGER.toInt(), SQLType.STRING.toInt()};
	
	private static final String SQL_INSERT_TASK_TYPE = "INSERT INTO "+TABLE_TASKS_TYPES+" ("+COLUMN_TASK_ID+", "+COLUMN_TASK_TYPE+", "+COLUMN_ROW_CREATED+") VALUES (?,?,NOW())";
	private static final int[] SQL_INSERT_TASK_TYPE_SQL_TYPES = {SQLType.STRING.toInt(), SQLType.STRING.toInt()};
	
	private static final int[] SQL_TASK_ID_SQL_TYPE = {SQLType.STRING.toInt()};
	private static final JoinClause SQL_TASK_LIST_JOIN = new JoinClause("LEFT JOIN "+TABLE_TASKS_BACKENDS+" ON "+TABLE_TASKS_BACKENDS+"."+COLUMN_TASK_ID+"="+COLUMN_TASK_ID_WITH_TABLE_NAME);
	
	private static final String SQL_UPDATE_TASK = "UPDATE "+TABLE_TASKS+" SET "+COLUMN_USER_ID+"=?, "+COLUMN_DAO_CLASS+"=?, "+COLUMN_NAME+"=?, "+COLUMN_DESCRIPTION+"=?, "+COLUMN_DATA_VISIBILITY+"=?, "+COLUMN_STATE+"=?, "+COLUMN_ROW_UPDATED+"=NOW() WHERE "+COLUMN_TASK_ID+"=?";
	private static final int[] SQL_UPDATE_TASK_SQL_TYPES = {SQLType.LONG.toInt(), SQLType.STRING.toInt(), SQLType.STRING.toInt(), SQLType.STRING.toInt(), SQLType.INTEGER.toInt(), SQLType.INTEGER.toInt(), SQLType.STRING.toInt()};
	
	private static final String SQL_UPDATE_TASK_BACKEND = "UPDATE "+TABLE_TASKS_BACKENDS+" SET "+COLUMN_STATUS+"=?, "+COLUMN_MESSAGE+"=?, "+COLUMN_ROW_UPDATED+"=NOW() WHERE "+COLUMN_TASK_ID+"=? AND "+COLUMN_BACKEND_ID+"=?";
	private static final int[] SQL_UPDATE_TASK_BACKEND_SQL_TYPES = {SQLType.INTEGER.toInt(), SQLType.STRING.toInt(), SQLType.STRING.toInt(), SQLType.LONG.toInt()};
	
	private static final String SQL_UPDATE_TASK_TIMESTAMP = "UPDATE "+TABLE_TASKS+" SET "+COLUMN_ROW_UPDATED+"=NOW() WHERE "+COLUMN_TASK_ID+"=?";
	
	private static final String SQL_GET_TASK_OWNER_VISIBILITY = "SELECT "+COLUMN_COUNT+", "+COLUMN_USER_ID+", "+COLUMN_DATA_VISIBILITY+" FROM "+TABLE_TASKS+" WHERE "+COLUMN_TASK_ID+"=?";
	private static final int[] SQL_GET_TASK_OWNER_VISIBILITY_SQL_TYPES = {SQLType.STRING.toInt()};
	
	private static final String SQL_IS_VIRTUAL_TASK = "SELECT "+COLUMN_COUNT+" FROM "+TABLE_TASKS_TYPES+" WHERE "+COLUMN_TASK_ID+"=? AND "+COLUMN_TASK_TYPE+"='"+Definitions.TASK_TYPE_VIRTUAL+"' LIMIT 1";
				
	@Autowired
	private BackendDAO _backendDAO = null;
	
	/**
	 * If the task has the task type of {@value service.tut.pori.tasks.Definitions#TASK_TYPE_VIRTUAL}, no permission checks for the back ends will be made
	 * 
	 * @param resolveBackendPermissions if true, the back end permissions will be resolved
	 * @param taskId
	 * @param userId
	 * @return the permission for the user for the given task
	 */
	public TaskPermissions getTaskPermissions(boolean resolveBackendPermissions, String taskId, UserIdentity userId) {
		TaskPermissions permissions = new TaskPermissions(taskId, userId);
		
		JdbcTemplate t = getJdbcTemplate();
		
		Object[] ob = {taskId};
		Map<String, Object> row = t.queryForMap(SQL_GET_TASK_OWNER_VISIBILITY, ob, SQL_GET_TASK_OWNER_VISIBILITY_SQL_TYPES);
		if((long) row.get(COLUMN_COUNT) < 1){
			LOGGER.warn("Task not found. Task id: "+taskId);
			return permissions;
		}
		
		permissions.setTaskExists(true);
		permissions.setTaskOwner(UserIdentity.equals(userId, (Long) row.get(COLUMN_USER_ID)));
		permissions.setDataVisibility(Visibility.fromInt((int) row.get(COLUMN_DATA_VISIBILITY)));			

		if(resolveBackendPermissions){
			LOGGER.debug("Resolving back end permissions...");
			if(t.queryForObject(SQL_IS_VIRTUAL_TASK, ob, SQL_TASK_ID_SQL_TYPE, Integer.class) > 0){
				LOGGER.debug("Task, id: "+taskId+" has task type "+Definitions.TASK_TYPE_VIRTUAL+", not checking back end access.");
				return permissions;
			}
			
			List<Long> backendIds = t.queryForList(SQL_GET_BACKEND_IDS, ob, SQL_TASK_ID_SQL_TYPE, Long.class);
			if(backendIds.isEmpty()){
				LOGGER.warn("No back end for task, id: "+taskId);
				return permissions;
			}
			
			for(Long backendId : backendIds) { // return what permissions the user has through accessing the task using any of the associated back ends
				Set<UserPermission> backendPermissions = _backendDAO.getBackendPermissions(backendId, userId);
				if(backendPermissions != null){
					permissions.setBackendPermissions(backendId, backendPermissions);
				} // if
			} // for
		}
		
		return permissions;
	}
	
	/**
	 * insert the basic details of the task, the identifier for the generated task will be set to the passed object, and also returned by the method
	 * 
	 * @param task
	 * @return the generated identifier for the task
	 */
	public String createTask(Task task) {
		return getTransactionTemplate().execute(new TransactionCallback<String>() {

			@Override
			public String doInTransaction(TransactionStatus status) {
				SimpleJdbcInsert sql = new SimpleJdbcInsert(getJdbcTemplate());
				sql.withTableName(TABLE_TASKS);
				sql.withoutTableColumnMetaDataAccess();
				sql.usingColumns(SQL_COLUMNS_TASK);
		
				Map<String, Object> params = new HashMap<>(SQL_COLUMNS_TASK.length);
				params.put(COLUMN_USER_ID, task.getUserId().getUserId());
				params.put(COLUMN_DAO_CLASS, task.getTaskDao().getClass().getName());
				params.put(COLUMN_ROW_CREATED, task.getCreated());
				params.put(COLUMN_NAME, task.getName());
				params.put(COLUMN_DESCRIPTION, task.getDescription());
				Visibility visibility = task.getDataVisibility();
				params.put(COLUMN_DATA_VISIBILITY, (visibility == null ? Definitions.DEFAULT_VISIBILITY.toInt() : visibility.toInt()));
				State state = task.getState();
				params.put(COLUMN_STATE, (state == null ? Definitions.DEFAULT_TASK_STATE.toInt() : state.toInt()));
				
				String taskId = UUID.randomUUID().toString();
				params.put(COLUMN_TASK_ID, taskId);
				
				sql.execute(params);
				task.addTaskId(taskId);
				insertBackends(task.getBackends(), taskId);
				insertTaskTypes(taskId, task.getTaskTypes());
				
				return taskId;
			}
		});
	}
	
	/**
	 * 
	 * @param taskId
	 */
	private void removeTaskTypes(String taskId){
		getJdbcTemplate().update(SQL_DELETE_TASK_TYPES, new Object[]{taskId}, SQL_TASK_ID_SQL_TYPE);
	}
	
	/**
	 * 
	 * @param taskId
	 * @param taskTypes
	 */
	private void insertTaskTypes(String taskId, Collection<String> taskTypes){
		if(taskTypes == null || taskTypes.isEmpty()){
			LOGGER.warn("No task types for task, id: "+taskId);
			return;
		}
		
		JdbcTemplate t = getJdbcTemplate();
		Object[] ob = {taskId, null};
		for(String taskType : taskTypes) {
			ob[1] = taskType;
			t.update(SQL_INSERT_TASK_TYPE, ob, SQL_INSERT_TASK_TYPE_SQL_TYPES);
		}
	}
	
	/**
	 * This base implementation allows to update the basic details of the task: task type, user id and back ends.
	 * 
	 * If only back end status update is required, that also be achieve by calling {@link #service.tut.pori.tasks.TaskDAO.statusUpdated(service.tut.pori.tasks.datatypes.TaskBackend, String)}
	 * 
	 * Note: this will <i>set</i> the updated data and all previous data, such as old back end details (including status messages) will be lost, and replaced with the details given in the passed task object.
	 * 
	 * If the task has multiple identifiers, <i>all</i> tasks with the given values will be given updated to match the same (given) details
	 * 
	 * @param task
	 * @return true on success, false on failure
	 * @throws IllegalArgumentException on invalid task
	 */
	public boolean updateTask(Task task) throws IllegalArgumentException {
		return getTransactionTemplate().execute(new TransactionCallback<Boolean>() {

			@Override
			public Boolean doInTransaction(TransactionStatus status) {
				for(String taskId : task.getTaskIds()){
					Visibility visibility = task.getDataVisibility();
					State state = task.getState();
					if(getJdbcTemplate().update(SQL_UPDATE_TASK, new Object[]{task.getUserId().getUserId(), task.getTaskDao().getClass().getName(), task.getName(), task.getDescription(), (visibility == null ? Definitions.DEFAULT_VISIBILITY.toInt() : visibility.toInt()), (state == null ? Definitions.DEFAULT_TASK_STATE.toInt() : state.toInt()), taskId}, SQL_UPDATE_TASK_SQL_TYPES) != 1){ // this will force update row_updated and should always return 1 if the task exists
						throw new IllegalArgumentException("Failed to update task, id: "+taskId);
					}
	
					removeTaskTypes(taskId);
					insertTaskTypes(taskId, task.getTaskTypes());
					removeBackends(taskId); // remove existing back ends
					insertBackends(task.getBackends(), taskId);
				}
				return Boolean.TRUE;
			}
		});
	}
	
	/**
	 * 
	 * @param backendId 
	 * @param dataGroups this base implementation accepts two data groups, @value{core.tut.pori.http.parameters.DataGroups.DATA_GROUP_ALL} (retrieves all task details) and @value{core.tut.pori.http.parameters.DataGroups.DATA_GROUP_BASIC} (only the basic details without back end information), other data groups (if present) are ignored. If no data groups are given, the data group @value{core.tut.pori.http.parameters.DataGroups.DATA_GROUP_BASIC} is used.
	 * @param limits limits for the retrieval task, ignored in this base implementation
	 * @param taskId
	 * @return the task or null if not found
	 */
	public Task getTask(Long backendId, DataGroups dataGroups, Limits limits, String taskId) {
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(SQL_GET_TASK, new Object[]{taskId, backendId}, SQL_GET_TASK_SQL_TYPES);
		if(rows.isEmpty()){
			LOGGER.debug("Task not found, task id: "+taskId);
			return null;
		}

		Task task = extractTask(rows.iterator().next());
		task.addTaskId(taskId);
		if(DataGroups.hasDataGroup(DataGroups.DATA_GROUP_ALL, dataGroups)){
			LOGGER.debug("Resolving back ends for task, id: "+taskId);
			task.setBackends(getBackends(limits, taskId));
		}
		return task;
	}
	
	/**
	 * Note: this only returns the basic details of a task: identifier, task type, user identifier and updated timestamp.
	 * 
	 * Use {@link #getTask(Long, DataGroups, Limits, String)} for resolving the full task details.
	 * 
	 * @param backendIdFilter optional back end id filter
	 * @param createdFilter optional filter
	 * @param limits
	 * @param stateFilter 
	 * @param userIdfilter optional user id filter for retrieving only the tasks, which are created by one of the given users 
	 * @return list of tasks for the given back end
	 */
	public TaskList getTaskList(long[] backendIdFilter, Collection<Interval> createdFilter, Limits limits, Collection<State> stateFilter, long[] userIdfilter) {
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_TASKS);
		sql.addSelectColumns(SQL_COLUMNS_TASK_LIST);
		sql.setLimits(limits);
		sql.addOrderBy(COLUMN_TASK_ID_WITH_TABLE_NAME, OrderDirection.DESCENDING);
		
		if(!ArrayUtils.isEmpty(backendIdFilter)){
			LOGGER.debug("Using back end id filter.");
			sql.addJoin(SQL_TASK_LIST_JOIN);
			sql.addWhereClause(new AndClause(COLUMN_BACKEND_ID, backendIdFilter));
			sql.addGroupBy(COLUMN_TASK_ID_WITH_TABLE_NAME);
		}
		
		if(!ArrayUtils.isEmpty(userIdfilter)) {
			LOGGER.debug("Using user id filter.");
			sql.addWhereClause(new AndClause(COLUMN_USER_ID, userIdfilter));
		}
		
		if(createdFilter != null && !createdFilter.isEmpty()){
			LOGGER.debug("Adding created filter.");
			AndSubClause asc = new AndSubClause();
			for(Interval interval : createdFilter){
				asc.addWhereClause(new OrSubClause(new AndCompareClause[]{new AndCompareClause(COLUMN_ROW_CREATED_WITH_TABLE_NAME, interval.getStart(), CompareType.GREATER_OR_EQUAL, SQLType.TIMESTAMP), new AndCompareClause(COLUMN_ROW_CREATED_WITH_TABLE_NAME, interval.getEnd(), CompareType.LESS_OR_EQUAL, SQLType.TIMESTAMP)}));
			}
			sql.addWhereClause(asc);
		}
		
		if(stateFilter != null && !stateFilter.isEmpty()){
			LOGGER.debug("Adding state filter.");
			sql.addWhereClause(new AndClause(COLUMN_STATE, TaskUtils.statesToInts(stateFilter)));
		}
		
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql.toSQLString(Definitions.ELEMENT_TASK_LIST), sql.getValues(), sql.getValueTypes());
		if(rows.isEmpty()){
			LOGGER.debug("No tasks found.");
			return null;
		}
		
		List<Task> tasks = new ArrayList<>(rows.size());
		for(Map<String, Object> row : rows) {
			Task task = extractTask(row);
			task.setBackends(getBackends(limits, task.getTaskIds().iterator().next()));
			tasks.add(task);
		}
		
		TaskList list = new TaskList();
		list.setTasks(tasks);
		return list;
	}
	
	/**
	 * 
	 * @param row
	 * @return task or null if no task was extracted
	 */
	private Task extractTask(Map<String, Object> row) {
		Task task = new TaskImpl();
		String taskId = null;
		for(Entry<String, Object> entry : row.entrySet()) {
			switch(entry.getKey()) {
				case COLUMN_TASK_ID:
					taskId = (String) entry.getValue();
					task.addTaskId(taskId);
					break;
				case COLUMN_USER_ID:
					task.setUserId(new UserIdentity((Long) entry.getValue()));
					break;
				case COLUMN_DAO_CLASS: // nothing needed
					break;
				case COLUMN_ROW_UPDATED:
					task.setUpdated((Date) entry.getValue());
					break;
				case COLUMN_ROW_CREATED:
					task.setCreated((Date) entry.getValue());
					break;
				case COLUMN_NAME:
					task.setName((String) entry.getValue());
					break;
				case COLUMN_DESCRIPTION:
					task.setDescription((String) entry.getValue());
					break;
				case COLUMN_DATA_VISIBILITY:
					task.setDataVisibility(Visibility.fromInt((int) entry.getValue()));
					break;
				case COLUMN_STATE:
					task.setState(State.fromInt((int) entry.getValue()));
					break;
				default:
					if(checkCountColumn(entry.getKey(), entry.getValue()) < 1){	// this should be count(*)
						LOGGER.warn("Unknown column name, or no results, column: "+entry.getKey());
						return null;
					}
				break;
			}
		}
		
		task.setTaskTypes(getTaskTypes(taskId));
		
		return task;
	}
	
	/**
	 * 
	 * @param taskId
	 * @return set of task types for the given task or null if none was found
	 */
	private Set<String> getTaskTypes(String taskId){
		HashSet<String> taskTypes = new HashSet<>();
		getJdbcTemplate().query(SQL_GET_TASK_TYPES, new Object[]{taskId}, SQL_TASK_ID_SQL_TYPE, new RowCallbackHandler() {
			
			@Override
			public void processRow(ResultSet set) throws SQLException {
				taskTypes.add(set.getString(1));
			}
		});
		
		if(taskTypes.isEmpty()){
			LOGGER.debug("No task types found for task, id: "+taskId);
			return null;
		}else{
			return taskTypes;
		}
	}
	
	/**
	 * 
	 * @param limits 
	 * @param taskId
	 * @return list of back ends for the given task or null if none was found
	 */
	public List<TaskBackend> getBackends(Limits limits, String taskId) {
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_TASKS_BACKENDS);
		sql.addSelectColumns(SQL_COLUMNS_TASK_BACKEND);
		sql.addWhereClause(new AndClause(COLUMN_TASK_ID, taskId, SQLType.STRING));
		sql.setLimits(limits);
		sql.addOrderBy(COLUMN_BACKEND_ID, OrderDirection.ASCENDING);
		
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql.toSQLString(service.tut.pori.backends.Definitions.ELEMENT_BACKEND_LIST), sql.getValues(), sql.getValueTypes());
		if(rows.isEmpty()){
			LOGGER.debug("No back ends for task, id: "+taskId);
			return null;
		}
		
		int size = rows.size();
		List<TaskBackend> backends = new ArrayList<>(size);
		long[] backendIds = new long[size];
		for(Map<String, Object> row : rows){
			TaskBackend backend = extractBackend(row);
			backends.add(backend);
			backendIds[--size] = backend.getBackendId();	//get id and put to array
		}
		BackendList list = _backendDAO.getBackends(backendIds, null, null);
		if(BackendList.isEmpty(list)){
			LOGGER.warn("Retrieved backend list was empty.");
			return null;
		}
		List<Backend> resolvedBackends = list.getBackends();
		for(Iterator<TaskBackend> iter = backends.iterator(); iter.hasNext();){
			TaskBackend taskBackend = iter.next();
			Long backendId = taskBackend.getBackendId();
			for(Backend backend : resolvedBackends){
				if(backendId.equals(backend.getBackendId())){
					taskBackend.setAnalysisUri(backend.getAnalysisUri());
					taskBackend.setDefaultTaskDataGroups(backend.getDefaultTaskDataGroups());
					taskBackend.setEnabled(backend.isEnabled());
					taskBackend = null;
					break;
				}
			}
			if(taskBackend != null){
				iter.remove();
			}
		}
		return backends;
	}
	
	/**
	 * 
	 * @param row
	 * @return the extracted back end or null if no back end was extracted
	 */
	private TaskBackend extractBackend(Map<String, Object> row) {
		TaskBackend end = new TaskBackend();
		for(Entry<String, Object> entry : row.entrySet()){
			switch(entry.getKey()){
				case COLUMN_BACKEND_ID:
					end.setBackendId((Long) entry.getValue());
					break;
				case COLUMN_STATUS:
					end.setStatus(Status.fromInt((int) entry.getValue()));
					break;
				case COLUMN_MESSAGE:
					end.setMessage((String) entry.getValue());
					break;
				case COLUMN_TASK_ID: // valid column, but no action needed
					break;
				default:
					if(checkCountColumn(entry.getKey(), entry.getValue()) < 1){	// this should be count(*)
						LOGGER.warn("Unknown column name, or no results, column: "+entry.getKey());
						return null;
					}
					break;
			}
		}
		return end;
	}
	
	/**
	 * remove the task and delete all associated information
	 * 
	 * @param taskId
	 */
	public void removeTask(String taskId) {
		getTransactionTemplate().execute(new TransactionCallback<Void>() {

			@Override
			public Void doInTransaction(TransactionStatus status) {
				removeBackends(taskId);
				removeTaskTypes(taskId);
				getJdbcTemplate().update(SQL_DELETE_TASK, new Object[]{taskId}, SQL_TASK_ID_SQL_TYPE);
				return null;
			}
		});
	}
	
	/**
	 * remove all back ends for the given task
	 * 
	 * @param taskId
	 */
	private void removeBackends(String taskId) {
		getJdbcTemplate().update(SQL_DELETE_TASK_BACKENDS, new Object[]{taskId}, SQL_TASK_ID_SQL_TYPE);
	}
	
	/**
	 * Remove the back end from all tasks
	 * 
	 * @param backendId
	 */
	protected void removeBackend(Long backendId) {
		getJdbcTemplate().update(SQL_DELETE_TASK_BACKEND, new Object[]{backendId}, SQL_DELETE_TASK_BACKEND_SQL_TYPES);
	}
	
	/**
	 * update the task status for the given task using the given status information
	 *  
	 * @param backend
	 * @param taskId
	 * @return true on success, false on failure (i.e. if the task does not exist or the back end is not associated with the task)
	 */
	public boolean statusUpdated(TaskBackend backend, String taskId) {
		if(getJdbcTemplate().update(SQL_UPDATE_TASK_BACKEND, new Object[]{backend.getStatus().toInt(), backend.getMessage(), taskId, backend.getBackendId()}, SQL_UPDATE_TASK_BACKEND_SQL_TYPES) != 1){ // this will force update the row_updated column, so it should always return 1 if the back end exists for the given task
			LOGGER.warn("The back end, id: "+backend.getBackendId()+" does not exist for task, id: "+taskId);
			return false;
		}
		if(!updateTaskTimestamp(taskId)){ // this may or may not be an error
			LOGGER.warn("Did not update time stamp for task, id: "+taskId+" on back end status update, back end id: "+backend.getBackendId());
		}
		return true;
	}
	
	/**
	 * convenience method for updating the main tasks table {@value SQLDAO#COLUMN_ROW_UPDATED} column.
	 * 
	 * @param taskId
	 * @return true if the timestamp was updated, false if not
	 */
	protected boolean updateTaskTimestamp(String taskId) {
		return (getJdbcTemplate().update(SQL_UPDATE_TASK_TIMESTAMP, new Object[]{taskId}, SQL_TASK_ID_SQL_TYPE) == 1);
	}
	
	/**
	 * insert the given back ends for the given task, if the back end already exist for the task, the back end details are updated.
	 * @param backends
	 * @param taskId
	 */
	private void insertBackends(Collection<TaskBackend> backends, String taskId) {
		if(backends == null || backends.isEmpty()){
			LOGGER.warn("No back ends for task, id: "+taskId);
			return;
		}
		JdbcTemplate t = getJdbcTemplate();
		Object[] ob = {taskId, null, null, null};
		for(TaskBackend tb : backends){
			ob[1] = tb.getBackendId();
			ob[2] = tb.getStatus().toInt();
			ob[3] = tb.getMessage();
			t.update(SQL_INSERT_TASK_BACKEND, ob, SQL_INSERT_TASK_BACKEND_SQL_TYPES);
		}
	}
	
	/**
	 * 
	 * @param taskId
	 * @return the dao for the given task id or null if none was found
	 * @throws IllegalArgumentException if the task was found but applicable DAO could not be resolved
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends TaskDAO> getDAOClass(String taskId) throws IllegalArgumentException {
		List<String> classes = getJdbcTemplate().queryForList(SQL_GET_DAO_CLASS, new Object[]{taskId}, SQL_TASK_ID_SQL_TYPE, String.class);
		if(classes.isEmpty()){
			LOGGER.debug("Task not found, id: "+taskId);
			return null;
		}
		try {
			return (Class<? extends TaskDAO>) Class.forName(classes.iterator().next());
		} catch (ClassNotFoundException ex) { // should not happen unless there is a database error (the database contains deprecated or old invalid)
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to resolve DAO for task, id: "+taskId);
		}
	}
	
	/**
	 * 
	 * @param userId
	 * @return list of task identifiers for the given user or null if none was found
	 */
	protected List<String> getTaskIds(UserIdentity userId) {
		List<String> taskIds = getJdbcTemplate().queryForList(SQL_GET_TASK_IDS_FOR_USER_ID, new Object[]{userId.getUserId()}, SQL_GET_TASK_IDS_FOR_USER_ID_SQL_TYPES, String.class);
		return (taskIds.isEmpty() ? null : taskIds);
	}
	
	/**
	 * Minimal implementation of the Task class
	 *
	 */
	private class TaskImpl extends Task {

		@Override
		public String getCallbackUri() {
			return null;
		}

		@Override
		public TaskDAO getTaskDao() {
			return ServiceInitializer.getDAOHandler().getDAO(TaskDAO.class);
		}

		@Override
		public JobBuilder getBuilder() {
			return BackendTaskJob.getBuilder(this);
		}
	} // class TaskImpl
}
