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
package service.tut.pori.apilta.files.datatypes;

import java.util.Date;

/**
 * contains a signed url and the validity time
 *
 */
public class SignedURL {
	private String _url = null;
	private Date _validUntil = null;
	
	/**
	 * 
	 * @param url
	 * @param validUntil
	 */
	public SignedURL(String url, Date validUntil) {
		_url = url;
		_validUntil = validUntil;
	}
	
	/**
	 * @return the url
	 */
	public String getUrl() {
		return _url;
	}
	
	/**
	 * @return the validUntil
	 */
	public Date getValidUntil() {
		return _validUntil;
	}
}
