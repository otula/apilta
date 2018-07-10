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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import core.tut.pori.dao.SQLDAO;
import core.tut.pori.dao.SQLSelectBuilder;
import core.tut.pori.dao.clause.AndClause;
import core.tut.pori.dao.clause.SQLClause.SQLType;
import core.tut.pori.users.UserIdentity;
import service.tut.pori.apilta.alerts.datatypes.AlertUserIdentity.UserPermission;

/**
 * 
 * dao for accessing alert groups
 *
 */
public class AlertGroupsDAO extends SQLDAO {
	private static final Logger LOGGER = Logger.getLogger(AlertGroupsDAO.class);
	/* tables */
//	private static final String TABLE_ALERTS_GROUPS = DATABASE+".alerts_groups";
	private static final String TABLE_ALERTS_GROUPS_USERS = DATABASE+".alerts_groups_users";
	/* columns */
	private static final String COLUMN_ALERT_GROUP_ID = "alert_group_id";
//	private static final String COLUMN_DESCRIPTION = "description";
//	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_PERMISSION = "permission";
	
	/**
	 * 
	 * @param alertGroupIds only search for these identifiers
	 * @param permission the user must have the given permission for the group (or the permission must be global for the group)
	 * @param userId if null, only global permissions are matched
	 * @return list of alert group identifiers matching the given term or null if none was found
	 */
	public List<Long> getAlertGroupIds(long[] alertGroupIds, UserPermission permission, UserIdentity userId) {
		SQLSelectBuilder sql = new SQLSelectBuilder(TABLE_ALERTS_GROUPS_USERS);
		sql.addSelectColumn(COLUMN_ALERT_GROUP_ID);
		
		if(UserIdentity.isValid(userId)){
			ArrayList<Long> userIds = new ArrayList<>(2);
			userIds.add(null);
			userIds.add(userId.getUserId());
			sql.addWhereClause(new AndClause(COLUMN_USER_ID, userIds, SQLType.LONG));
		}else{
			sql.addWhereClause(new AndClause(COLUMN_USER_ID, (Object) null, SQLType.LONG));
		}
		
		sql.addWhereClause(new AndClause(COLUMN_PERMISSION, permission.toInt(), SQLType.INTEGER));
		
		if(!ArrayUtils.isEmpty(alertGroupIds)){
			LOGGER.debug("Using alert group id filter...");
			sql.addWhereClause(new AndClause(COLUMN_ALERT_GROUP_ID, alertGroupIds));
		}
		
		List<Long> ids = getJdbcTemplate().queryForList(sql.toSQLString(), sql.getValues(), sql.getValueTypes(), Long.class);
		return (ids.isEmpty() ? null : ids);
	}
	
	//TODO add/modify/delete/get alert group methods
}
