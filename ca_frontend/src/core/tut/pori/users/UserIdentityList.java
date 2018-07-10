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
 * List of users usable with Response.
 */
@XmlRootElement(name=Definitions.ELEMENT_USER_IDENTITY_LIST)
@XmlAccessorType(XmlAccessType.NONE)
public class UserIdentityList extends ResponseData {
	private static final Logger LOGGER = Logger.getLogger(UserIdentityList.class);
	@XmlElement(name=Definitions.ELEMENT_USER_IDENTITY)
	private List<UserIdentity> _userIds = null;

	/**
	 * @return the userIds
	 */
	public List<UserIdentity> getUserIds() {
		return _userIds;
	}

	/**
	 * @param userIds the userIds to set
	 */
	public void setUserIds(List<UserIdentity> userIds) {
		_userIds = userIds;
	}
	
	/**
	 * 
	 * @param userId null userId is ignored
	 */
	public void addUserId(UserIdentity userId){
		if(userId == null){
			LOGGER.debug("Ignored null userId.");
			return;
		}
		
		if(_userIds == null){
			_userIds = new ArrayList<>();
		}
		_userIds.add(userId);
	}
	
	/**
	 * for sub-classing, use the static.
	 * 
	 * @return true if this list is valid
	 */
	protected boolean isValid(){
		if(isEmpty()){
			return false;
		}else{
			for(UserIdentity userId : _userIds){
				if(!UserIdentity.isValid(userId)){
					return false;
				}
			}
			return true;
		}
	}
	
	/**
	 * for sub-classing, use the static
	 * @return true if this list is empty
	 */
	protected boolean isEmpty(){
		return (_userIds == null ? true : _userIds.isEmpty());
	}
	
	/**
	 * 
	 * @param list
	 * @return true if the list is empty or null
	 */
	public static boolean isEmpty(UserIdentityList list){
		return (list == null ? true : list.isEmpty());
	}
	
	/**
	 * 
	 * @param list
	 * @return false if list is null, empty or invalid
	 */
	public static boolean isValid(UserIdentityList list){
		return (list == null ? false : list.isValid());
	}
}
