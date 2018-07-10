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
package service.tut.pori.apilta;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.properties.SystemProperty;

/**
 * Apilta Properties
 *
 */
public class ApiltaProperties extends SystemProperty {
	private static final String PROPERTY_SERVICE_TUT_PORI_APILTA = PROPERTY_SERVICE_PORI+".apilta";
	private static final String PROPERTY_SERVICE_TUT_PORI_APILTA_ALERT_REMOVE_THRESHOLD = PROPERTY_SERVICE_TUT_PORI_APILTA+".alert.default_remove_threshold";
	private static final String PROPERTY_SERVICE_TUT_PORI_APILTA_ALERT_VALIDITY_TIME = PROPERTY_SERVICE_TUT_PORI_APILTA+".alert.default_validity_time";
	private static final String PROPERTY_SERVICE_TUT_PORI_APILTA_CONTAINER = PROPERTY_SERVICE_TUT_PORI_APILTA+".swift.container";
	private static final String PROPERTY_SERVICE_TUT_PORI_APILTA_CREDENTIAL = PROPERTY_SERVICE_TUT_PORI_APILTA+".swift.credential";
	private static final String PROPERTY_SERVICE_TUT_PORI_APILTA_DEFAULT_EXPIRATION_TIME = PROPERTY_SERVICE_TUT_PORI_APILTA+".swift.default_expiration_time";
	private static final String PROPERTY_SERVICE_TUT_PORI_APILTA_ENDPOINT = PROPERTY_SERVICE_TUT_PORI_APILTA+".swift.endpoint";
	private static final String PROPERTY_SERVICE_TUT_PORI_APILTA_IDENTITY = PROPERTY_SERVICE_TUT_PORI_APILTA+".swift.identity";
	private static final String PROPERTY_SERVICE_TUT_PORI_APILTA_PREFERRED_REGION = PROPERTY_SERVICE_TUT_PORI_APILTA+".swift.preferred_region";
	private static final String PROPERTY_SERVICE_TUT_PORI_APILTA_PROVIDER = PROPERTY_SERVICE_TUT_PORI_APILTA+".swift.provider";
	private static final String PROPERTY_SERVICE_TUT_PORI_APILTA_PUBLIC_ENDPOINT = PROPERTY_SERVICE_TUT_PORI_APILTA+".swift.public_endpoint";
	private static final String PROPERTY_SERVICE_TUT_PORI_APILTA_SHOCK_GROUP_RANGE = PROPERTY_SERVICE_TUT_PORI_APILTA+".shock.group_range";
	private static final String PROPERTY_SERVICE_TUT_PORI_APILTA_SHOCK_TIME_GROUP_DIFFERENCE = PROPERTY_SERVICE_TUT_PORI_APILTA+".shock.group_time_difference";
	private int _alertRemoveThreshold = 5;
	private long _alertValidityTime = 10000;
	private String _container = null;
	private String _credential = null;
	private long _defaultExpirationTime = 900;	//900 seconds, 15 minutes
	private String _endpoint = null;
	private String _identity = null;
	private String _preferredRegion = null;
	private String _provider = null;
	private String _publicEndpoint = null;
	private double _shockGroupRange = 0;
	private long _shockGroupTimeDifference = 0;
	
	@Override
	public void initialize(Properties properties) throws IllegalArgumentException {
		_container = properties.getProperty(PROPERTY_SERVICE_TUT_PORI_APILTA_CONTAINER);
		if(StringUtils.isBlank(_container)){
			throw new IllegalArgumentException("Bad "+PROPERTY_SERVICE_TUT_PORI_APILTA_CONTAINER);
		}
		_credential = properties.getProperty(PROPERTY_SERVICE_TUT_PORI_APILTA_CREDENTIAL);
		if(StringUtils.isBlank(_credential)){
			throw new IllegalArgumentException("Bad "+PROPERTY_SERVICE_TUT_PORI_APILTA_CREDENTIAL);
		}
		_endpoint = properties.getProperty(PROPERTY_SERVICE_TUT_PORI_APILTA_ENDPOINT);
		if(StringUtils.isBlank(_endpoint)){
			throw new IllegalArgumentException("Bad "+PROPERTY_SERVICE_TUT_PORI_APILTA_ENDPOINT);
		}
		_identity = properties.getProperty(PROPERTY_SERVICE_TUT_PORI_APILTA_IDENTITY);
		if(StringUtils.isBlank(_identity)){
			throw new IllegalArgumentException("Bad "+PROPERTY_SERVICE_TUT_PORI_APILTA_IDENTITY);
		}
		_preferredRegion = properties.getProperty(PROPERTY_SERVICE_TUT_PORI_APILTA_PREFERRED_REGION, "");	//can be left blank on the .properties file
		_provider = properties.getProperty(PROPERTY_SERVICE_TUT_PORI_APILTA_PROVIDER);
		if(StringUtils.isBlank(_provider)){
			throw new IllegalArgumentException("Bad "+PROPERTY_SERVICE_TUT_PORI_APILTA_PROVIDER);
		}
		_publicEndpoint = properties.getProperty(PROPERTY_SERVICE_TUT_PORI_APILTA_PUBLIC_ENDPOINT);
		if(StringUtils.isBlank(_publicEndpoint)){
			throw new IllegalArgumentException("Bad "+PROPERTY_SERVICE_TUT_PORI_APILTA_PUBLIC_ENDPOINT);
		}
		try{
			_defaultExpirationTime = Long.parseLong(properties.getProperty(PROPERTY_SERVICE_TUT_PORI_APILTA_DEFAULT_EXPIRATION_TIME));
			_alertRemoveThreshold = Integer.parseInt(properties.getProperty(PROPERTY_SERVICE_TUT_PORI_APILTA_ALERT_REMOVE_THRESHOLD));
			_alertValidityTime = Long.parseLong(properties.getProperty(PROPERTY_SERVICE_TUT_PORI_APILTA_ALERT_VALIDITY_TIME));
			_shockGroupTimeDifference = Long.parseLong(properties.getProperty(PROPERTY_SERVICE_TUT_PORI_APILTA_SHOCK_TIME_GROUP_DIFFERENCE));
			_shockGroupRange = Double.parseDouble(properties.getProperty(PROPERTY_SERVICE_TUT_PORI_APILTA_SHOCK_GROUP_RANGE)) / 1000;
		} catch (NullPointerException | NumberFormatException ex) {
			Logger.getLogger(getClass()).debug(ex, ex);
			throw new IllegalArgumentException("Bad "+PROPERTY_SERVICE_TUT_PORI_APILTA_SHOCK_TIME_GROUP_DIFFERENCE+", "+PROPERTY_SERVICE_TUT_PORI_APILTA_DEFAULT_EXPIRATION_TIME+", "+PROPERTY_SERVICE_TUT_PORI_APILTA_ALERT_REMOVE_THRESHOLD+", "+PROPERTY_SERVICE_TUT_PORI_APILTA_SHOCK_GROUP_RANGE+" or "+PROPERTY_SERVICE_TUT_PORI_APILTA_ALERT_VALIDITY_TIME);
		}
	}
	
	@Override
	public String getPropertyFilePath() {
		return ServiceInitializer.getConfigHandler().getPropertyFilePath()+"apilta.properties";
	}

	/**
	 * @return the container
	 */
	public String getContainer() {
		return _container;
	}

	/**
	 * @return the credential
	 */
	public String getCredential() {
		return _credential;
	}

	/**
	 * @return the defaultExpirationTime
	 */
	public long getDefaultExpirationTime() {
		return _defaultExpirationTime;
	}

	/**
	 * @return the endpoint
	 */
	public String getEndpoint() {
		return _endpoint;
	}

	/**
	 * @return the identity
	 */
	public String getIdentity() {
		return _identity;
	}
	
	/**
	 * @return the preferredRegion
	 */
	public String getPreferredRegion() {
		return _preferredRegion;
	}

	/**
	 * @return the provider
	 */
	public String getProvider() {
		return _provider;
	}

	/**
	 * @return the publicEndpoint
	 */
	public String getPublicEndpoint() {
		return _publicEndpoint;
	}

	/**
	 * @return the default threshold until an alert is removed after negative reports have been removed
	 */
	public int getAlertRemoveThreshold() {
		return _alertRemoveThreshold;
	}

	/**
	 * @return the default time a new alert is valid (in minutes)
	 */
	public long getAlertValidityTime() {
		return _alertValidityTime;
	}

	/**
	 * @return the shockGroupTimeDifference
	 */
	public long getShockGroupTimeDifference() {
		return _shockGroupTimeDifference;
	}

	/**
	 * @return the default range used for grouping shock measurements (in km)
	 */
	public double getShockGroupRange() {
		return _shockGroupRange;
	}
}
