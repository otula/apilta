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
package service.tut.pori.backends;

/**
 * Definitions for the back ends package.
 * 
 */
public final class Definitions {
	/* data groups */
	/** data group for retrieving back end details (and the group identifier) for the group */
	public static final String DATA_GROUP_BACKENDS = "backends";
	/** data group for retrieving groups which are publicly visible. That is, have the permission {@value service.tut.pori.backends.datatypes.BackendUserIdentity.UserPermission#READ_BACKENDS} as a global permission.*/
	public static final String DATA_GROUP_PUBLIC = "public";
	/** data group for retrieving user details (and the group identifier) for the group */
	public static final String DATA_GROUP_USERS = "users";
	
	/* xml elements */
	/** xml element declaration */
	public static final String ELEMENT_ANALYSIS_URI = "url";
	/** xml element declaration */
	public static final String ELEMENT_BACKEND = "backend";
	/** xml element declaration */
	public static final String ELEMENT_BACKEND_GROUP = "backendGroup";
	/** xml element declaration */
	public static final String ELEMENT_BACKEND_GROUP_ID = "backendGroupId";
	/** xml element declaration */
	public static final String ELEMENT_BACKEND_GROUP_LIST = "backendGroupList";
	/** xml element declaration */
	public static final String ELEMENT_BACKEND_ID = "backendId";
	/** xml element declaration */
	public static final String ELEMENT_BACKEND_LIST = "backendList";
	/** xml element declaration */
	public static final String ELEMENT_CAPABILITY = "capability";
	/** xml element declaration */
	public static final String ELEMENT_CAPABILITY_LIST = "capabilityList";
	/** xml element declaration */
	public static final String ELEMENT_DESCRIPTION = "description";
	/** xml element declaration */
	public static final String ELEMENT_ENABLED = "enabled";
	/** xml element declaration */
	public static final String ELEMENT_NAME = "name";
	/** xml element declaration */
	public static final String ELEMENT_PERMISSION = "permission";
	/** xml element declaration */
	public static final String ELEMENT_PERMISSION_LIST = "permissionList";
	
	/* local methods */
	/** local service method declaration */
	public static final String METHOD_ASSOCIATE_BACKEND = "associateBackend";
	/** local service method declaration */
	public static final String METHOD_CREATE_BACKEND = "createBackend";
	/** local service method declaration */
	public static final String METHOD_DELETE_BACKEND = "deleteBackend";
	/** local service method declaration */
	public static final String METHOD_GET_BACKEND_GROUPS = "getBackendGroups";
	/** local service method declaration */
	public static final String METHOD_GET_BACKENDS = "getBackends";
	/** local service method declaration */
	public static final String METHOD_UPDATE_BACKEND = "updateBackend";
	/** local service method declaration */
	public static final String METHOD_CREATE_BACKEND_GROUP = "createBackendGroup";
	/** local service method declaration */
	public static final String METHOD_DELETE_BACKEND_GROUP = "deleteBackendGroup";
	/** local service method declaration */
	public static final String METHOD_UPDATE_BACKEND_GROUP = "updateBackendGroup";
	/** local service method declaration */
	public static final String METHOD_UNASSOCIATE_BACKEND = "unassociateBackend";
	
	/** service method parameter declaration */
	public static final String PARAMETER_BACKEND_GROUP_ID = "backend_group_id";
	/** service method parameter declaration */
	public static final String PARAMETER_BACKEND_ID = "backend_id";
	
	/* services */
	/** service name declaration */
	public static final String SERVICE_BACKENDS = "backends";
	
	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
