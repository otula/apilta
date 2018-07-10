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

/**
 * Definitions for the users package.
 * 
 */
public final class Definitions {
	
	/* elements */
	/** xml element declaration */
	public static final String ELEMENT_DESCRIPTION = "description";
	/** xml element declaration */
	public static final String ELEMENT_EXTERNAL_ACCOUNT_CONNECTION = "externalAccountConnection";
	/** xml element declaration */
	public static final String ELEMENT_EXTERNAL_ID = "externalId";
	/** xml element declaration */
	public static final String ELEMENT_GROUP_ID = "groupId";
	/** xml element declaration */
	public static final String ELEMENT_NAME = "name";
	/** xml element declaration */
	public static final String ELEMENT_PERMISSION_LIST = "permissionList";
	/** xml element declaration */
	public static final String ELEMENT_SERVICE_TYPE = "serviceType";
	/** xml element declaration */
	public static final String ELEMENT_USER_ID = "userId";
	/** xml element declaration */
	public static final String ELEMENT_USER_IDENTITY = "userDetails";
	/** xml element declaration */
	public static final String ELEMENT_USER_IDENTITY_LIST = "userDetailsList";
	/** xml element declaration */
	public static final String ELEMENT_USER_GROUP = "userGroup";
	/** xml element declaration */
	public static final String ELEMENT_USER_GROUP_LIST = "userGroupList";
	/** xml element declaration */
	public static final String ELEMENT_USERNAME = "username";
	
	/* service types */
	/** service type name declaration */
	protected static final String SERVICE_TYPE_GOOGLE = "GOOGLE";
	/** service type name declaration */
	protected static final String SERVICE_TYPE_FACEBOOK = "FACEBOOK";
	/** service type name declaration */
	protected static final String SERVICE_TYPE_OTHER= "OTHER";
	/** service type name declaration */
	protected static final String SERVICE_TYPE_TWITTER = "TWITTER";
	
	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
