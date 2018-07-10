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

import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import service.tut.pori.apilta.alerts.datatypes.AlertUserIdentity.UserPermission;


/**
 * permission group for an alert
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_ALERT_GROUP)
@XmlAccessorType(XmlAccessType.NONE)
public class AlertGroup {
	@XmlElement(name=Definitions.ELEMENT_ALERT_GROUP_ID)
	private Long _alertGroupId = null;
	@XmlElement(name=Definitions.ELEMENT_DESCRIPTION)
	private String _description = null;
	@XmlElement(name=Definitions.ELEMENT_NAME)
	private String _name = null;
	@XmlElementWrapper(name=Definitions.ELEMENT_PERMISSION_LIST)
	@XmlElement(name=Definitions.ELEMENT_PERMISSION)
	private Set<UserPermission> _permissions = null; // global permissions
	@XmlElementWrapper(name=core.tut.pori.users.Definitions.ELEMENT_USER_IDENTITY_LIST)
	@XmlElement(name=core.tut.pori.users.Definitions.ELEMENT_USER_IDENTITY)
	private List<AlertUserIdentity> _users = null;
	
		/**
	 * @return the alertGroupId
	 * @see #setAlertGroupId(Long)
	 */
	public Long getAlertGroupId() {
		return _alertGroupId;
	}

	/**
	 * @param alertGroupId the alertGroupId to set
	 * @see #getAlertGroupId()
	 */
	public void setAlertGroupId(Long alertGroupId) {
		_alertGroupId = alertGroupId;
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
	
	/**
	 * @return the users
	 * @see #setUsers(List)
	 */
	public List<AlertUserIdentity> getUsers() {
		return _users;
	}
	
	/**
	 * @param users the users to set
	 * @see #getUsers()
	 */
	public void setUsers(List<AlertUserIdentity> users) {
		_users = users;
	}
}
