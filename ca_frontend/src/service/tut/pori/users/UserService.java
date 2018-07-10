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

import core.tut.pori.http.Response;
import core.tut.pori.http.Response.Status;
import core.tut.pori.http.annotations.HTTPAuthenticationParameter;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.AuthenticationParameter;
import core.tut.pori.http.parameters.InputStreamParameter;
import core.tut.pori.http.parameters.LongParameter;
import core.tut.pori.http.parameters.StringParameter;
import core.tut.pori.users.ExternalAccountConnection.UserServiceType;
import core.tut.pori.users.UserGroup;
import core.tut.pori.users.UserGroupList;
import core.tut.pori.users.UserIdentityList;
import core.tut.pori.utils.XMLFormatter;
import service.tut.pori.users.Registration.RegistrationStatus;

/**
 * User Service method declarations.
 * 
 * This chapter defines the APIs for registering a new account with the system, removing an existing account and retrieving the basic user details known by the system as well as retrieving a listing of the external accounts connected to a user account
 *
 */
@HTTPService(name=Definitions.SERVICE_USERS) 
public class UserService {
	private XMLFormatter _formatter = new XMLFormatter();
	
	/**
	 * The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".
	 * 
	 * @param xml Details of the new user. See {@link service.tut.pori.users.Registration}
	 * @return response
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_REGISTER, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response register(@HTTPMethodParameter(name=InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter=true) InputStreamParameter xml) {
		RegistrationStatus status = UserCore.register(_formatter.toObject(xml.getValue(), Registration.class));
		if(status == RegistrationStatus.OK){
			return new Response(); // 200 OK
		}else{
			Response r = new Response(Status.BAD_REQUEST);
			r.setMessage(status.toStatusString());
			return r;
		}	
	}
	
	/**
	 * This method will immediately revoke user login permissions for the authenticated user and schedule the removal of all content owned by the user. The content is removed in phases (service-by-service), and may not happen immediately.
	 * 
	 * @param authenticatedUser
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_UNREGISTER, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public void unregister(@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser) {
		UserCore.unregister(authenticatedUser.getUserIdentity());
	}
	
	/**
	 * This method can be used to retrieve all external account connections known by the system for the authenticated user.
	 * 
	 * @param authenticatedUser
	 * @param serviceTypes optional service type filter {@link core.tut.pori.users.ExternalAccountConnection.UserServiceType}
	 * @return See {@link service.tut.pori.users.ExternalAccountConnectionList}
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_GET_EXTERNAL_ACCOUNT_CONNECTIONS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response getExternalAccountConnections(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_SERVICE_TYPE, required = false) StringParameter serviceTypes) 
	{
		return new Response(UserCore.getExternalAccountConnections(UserServiceType.fromUserServiceTypeStrings(serviceTypes.getValues()), authenticatedUser.getUserIdentity()));
	}
	
	/**
	 * This method can be used to remove external account connection for the authenticated user.
	 * 
	 * @param authenticatedUser
	 * @param serviceTypes the service to remove {@link core.tut.pori.users.ExternalAccountConnection.UserServiceType}
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_DELETE_EXTERNAL_ACCOUNT_CONNECTION, acceptedMethods = {core.tut.pori.http.Definitions.METHOD_DELETE})
	public void deleteExternalAccountConnection(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name = Definitions.PARAMETER_SERVICE_TYPE) StringParameter serviceTypes) 
	{
		UserCore.deleteExternalAccountConnections(UserServiceType.fromUserServiceTypeStrings(serviceTypes.getValues()), authenticatedUser.getUserIdentity());
	}
	
	/**
	 * This method can be used to retrieve details for users.
	 * 
	 * @param authenticatedUser
	 * @param userIdFilter If parameter is missing, details for the authenticated user will be returned.
	 * @return See {@link core.tut.pori.users.UserIdentityList}
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_GET_USER_DETAILS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response getUserDetails(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=Definitions.PARAMETER_USER_ID, required=false) LongParameter userIdFilter
			) 
	{
		UserIdentityList list = UserCore.getUserDetails(authenticatedUser.getUserIdentity(), userIdFilter.getValues());
		if(UserIdentityList.isEmpty(list)){ // if the details could not be retrieved, the user either tried to retrieve non-existing ids or ids he/she does not have permissions to access
			return new Response(Status.FORBIDDEN);
		}else{
			return new Response(list);
		}
	}
	
	/**
	 * This method can be used to retrieve user groups.
	 * 
	 * @param authenticatedUser
	 * @param userGroupIdFilter If parameter is missing, details for the authenticated user will be returned.
	 * @return See {@link core.tut.pori.users.UserGroupList}
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_GET_USER_GROUPS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response getUserGroups(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=Definitions.PARAMETER_USER_GROUP_ID, required=false) LongParameter userGroupIdFilter
			) 
	{
		return new Response(UserCore.getUserGroups(authenticatedUser.getUserIdentity(), userGroupIdFilter.getValues()));
	}
	
	/**
	 * The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".
	 * @param authenticatedUser 
	 * 
	 * @param xml Details of the user group. See {@link core.tut.pori.users.UserGroup}
	 * @return response
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_CREATE_USER_GROUP, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response createUserGroup(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter=true) InputStreamParameter xml
			) 
	{
				Long userGroupId = UserCore.createUserGroup(authenticatedUser.getUserIdentity(), _formatter.toObject(xml.getValue(), UserGroup.class));
		if(userGroupId == null){
			return new Response(Status.FORBIDDEN);
		}
		
		UserGroup group = new UserGroup();
		group.setGroupId(userGroupId);
		UserGroupList groupList = new UserGroupList();
		groupList.addUserGroup(group);
		Response r = new Response(groupList);
		return r;
	}
	
	/**
	 * The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".
	 * @param authenticatedUser 
	 * 
	 * @param xml Details of the user group. See {@linkcore.tut.pori.users.UserGroup}
	 * @return response
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_MODIFY_USER_GROUP, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response modifyUserGroup(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter=true) InputStreamParameter xml
			) 
	{
		if(!UserCore.modifyUserGroup(authenticatedUser.getUserIdentity(), _formatter.toObject(xml.getValue(), UserGroup.class))){
			return new Response(Status.FORBIDDEN);
		}else{
			return new Response();
		}
	}
}
