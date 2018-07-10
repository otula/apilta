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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationListener;

import service.tut.pori.backends.datatypes.Backend;
import service.tut.pori.backends.datatypes.BackendEvent;
import service.tut.pori.backends.datatypes.BackendGroup;
import service.tut.pori.backends.datatypes.BackendGroupList;
import service.tut.pori.backends.datatypes.BackendList;
import service.tut.pori.backends.datatypes.BackendUserIdentity;
import service.tut.pori.backends.datatypes.BackendUserIdentity.UserPermission;
import service.tut.pori.users.UserCore;
import service.tut.pori.users.UserDAO;
import service.tut.pori.users.UserServiceEvent;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserEvent.EventType;
import core.tut.pori.users.UserGroup.Permission;
import core.tut.pori.users.UserIdentity;

/**
 * core methods for handling back ends
 * 
 */
public final class BackendsCore {
	private static final Logger LOGGER = Logger.getLogger(BackendsCore.class);
	
	/**
	 * 
	 */
	private BackendsCore() {
		// nothing needed
	}
	
	/**
	 * 
	 * @param authenticatedUser 
	 * @param backend
	 * @param backendGroupId the group for the back end
	 * @return the identifier of the generated back end or null if the back end generation failed (permission denied for the given group)
	 * @throws IllegalArgumentException on invalid back end
	 */
	public static Long createBackend(UserIdentity authenticatedUser, Backend backend, Long backendGroupId) throws IllegalArgumentException {
		if(!Backend.isValid(backend)){
			throw new IllegalArgumentException("Invalid back end.");
		}
		
		if(backend.getBackendId() != null){
			throw new IllegalArgumentException("New back ends cannot have identifier.");
		}
		
		BackendDAO bDAO = ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class);
		Set<UserPermission> permissions = bDAO.getBackendGroupPermissions(backendGroupId, authenticatedUser);
		if(permissions == null || !permissions.contains(UserPermission.MODIFY_BACK_ENDS)){
			LOGGER.warn("Permission denied for user, id: "+authenticatedUser.getUserId()+", back end group, id: "+backendGroupId);
			return null;
		}
		
		Long backendId = bDAO.createBackend(backend, backendGroupId);
		if(backendId == null){
			LOGGER.warn("Failed to create new back end.");
		}
		
		return backendId;
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param backendIds
	 * @return true if the operation was success. Note that failure means that the permission was denied (this could also mean that the back end did not exist).
	 */
	public static boolean deleteBackend(UserIdentity authenticatedUser, long[] backendIds) {
		BackendDAO bDAO = ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class);
		for(long backendId : backendIds){
			Set<UserPermission> permissions = bDAO.getBackendPermissions(backendId, authenticatedUser);
			if(permissions == null || !permissions.contains(UserPermission.MODIFY_BACK_ENDS)){
				LOGGER.warn("Permission denied for user, id: "+authenticatedUser.getUserId()+", for back end, id: "+backendId);
				return false;
			}
		}
		
		deleteBackend(backendIds);
		return true;
	}
	
	/**
	 * delete the back ends and signal the deletion using event handler, no events will be delivered for non-existent back end ids
	 * 
	 * @param backendIds
	 */
	protected static void deleteBackend(long[] backendIds){
		BackendDAO bDAO = ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class);
		for(long backendId : backendIds){
			if(bDAO.removeBackend(backendId)){
				ServiceInitializer.getEventHandler().publishEvent(new BackendEvent(BackendsCore.class, backendId, service.tut.pori.backends.datatypes.BackendEvent.EventType.BACKEND_REMOVED));
			}
		}
	}

	/**
	 * Update an existing back end. The update is <i>not</i> partial, all existing details will be replaced with the provided data.
	 * 
	 * @param authenticatedUser
	 * @param backend
	 * @return true if updated, false on failure (permission was denied)
	 * @throws IllegalArgumentException on invalid back end
	 */
	public static boolean updateBackend(UserIdentity authenticatedUser, Backend backend) throws IllegalArgumentException {
		if(!Backend.isValid(backend)){
			throw new IllegalArgumentException("Invalid back end.");
		}
		
		Long backendId = backend.getBackendId();
		if(backendId == null){
			throw new IllegalArgumentException("Back end identifier not given.");
		}
		
		BackendDAO bDAO = ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class);
		Set<UserPermission> permissions = bDAO.getBackendPermissions(backendId, authenticatedUser);
		if(permissions == null || !permissions.contains(UserPermission.MODIFY_BACK_ENDS)){
			LOGGER.warn("Permission denied for user, id: "+authenticatedUser.getUserId()+", for back end, id: "+backendId);
			return false;
		}
		
		if(!bDAO.updateBackend(backend)){
			throw new IllegalArgumentException("Failed to update back end.");
		}

		return true;
	}
	
	/**
	 * Note: this can NOT be used to add back ends to the group. The back ends, if given, are IGNORED.
	 * 
	 * @param authenticatedUser 
	 * @param backendGroup
	 * @return identifier for the created back end group or null on failure (permission denied)
	 * @throws IllegalArgumentException on bad group
	 */
	public static Long createBackendGroup(UserIdentity authenticatedUser, BackendGroup backendGroup) throws IllegalArgumentException {
		if(!BackendGroup.isValid(backendGroup)){
			throw new IllegalArgumentException("Invalid back end group.");
		}
		
		if(!hasPermissions(authenticatedUser, backendGroup)){
			LOGGER.warn("Permission check failed for user, id: "+authenticatedUser.getUserId());
			return null;
		}
		
		Long backendGroupId = ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class).createBackendGroup(backendGroup);
		
		List<Backend> backends = backendGroup.getBackends();
		if(backends != null && !backends.isEmpty()){ // do not process the back end, but print a warning to log
			LOGGER.warn("Ignored back end list for a new back end group, id: "+backendGroupId);
		}
		
		return backendGroupId;
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param backendGroup
	 * @return true if the user has permissions for the group
	 */
	private static boolean hasPermissions(UserIdentity authenticatedUser, BackendGroup backendGroup) {
		Long backendGroupId = backendGroup.getBackendGroupId();
		if(backendGroupId != null){
			Set<UserPermission> permissions = ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class).getBackendGroupPermissions(backendGroupId, authenticatedUser);
			if(permissions == null || !permissions.contains(UserPermission.MODIFY_BACK_ENDS)){
				LOGGER.warn("Permission denied for user, id: "+authenticatedUser.getUserId()+", for back end group, id: "+backendGroupId);
				return false;
			}
		}
		
		UserDAO userDAO = ServiceInitializer.getDAOHandler().getDAO(UserDAO.class);
		for(BackendUserIdentity user : backendGroup.getUsers()) {
			if(!userDAO.hasPermission(authenticatedUser, user, Permission.MODIFY_USERS)){ // check permissions
				LOGGER.warn("Permission denied for user, id: "+authenticatedUser.getUserId()+", attempted to modify user, id: "+user.getUserId());
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Note: this cannot be used to add back ends to the group, use @link {@link BackendsCore#associateBackends(UserIdentity, long[], long[])} or {@link BackendsCore#createBackend(UserIdentity, Backend, Long)} for adding new back ends to the group.
	 * 
	 * Attempting to remove all users from a group results in an error. Similarly, attempting to modify group to contain invalid permission set results in an errors (e.g. no users with modify permissions).
	 * 
	 * @param authenticatedUser
	 * @param backendGroup
	 * @return true on success, false on failure (permission denied)
	 * @throws IllegalArgumentException on invalid values
	 */
	public static boolean updateBackendGroup(UserIdentity authenticatedUser, BackendGroup backendGroup) throws IllegalArgumentException {
		Long backendGroupId = backendGroup.getBackendGroupId();
		if(!BackendGroup.isValid(backendGroup) || backendGroupId == null){
			throw new IllegalArgumentException("Invalid back end group.");
		}
		
		if(!hasPermissions(authenticatedUser, backendGroup)){
			LOGGER.warn("Permission check failed for user, id: "+authenticatedUser.getUserId());
			return false;
		}
				
		if(!ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class).updateBackendGroup(backendGroup)){
			throw new IllegalArgumentException("Failed to update back end group, id: "+backendGroupId);
		}
				
		List<Backend> backends = backendGroup.getBackends();
		if(backends != null && !backends.isEmpty()){ // do not process the back end, but print a warning to log
			LOGGER.warn("Ignored back end list for a new back end group, id: "+backendGroupId);
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param authenticatedUser 
	 * @param backendGroupIds
	 * @param backendIds
	 * @return true on success, false on failure (permission denied or invalid identifiers)
	 */
	public static boolean associateBackends(UserIdentity authenticatedUser, long[] backendGroupIds, long[] backendIds) {
		if(checkAssociationPermissions(authenticatedUser, backendGroupIds, backendIds)){
			ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class).associate(backendGroupIds, backendIds);
			return true;
		}else{
			LOGGER.warn("Permission denied for user, id: "+authenticatedUser.getUserId());
			return false;
		}
	}
	
	/**
	 * 
	 * @param authenticatedUser 
	 * @param backendGroupIds
	 * @param backendIds
	 * @return true on success, false on failure (permission denied or invalid identifiers)
	 */
	public static boolean unassociateBackends(UserIdentity authenticatedUser, long[] backendGroupIds, long[] backendIds) {
		if(checkAssociationPermissions(authenticatedUser, backendGroupIds, backendIds)){
			ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class).unassociate(backendGroupIds, backendIds);
			return true;
		}else{
			LOGGER.warn("Permission denied for user, id: "+authenticatedUser.getUserId());
			return false;
		}
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param backendGroupIds
	 * @param backendIds
	 * @return true if the user has permissions to change associations for the given back ends and back end groups
	 */
	private static boolean checkAssociationPermissions(UserIdentity authenticatedUser, long[] backendGroupIds, long[] backendIds) {
		BackendDAO bDAO = ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class);
		for(long backendGroupId : backendGroupIds){
			Set<UserPermission> permissions = bDAO.getBackendGroupPermissions(backendGroupId, authenticatedUser);
			if(permissions == null || !permissions.contains(UserPermission.MODIFY_BACK_ENDS)){
				LOGGER.debug("Permission denied for user, id: "+authenticatedUser.getUserId()+", for back end group, id: "+backendGroupId);
				return false;
			}
		}
		for(long backendId : backendIds){
			Set<UserPermission> permissions = bDAO.getBackendPermissions(backendId, authenticatedUser);
			if(permissions == null || !permissions.contains(UserPermission.MODIFY_BACK_ENDS)){
				LOGGER.debug("Permission denied for user, id: "+authenticatedUser.getUserId()+", for back end, id: "+backendId);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param authenticatedUser
	 * @param backendGroupIds
	 * @return true on success, false on failure (permission denied)
	 */
	public static boolean deleteBackendGroup(UserIdentity authenticatedUser, long[] backendGroupIds) {
		BackendDAO bDAO = ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class);
		for(long backendGroupId : backendGroupIds){
			Set<UserPermission> permissions = bDAO.getBackendGroupPermissions(backendGroupId, authenticatedUser);
			if(permissions == null || !permissions.contains(UserPermission.MODIFY_BACK_ENDS)){
				LOGGER.warn("Permission denied for user, id: "+authenticatedUser.getUserId()+", for back end group, id: "+backendGroupId);
				return false;
			}
		}
		
		deleteBackendGroup(backendGroupIds);
		return true;
	}
	
	/**
	 * 
	 * @param backendGroupIds
	 */
	protected static void deleteBackendGroup(long[] backendGroupIds) {
		BackendDAO bDAO = ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class);
		for(long backendGroupId : backendGroupIds){
			bDAO.removeBackendGroup(backendGroupId);
		}
	}
	
	/**
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
	 * @param authenticatedUser
	 * @param backendGroupIds optional data group ids
	 * @param dataGroups optional data groups
	 * @param limits optional limits
	 * @return list of back end groups matching the given terms. If only authenticatedUser is given, all groups the user belongs to will be returned.
	 * @throws IllegalArgumentException on invalid back end group ids
	 */
	public static BackendGroupList getBackendGroups(UserIdentity authenticatedUser, long[] backendGroupIds, DataGroups dataGroups, Limits limits) throws IllegalArgumentException {
		BackendDAO bDAO = ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class);
		long[] userIds = null;
		if(ArrayUtils.isEmpty(backendGroupIds)){ // if no ids are given, limit the search to the authenticated user
			userIds = new long[]{authenticatedUser.getUserId()};
		}else{ // otherwise, check that the user has permissions to retrieve the back end groups
			for(long backendGroupId : backendGroupIds){
				Set<UserPermission> permissions = bDAO.getBackendGroupPermissions(backendGroupId, authenticatedUser);
				if(permissions == null || !permissions.contains(UserPermission.READ_BACKENDS)){
					throw new IllegalArgumentException("Back end group, id: "+backendGroupId+" does not exist, or permission was denied.");
				}
			}
		}
		return bDAO.getBackendGroups(backendGroupIds, dataGroups, limits, userIds);
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param backendIds optional list of backend ids
	 * @param limits optional limits
	 * @return list of back ends matching the terms or null if none was found
	 * @throws IllegalArgumentException on invalid back end ids
	 */
	public static BackendList getBackends(UserIdentity authenticatedUser, long[] backendIds, Limits limits) throws IllegalArgumentException {
		BackendDAO bDAO = ServiceInitializer.getDAOHandler().getDAO(BackendDAO.class);
		long[] userIds = null;
		if(ArrayUtils.isEmpty(backendIds)){ // if no ids are given, limit the search to the authenticated user
			userIds = new long[]{authenticatedUser.getUserId()};
		}else{ // otherwise, check that the user has permissions to retrieve the back end groups
			for(long backendId : backendIds){
				Set<UserPermission> permissions = bDAO.getBackendPermissions(backendId, authenticatedUser);
				if(permissions == null || !permissions.contains(UserPermission.READ_BACKENDS)){
					throw new IllegalArgumentException("Back end, id: "+backendId+" does not exist, or permission was denied.");
				}
			}
		}
		return bDAO.getBackends(backendIds, limits, userIds);
	}
	
	/**
	 * Event listener for user related events.
	 * 
	 * Automatically instantiated by Spring as a bean.
	 */
	@SuppressWarnings("unused")
	private static class UserEventListener implements ApplicationListener<UserServiceEvent>{
		private static final EnumSet<UserPermission> ENUMSET_BACKEND_MODIFIERS = EnumSet.of(UserPermission.MODIFY_BACK_ENDS);

		@Override
		public void onApplicationEvent(UserServiceEvent event) {
			EventType type = event.getType();
			if(type == EventType.USER_REMOVED && event.getSource().equals(UserCore.class)){
				UserIdentity userId = event.getUserId();
				LOGGER.debug("Detected event of type "+type.name()+", removing back end groups for user, id: "+userId.getUserId());
		
				BackendGroupList groups = getBackendGroups(userId, null, new DataGroups(Definitions.DATA_GROUP_USERS), null);
				if(BackendGroupList.isEmpty(groups)){
					LOGGER.debug("No back end groups for user, id: "+userId.getUserId());
					return;
				}
				
				for(BackendGroup group : groups.getBackendGroups()) {
					List<BackendUserIdentity> users = group.getUsers(ENUMSET_BACKEND_MODIFIERS);
					if(users == null || users.isEmpty()){
						LOGGER.warn("Detected back end group, id: "+group.getBackendGroupId()+" without user with permission "+UserPermission.MODIFY_BACK_ENDS.name());
					}else if(users.size() == 1 && BackendUserIdentity.equals(userId, users.iterator().next())){ // if there is only one user with modify permissions, and the user is this user
						long[] backendGroupId = {group.getBackendGroupId()};
						LOGGER.debug("Removing back end group, id: "+backendGroupId[0]);
						BackendsCore.deleteBackendGroup(backendGroupId);
					}
				}
				
				LOGGER.debug("Back end groups removed for user, id: "+userId.getUserId());
			}
		}
	} // class UserEventListener
}
