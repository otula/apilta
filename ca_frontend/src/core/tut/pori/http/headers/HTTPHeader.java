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
package core.tut.pori.http.headers;

import org.apache.commons.lang3.StringUtils;

/**
 * A HTTPHeader, as defined by http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
 *
 */
public class HTTPHeader {
	private String _name = null;
	private String _value = null;
	
	/**
	 * The inherited class should have no-args default constructor
	 */
	public HTTPHeader(){
		// nothing needed
	}
	
	/**
	 * 
	 * @return true if this parameter has one or more values
	 */
	public boolean hasValue(){
		return !StringUtils.isBlank(_value);
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return _value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		_value = value;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		_name = name;
	}
}
