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
package core.tut.pori.users;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import core.tut.pori.http.ResponseData;

/**
 * List of user groups usable with Response.
 */
@XmlRootElement(name=Definitions.ELEMENT_USER_GROUP_LIST)
@XmlAccessorType(XmlAccessType.NONE)
public class UserGroupList extends ResponseData {
	private static final Logger LOGGER = Logger.getLogger(UserGroupList.class);
	@XmlElement(name=Definitions.ELEMENT_USER_GROUP)
	private List<UserGroup> _userGroups = null;

	
	
	/**
	 * @return the userGroups
	 */
	public List<UserGroup> getUserGroups() {
		return _userGroups;
	}

	/**
	 * @param userGroups the userGroups to set
	 */
	public void setUserGroups(List<UserGroup> userGroups) {
		_userGroups = userGroups;
	}

	/**
	 * 
	 * @param userGroup null userGroup is ignored
	 */
	public void addUserGroup(UserGroup userGroup){
		if(userGroup == null){
			LOGGER.debug("Ignored null userGroup.");
			return;
		}
		
		if(_userGroups == null){
			_userGroups = new ArrayList<>();
		}
		_userGroups.add(userGroup);
	}
	
	/**
	 * for sub-classing, use the static
	 * @return true if this list is empty
	 */
	protected boolean isEmpty(){
		return (_userGroups == null ? true : _userGroups.isEmpty());
	}
	
	/**
	 * 
	 * @param list
	 * @return true if the list is empty or null
	 */
	public static boolean isEmpty(UserGroupList list){
		return (list == null ? true : list.isEmpty());
	}
}
