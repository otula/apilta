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
package service.tut.pori.backends.datatypes;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import service.tut.pori.backends.Definitions;
import service.tut.pori.backends.datatypes.BackendUserIdentity.UserPermission;

/**
 * A group consisting of one or more back ends and of one or more users
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_BACKEND_GROUP)
@XmlAccessorType(XmlAccessType.NONE)
public class BackendGroup {
	private static final Logger LOGGER = Logger.getLogger(BackendGroup.class);
	@XmlElement(name=Definitions.ELEMENT_BACKEND_GROUP_ID)
	private Long _backendGroupId = null;
	@XmlElementWrapper(name=Definitions.ELEMENT_BACKEND_LIST)
	@XmlElement(name=Definitions.ELEMENT_BACKEND)
	private List<Backend> _backends = null;
	@XmlElement(name=Definitions.ELEMENT_DESCRIPTION)
	private String _description = null;
	@XmlElementWrapper(name=Definitions.ELEMENT_PERMISSION_LIST)
	@XmlElement(name=Definitions.ELEMENT_PERMISSION)
	private Set<UserPermission> _groupPermissions = null;
	@XmlElement(name=Definitions.ELEMENT_NAME)
	private String _name = null;
	@XmlElementWrapper(name=core.tut.pori.users.Definitions.ELEMENT_USER_IDENTITY_LIST)
	@XmlElement(name=core.tut.pori.users.Definitions.ELEMENT_USER_IDENTITY)
	private List<BackendUserIdentity> _users = null;
	
	/**
	 * @return the backendGroupId
	 * @see #setBackendGroupId(Long)
	 */
	public Long getBackendGroupId() {
		return _backendGroupId;
	}
	
	/**
	 * @param backendGroupId the backendGroupId to set
	 *  @see #getBackendGroupId()
	 */
	public void setBackendGroupId(Long backendGroupId) {
		_backendGroupId = backendGroupId;
	}
	
	/**
	 * @return the description
	 * @see #setDescription(String)
	 */
	public String getDescription() {
		return _description;
	}
	
	/**
	 * @param description the description to set
	 * @see #getDescription()
	 */
	public void setDescription(String description) {
		_description = description;
	}
	
	/**
	 * @return global permission for this group
	 * @see #setGroupPermissions(Set)
	 */
	public Set<UserPermission> getGroupPermissions() {
		return _groupPermissions;
	}
	
	/**
	 * @param groupPermissions global permission for this group
	 * @see #getGroupPermissions()
	 */
	public void setGroupPermissions(Set<UserPermission> groupPermissions) {
		_groupPermissions = groupPermissions;
	}
	
	/**
	 * 
	 * @param groupPermission
	 * @see #getGroupPermissions()
	 */
	public void addGroupPermission(UserPermission groupPermission) {
		if(_groupPermissions == null){
			_groupPermissions = EnumSet.of(groupPermission);
		}else{
			_groupPermissions.add(groupPermission);
		}
	}
	
	/**
	 * @return the name
	 * @see #setName(String)
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * @param name the name to set
	 * @see #getName()
	 */
	public void setName(String name) {
		_name = name;
	}
	
	/**
	 * @return the users
	 * @see #setUsers(List)
	 */
	public List<BackendUserIdentity> getUsers() {
		return _users;
	}
	
	/**
	 * 
	 * @param permissions
	 * @return list of users which have all of the given permissions or null of no users matching the given permissions are found
	 */
	public List<BackendUserIdentity> getUsers(EnumSet<UserPermission> permissions) {
		if(_users == null || _users.isEmpty()){
			LOGGER.debug("No users.");
			return null;
		}
		List<BackendUserIdentity> matchingUsers = new ArrayList<>();
		for(BackendUserIdentity user : _users) {
			Set<UserPermission> uPerms = user.getPermissions();
			if(uPerms != null && uPerms.containsAll(permissions)){
				matchingUsers.add(user);
			}
		}
		return (matchingUsers.isEmpty() ? null : matchingUsers);
	}

	/**
	 * @param users the users to set
	 * @see #getUsers()
	 */
	public void setUsers(List<BackendUserIdentity> users) {
		_users = users;
	}

	/**
	 * @return the back ends
	 * @see #setBackends(List)
	 */
	public List<Backend> getBackends() {
		return _backends;
	}

	/**
	 * @param backends the back ends to set
	 * @see #getBackends()
	 */
	public void setBackends(List<Backend> backends) {
		_backends = backends;
	}
	
	/**
	 * 
	 * @param backends
	 */
	public void setBackends(BackendList backends) {
		_backends = (BackendList.isEmpty(backends) ? null : backends.getBackends());
	}
	
	/**
	 * add new user to this group, if the user already exists, the permissions of the passed user object are added for the existing user
	 * 
	 * @param userId
	 */
	public void addUser(BackendUserIdentity userId) {
		if(_users == null){
			_users = new ArrayList<>();
		}else{
			for(BackendUserIdentity u : _users){
				if(BackendUserIdentity.equals(u, userId)){
					u.addPermissions(userId.getPermissions());
					return;
				}
			}
		}
		_users.add(userId);
	}

	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if valid
	 * @see #isValid(BackendGroup)
	 */
	protected boolean isValid(){
		if(StringUtils.isBlank(_name)){
			LOGGER.debug("Invalid name.");
			return false;
		}
		
		if(_users == null || _users.isEmpty()){
			LOGGER.debug("The group has no users.");
			return false;
		}
		boolean noModify = true;
		for(BackendUserIdentity user : _users){
			if(!BackendUserIdentity.isValid(user)){
				LOGGER.warn("Invalid user identity.");
				return false;
			}else if(noModify){
				if(user.getPermissions().contains(UserPermission.MODIFY_BACK_ENDS)){
					noModify = false;
				}
			}
		}
		if(noModify){
			LOGGER.debug("The group has no users with permissions "+UserPermission.MODIFY_BACK_ENDS.name());
			return false;
		}
		
		if(_backends != null && !_backends.isEmpty()){
			for(Backend backend : _backends){
				if(backend.getBackendId() == null){
					LOGGER.warn("Back end without an identifier detected.");
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param group
	 * @return true if group is not null and is a valid backend group
	 */
	public static boolean isValid(BackendGroup group) {
		return (group == null ? false : group.isValid());
	}
}
