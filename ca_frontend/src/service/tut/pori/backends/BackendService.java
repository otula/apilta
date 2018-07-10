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

import service.tut.pori.backends.datatypes.Backend;
import service.tut.pori.backends.datatypes.BackendGroup;
import service.tut.pori.backends.datatypes.BackendGroupList;
import service.tut.pori.backends.datatypes.BackendList;
import core.tut.pori.http.Response;
import core.tut.pori.http.Response.Status;
import core.tut.pori.http.annotations.HTTPAuthenticationParameter;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.parameters.AuthenticationParameter;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.InputStreamParameter;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.http.parameters.LongParameter;
import core.tut.pori.utils.XMLFormatter;

/**
 * service declarations for the back end configuration service
 * 
 */
@HTTPService(name=Definitions.SERVICE_BACKENDS)
public class BackendService {
	private XMLFormatter _formatter = new XMLFormatter();
	
	/**
	 * The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".
	 * 
	 * @param authenticatedUser
	 * @param backendGroupId group for the back end
	 * @param xml the details of the new back end. See {@link service.tut.pori.backends.datatypes.Backend}
	 * @return response the details of the created back end with the automatically generated back end identifier included.
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_CREATE_BACKEND, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response createBackend(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=Definitions.PARAMETER_BACKEND_GROUP_ID) LongParameter backendGroupId,
			@HTTPMethodParameter(name=InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter=true) InputStreamParameter xml
			)
	{
		Long backendId = BackendsCore.createBackend(authenticatedUser.getUserIdentity(), _formatter.toObject(xml.getValue(), Backend.class), backendGroupId.getValue());
		if(backendId == null){
			return new Response(Status.FORBIDDEN);
		}
		Backend backend = new Backend();
		backend.setBackendId(backendId);
		return new Response(new BackendList(backend));
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param backendId
	 * @return response
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_DELETE_BACKEND, acceptedMethods={core.tut.pori.http.Definitions.METHOD_DELETE})
	public Response deleteBackend(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=service.tut.pori.tasks.Definitions.PARAMETER_BACKEND_ID) LongParameter backendId
			)
	{
		if(BackendsCore.deleteBackend(authenticatedUser.getUserIdentity(), backendId.getValues())){
			return new Response();
		}else{
			return new Response(Status.FORBIDDEN);
		}
	}
	
	/**
	 * The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".
	 * 
	 * @param authenticatedUser
	 * @param xml the details of the back end to be updated. See {@link service.tut.pori.backends.datatypes.Backend}
	 * @return response
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_UPDATE_BACKEND, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response updateBackend(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter=true) InputStreamParameter xml
			)
	{
		if(BackendsCore.updateBackend(authenticatedUser.getUserIdentity(), _formatter.toObject(xml.getValue(), Backend.class))){
			return new Response();
		}else{
			return new Response(Status.FORBIDDEN);
		}
	}
	
	/**
	 * The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".
	 * 
	 * @param authenticatedUser
	 * @param xml the details of the back end group to be created. See {@link service.tut.pori.backends.datatypes.BackendGroup}
	 * @return response
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_CREATE_BACKEND_GROUP, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response createBackendGroup(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter=true) InputStreamParameter xml
			)
	{
		BackendGroup backendGroup = _formatter.toObject(xml.getValue(), BackendGroup.class);
		Long id = BackendsCore.createBackendGroup(authenticatedUser.getUserIdentity(), backendGroup);
		if(id == null){
			return new Response(Status.FORBIDDEN);
		}else{
			backendGroup.setBackendGroupId(id);
			return new Response(new BackendGroupList(backendGroup));
		}
	}
	
	/**
	 * Delete one or more back end groups. 
	 * 
	 * Note that removing a back end <i>might</i> also remove one or more back ends,
	 * because back ends cannot exist without a back end group (deleting back end's last group will also delete the back end).
	 * 
	 * @param authenticatedUser
	 * @param backendGroupId
	 *@return response
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_DELETE_BACKEND_GROUP, acceptedMethods={core.tut.pori.http.Definitions.METHOD_DELETE})
	public Response deleteBackendGroup(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=Definitions.PARAMETER_BACKEND_GROUP_ID) LongParameter backendGroupId
			)
	{
		if(BackendsCore.deleteBackendGroup(authenticatedUser.getUserIdentity(), backendGroupId.getValues())){
			return new Response();
		}else{
			return new Response(Status.FORBIDDEN);
		}
	}
	
	/**
	 * The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".The request is to be sent in the body of POST method. The Content-Type header MUST be set to "text/xml". The character set MUST be UTF-8. For example, "Content-Type: text/xml; charset=UTF-8".
	 * 
	 * @param authenticatedUser
	 * @param xml the details of the back end group to be created. See {@link service.tut.pori.backends.datatypes.BackendGroup}
	 * @return response
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_UPDATE_BACKEND_GROUP, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response updateBackendGroup(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=InputStreamParameter.PARAMETER_DEFAULT_NAME, bodyParameter=true) InputStreamParameter xml
			)
	{
		if(BackendsCore.updateBackendGroup(authenticatedUser.getUserIdentity(), _formatter.toObject(xml.getValue(), BackendGroup.class))){
			return new Response();
		}else{
			return new Response(Status.FORBIDDEN);
		}
	}
	
	/**
	 * Associate any number of back ends with any number of back end groups.
	 * 
	 * @param authenticatedUser
	 * @param backendGroupId
	 * @param backendId
	 * @return response
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_ASSOCIATE_BACKEND, acceptedMethods={core.tut.pori.http.Definitions.METHOD_POST})
	public Response associateBackend(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=Definitions.PARAMETER_BACKEND_GROUP_ID) LongParameter backendGroupId,
			@HTTPMethodParameter(name=Definitions.PARAMETER_BACKEND_ID) LongParameter backendId
			)
	{
		if(BackendsCore.associateBackends(authenticatedUser.getUserIdentity(), backendGroupId.getValues(), backendId.getValues())){
			return new Response();
		}else{
			return new Response(Status.FORBIDDEN);
		}
	}
	
	/**
	 * Unassociate any number of back ends from any number of back end groups.
	 * 
	 * @param authenticatedUser
	 * @param backendGroupId
	 * @param backendId
	 * @return response
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_UNASSOCIATE_BACKEND, acceptedMethods={core.tut.pori.http.Definitions.METHOD_DELETE})
	public Response unassociateBackend(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=Definitions.PARAMETER_BACKEND_GROUP_ID) LongParameter backendGroupId,
			@HTTPMethodParameter(name=Definitions.PARAMETER_BACKEND_ID) LongParameter backendId
			)
	{
		if(BackendsCore.unassociateBackends(authenticatedUser.getUserIdentity(), backendGroupId.getValues(), backendId.getValues())){
			return new Response();
		}else{
			return new Response(Status.FORBIDDEN);
		}
	}
	
	/**
	 * For acceptable data groups see {@link BackendsCore#getBackendGroups(core.tut.pori.users.UserIdentity, long[], DataGroups, Limits)}
	 * 
	 * @param authenticatedUser
	 * @param backendGroupId
	 * @param dataGroups 
	 * @param limits
	 * @return list of back end groups matching the given values
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_GET_BACKEND_GROUPS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response getBackendGroups(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=Definitions.PARAMETER_BACKEND_GROUP_ID, required=false) LongParameter backendGroupId,
			@HTTPMethodParameter(name=DataGroups.PARAMETER_DEFAULT_NAME, required=false) DataGroups dataGroups,
			@HTTPMethodParameter(name=Limits.PARAMETER_DEFAULT_NAME, required=false) Limits limits
			)
	{
		return new Response(BackendsCore.getBackendGroups(authenticatedUser.getUserIdentity(), backendGroupId.getValues(), dataGroups, limits));
	}
	
	/**
	 * 
	 * @param authenticatedUser
	 * @param backendId
	 * @param limits
	 * @return list of back ends matching the given values
	 */
	@HTTPServiceMethod(name=Definitions.METHOD_GET_BACKENDS, acceptedMethods={core.tut.pori.http.Definitions.METHOD_GET})
	public Response getBackends(
			@HTTPAuthenticationParameter AuthenticationParameter authenticatedUser,
			@HTTPMethodParameter(name=Definitions.PARAMETER_BACKEND_ID, required=false) LongParameter backendId,
			@HTTPMethodParameter(name=Limits.PARAMETER_DEFAULT_NAME, required=false) Limits limits
			)
	{
		return new Response(BackendsCore.getBackends(authenticatedUser.getUserIdentity(), backendId.getValues(), limits));
	}
}
