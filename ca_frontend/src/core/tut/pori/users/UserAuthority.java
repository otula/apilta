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

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * utility class for creating user authorities, and converting role string to Authority objects.
 * 
 */
public final class UserAuthority {
	/** admin role string */
	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	/** back-end role string */
	public static final String ROLE_BACKEND = "ROLE_BACKEND";
	/** user role string */
	public static final String ROLE_USER = "ROLE_USER";
	/** moderator role string */
	public static final String ROLE_MODERATOR = "ROLE_MODERATOR";
	/** authority admin */
	public static final GrantedAuthority AUTHORITY_ROLE_ADMIN = new SimpleGrantedAuthority(ROLE_ADMIN);
	/** authority back-end */
	public static final GrantedAuthority AUTHORITY_ROLE_BACKEND = new SimpleGrantedAuthority(ROLE_BACKEND);
	/** authority user */
	public static final GrantedAuthority AUTHORITY_ROLE_USER = new SimpleGrantedAuthority(ROLE_USER);
	/** authority moderator */
	public static final GrantedAuthority AUTHORITY_ROLE_MODERATOR = new SimpleGrantedAuthority(ROLE_MODERATOR);
	
	/**
	 * 
	 * @param roleString
	 * @return the given string converted to an authority
	 * @throws IllegalArgumentException on bad string
	 */
	public static GrantedAuthority getGrantedAuthority(String roleString) throws IllegalArgumentException {
		if(roleString != null){
			switch (roleString) {
				case ROLE_USER:
					return AUTHORITY_ROLE_USER;
				case ROLE_ADMIN:
					return AUTHORITY_ROLE_ADMIN;
				case ROLE_BACKEND:
					return AUTHORITY_ROLE_BACKEND;
				case ROLE_MODERATOR:
					return AUTHORITY_ROLE_MODERATOR;
				default:
					break;
			}
		}
		throw new IllegalArgumentException("Bad "+GrantedAuthority.class.toString()+" : "+roleString);
	}
}
