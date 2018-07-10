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
package service.tut.pori.users;

/**
 * Definitions for users package.
 *
 */
public final class Definitions {
	/* elements */
	/** xml element declaration */
	public static final String ELEMENT_EXTERNAL_ACCOUNT_CONNECTION_LIST = "externalAccountConnectionList";
	/** xml element declaration */
	public static final String ELEMENT_PASSWORD = "password";
	/** xml element declaration */
	public static final String ELEMENT_REGISTER_PASSWORD = "registerPassword";
	/** xml element declaration */
	public static final String ELEMENT_REGISTRATION = "registration";
	
	/* local methods */
	/** local service method declaration */
	public static final String METHOD_CREATE_USER_GROUP = "createUserGroup";
	/** local service method declaration */
	public static final String METHOD_DELETE_EXTERNAL_ACCOUNT_CONNECTION = "deleteExternalAccountConnection";
	/** local service method declaration */
	public static final String METHOD_GET_EXTERNAL_ACCOUNT_CONNECTIONS = "getExternalAccountConnections";
	/** local service method declaration */
	public static final String METHOD_GET_USER_DETAILS = "getUserDetails";
	/** local service method declaration */
	public static final String METHOD_GET_USER_GROUPS = "getUserGroups";
	/** local service method declaration */
	public static final String METHOD_MODIFY_USER_GROUP = "modifyUserGroup";
	/** local service method declaration */
	public static final String METHOD_REGISTER = "register";
	/** local service method declaration */
	public static final String METHOD_UNREGISTER = "unregister";
	
	/* parameters */
	/** method parameter declaration */
	public static final String PARAMETER_SERVICE_TYPE = "service_type";
	/** method parameter declaration */
	public static final String PARAMETER_USER_GROUP_ID = "user_group_id";
	/** method parameter declaration */
	public static final String PARAMETER_USER_ID = "user_id";
	
	/* services */
	/** service name declaration */
	public static final String SERVICE_USERS = "user";
	
		/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
