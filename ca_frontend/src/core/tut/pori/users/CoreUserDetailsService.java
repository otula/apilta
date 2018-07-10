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

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import service.tut.pori.users.UserCore;

/**
 * User details service. This is the main bean used by Spring Security for authenticating the user on REST/servlet call.
 * 
 * http://docs.spring.io/spring-security/site/docs/3.2.x/reference/html/ns-config.html
 *
 */
public class CoreUserDetailsService implements UserDetailsService{

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserDetails details = UserCore.getUserIdentity(username);
		if(details == null){
			throw new UsernameNotFoundException("The requested user does not exist.");
		}
		return details;
	}
}
