/**
 * Copyright 2017 Tampere University of Technology, Pori Department
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
package otula.backend.parking.datatypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Simple data
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_SIMPLE_DATA)
@XmlAccessorType(XmlAccessType.NONE)
public class SimpleData {
	@XmlAttribute(name = Definitions.ATTRIBUTE_NAME)
	private String _name = null;
	@XmlValue
	private String _value = null;
	
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
}
