/**
 * Copyright 2017 Tampere University of Technology, Pori Department
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import otula.backend.tasks.datatypes.Definitions;

/**
 * 
 * Minimal Implementation of the User Identity.
 *
 * Contains the details of a single user.
 * 
 * This class has been modified from the original version:
 * 
 * - Only the user identity field is important for the back end functionality
 */
@XmlRootElement(name=Definitions.ELEMENT_USER_IDENTITY)
@XmlAccessorType(XmlAccessType.NONE)
public class UserIdentity  {
	@XmlElement(name=Definitions.ELEMENT_USER_ID)
	private Long _userId = null;
	

	/**
	 * 
	 * @param userId
	 */
	public UserIdentity(Long userId){
		_userId = userId;
	}
	
	/**
	 * create an empty user identity
	 */
	public UserIdentity(){
		// nothing needed
	}
	
	/**
	 * 
	 * @return user id value
	 * @see #setUserId(Long)
	 */
	public Long getUserId(){
		return _userId;
	}
	
	/**
	 * 
	 * @param userId can be null
	 * @return true if userId is set
	 */
	public static boolean isValid(UserIdentity userId){
		if(userId == null){
			return false;
		}else{
			return userId.isValid();
		}
	}
	
	/**
	 * use the static, implemented for sub-classing
	 * @return true if valid
	 * @see #isValid(UserIdentity)
	 */
	protected boolean isValid(){
		return (_userId == null ? false : true);
	}

	/**
	 * @param userId the userId to set
	 * @see #getUserId()
	 */
	public void setUserId(Long userId) {
		_userId = userId;
	}
}
