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

import java.util.EnumSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import core.tut.pori.users.UserIdentity;
import service.tut.pori.backends.Definitions;

/**
 * user for back end group
 * 
 */
@XmlRootElement(name=core.tut.pori.users.Definitions.ELEMENT_USER_IDENTITY)
@XmlAccessorType(XmlAccessType.NONE)
public class BackendUserIdentity extends UserIdentity {
	private static final Logger LOGGER = Logger.getLogger(BackendUserIdentity.class);
	/** serial version UID */
	private static final long serialVersionUID = 1603235942862542732L;
	@XmlElementWrapper(name=Definitions.ELEMENT_PERMISSION_LIST)
	@XmlElement(name=Definitions.ELEMENT_PERMISSION)
	private Set<UserPermission> _permissions = null;

	/**
	 * user permission for a group/user
	 * 
	 */
	@XmlEnum
	public enum UserPermission {
		/** the user can read back end details */
		READ_BACKENDS(0),
		/** the user can modify the back end of this group */
		MODIFY_BACK_ENDS(1),
		/** the user can create new tasks for the back ends in this group. Note: this does <i>not</i> give the user permission to view tasks created by other users. */
		TASKS(2),
		/** the user can authenticate as one of the back ends of this group (for example, for submitting task results) */
		AUTH_BACKENDS(3);
		
		private int _value;
		
		/**
		 * 
		 * @param value
		 */
		private UserPermission(int value) {
			_value = value;
		}
		
		/**
		 * 
		 * @return the permission as value
		 */
		public int toInt() {
			return _value;
		}
		
		/**
		 * 
		 * @param value
		 * @return the value as UserPermission
		 * @throws IllegalArgumentException on invalid value
		 */
		public static UserPermission fromInt(int value) throws IllegalArgumentException {
			for(UserPermission p : values()){
				if(p._value == value){
					return p;
				}
			}
			throw new IllegalArgumentException("Invalid value: "+value+" for "+UserPermission.class.toString());
		}
	} // enum Permission

	/**
	 * @return the groupPermissions
	 * @see #setPermissions(Set)
	 */
	public Set<UserPermission> getPermissions() {
		return _permissions;
	}

	/**
	 * @param permissions the groupPermissions to set
	 * @see #getPermissions()
	 */
	public void setPermissions(Set<UserPermission> permissions) {
		_permissions = permissions;
	}
	
	/**
	 * add the given set of permissions in to the existing set of permissions
	 * 
	 * @param permissions
	 * @see #getPermissions()
	 */
	public void addPermissions(Set<UserPermission> permissions) {
		if(permissions == null || permissions.isEmpty()){
			LOGGER.debug("Empty permission set.");
			_permissions = null;
		}else if(_permissions == null){
			_permissions = EnumSet.copyOf(permissions);
		}else{
			_permissions.addAll(permissions);
		}
	}
	
	/**
	 * add the given permission in to the existing set of permissions
	 * 
	 * @param permission
	 * @see #getPermissions()
	 */
	public void addPermission(UserPermission permission) {
		if(_permissions == null){
			_permissions = EnumSet.of(permission); 
		}else{
			_permissions.add(permission);
		}
	}
	
	@Override
	protected boolean isValid() {
		if(_permissions == null || _permissions.isEmpty()){
			LOGGER.debug("No permissions for the user.");
			return false;
		}else{
			return super.isValid();
		}
	}
	
	/**
	 * 
	 * @param userId
	 * @param permissions
	 */
	public BackendUserIdentity(Long userId, Set<UserPermission> permissions) {
		super(userId);
		_permissions = permissions;
	}
	
	/**
	 * 
	 */
	public BackendUserIdentity() {
		super();
	}
}
