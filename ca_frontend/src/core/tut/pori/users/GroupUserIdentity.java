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

import java.util.EnumSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import core.tut.pori.users.UserGroup.Permission;

/**
 * basic user identity extended with group permissions
 *
 */
@XmlRootElement(name=Definitions.ELEMENT_USER_IDENTITY)
@XmlAccessorType(XmlAccessType.NONE)
public class GroupUserIdentity extends UserIdentity {
	private static final Logger LOGGER = Logger.getLogger(GroupUserIdentity.class);
	/** serial version UID */
	private static final long serialVersionUID = -92808571941354253L;		
	@XmlElement(name=Definitions.ELEMENT_PERMISSION_LIST)
	private EnumSet<Permission> _permissions = null;

	/**
	 * @return the permissions
	 */
	public EnumSet<Permission> getPermissions() {
		return _permissions;
	}

	/**
	 * @param permissions the permissions to set
	 */
	public void setPermissions(EnumSet<Permission> permissions) {
		_permissions = permissions;
	}
	
	/**
	 * add the given permission, duplicate permissions are silently ignored
	 * 
	 * @param permission
	 */
	public void addPermission(Permission permission) {
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
}
