/**
 * Copyright 2014 Tampere University of Technology, Pori Department
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
package service.tut.pori.users;

import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import core.tut.pori.context.EventHandler;
import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.users.ExternalAccountConnection;
import core.tut.pori.users.ExternalAccountConnection.UserServiceType;
import core.tut.pori.users.GroupUserIdentity;
import core.tut.pori.users.UserAuthority;
import core.tut.pori.users.UserEvent.EventType;
import core.tut.pori.users.UserGroup;
import core.tut.pori.users.UserGroup.Permission;
import core.tut.pori.users.UserGroupList;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.users.UserIdentityList;
import service.tut.pori.users.Registration.RegistrationStatus;

/**
 * User Core methods.
 * 
 * This class emits events of type {@link service.tut.pori.users.UserServiceEvent} for user account modifications with one of the listed {@link core.tut.pori.users.UserEvent.EventType} :
 * <ul>
 *  <li>{@link core.tut.pori.users.UserEvent.EventType#USER_CREATED} for newly created user accounts.</li>
 *  <li>{@link core.tut.pori.users.UserEvent.EventType#USER_REMOVED} for removed user accounts.</li>
 *  <li>{@link core.tut.pori.users.UserEvent.EventType#USER_AUTHORIZATION_REVOKED} for removed external account connection. The external connection type will can be retrieved from the service type getter ({@link service.tut.pori.users.UserServiceEvent#getUserServiceType()})</li>
 * </ul>
 */
public class UserCore {
	private static final Logger LOGGER = Logger.getLogger(UserCore.class);
	
	/**
	 * 
	 */
	private UserCore(){
		// nothing needed
	}
	
	/**
	 * @param serviceTypes
	 * @param userId
	 * @throws IllegalArgumentException on bad values
	 */
	public static void deleteExternalAccountConnections(EnumSet<UserServiceType> serviceTypes, UserIdentity userId) throws IllegalArgumentException {
		if(!UserIdentity.isValid(userId)){
			throw new IllegalArgumentException("Invalid user identity.");
		}
		
		if(serviceTypes == null || serviceTypes.isEmpty()){
			LOGGER.warn("Ignored empty service type list.");
			return;
		}
		
		UserDAO userDao = ServiceInitializer.getDAOHandler().getDAO(UserDAO.class);
		EventHandler eventHandler = ServiceInitializer.getEventHandler();
		for(UserServiceType t : serviceTypes){
			if(userDao.deleteExternalAccountConnection(t, userId)){
				eventHandler.publishEvent(new UserServiceEvent(EventType.USER_AUTHORIZATION_REVOKED, t, UserCore.class, userId));
			}else{
				LOGGER.warn("Could not remove requested user service connection "+t.toUserServiceTypeString()+" for user, id: "+userId.getUserId());
			}
		}
	}
	
	/**
	 * 
	 * @param username
	 * @return user identity for the username or null if not found
	 */
	public static UserIdentity getUserIdentity(String username){
		return ServiceInitializer.getDAOHandler().getDAO(UserDAO.class).getUser(username);
	}
	
	/**
	 * 
	 * @param userId
	 * @return user identity for the given id or null if not found
	 */
	public static UserIdentity getUserIdentity(Long userId){
		return ServiceInitializer.getDAOHandler().getDAO(UserDAO.class).getUser(userId);
	}
	
	/**
	 * 
	 * @param serviceTypes optional service type filters
	 * @param userId
	 * @return list of connections for the given user or null if none was found
	 */
	public static ExternalAccountConnectionList getExternalAccountConnections(EnumSet<UserServiceType> serviceTypes, UserIdentity userId){
		return ServiceInitializer.getDAOHandler().getDAO(UserDAO.class).getExternalAccountConnections(serviceTypes, userId);
	}
	
	/**
	 *  Register a new user, checking for valid system registration password, if one is set in the system properties.
	 * 
	 * @param registration
	 * @return status
	 */
	public static RegistrationStatus register(Registration registration) {
		String registerPassword = ServiceInitializer.getPropertyHandler().getSystemProperties(UserServiceProperties.class).getRegisterPassword();
		if(!StringUtils.isBlank(registerPassword) && !registerPassword.equals(registration.getRegisterPassword())){
			LOGGER.warn("The given registeration password was invalid.");
			return RegistrationStatus.FORBIDDEN;
		}
		
		return createUser(registration);
	}
	
	/**
	 * 
	 * @param connection
	 * @return UserIdentity with the id value set or null if none is found
	 */
	public static UserIdentity getUserId(ExternalAccountConnection connection){
		return ServiceInitializer.getDAOHandler().getDAO(UserDAO.class).getUserId(connection);
	}
	
	/**
	 * 
	 * @param connection
	 * @param userId
	 * @throws IllegalArgumentException
	 */
	public static void insertExternalAccountConnection(ExternalAccountConnection connection, UserIdentity userId) throws IllegalArgumentException{
		if(!UserIdentity.isValid(userId)){
			throw new IllegalArgumentException("Bad userId.");
		}
		ServiceInitializer.getDAOHandler().getDAO(UserDAO.class).insertExternalAccountConnection(connection, userId);
	}
	
	/**
	 * Crate user based on the registration information. On success this will publish event notification for newly created user.
	 * 
	 * @param registration
	 * @return status
	 */
	public static RegistrationStatus createUser(Registration registration){
		RegistrationStatus status = Registration.isValid(registration);
		if(status != RegistrationStatus.OK){
			LOGGER.debug("Invalid registration.");
			return status;
		}
		
		UserIdentity userId = new UserIdentity(registration.getEncryptedPassword(), null, registration.getUsername());
		userId.addAuthority(UserAuthority.AUTHORITY_ROLE_USER); // add with role user
		if(ServiceInitializer.getDAOHandler().getDAO(UserDAO.class).addUser(userId)){
			registration.setRegisteredUserId(userId);
			ServiceInitializer.getEventHandler().publishEvent(new UserServiceEvent(EventType.USER_CREATED, null, UserCore.class, userId));
			return RegistrationStatus.OK;
		}else{
			LOGGER.debug("Failed to add new user: reserved username.");
			return RegistrationStatus.BAD_USERNAME;
		}
	}
	
	/**
	 * Remove the user from the system. Successful call will publish event notification for removed user.
	 * 
	 * @param userId
	 * @throws IllegalArgumentException
	 */
	public static void unregister(UserIdentity userId) throws IllegalArgumentException{
		if(ServiceInitializer.getDAOHandler().getDAO(UserDAO.class).removeUser(userId)){
			ServiceInitializer.getSessionHandler().removeSessionInformation(userId); // remove all user's sessions
			ServiceInitializer.getEventHandler().publishEvent(new UserServiceEvent(EventType.USER_REMOVED, null, UserCore.class, userId));
		}else{
			throw new IllegalArgumentException("Failed to remove user, id: "+userId.getUserId());
		}
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param userIdFilter optional filter for retrieving a list of users, if null, the details of the authenticatedUser will be returned
	 * @return user details for the requested userId or null if not available
	 * @throws IllegalArgumentException
	 */
	public static UserIdentityList getUserDetails(UserIdentity authenticatedUser, long[] userIdFilter) throws IllegalArgumentException{
		if(!UserIdentity.isValid(authenticatedUser)){
			throw new IllegalArgumentException("Bad authenticated user.");
		}
		Long authId = authenticatedUser.getUserId();
		if(ArrayUtils.isEmpty(userIdFilter) || (userIdFilter.length == 1 && userIdFilter[0] == authId)){ // if no filters have been given or the only filter is the authenticated user
			UserIdentityList list = new UserIdentityList();
			list.addUserId(authenticatedUser);
			return list;
		}
		
		UserDAO userDAO = ServiceInitializer.getDAOHandler().getDAO(UserDAO.class);
		UserIdentity target = new UserIdentity();
		for(long userId : userIdFilter){
			target.setUserId(userId);
			if(!userDAO.hasPermission(authenticatedUser, target, Permission.READ_GROUP)){
				LOGGER.warn("User, id: "+authId+" did not have the required permission "+Permission.READ_GROUP.name()+" for accessing user, id: "+userId);
				return null;
			} // if
		} // for

		return userDAO.getUsers(userIdFilter);
	}
	
	/**
	 * 
	 * @param userIdentity
	 * @return true if the user has admin permissions
	 */
	@Deprecated
	private static boolean isAdmin(UserIdentity userIdentity){
		Long userId = userIdentity.getUserId();
		UserDAO userDAO = ServiceInitializer.getDAOHandler().getDAO(UserDAO.class);
		userIdentity = userDAO.getUser(userId); // populate the details
		if(!UserIdentity.isValid(userIdentity)){
			LOGGER.warn("Could not resolve user identity for user, id: "+userId);
			return false;
		}
		if(userIdentity.getAuthorities().contains(UserAuthority.AUTHORITY_ROLE_ADMIN)){ // check if the user is a system level admin user
			return true; // the user is an admin and has all permissions
		}else{
			return false;
		} // else
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param userGroupIds
	 * @return list of user groups or null if none found
	 */
	public static UserGroupList getUserGroups(UserIdentity authenticatedUser, long[] userGroupIds) {
		UserDAO userDAO = ServiceInitializer.getDAOHandler().getDAO(UserDAO.class);
		long[] userIds = null;
		if(ArrayUtils.isEmpty(userGroupIds)){ // if no group ids, add the authenticated user as the user id filter
			userIds = new long[]{authenticatedUser.getUserId()};
		}else{
			for(long userGroupId : userGroupIds){
				Set<Permission> permissions = userDAO.getUserPermissions(userGroupId, authenticatedUser);
				if(permissions == null || !permissions.contains(Permission.READ_GROUP)){
					LOGGER.warn("User, id: "+authenticatedUser.getUserId()+" did not have the required permission "+Permission.READ_GROUP.name()+" for accessing user group, id: "+userGroupId);
					return null;
				} // if
			} // for
		} // else
		
		return userDAO.getUserGroups(userGroupIds, userIds);
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param userGroup
	 * @return id of the created group or null on failure (permission denied)
	 * @throws IllegalArgumentException on bad parameter values
	 */
	public static Long createUserGroup(UserIdentity authenticatedUser, UserGroup userGroup) throws IllegalArgumentException {
		if(!UserGroup.isValid(userGroup)){
			throw new IllegalArgumentException("Invalid user group.");
		}
		
		UserDAO userDAO = ServiceInitializer.getDAOHandler().getDAO(UserDAO.class);
		for(GroupUserIdentity user : userGroup.getUsers()){
			if(!userDAO.hasPermission(authenticatedUser, user, Permission.MODIFY_USERS)){
				LOGGER.warn("User, id: "+authenticatedUser.getUserId()+" did not have the required permission "+Permission.MODIFY_USERS.name()+" for modifying user, id: "+user.getUserId());
				
				if(isAdmin(authenticatedUser)){
					LOGGER.debug("Granting access for user, id: "+authenticatedUser.getUserId()+" for modifying user, id: "+user.getUserId()+" with authority "+UserAuthority.AUTHORITY_ROLE_ADMIN.getAuthority());
					break; // the user is an admin and has all permissions
				}else{
					LOGGER.warn("User, id: "+authenticatedUser.getUserId()+" tried to modify user, id: "+user.getUserId()+", but does not have the required role: "+UserAuthority.AUTHORITY_ROLE_ADMIN.getAuthority());
					return null;
				} // else
			} // TODO else { send "invite" ?
		}
		
		return userDAO.createUserGroup(userGroup);
	}

	/**
	 * 
	 * @param authenticatedUser
	 * @param userGroup
	 * @return true on success, false on failure (permission denied)
	 * @throws IllegalArgumentException on bad parameter values
	 */
	public static boolean modifyUserGroup(UserIdentity authenticatedUser, UserGroup userGroup) throws IllegalArgumentException {
		Long groupId = userGroup.getGroupId();
		if(groupId == null){
			throw new IllegalArgumentException("Invalid user group.");
		}
		
		if(!UserGroup.isValid(userGroup)){
			throw new IllegalArgumentException("Invalid user group.");
		}
		
		UserDAO userDAO = ServiceInitializer.getDAOHandler().getDAO(UserDAO.class);
		
		Set<Permission> permissions = userDAO.getUserPermissions(groupId, authenticatedUser);
		if(permissions == null || !permissions.contains(Permission.MODIFY_GROUP)){
			LOGGER.warn("User, id: "+authenticatedUser.getUserId()+" did not have the required permission "+Permission.MODIFY_GROUP.name()+" for user group, id: "+groupId);
			return false;
		}
		
		for(GroupUserIdentity user : userGroup.getUsers()){
			if(!userDAO.hasPermission(authenticatedUser, user, Permission.MODIFY_USERS)){
				LOGGER.warn("User, id: "+authenticatedUser.getUserId()+" did not have the required permission "+Permission.MODIFY_USERS.name()+" for modifying user, id: "+user.getUserId());
				return false;
			} // TODO else { send "invite" ?
		}
		
		return userDAO.modifyUserGroup(userGroup);
	}
}
