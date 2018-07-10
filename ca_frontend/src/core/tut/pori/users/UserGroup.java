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
package core.tut.pori.users;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Represent a single user group with one or more users.
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_USER_GROUP)
@XmlAccessorType(XmlAccessType.NONE)
public class UserGroup {
	private static final Logger LOGGER = Logger.getLogger(UserGroup.class);
	@XmlElement(name=Definitions.ELEMENT_DESCRIPTION)
	private String _description = null;
	@XmlElement(name=Definitions.ELEMENT_GROUP_ID)
	private Long _groupId = null;
	@XmlElement(name=Definitions.ELEMENT_NAME)
	private String _name = null;
	@XmlElementWrapper(name=Definitions.ELEMENT_USER_IDENTITY_LIST)
	@XmlElement(name=Definitions.ELEMENT_USER_IDENTITY)
	private List<GroupUserIdentity> _users = null;
	
	/**
	 * Group permission
	 * 
	 */
	@XmlEnum
	public enum Permission{
		/**
		 * the basic permissions for a user: user can read the group details (including group's user)
		 */
		READ_GROUP(0),
		/**
		 * the user can modify the group's basic details such as name and description, and add/remove users to/from the group
		 */
		MODIFY_GROUP(1),
		/**
		 * the user can modify any user of this group
		 */
		MODIFY_USERS(2);
		
		private int _value;
		
		/**
		 * 
		 * @param value
		 */
		private Permission(int value) {
			_value = value;
		}
		
		/**
		 * 
		 * @return the permission as integer
		 */
		public int toInt() {
			return _value;
		}
		
		/**
		 * 
		 * @param value
		 * @return the permission associated with the value
		 * @throws IllegalArgumentException
		 */
		public static Permission fromInt(int value) throws IllegalArgumentException {
			for(Permission p : values()){
				if(p._value == value){
					return p;
				}
			}
			throw new IllegalArgumentException("Invalid permission value: "+value+" for "+Permission.class.toString());
		}
	} // enum permission
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return _description;
	}
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		_description = description;
	}
	
	/**
	 * @return the groupId
	 */
	public Long getGroupId() {
		return _groupId;
	}
	
	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(Long groupId) {
		_groupId = groupId;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		_name = name;
	}
	
	/**
	 * @return the users
	 */
	public List<GroupUserIdentity> getUsers() {
		return _users;
	}
	
	/**
	 * @param users the users to set
	 */
	public void setUsers(List<GroupUserIdentity> users) {
		_users = users;
	}
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if this group contains valid data
	 * @see #isValid(core.tut.pori.users.UserGroup)
	 */
	protected boolean isValid() {
		if(StringUtils.isBlank(_name)){
			LOGGER.debug("No name given.");
			return false;
		}
		
		if(_users == null || _users.isEmpty()){
			LOGGER.debug("Empty user list.");
			return false;
		}
		
		boolean noModify = true;
		for(GroupUserIdentity user : _users){
			if(!GroupUserIdentity.isValid(user)){
				LOGGER.debug("Invalid user identity.");
				return false;
			}else if(noModify){
				if(user.getPermissions().contains(Permission.MODIFY_GROUP)){
					noModify = true;
				}
			}
		}
		if(noModify){
			LOGGER.debug("No users with permission "+Permission.MODIFY_GROUP.name());
			return false;
		}
				
		return true;
	}
	
	/**
	 * 
	 * @param userGroup
	 * @return true if the given grop is valid and not null
	 */
	public static boolean isValid(UserGroup userGroup) {
		if(userGroup == null){
			return false;
		}else{
			return userGroup.isValid();
		}
	}
}
