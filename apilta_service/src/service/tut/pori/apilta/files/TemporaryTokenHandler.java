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
package service.tut.pori.apilta.files;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * temporary file token handler
 * 
 */
public class TemporaryTokenHandler {
	/** default cache validity, in seconds */
	public static final long CACHE_VALIDITY = 900;
	private Cache<String, String> _cache = null; // key: token, value: GUID
	private SecureRandom _random = new SecureRandom();
	
	/**
	 * 
	 */
	public TemporaryTokenHandler(){
		_cache = CacheBuilder.newBuilder().expireAfterAccess(CACHE_VALIDITY, TimeUnit.SECONDS).build();
	}

	/**
	 * 
	 * @param guid
	 * @return existing or newly generated token for the GUID
	 */
	public String getToken(String guid) {
		String token = new BigInteger(130, _random).toString(32); // from http://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
		_cache.put(token, guid);
		return token;
	}

	/**
	 * 
	 * @param token
	 * @return guid for the given token or null if not found
	 */
	public String getGUID(String token) {
		return _cache.getIfPresent(token);
	}
}
