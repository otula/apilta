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
package service.tut.pori.apilta.alerts.datatypes;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

import core.tut.pori.users.UserIdentity;

/**
 * 
 * user for alert group
 */
@XmlRootElement(name=core.tut.pori.users.Definitions.ELEMENT_USER_IDENTITY)
@XmlAccessorType(XmlAccessType.NONE)
public class AlertUserIdentity extends UserIdentity {
	/** serial version UID */
	private static final long serialVersionUID = 2525582092128969017L;
	@XmlElementWrapper(name=Definitions.ELEMENT_PERMISSION_LIST)
	@XmlElement(name=Definitions.ELEMENT_PERMISSION)
	private Set<UserPermission> _permissions = null;

	/**
	 * 
	 * user permission for a group/user
	 *
	 */
	@XmlEnum
	public enum UserPermission {
		/** the user can read alerts that are in this group */
		READ_ALERTS(1),
		/** the user can post new alerts into this group */
		POST_ALERT(2),
		/** the user can modify existing alerts of this group */
		MODIFY_ALERTS(3),
		/** user can read the alert group details, including group users */
		READ_GROUP(4),
		/** the user can modify the alert group */
		MODIFY_GROUP(5);
		
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
		 * @return the permission as an integer value
		 */
		public int toInt() {
			return _value;
		}
		
		/**
		 * 
		 * @param value
		 * @return the integer as user permission
		 * @throws IllegalArgumentException on bad value
		 */
		public static UserPermission fromInt(int value) throws IllegalArgumentException {
			for(UserPermission p : values()){
				if(p._value == value){
					return p;
				}
			}
			throw new IllegalArgumentException("Invalid value: "+value);
		}
	} // enum UserPermission

	/**
	 * @return the permissions
	 * @see #setPermissions(Set)
	 */
	public Set<UserPermission> getPermissions() {
		return _permissions;
	}

	/**
	 * @param permissions the permissions to set
	 * @see #getPermissions()
	 */
	public void setPermissions(Set<UserPermission> permissions) {
		_permissions = permissions;
	}
}
