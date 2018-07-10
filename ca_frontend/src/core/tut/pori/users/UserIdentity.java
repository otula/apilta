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

import java.util.Collection;
import java.util.HashSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Details of a single user
 *
 */
@XmlRootElement(name=Definitions.ELEMENT_USER_IDENTITY)
@XmlAccessorType(XmlAccessType.NONE)
public class UserIdentity implements UserDetails {
	private static final Logger LOGGER = Logger.getLogger(UserIdentity.class);
	/** serial UID */
	private static final long serialVersionUID = 967143020744296850L;
	private boolean _accountNonExpired = true;
	private boolean _accountNonLocked = true;
	private Collection<GrantedAuthority> _authorities = new HashSet<>();
	private boolean _credentialsNonExpired = true;
	private boolean _enabled = true;
	private String _password = null;
	@XmlElement(name=Definitions.ELEMENT_USER_ID)
	private Long _userId = null;
	@XmlElement(name=Definitions.ELEMENT_USERNAME)
	private String _username = null;
	

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
	 * @param password
	 * @param userId
	 * @param username
	 */
	public UserIdentity(String password, Long userId, String username){
		_userId = userId;
		_username = username;
		_password = password;
	}
	
	/**
	 * 
	 * @return user id value
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
	 */
	protected boolean isValid(){
		return (_userId == null ? false : true);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return _authorities;
	}

	/**
	 * @param authority
	 */
	public void addAuthority(GrantedAuthority authority) {
		_authorities.add(authority);
	}

	@Override
	public String getPassword() {
		return _password;
	}

	@Override
	public String getUsername() {
		return _username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return _accountNonExpired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return _accountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return _credentialsNonExpired;
	}

	@Override
	public boolean isEnabled() {
		return _enabled;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(Long userId) {
		_userId = userId;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		_username = username;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		_password = password;
	}

	/**
	 * @param accountNonExpired the accountNonExpired to set
	 */
	public void setAccountNonExpired(boolean accountNonExpired) {
		_accountNonExpired = accountNonExpired;
	}

	/**
	 * @param accountNonLocked the accountNonLocked to set
	 */
	public void setAccountNonLocked(boolean accountNonLocked) {
		_accountNonLocked = accountNonLocked;
	}

	/**
	 * @param credentialsNonExpired the credentialsNonExpired to set
	 */
	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		_credentialsNonExpired = credentialsNonExpired;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		_enabled = enabled;
	}
	
	/**
	 * 
	 * @param userIdOne can be null
	 * @param userIdTwo can be null
	 * @return true if the users are the same user, comparison is done by user id
	 */
	public static boolean equals(UserIdentity userIdOne, UserIdentity userIdTwo){
		if(!UserIdentity.isValid(userIdOne) || !UserIdentity.isValid(userIdTwo)){
			return false;
		}else{
			return (userIdOne.getUserId().equals(userIdTwo.getUserId()));
		}
	}
	
	/**
	 * 
	 * @param userIdentity can be null
	 * @param userId can be null
	 * @return true if the user are the same, comparison is done by user id
	 */
	public static boolean equals(UserIdentity userIdentity, Long userId){
		if(!UserIdentity.isValid(userIdentity) || userId == null){
			return false;
		}else{
			return (userIdentity.getUserId().equals(userId));
		}
	}
	
	/**
	 * 
	 * @param grantedAuthority
	 * @param userIdentity
	 * @return true if grantedAuthority is not null, userIdentity was valid and the given authority was present in the given user object
	 */
	public static boolean hasAuthority(GrantedAuthority grantedAuthority, UserIdentity userIdentity){
		if(grantedAuthority == null){
			LOGGER.debug("Granted authority was null.");
		}else if(!UserIdentity.isValid(userIdentity)){
			LOGGER.debug("Invalid user id.");
		}else{
			return userIdentity.getAuthorities().contains(grantedAuthority);
		}
		return false;
	}
}
