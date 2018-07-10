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
package service.tut.pori.backends;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import service.tut.pori.backends.datatypes.Backend;
import service.tut.pori.backends.datatypes.BackendGroup;
import service.tut.pori.backends.datatypes.BackendGroupList;
import service.tut.pori.backends.datatypes.BackendList;
import service.tut.pori.backends.datatypes.BackendUserIdentity;
import service.tut.pori.backends.datatypes.BackendUserIdentity.UserPermission;
import core.tut.pori.dao.SQLDAO;
import core.tut.pori.dao.SQLDeleteBuilder;
import core.tut.pori.dao.SQLSelectBuilder;
import core.tut.pori.dao.SQLSelectBuilder.OrderDirection;
import core.tut.pori.dao.clause.AndClause;
import core.tut.pori.dao.clause.JoinClause;
import core.tut.pori.dao.clause.SQLClause.SQLType;
import core.tut.pori.dao.clause.WhereClause;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.ListUtils;

/**
 * dao for handling back ends
 * 
 */
public class BackendDAO extends SQLDAO {
	private static final Logger LOGGER = Logger.getLogger(BackendDAO.class);
	/* table names */
	private static final String TABLE_BACKEND_GROUPS = DATABASE+".backend_groups";
	private static final String TABLE_BACKEND_GROUPS_BACKENDS = DATABASE+".backend_groups_backends";
	private static final String TABLE_BACKEND_GROUPS_PERMISSIONS = DATABASE+".backend_groups_permissions";
	private static final String TABLE_BACKENDS = DATABASE+".backends";
	private static final String TABLE_BACKENDS_CAPABILITIES = DATABASE+".backends_capabilities";
	/* columns */
	private static final String COLUMN_ANALYSIS_URI = "analysis_uri";
	private static final String COLUMN_BACKEND_ID = "backend_id";
	private static final String COLUMN_BACKEND_GROUP_ID = "backend_group_id";
	private static final String COLUMN_BACKEND_GROUP_ID_WITH_TABLE_NAME = TABLE_BACKEND_GROUPS+"."+COLUMN_BACKEND_GROUP_ID;
	private static final String COLUMN_CAPABILITY = "capability";
	private static final String COLUMN_DESCRIPTION = "description";
	private static final String COLUMN_DEFAULT_TASK_DATAGROUPS = "default_task_datagroups";
	private static final String COLUMN_ENABLED = "enabled";
	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_PERMISSION = "permission";
	private static final String COLUMN_READ_PUBLIC = "read_public";
	private static final String COLUMN_TASK_PUBLIC = "task_public";
	private static final String COLUMN_USER_ID_WITH_TABLE_NAME = TABLE_BACKEND_GROUPS_PERMISSIONS+"."+COLUMN_USER_ID;
	/* sql */
	private static final String SQL_ALIAS_TASK_PUBLIC_SUM = COLUMN_TASK_PUBLIC+"_sum";
	private static final String SQL_ALIAS_READ_PUBLIC_SUM = COLUMN_READ_PUBLIC+"_sum";
	
	private static final WhereClause SQL_AND_PUBLIC_READ = new AndClause(TABLE_BACKEND_GROUPS+"."+COLUMN_READ_PUBLIC, UserPermission.READ_BACKENDS.toInt(), SQLType.INTEGER);
	
	private static final String SQL_ASSOCIATE_BACKEND = "INSERT INTO "+TABLE_BACKEND_GROUPS_BACKENDS+" ("+COLUMN_BACKEND_GROUP_ID+", "+COLUMN_BACKEND_ID+", "+COLUMN_ROW_CREATED+") VALUES (?,?,NOW())";
	private static final int[] SQL_ASSOCIATE_BACKEND_SQL_TYPES = {SQLType.LONG.toInt(), SQLType.LONG.toInt()};
	
	private static final String SQL_ASSOCIATE_UPDATE_BACKEND = "INSERT INTO "+TABLE_BACKEND_GROUPS_BACKENDS+" ("+COLUMN_BACKEND_GROUP_ID+", "+COLUMN_BACKEND_ID+", "+COLUMN_ROW_CREATED+") VALUES (?,?,NOW()) ON DUPLICATE KEY UPDATE "+COLUMN_ROW_UPDATED+"=NOW()";
	
	private static final String[] SQL_COLUMNS_BACKEND = {COLUMN_NAME, COLUMN_BACKEND_ID, COLUMN_ANALYSIS_URI, COLUMN_ENABLED, COLUMN_DESCRIPTION, COLUMN_DEFAULT_TASK_DATAGROUPS};
	private static final String[] SQL_COLUMNS_CAPABILITIES = {COLUMN_BACKEND_ID, COLUMN_CAPABILITY};
	private static final String[] SQL_COLUMNS_CREATE_BACKEND = {COLUMN_NAME, COLUMN_ANALYSIS_URI, COLUMN_ENABLED, COLUMN_DESCRIPTION, COLUMN_DEFAULT_TASK_DATAGROUPS, COLUMN_ROW_CREATED};
	private static final String[] SQL_COLUMNS_CREATE_BACKEND_GROUP = {COLUMN_NAME, COLUMN_DESCRIPTION, COLUMN_READ_PUBLIC, COLUMN_TASK_PUBLIC, COLUMN_ROW_CREATED};
	private static final String[] SQL_COLUMNS_GET_BACKEND = {TABLE_BACKENDS+"."+COLUMN_BACKEND_ID, TABLE_BACKENDS+"."+COLUMN_NAME, TABLE_BACKENDS+"."+COLUMN_ANALYSIS_URI, TABLE_BACKENDS+"."+COLUMN_ENABLED, TABLE_BACKENDS+"."+COLUMN_DESCRIPTION, TABLE_BACKENDS+"."+COLUMN_DEFAULT_TASK_DATAGROUPS};
	private static final String[] SQL_COLUMNS_GET_BACKEND_GROUP = {TABLE_BACKEND_GROUPS+"."+COLUMN_NAME, TABLE_BACKEND_GROUPS+"."+COLUMN_DESCRIPTION, TABLE_BACKEND_GROUPS+"."+COLUMN_READ_PUBLIC, TABLE_BACKEND_GROUPS+"."+COLUMN_TASK_PUBLIC};
	private static final String[] SQL_COLUMNS_GET_PERMISSIONS = {COLUMN_USER_ID, COLUMN_PERMISSION};
	private static final String[] SQL_COLUMNS_PUBLIC = {COLUMN_COUNT, "SUM("+COLUMN_TASK_PUBLIC+") AS "+SQL_ALIAS_TASK_PUBLIC_SUM, "SUM("+COLUMN_READ_PUBLIC+") AS "+SQL_ALIAS_READ_PUBLIC_SUM};
	
	private static final String SQL_BACKEND_GROUP_EXISTS = "SELECT "+COLUMN_COUNT+" FROM "+TABLE_BACKEND_GROUPS+" WHERE "+COLUMN_BACKEND_GROUP_ID+"=?";
	private static final int[] SQL_BACKEND_GROUP_EXISTS_SQL_TYPES = {SQLType.LONG.toInt()};
	
	private static final int[] SQL_BACKEND_ID_SQL_TYPES = {SQLType.LONG.toInt()};
	private static final int[] SQL_BACKEND_GROUP_ID_SQL_TYPES = {SQLType.LONG.toInt()};
	
	private static final String SQL_DELETE_CAPABILITIES = "DELETE FROM "+TABLE_BACKENDS_CAPABILITIES+" WHERE "+COLUMN_BACKEND_ID+"=?";
	
	private static final String SQL_DELETE_BACKEND = "DELETE FROM "+TABLE_BACKENDS+" WHERE "+COLUMN_BACKEND_ID+"=?";
	
	private static final String SQL_DELETE_BACKEND_GROUP_PERMISSIONS = "DELETE FROM "+TABLE_BACKEND_GROUPS_PERMISSIONS+" WHERE "+COLUMN_BACKEND_GROUP_ID+"=?";
	
	private static final String SQL_DELETE_BACKEND_FROM_GROUPS = "DELETE FROM "+TABLE_BACKEND_GROUPS_BACKENDS+" WHERE "+COLUMN_BACKEND_ID+"=?";
	
	private static final String SQL_DELETE_BACKEND_GROUP = "DELETE FROM "+TABLE_BACKEND_GROUPS+" WHERE "+COLUMN_BACKEND_GROUP_ID+"=?";
	
	private static final String SQL_GET_BACKEND_CAPABILITIES = "SELECT "+COLUMN_CAPABILITY+" FROM "+TABLE_BACKENDS_CAPABILITIES+" WHERE "+COLUMN_BACKEND_ID+"=?";
	
	private static final String SQL_GET_BACKEND_GROUP_IDS = "SELECT "+COLUMN_BACKEND_GROUP_ID+" FROM "+TABLE_BACKEND_GROUPS_BACKENDS+" WHERE "+COLUMN_BACKEND_ID+"=?";
	
	private static final String SQL_GET_BACKEND_IDS = "SELECT "+COLUMN_BACKEND_ID+" FROM "+TABLE_BACKEND_GROUPS_BACKENDS+" WHERE "+COLUMN_BACKEND_GROUP_ID+"=?";
	
	private static final String SQL_GET_BACKEND_GROUP_PERMISSIONS = "SELECT "+COLUMN_COUNT+", "+COLUMN_READ_PUBLIC+", "+COLUMN_TASK_PUBLIC+" FROM "+TABLE_BACKEND_GROUPS+" WHERE "+COLUMN_BACKEND_GROUP_ID+"=?";
	
	private static final String SQL_GET_BACKEND_GROUP_USER_PERMISSIONS_FOR_USER = "SELECT "+COLUMN_PERMISSION+" FROM "+TABLE_BACKEND_GROUPS_PERMISSIONS+" WHERE "+COLUMN_BACKEND_GROUP_ID+"=? AND "+COLUMN_USER_ID+"=?";
	private static final int[] SQL_GET_BACKEND_GROUP_USER_PERMISSIONS_FOR_USER_SQL_TYPES = {SQLType.LONG.toInt(), SQLType.LONG.toInt()};
	
	private static final JoinClause SQL_JOIN_BACK_END_GROUPS = new JoinClause("INNER JOIN "+TABLE_BACKEND_GROUPS_BACKENDS+" ON "+TABLE_BACKEND_GROUPS_BACKENDS+"."+COLUMN_BACKEND_ID+"="+TABLE_BACKENDS+"."+COLUMN_BACKEND_ID);
	private static final JoinClause SQL_JOIN_BACK_END_PERMISSIONS = new JoinClause("INNER JOIN "+TABLE_BACKEND_GROUPS_PERMISSIONS+" ON "+TABLE_BACKEND_GROUPS_PERMISSIONS+"."+COLUMN_BACKEND_GROUP_ID+"="+TABLE_BACKEND_GROUPS_BACKENDS+"."+COLUMN_BACKEND_GROUP_ID);
	
	private static final String SQL_INSERT_BACKEND_GROUP_PERMISSION = "INSERT INTO "+TABLE_BACKEND_GROUPS_PERMISSIONS+" ("+COLUMN_BACKEND_GROUP_ID+", "+COLUMN_USER_ID+", "+COLUMN_PERMISSION+", "+COLUMN_ROW_CREATED+") VALUES (?,?,?,NOW())";
	private static final int[] SQL_INSERT_BACKEND_GROUP_PERMISSION_SQL_TYPES = {SQLType.LONG.toInt(), SQLType.LONG.toInt(), SQLType.INTEGER.toInt()};
	
	private static final String SQL_INSERT_CAPABILITIES = "INSERT INTO "+TABLE_BACKENDS_CAPABILITIES+" ("+COLUMN_BACKEND_ID+", "+COLUMN_CAPABILITY+", "+COLUMN_ROW_CREATED+") VALUES (?, ?, NOW())";
	private static final int[] SQL_INSERT_CAPABILITIES_SQL_TYPES = {SQLType.LONG.toInt(), SQLType.STRING.toInt()};
	
	private static final String SQL_SELECT_ASSOCIATED_BACK_ENDS = "SELECT t1."+COLUMN_BACKEND_ID+" from "+TABLE_BACKEND_GROUPS_BACKENDS+" t1 INNER JOIN "+TABLE_BACKEND_GROUPS_BACKENDS+" t2 ON t1."+COLUMN_BACKEND_ID+"=t2."+COLUMN_BACKEND_ID+" WHERE t2."+COLUMN_BACKEND_GROUP_ID+"=?";
	
	private static final String SQL_UPDATE_BACKEND = "UPDATE "+TABLE_BACKENDS+" SET "+COLUMN_NAME+"=?, "+COLUMN_ANALYSIS_URI+"=?, "+COLUMN_ENABLED+"=?, "+COLUMN_DESCRIPTION+"=?, "+COLUMN_DEFAULT_TASK_DATAGROUPS+"=?, "+COLUMN_ROW_UPDATED+"=NOW() WHERE "+COLUMN_BACKEND_ID+"=?";
	private static final int[] SQL_UPDATE_BACKEND_SQL_TYPES = {SQLType.STRING.toInt(), SQLType.STRING.toInt(), SQLType.INTEGER.toInt(), SQLType.STRING.toInt(), SQLType.STRING.toInt(), SQLType.LONG.toInt()};
	
	private static final String SQL_UPDATE_BACKEND_GROUP = "UPDATE "+TABLE_BACKEND_GROUPS+" SET "+COLUMN_NAME+"=?, "+COLUMN_DESCRIPTION+"=?, "+COLUMN_READ_PUBLIC+"=?, "+COLUMN_TASK_PUBLIC+"=?, "+COLUMN_ROW_UPDATED+"=NOW() WHERE "+COLUMN_BACKEND_GROUP_ID+"=?";
	private static final int[] SQL_UPDATE_BACKEND_GROUP_SQL_TYPES = {SQLType.STRING.toInt(), SQLType.STRING.toInt(), SQLType.INTEGER.toInt(), SQLType.INTEGER.toInt(), SQLType.LONG.toInt()};
					
	
	/**
	 * create the given back end, the generated identifier will be set in the passed back end object, 
	 * and is also returned by the method.
	 * 
	 * The existence of the given back end group id is verified, no permission checks are performed. Back end id for the back end (if given) is ignored.
	 * 
	 * @param backend
	 * @param backendGroupId associate the back end to this group
	 * @return id for the generated back end or null if creation failed
	 */
	public Long createBackend(Backend backend, Long backendGroupId) {
		return getTransactionTemplate().execute(new TransactionCallback<Long>() {

			@Override
			public Long doInTransaction(TransactionStatus status) {
				JdbcTemplate t = getJdbcTemplate();
				
				if(t.queryForObject(SQL_BACKEND_GROUP_EXISTS, new Object[]{backendGroupId}, SQL_BACKEND_GROUP_EXISTS_SQL_TYPES, Long.class) < 1){
					LOGGER.warn("Failed to add back end for non-existing group, id: "+backendGroupId);
					return null;
				}
				
				SimpleJdbcInsert sql = new SimpleJdbcInsert(t);
				sql.withTableName(TABLE_BACKENDS);
				sql.setGeneratedKeyName(COLUMN_BACKEND_ID);
				sql.usingColumns(SQL_COLUMNS_CREATE_BACKEND);
				sql.withoutTableColumnMetaDataAccess();
				
				Map<String, Object> params = new HashMap<>(SQL_COLUMNS_CREATE_BACKEND.length);
				params.put(COLUMN_NAME, backend.getName());
				params.put(COLUMN_ANALYSIS_URI, backend.getAnalysisUri());
				params.put(COLUMN_ENABLED, BooleanUtils.toIntegerObject(backend.isEnabled()));
				params.put(COLUMN_DESCRIPTION, backend.getDescription());
				params.put(COLUMN_DEFAULT_TASK_DATAGROUPS, backend.getDefaultTaskDataGroups().toDataGroupString());
				params.put(COLUMN_ROW_UPDATED, null);
				
				Long backendId = sql.executeAndReturnKey(params).longValue();
								
				Set<String> capabilities = backend.getCapabilities();
				if(capabilities != null && !capabilities.isEmpty()){
					setBackendCapabilities(backendId, capabilities);
				}else{
					LOGGER.debug("No capabilities given for new back end, id: "+backendId);
				}
				
				t.update(SQL_ASSOCIATE_BACKEND, new Object[]{backendGroupId, backendId}, SQL_ASSOCIATE_BACKEND_SQL_TYPES);
				
				backend.setBackendId(backendId);
				return backendId;
			}
		});
	}
	
	/**
	 * 
	 * @param backend
	 * @return true on success
	 */
	public boolean updateBackend(Backend backend) {
		return getTransactionTemplate().execute(new TransactionCallback<Boolean>() {

			@Override
			public Boolean doInTransaction(TransactionStatus status) {
				Long backendId = backend.getBackendId();
				if(getJdbcTemplate().update(SQL_UPDATE_BACKEND, new Object[]{backend.getName(), backend.getAnalysisUri(), BooleanUtils.toIntegerObject(backend.isEnabled()), backend.getDescription(), backend.getDefaultTaskDataGroups().toDataGroupString(), backendId}, SQL_UPDATE_BACKEND_SQL_TYPES) < 1){ // at least updated column should update
					LOGGER.warn("Nothing updated for back end, id: "+backendId);
					return Boolean.FALSE;
				}

				setBackendCapabilities(backendId, backend.getCapabilities());
				return Boolean.TRUE;
			}
		});
	}
	
	/**
	 * 
	 * @param backendId
	 * @param capabilities
	 */
	public void setBackendCapabilities(Long backendId, Set<String> capabilities) {
		getTransactionTemplate().execute(new TransactionCallback<Void>() {

			@Override
			public Void doInTransaction(TransactionStatus status) {
				removeCapabilities(backendId);
				
				if(capabilities != null && !capabilities.isEmpty()){
					JdbcTemplate t = getJdbcTemplate();
					Object[] ob = {backendId, null};
					for(String capability : capabilities){
						ob[1] = capability;
						t.update(SQL_INSERT_CAPABILITIES, ob, SQL_INSERT_CAPABILITIES_SQL_TYPES);
					}
				}else{
					LOGGER.warn("No capabilities for back end, id: "+backendId);
				}
				
				return null;
			}
		});
	}
	
	/**
	 * remove all capabilities for the given back end
	 * 
	 * @param backendId
	 */
	private void removeCapabilities(Long backendId) {
		getJdbcTemplate().update(SQL_DELETE_CAPABILITIES, new Object[]{backendId}, SQL_BACKEND_ID_SQL_TYPES);
	}
	
	/**
	 * resolve and set capabilities for the back end
	 * 
	 * @param backend
	 */
	private void resolveCapabilities(Backend backend){
		Set<String> capabilities = new HashSet<>();
		getJdbcTemplate().query(SQL_GET_BACKEND_CAPABILITIES, new Object[]{backend.getBackendId()}, SQL_BACKEND_ID_SQL_TYPES, new RowCallbackHandler() {
			
			@Override
			public void processRow(ResultSet set) throws SQLException {
				capabilities.add(set.getString(COLUMN_CAPABILITY));
			}
		});
		
		if(capabilities.isEmpty()){
			LOGGER.debug("No capabilities for back end, id: "+backend.getBackendId());
		}else{
			backend.setCapabilities(capabilities);
		}
	}
	
	/**
	 * 
	 * @param capabilities
	 * @return list of back end which have <b>all</b> of the given capabilities
	 */
	public List<Backend> getBackends(Set<String> capabilities) {
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_BACKENDS_CAPABILITIES);
		sql.addSelectColumns(SQL_COLUMNS_CAPABILITIES); // we need to resolve the capabilities anyway, so we can just as well retrieve the capabilities now
		sql.addWhereClause(new AndClause(COLUMN_CAPABILITY, capabilities, SQLType.STRING));
		
		JdbcTemplate t = getJdbcTemplate();
		List<Map<String, Object>> rows = t.queryForList(sql.toSQLString(), sql.getValues(), sql.getValueTypes());
		if(rows.isEmpty()){
			LOGGER.debug("No back ends matching the given capabilities found.");
			return null;
		}
		
		Map<Long, Set<String>> backendCaps = new HashMap<>();
		for(Map<String, Object> row : rows){
			Long backendId = (Long) row.get(COLUMN_BACKEND_ID);
			Set<String> caps = backendCaps.get(backendId);
			if(caps == null){
				caps = new HashSet<>();
				backendCaps.put(backendId, caps);
			}
			caps.add((String) row.get(COLUMN_CAPABILITY));
		}
		
		sql = new SQLSelectBuilder(TABLE_BACKENDS);
		sql.addSelectColumns(SQL_COLUMNS_BACKEND);
		sql.addWhereClause(new AndClause(COLUMN_BACKEND_ID, backendCaps.keySet(), SQLType.LONG));
		rows = t.queryForList(sql.toSQLString(), sql.getValues(), sql.getValueTypes());
		if(rows.isEmpty()){
			LOGGER.warn("Capabilities found, but no back end details: database mismatch?");
			return null;
		}
		
		List<Backend> backends = new ArrayList<>(backendCaps.size());
		for(Map<String, Object> row : rows){
			Backend backend = extractBackend(row, false);
			backend.setCapabilities(backendCaps.get(backend.getBackendId()));
			backends.add(backend);
		}
		
		return backends;
	}
	
	/**
	 * 
	 * @param backendId
	 * @return true if the back end was deleted, false if not (back end did not exist)
	 */
	public boolean removeBackend(Long backendId) {
		removeCapabilities(backendId);
		JdbcTemplate t = getJdbcTemplate();
		Object[] ob = new Object[]{backendId};
		t.update(SQL_DELETE_BACKEND_FROM_GROUPS, ob, SQL_BACKEND_ID_SQL_TYPES);
		return (t.update(SQL_DELETE_BACKEND, ob, SQL_BACKEND_ID_SQL_TYPES) > 0);
	}
	
	/**
	 * 
	 * @param row
	 * @param resolveCapabilities if true, capabilities will be resolved automatically. If true, column @value{service.tut.pori.backends.BackendDAO.COLUMN_BACKEND_ID} must be present for this to work.
	 * @return the extracted back end
	 */
	private Backend extractBackend(Map<String, Object> row, boolean resolveCapabilities) {
		Backend backend = new Backend();
		for(Entry<String, Object> entry : row.entrySet()){
			switch(entry.getKey()){
				case COLUMN_NAME:
					backend.setName((String) entry.getValue());
					break;
				case COLUMN_BACKEND_ID:
					backend.setBackendId((Long) entry.getValue());
					break;
				case COLUMN_ANALYSIS_URI:
					backend.setAnalysisUri((String) entry.getValue());
					break;
				case COLUMN_ENABLED:
					backend.setEnabled(BooleanUtils.toBooleanObject((Integer) entry.getValue()));
					break;
				case COLUMN_DESCRIPTION:
					backend.setDescription((String) entry.getValue());
					break;
				case COLUMN_DEFAULT_TASK_DATAGROUPS:
					backend.setDefaultTaskDataGroups(DataGroups.fromDataGroupString((String) entry.getValue()));
					break;
				default:
					if(checkCountColumn(entry.getKey(), entry.getValue()) < 1){	// this should be count(*)
						LOGGER.warn("Unknown column name, or no results, column: "+entry.getKey());
						return null;
					}
					break;
			}
		}
		if(resolveCapabilities){
			resolveCapabilities(backend);
		}
		return backend;
	}
	
	/**
	 * 
	 * @param backendId
	 * @param userId
	 * @return the permission the user has for the given back end or null if none
	 */
	public Set<UserPermission> getBackendPermissions(Long backendId, UserIdentity userId) {
		JdbcTemplate t = getJdbcTemplate();
		List<Long> backendGroupIds = t.queryForList(SQL_GET_BACKEND_GROUP_IDS, new Object[]{backendId}, SQL_BACKEND_ID_SQL_TYPES, Long.class);
		if(backendGroupIds.isEmpty()){
			LOGGER.debug("No groups for back end, id: "+backendId); // back end must always belong to a group, so the given back end id does not exists
			return null;
		}
		
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_BACKEND_GROUPS);
		sql.addSelectColumns(SQL_COLUMNS_PUBLIC);
		AndClause begIdClause = new AndClause(COLUMN_BACKEND_GROUP_ID, backendGroupIds, SQLType.LONG);
		sql.addWhereClause(begIdClause);
		Map<String, Object> row = t.queryForMap(sql.toSQLString(), sql.getValues(), sql.getValueTypes());
		
		Set<UserPermission> permissions = new HashSet<>();
		for(Entry<String, Object> e : row.entrySet()){
			switch(e.getKey()) {
				case COLUMN_COUNT:
					if((Long)e.getValue() < 1){ // in principle, this should not happen, but check it anyway
						LOGGER.warn("No groups found.");
						return null;
					}
					break;
				case SQL_ALIAS_READ_PUBLIC_SUM:
					if(((BigDecimal)e.getValue()).intValue() > 0){ // if there are more than one read public permission, then there are permissions for the user
						permissions.add(UserPermission.READ_BACKENDS);
					}
					break;
				case SQL_ALIAS_TASK_PUBLIC_SUM:
					if(((BigDecimal)e.getValue()).intValue() > 0){ // if there are more than one task public permission, then there are permissions for the user
						permissions.add(UserPermission.TASKS);
					}
					break;
				default:
					LOGGER.warn("Unhandled column: "+e.getKey()); // should never happen
					break;
			}
		}
		
		sql = new SQLSelectBuilder(TABLE_BACKEND_GROUPS_PERMISSIONS);
		sql.addSelectColumn(COLUMN_PERMISSION);
		sql.addWhereClause(begIdClause);
		sql.addWhereClause(new AndClause(COLUMN_USER_ID, userId.getUserId(), SQLType.LONG));
		sql.addGroupBy(COLUMN_PERMISSION);
		
		List<Integer> permInts = t.queryForList(sql.toSQLString(), sql.getValues(), sql.getValueTypes(), Integer.class);
		for(Integer permission : permInts){
			permissions.add(UserPermission.fromInt(permission));
		}
		
		return (permissions.isEmpty() ? null : permissions);
	}
	
	/**
	 * 
	 * @param backendGroupId
	 * @param userId
	 * @return the permission the user has for the given back end or null if none
	 */
	public Set<UserPermission> getBackendGroupPermissions(Long backendGroupId, UserIdentity userId) {
		JdbcTemplate t = getJdbcTemplate();
		Map<String, Object> row = t.queryForMap(SQL_GET_BACKEND_GROUP_PERMISSIONS, new Object[]{backendGroupId}, SQL_BACKEND_GROUP_ID_SQL_TYPES);
		Set<UserPermission> permissions = new HashSet<>();
		for(Entry<String, Object> e : row.entrySet()){
			switch(e.getKey()){
				case COLUMN_COUNT:
					if((Long) e.getValue() < 1){ // in principle, this should not happen, but check it anyway
						LOGGER.debug("Back end group not found, id: "+backendGroupId);
						return null;
					}
					break;
				case COLUMN_READ_PUBLIC:
					if(BooleanUtils.toBoolean((int) e.getValue())){
						permissions.add(UserPermission.READ_BACKENDS);
					}
					break;
				case COLUMN_TASK_PUBLIC:
					if(BooleanUtils.toBoolean((int) e.getValue())){
						permissions.add(UserPermission.TASKS);
					}
					break;
				default:
					LOGGER.warn("Unhandeled column: "+e.getKey()); // should never happen
					break;
			}
		}
		
		List<Integer> permInts = t.queryForList(SQL_GET_BACKEND_GROUP_USER_PERMISSIONS_FOR_USER, new Object[]{backendGroupId, userId.getUserId()}, SQL_GET_BACKEND_GROUP_USER_PERMISSIONS_FOR_USER_SQL_TYPES, Integer.class);
		for(Integer permission : permInts){
			permissions.add(UserPermission.fromInt(permission));
		}

		return (permissions.isEmpty() ? null : permissions);
	}

	/**
	 * Note: this will NOT perform permission checks.
	 * 
	 * Note: if the group has back ends, the back ends are IGNORED and not added.
	 * 
	 * @param backendGroup
	 * @return the identifier for the created back end group or null on failure
	 * @throws IllegalArgumentException on a bad value
	 */
	public Long createBackendGroup(BackendGroup backendGroup) throws IllegalArgumentException {
		return getTransactionTemplate().execute(new TransactionCallback<Long>() {

			@Override
			public Long doInTransaction(TransactionStatus status) {
				JdbcTemplate t = getJdbcTemplate();
				SimpleJdbcInsert sql = new SimpleJdbcInsert(t);
				sql.withTableName(TABLE_BACKEND_GROUPS);
				sql.setGeneratedKeyName(COLUMN_BACKEND_GROUP_ID);
				sql.usingColumns(SQL_COLUMNS_CREATE_BACKEND_GROUP);
				sql.withoutTableColumnMetaDataAccess();
				
				Map<String, Object> params = new HashMap<>(SQL_COLUMNS_CREATE_BACKEND_GROUP.length);
				params.put(COLUMN_NAME, backendGroup.getName());
				params.put(COLUMN_DESCRIPTION, backendGroup.getDescription());
				Set<UserPermission> permissions = backendGroup.getGroupPermissions();
				if(permissions != null){
					if(permissions.contains(UserPermission.READ_BACKENDS)){
						params.put(COLUMN_READ_PUBLIC, 1);
					}else{
						params.put(COLUMN_READ_PUBLIC, 0);
					}
					if(permissions.contains(UserPermission.TASKS)){
						params.put(COLUMN_TASK_PUBLIC, 1);
					}else{
						params.put(COLUMN_TASK_PUBLIC, 0);
					}
				}else{
					params.put(COLUMN_READ_PUBLIC, 0);
					params.put(COLUMN_TASK_PUBLIC, 0);
				}
				params.put(COLUMN_ROW_UPDATED, null);
				
				Long backendGroupId = sql.executeAndReturnKey(params).longValue();
								
				setBackendGroupPermissions(backendGroupId, backendGroup.getUsers());
				
				backendGroup.setBackendGroupId(backendGroupId);
				return backendGroupId;
			}
		});
	}
	
	/**
	 * Warning: this does NOT validate the given users/permissions, thus, it is possible to create invalid or unusable groups with this method.
	 * 
	 * @param backendGroupId
	 * @param users
	 */
	protected void setBackendGroupPermissions(Long backendGroupId, List<BackendUserIdentity> users) {
		getTransactionTemplate().execute(new TransactionCallback<Void>() {

			@Override
			public Void doInTransaction(TransactionStatus status) {
				removeBackendGroupPermissions(backendGroupId);
				
				if(users == null || users.isEmpty()){
					LOGGER.warn("No permissions for back end group, id: "+backendGroupId);
					return null;
				}
				JdbcTemplate t = getJdbcTemplate();
				Object[] ob = {backendGroupId, null, null};
				for(BackendUserIdentity user : users){
					ob[1] = user.getUserId();
					for(UserPermission permission : user.getPermissions()){
						ob[2] = permission.toInt();
						t.update(SQL_INSERT_BACKEND_GROUP_PERMISSION, ob, SQL_INSERT_BACKEND_GROUP_PERMISSION_SQL_TYPES);
					}
				}
				
				return null;
			}
		});
	}
	
	/**
	 * remove all backend group permissions for the back end
	 * 
	 * @param backendGroupId
	 */
	private void removeBackendGroupPermissions(Long backendGroupId) {
		getJdbcTemplate().update(SQL_DELETE_BACKEND_GROUP_PERMISSIONS, new Object[]{backendGroupId}, SQL_BACKEND_GROUP_ID_SQL_TYPES);
	}

	/**
	 * Note: this can NOT be used to update associated back ends. Back end list, if given, is IGNORED.
	 * 
	 * @param backendGroup
	 * @return true on success
	 */
	public boolean updateBackendGroup(BackendGroup backendGroup) {
		return getTransactionTemplate().execute(new TransactionCallback<Boolean>() {

			@Override
			public Boolean doInTransaction(TransactionStatus status) {
				Long backendGroupId = backendGroup.getBackendGroupId();
				Set<UserPermission> permissions = backendGroup.getGroupPermissions();
				if(getJdbcTemplate().update(SQL_UPDATE_BACKEND_GROUP, new Object[]{backendGroup.getName(), backendGroup.getDescription(), BooleanUtils.toIntegerObject((permissions != null && permissions.contains(UserPermission.READ_BACKENDS))), BooleanUtils.toIntegerObject((permissions != null && permissions.contains(UserPermission.TASKS))), backendGroupId}, SQL_UPDATE_BACKEND_GROUP_SQL_TYPES) < 1){ // at least updated column should update
					LOGGER.warn("Nothing updated for back end group, id: "+backendGroupId);
					return Boolean.FALSE;
				}

				setBackendGroupPermissions(backendGroupId, backendGroup.getUsers());
				return Boolean.TRUE;
			}
		});
	}

	/**
	 * 
	 * @param backendGroupId
	 * @return true if the back end group was removed, false if not (group did not exist)
	 */
	public boolean removeBackendGroup(long backendGroupId) {
		return getTransactionTemplate().execute(new TransactionCallback<Boolean>() {

			@Override
			public Boolean doInTransaction(TransactionStatus status) {
				JdbcTemplate t = getJdbcTemplate();
				Object[] ob = new Object[]{backendGroupId};
				List<Long> backendIds = t.queryForList(SQL_SELECT_ASSOCIATED_BACK_ENDS, ob, SQL_BACKEND_GROUP_ID_SQL_TYPES, Long.class); // resolve back end ids of this group and other groups the back ends are part of
				if(!backendIds.isEmpty()){
					Map<Long, MutableInt> counts = new HashMap<>(); // count the number of back end groups for each back end id to figure out back ends, which would be left without a group after the group deletion
					for(Long backendId : backendIds){
						MutableInt count = counts.get(backendId);
						if(count == null){
							counts.put(backendId, new MutableInt(1));
						}else{
							count.increment();
						}
					}
					
					long[] backendId = new long[1];
					for(Entry<Long, MutableInt> e : counts.entrySet()){
						backendId[0] = e.getKey();
						if(e.getValue().intValue() > 1){ // if the back end has other groups, simply remove the association with this group
							unassociate(new long[]{backendGroupId}, backendId);
						}else{ // this is the back end's only group, delete the back end
							LOGGER.debug("Back end, id: "+backendId[0]+" is only member of the back end group, id: "+backendGroupId+". Removing back end...");
							BackendsCore.deleteBackend(backendId);
						}
					}
				}
				
				removeBackendGroupPermissions(backendGroupId);
				
				if(t.update(SQL_DELETE_BACKEND_GROUP, ob, SQL_BACKEND_GROUP_ID_SQL_TYPES) > 0) {
					return Boolean.TRUE;
				}else {
					return Boolean.FALSE;
				}
			}
		});
	}
	
	/**
	 * remove association between the given groups and back ends, non-existing identifiers are ignored
	 * 
	 * @param backendGroupIds
	 * @param backendIds
	 */
	public void unassociate(long[] backendGroupIds, long[] backendIds){
		SQLDeleteBuilder sql = new SQLDeleteBuilder(TABLE_BACKEND_GROUPS_BACKENDS);
		sql.addWhereClause(new AndClause(COLUMN_BACKEND_GROUP_ID, backendGroupIds));
		sql.addWhereClause(new AndClause(COLUMN_BACKEND_ID, backendIds));
		getJdbcTemplate().update(sql.toSQLString(), sql.getValues(), sql.getValueTypes());
	}
	
	/**
	 * 
	 * associate the given groups with the given back ends, if association already exists, only timestamp is updated
	 * 
	 * @param backendGroupIds
	 * @param backendIds
	 */
	public void associate(long[] backendGroupIds, long[] backendIds){
		JdbcTemplate t = getJdbcTemplate();
		Object[] ob = new Object[2];
		for(long backendGroupId : backendGroupIds){
			ob[0] = backendGroupId;
			for(long backendId : backendIds){
				ob[1] = backendId;
				t.update(SQL_ASSOCIATE_UPDATE_BACKEND, ob, SQL_ASSOCIATE_BACKEND_SQL_TYPES);
			}
		}
	}

	/**
	 * Note: this will <i>not</i> perform permission checks.
	 * 
	 * Data groups valid for this method:
	 * <ul>
	 * <li>{@value Definitions#DATA_GROUP_PUBLIC}, only search the groups that are publicly available. Note: this is <i>not</i> part of {@value DataGroups#DATA_GROUP_ALL}</li>
	 * <li>{@value DataGroups#DATA_GROUP_BASIC}, add only basic details, such as {@value Definitions#ELEMENT_NAME}, {@value Definitions#ELEMENT_DESCRIPTION} and global permission list ({@value Definitions#ELEMENT_PERMISSION_LIST})</li>
	 * <li>{@value Definitions#DATA_GROUP_USERS}, add users</li>
	 * <li>{@value Definitions#DATA_GROUP_BACKENDS}, add back ends</li>
	 * <li>{@value DataGroups#DATA_GROUP_ALL}, all of the above except {@value Definitions#DATA_GROUP_PUBLIC}</li>
	 * <li></li>
	 * </ul>
	 * 
	 * The default data group is {@value DataGroups#DATA_GROUP_ALL}.
	 * 
	 * @param backendGroupIds optional filter
	 * @param dataGroups optional filter
	 * @param limits optional filter
	 * @param userIds optional filter
	 * @return list of back end groups or null if none was found
	 */
	public BackendGroupList getBackendGroups(long[] backendGroupIds, DataGroups dataGroups, Limits limits, long[] userIds) {
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_BACKEND_GROUPS);
		sql.addOrderBy(COLUMN_BACKEND_GROUP_ID_WITH_TABLE_NAME, OrderDirection.DESCENDING);
		sql.addSelectColumn(COLUMN_BACKEND_GROUP_ID_WITH_TABLE_NAME);
		sql.setLimits(limits);
		
		boolean allGroups = (DataGroups.isEmpty(dataGroups) || DataGroups.hasDataGroup(DataGroups.DATA_GROUP_ALL, dataGroups) || DataGroups.hasDataGroup(DataGroups.DATA_GROUP_DEFAULTS, dataGroups));
		if(allGroups || DataGroups.hasDataGroup(DataGroups.DATA_GROUP_BASIC, dataGroups)){
			sql.addSelectColumns(SQL_COLUMNS_GET_BACKEND_GROUP);
		}
		
		if(DataGroups.hasDataGroup(Definitions.DATA_GROUP_PUBLIC, dataGroups)){
			LOGGER.debug("Targeting to back end groups with public permissions for "+UserPermission.READ_BACKENDS.name());
			sql.addWhereClause(SQL_AND_PUBLIC_READ);
		}
		
		if(!ArrayUtils.isEmpty(backendGroupIds)){
			LOGGER.debug("Using back end group id filter.");
			sql.addWhereClause(new AndClause(COLUMN_BACKEND_GROUP_ID_WITH_TABLE_NAME, backendGroupIds));
		}

		if(!ArrayUtils.isEmpty(userIds)){
			LOGGER.debug("Using user id filter.");
			sql.addWhereClause(new AndClause(COLUMN_USER_ID_WITH_TABLE_NAME, userIds)); // add the filter
			sql.addJoin(SQL_JOIN_BACK_END_PERMISSIONS);// join through permission table to get user's back end groups
		}
		
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql.toSQLString(Definitions.ELEMENT_BACKEND_GROUP_LIST), sql.getValues(), sql.getValueTypes());
		if(rows.isEmpty()){
			LOGGER.debug("No back end groups found.");
			return null;
		}
		
		boolean getUsers = (allGroups || DataGroups.hasDataGroup(Definitions.DATA_GROUP_USERS, dataGroups));
		boolean getBackends = (allGroups || DataGroups.hasDataGroup(Definitions.DATA_GROUP_BACKENDS, dataGroups));
		
		List<BackendGroup> groups = new ArrayList<>(rows.size());
		for(Map<String, Object> row : rows){
			groups.add(extractBackendGroup(row, getBackends, getUsers, limits));
		}
		
		return new BackendGroupList(groups);
	}

	/**
	 * 
	 * @param row
	 * @param getBackends
	 * @param getUsers
	 * @param limits for retrieving optional back end groups and users
	 * @return the extracted back end
	 */
	private BackendGroup extractBackendGroup(Map<String, Object> row, boolean getBackends, boolean getUsers, Limits limits) {
		BackendGroup group = new BackendGroup();
		Long backendGroupId = null;
		for(Entry<String, Object> entry : row.entrySet()){
			switch(entry.getKey()){
				case COLUMN_BACKEND_GROUP_ID:
					backendGroupId = (Long) entry.getValue();
					group.setBackendGroupId(backendGroupId);
					break;
				case COLUMN_NAME:
					group.setName((String) entry.getValue());
					break;
				case COLUMN_DESCRIPTION:
					group.setDescription((String) entry.getValue());
					break;
				case COLUMN_READ_PUBLIC:
					if(BooleanUtils.toBoolean((int) entry.getValue())){
						group.addGroupPermission(UserPermission.READ_BACKENDS);
					}
					break;
				case COLUMN_TASK_PUBLIC:
					if(BooleanUtils.toBoolean((int) entry.getValue())){
						group.addGroupPermission(UserPermission.TASKS);
					}
					break;
				default:
					if(checkCountColumn(entry.getKey(), entry.getValue()) < 1){	// this should be count(*)
						LOGGER.warn("Unknown column name, or no results, column: "+entry.getKey());
						return null;
					}
					break;
			}
		}
		
		if(getBackends){
			LOGGER.debug("Retrieving back ends, for back end group, id: "+backendGroupId);
			List<Long> backendIds = getJdbcTemplate().queryForList(SQL_GET_BACKEND_IDS, new Object[]{backendGroupId}, SQL_BACKEND_ID_SQL_TYPES, Long.class);
			if(backendIds.isEmpty()){
				LOGGER.debug("No back ends for back end group, id: "+backendGroupId);
			}else{
				group.setBackends(getBackends(ListUtils.toPrimitive(backendIds), limits, null));
			}
		}
		
		if(getUsers){
			group.setUsers(getUsers(backendGroupId, limits));
		}
		
		return group;
	}
	
	/**
	 * 
	 * @param backendGroupId
	 * @param limits
	 * @return list of user for the back end group or null if none was found
	 */
	private List<BackendUserIdentity> getUsers(Long backendGroupId, Limits limits) {
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_BACKEND_GROUPS_PERMISSIONS);
		sql.addOrderBy(COLUMN_USER_ID, OrderDirection.DESCENDING);
		sql.addSelectColumns(SQL_COLUMNS_GET_PERMISSIONS);
		sql.setLimits(limits);
		sql.addWhereClause(new AndClause(COLUMN_BACKEND_GROUP_ID, backendGroupId, SQLType.LONG));
		
		
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql.toSQLString(Definitions.ELEMENT_PERMISSION_LIST), sql.getValues(), sql.getValueTypes());
		if(rows.isEmpty()){
			LOGGER.warn("No users for backend group, id: "+backendGroupId);
			return null;
		}
	
		List<BackendUserIdentity> users = new ArrayList<>();
		for(Map<String, Object> row : rows){
			Long userId = (Long) row.get(COLUMN_USER_ID);
			BackendUserIdentity user = IterableUtils.find(users, new Predicate<BackendUserIdentity>() {
				@Override
				public boolean evaluate(BackendUserIdentity userIdentity) {
					return BackendUserIdentity.equals(userIdentity, userId);
				}
			});
			if(user == null){
				user = new BackendUserIdentity();
				user.setUserId(userId);
				users.add(user);
			}
			user.addPermission(UserPermission.fromInt((int) row.get(COLUMN_PERMISSION)));
		} // for
		return users;
	}

	/**
	 * Note: this will <i>not</i> perform permission checks.
	 * 
	 * @param backendIds
	 * @param limits
	 * @param userIds
	 * @return list of back ends or null if none was found
	 */
	public BackendList getBackends(long[] backendIds, Limits limits, long[] userIds) {
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_BACKENDS);
		sql.addOrderBy(SQL_COLUMNS_GET_BACKEND[0], OrderDirection.DESCENDING);
		sql.addSelectColumns(SQL_COLUMNS_GET_BACKEND);
		sql.setLimits(limits);
		
		if(!ArrayUtils.isEmpty(backendIds)){
			LOGGER.debug("Using back end id filter.");
			sql.addWhereClause(new AndClause(SQL_COLUMNS_GET_BACKEND[0], backendIds));
		}
		
		if(!ArrayUtils.isEmpty(userIds)){
			LOGGER.debug("Using user id filter.");
			sql.addWhereClause(new AndClause(COLUMN_USER_ID_WITH_TABLE_NAME, userIds)); // add the filter
			sql.addJoin(SQL_JOIN_BACK_END_GROUPS); // join through back end groups to get valid back end ids
			sql.addJoin(SQL_JOIN_BACK_END_PERMISSIONS);// join through permission table to get user's back end groups
			sql.addGroupBy(SQL_COLUMNS_GET_BACKEND[0]);
		}
		
		List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql.toSQLString(Definitions.ELEMENT_BACKEND_LIST), sql.getValues(), sql.getValueTypes());
		if(rows.isEmpty()){
			LOGGER.debug("No back ends found.");
			return null;
		}
		
		List<Backend> backends = new ArrayList<>(rows.size());
		for(Map<String, Object> row : rows){
			backends.add(extractBackend(row, true));
		}
		
		return new BackendList(backends);
	}
}
