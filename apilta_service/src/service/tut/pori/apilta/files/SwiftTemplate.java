/**
 * Copyright 2015 Tampere University of Technology, Pori Department
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
package service.tut.pori.apilta.files;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jclouds.Constants;
import org.jclouds.ContextBuilder;
import org.jclouds.http.HttpResponseException;
import org.jclouds.io.Payload;
import org.jclouds.openstack.swift.v1.SwiftApi;
import org.jclouds.openstack.swift.v1.TemporaryUrlSigner;
import org.jclouds.openstack.swift.v1.domain.SwiftObject;
import org.jclouds.openstack.swift.v1.features.AccountApi;
import org.jclouds.openstack.swift.v1.features.ContainerApi;
import org.jclouds.openstack.swift.v1.features.ObjectApi;

import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.Definitions;
import service.tut.pori.apilta.ApiltaProperties;
import service.tut.pori.apilta.files.datatypes.SignedURL;

/**
 * Template class for accessing a pre-configured swift instance
 * 
 */
public class SwiftTemplate implements Closeable {
	private static final long CHECK_API_INTERVAL = 1800L;
	private static final Logger LOGGER = Logger.getLogger(SwiftTemplate.class);
	private static final String PARAMETER_TEMP_URI_SIGNATURE = "temp_url_sig";
	private static final String PARAMETER_TEMP_URI_EXPIRATION = "temp_url_expires";
	private boolean _checkContainer = true;
	private ObjectApi _objectApi = null;
	private ApiltaProperties _properties = null;
	private Set<String> _regions = null;
	private SwiftApi _swiftApi = null;
	private TemporaryUrlSigner _tempUriSigner = null;
	
	/**
	 * 
	 */
	public SwiftTemplate() {
		initialize();
	}
	
	/**
	 * 
	 */
	private void initialize(){
		LOGGER.debug("Initializing Swift handler...");
		
		_properties = ServiceInitializer.getPropertyHandler().getSystemProperties(ApiltaProperties.class);
		
		Properties overrides = new Properties();
		overrides.setProperty(Constants.PROPERTY_RETRY_DELAY_START, 500 + "");
		overrides.setProperty(Constants.PROPERTY_MAX_RETRIES, 5 + "");
		
		_swiftApi = ContextBuilder.newBuilder(_properties.getProvider())
					.endpoint(_properties.getEndpoint())
					.credentials(_properties.getIdentity(), _properties.getCredential())
					.overrides(overrides)
					.buildApi(SwiftApi.class);
	}

	@Override
	public void close() {
		LOGGER.debug("Closing...");
		
		_tempUriSigner = null;
		
		_objectApi = null;
		try {
			_swiftApi.close();
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
		_swiftApi = null;

		//reset members
		_regions = null;
		_checkContainer = true;
	}
	
	/**
	 * 
	 * @param objectName
	 * @return details for the object matching the given GUID or null if object was not found
	 */
	public SwiftObject get(String objectName) {
		return getObjectApi().getWithoutBody(objectName); //TODO add try-catch for connection refused situation, maybe try to change to another region?
	}

	/**
	 * 
	 * @param objectName
	 */
	public void delete(String objectName) {
		getObjectApi().delete(objectName);	//TODO add try-catch for connection refused situation, maybe try to change to another region?
	}

	/**
	 * Returns the object for accessing files on Swift storage based on resolved region and configure container.
	 * @return Swift Object API
	 */
	private ObjectApi getObjectApi(){
		//TODO: the _objectApi (like any other swift API) might get stuck for some unknown reason. The best effort work around for this could just be re-instantiating the whole SwiftTemplate.
		if(_objectApi == null){	//empty on first call as it is loaded on the first use of swift template
			if(_checkContainer){ //just a safe guard to make sure required container exists
				prepareContainer();
			}
			_objectApi = _swiftApi.getObjectApi(resolveRegion(), _properties.getContainer());
		}
		return _objectApi;
	}
	
	/**
	 * <p>Checks for the required container, and creates it if it doesn't exist.</p>
	 * <p>Make the method synchronized just in case.</p>
	 */
	private synchronized void prepareContainer(){
		_checkContainer = false;	//disable future checks for container
		//Make sure that the needed container exists
		ContainerApi containerApi = _swiftApi.getContainerApi(resolveRegion());
		containerApi.create(_properties.getContainer());	//returns true if the container was created, false if the container already existed.
		//regardless of the return value of the previous method call, the requested container should now be available
	}
	
	/**
	 * <p>This function returns the preferred region for this service instance if configured. 
	 * Otherwise the first configured region is returned.</p>
	 * <p>Note that this function does not support multi region usage, i.e. the same selected region is used until the end of the world.</p>
	 * @return region name
	 */
	private String resolveRegion(){
		if(_regions == null){
			try{
				_regions = _swiftApi.getConfiguredRegions();
			}catch(HttpResponseException ex){
				LOGGER.error(ex, ex);
				throw new RuntimeException("SwiftAPI refused connection.");
			}
		}
		if(_regions.contains(_properties.getPreferredRegion())){
			return _properties.getPreferredRegion();	//return the preferred region, if available
		}else{
			return _regions.iterator().next();	//return the first region in any other case (as a default choice)
		}
	}

	/**
	 * 
	 * @param objectName
	 * @param payload
	 * @return ETag of the object
	 */
	public String put(String objectName, Payload payload) {
		return getObjectApi().put(objectName, payload); //TODO add try-catch for connection refused situation, maybe try to change to another region?
	}

	/**
	 * 
	 * @param object 
	 * @return temporary url signed by this template
	 */
	public SignedURL signURL(SwiftObject object) {
		if(_tempUriSigner == null){
			AccountApi accountApi = _swiftApi.getAccountApi(resolveRegion());
			_tempUriSigner = TemporaryUrlSigner.checkApiEvery(accountApi, CHECK_API_INTERVAL);
		}
		long expires = System.currentTimeMillis() / 1000 + _properties.getDefaultExpirationTime();
		String signature = _tempUriSigner.sign(Definitions.METHOD_GET, object.getUri().getPath(), expires);
		//TODO for some reason jclouds want to use public ip instead of internal ip when communicating service endpoints, which is kinda silly
		//TODO Also this workaround defeats the point of having multiple regions (as it is hardcoded to a one endpoint).
		return new SignedURL(_properties.getPublicEndpoint()+object.getUri().getPath()+Definitions.SEPARATOR_URI_METHOD_PARAMS+PARAMETER_TEMP_URI_SIGNATURE+Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE+signature+Definitions.SEPARATOR_URI_QUERY_PARAMS+PARAMETER_TEMP_URI_EXPIRATION+Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE+expires, new Date(expires*1000));	
	}
	
	/**
	 * 
	 * @param objectName 
	 * @param metadata 
	 */
	public void update(String objectName, Map<String, String> metadata){
		getObjectApi().updateMetadata(objectName, metadata); //TODO check what this does if the objectName is invalid or non-existent
	}
}
