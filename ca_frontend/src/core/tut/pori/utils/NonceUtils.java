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
package core.tut.pori.utils;

import org.apache.commons.text.RandomStringGenerator;

import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.properties.NonceProperties;

/**
 * Methods for generating nonce
 *
 */
public final class NonceUtils {
	private static final RandomStringGenerator GENERATOR = new RandomStringGenerator.Builder().withinRange('a', 'z').build();
	
	/**
	 * 
	 * @return unique nonce string
	 */
	public static Nonce generateNonce(){
		NonceProperties np = ServiceInitializer.getPropertyHandler().getSystemProperties(NonceProperties.class);
		return new Nonce(System.currentTimeMillis()+np.getNonceExpiresIn(), GENERATOR.generate(np.getNonceLength()));
	}
	
	/**
	 * A class presenting a generated nonce.
	 *
	 */
	public static class Nonce{
		private long _expires = -1;
		private String _nonce = null;
		
		/**
		 * 
		 * @param expires expiration date in unix time
		 * @param nonce
		 */
		public Nonce(long expires, String nonce){
			_nonce = nonce;
			_expires = expires;
		}

		/**
		 * @return the nonce
		 */
		public String getNonce() {
			return _nonce;
		}

		/**
		 * @return the expires in unix time
		 */
		public long getExpires() {
			return _expires;
		}
	}	// class Nonce
}
