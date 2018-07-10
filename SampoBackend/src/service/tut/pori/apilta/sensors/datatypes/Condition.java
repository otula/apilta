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
package service.tut.pori.apilta.sensors.datatypes;

import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

/**
 * Answers to the question "when this task is active?"
 *
 */
@XmlRootElement(name=Definitions.ELEMENT_CONDITION)
@XmlAccessorType(XmlAccessType.NONE)
public class Condition {
	@XmlElementWrapper(name = Definitions.ELEMENT_TERMS)
	private Map<String, String> _conditions = null;
	
	/**
	 * @return the conditions
	 * @see #setConditions(Map)
	 */
	public Map<String, String> getConditions() {
		return _conditions;
	}
	
	/**
	 * @param conditions the conditions to set
	 * @see #getConditions()
	 */
	public void setConditions(Map<String, String> conditions) {
		_conditions = conditions;
	}

	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if valid
	 * @see #isValid(Condition)
	 */
	protected boolean isValid() {
		if(_conditions == null || _conditions.isEmpty()){
			return false;
		}
		
		for(Entry<String, String> entry : _conditions.entrySet()){
			if(StringUtils.isBlank(entry.getKey()) || StringUtils.isBlank(entry.getValue())){
				return false;
			}
		}
		
		return true;
	}

	/**
	 * 
	 * @param condition
	 * @return false if condition is null or invalid
	 */
	public static boolean isValid(Condition condition) {
		if(condition == null){
			return false;
		}else{
			return condition.isValid();
		}
	}
}
