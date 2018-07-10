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
package service.tut.pori.users;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import core.tut.pori.http.ResponseData;
import core.tut.pori.users.UserIdentity;

/**
 * User registration details.
 *
 */
@XmlRootElement(name=Definitions.ELEMENT_REGISTRATION)
@XmlAccessorType(XmlAccessType.NONE)
public class Registration  extends ResponseData {
	private static final Logger LOGGER = Logger.getLogger(Registration.class);
	@XmlElement(name=core.tut.pori.users.Definitions.ELEMENT_USERNAME)
	private String _username = null;
	private String _password = null;
	private String _encryptedPassword = null;
	private BCryptPasswordEncoder _encoder = null;
	@XmlElement(name=Definitions.ELEMENT_REGISTER_PASSWORD)
	private String _registerPassword = null;
	private UserIdentity _registeredUserId = null; // contains the registered user details after successful registration or null if not available
	
	/**
	 * The status of registration process.
	 *
	 */
	public enum RegistrationStatus{
		/** registeration completed successfully */
		OK,
		/** given username was invalid or reserved */
		BAD_USERNAME,
		/** given password was invalid (too short or contained invalid characters */
		BAD_PASSWORD,
		/** required data was not given */
		NULL_DATA, 
		/** Registeration attempt was forbidden. */
		FORBIDDEN;
		
		/**
		 * 
		 * @return this status as a string
		 */
		public String toStatusString(){
			return name();
		}
	} // enum RegistrationStatus
	
	/**
	 * @return the username
	 */
	public String getUsername() {
		return _username;
	}
	
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		_username = username;
	}
	
	/**
	 * @return the password
	 */
	@XmlElement(name=Definitions.ELEMENT_PASSWORD)
	public String getPassword() {
		return _password;
	}
	
	/**
	 * 
	 * @return encrypted password
	 */
	public String getEncryptedPassword(){
		if(_password == null){
			LOGGER.debug("No password.");
			return null;
		}
		if(_encryptedPassword == null){
			if(_encoder == null){
				_encoder = new BCryptPasswordEncoder();
			}
			_encryptedPassword = _encoder.encode(_password);
		}
		return _encryptedPassword;
	}
	
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		_encryptedPassword = null; // may have changed
		_password = password;
	}
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if the registration object is valid
	 */
	protected RegistrationStatus isValid(){
		if(StringUtils.isBlank(_username)){
			LOGGER.debug("No username.");
			return RegistrationStatus.BAD_USERNAME;
		}else if(StringUtils.isBlank(_password)){
			LOGGER.debug("No password.");
			return RegistrationStatus.BAD_PASSWORD;
		}else{
			return RegistrationStatus.OK;
		}
	}
	
	/**
	 * 
	 * @param registration can be null
	 * @return true if the passed registration object is valid
	 */
	public static RegistrationStatus isValid(Registration registration){
		if(registration == null){
			return RegistrationStatus.NULL_DATA;
		}else{
			return registration.isValid();
		}
	}

	/**
	 * Returns the registered user details after successful registration
	 * 
	 * @return the registeredUserId
	 */
	public UserIdentity getRegisteredUserId() {
		return _registeredUserId;
	}

	/**
	 * @param registeredUserId the registeredUserId to set
	 */
	protected void setRegisteredUserId(UserIdentity registeredUserId) {
		_registeredUserId = registeredUserId;
	}

	/**
	 * @return the registerPassword
	 */
	public String getRegisterPassword() {
		return _registerPassword;
	}

	/**
	 * @param registerPassword the registerPassword to set
	 */
	public void setRegisterPassword(String registerPassword) {
		_registerPassword = registerPassword;
	}
}
