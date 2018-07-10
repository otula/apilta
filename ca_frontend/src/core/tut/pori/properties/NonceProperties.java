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
package core.tut.pori.properties;

import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * System property file, which contains the default settings for creating nonce (short-lived unique random string).
 *
 */
public class NonceProperties extends SystemProperty{
	/* properties */
	private static final String PROPERTY_CORE_PORI_UTILS_NONCE_LENGH = PROPERTY_CORE_PORI_UTILS+".nonce_length";
	private static final String PROPERTY_CORE_PORI_UTILS_NONCE_EXPIRES_IN = PROPERTY_CORE_PORI_UTILS+".nonce_expires_in";
	private long _nonceExpiresIn = -1;
	private int _nonceLength = -1;

	@Override
	public void initialize(Properties properties) throws IllegalArgumentException{
		try{
			_nonceLength = Integer.parseInt(properties.getProperty(PROPERTY_CORE_PORI_UTILS_NONCE_LENGH));
			_nonceExpiresIn = Long.parseLong(properties.getProperty(PROPERTY_CORE_PORI_UTILS_NONCE_EXPIRES_IN));
		}catch (NumberFormatException ex){
			Logger.getLogger(getClass()).error(ex, ex);
			throw new IllegalArgumentException("Bad "+PROPERTY_CORE_PORI_UTILS_NONCE_LENGH+" OR "+PROPERTY_CORE_PORI_UTILS_NONCE_EXPIRES_IN);
		}
	}

	/**
	 * @return the nonceLength
	 */
	public int getNonceLength() {
		return _nonceLength;
	}

	/**
	 * @return the maximum time to live for nonce, in ms
	 */
	public long getNonceExpiresIn() {
		return _nonceExpiresIn;
	}
}
