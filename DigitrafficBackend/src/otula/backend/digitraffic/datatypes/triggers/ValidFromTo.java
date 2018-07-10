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
package otula.backend.digitraffic.datatypes.triggers;

import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * time validity period
 *
 */
public class ValidFromTo {
	private Date _from = null;
	private Date _to = null;
	
	/**
	 * 
	 * @param from
	 * @param to
	 */
	public ValidFromTo(Date from, Date to) {
		_from = from;
		_to = to;
	}
	
	/**
	 * construct from validity string ISO8601_DATE/ISO8601_DATE
	 * 
	 * @param fromTo
	 * @throws IllegalArgumentException 
	 */
	public ValidFromTo(String fromTo) throws IllegalArgumentException {
		String[] parts = StringUtils.split(fromTo, '/');
		if(ArrayUtils.getLength(parts) != 2){
			throw new IllegalArgumentException("Invalid fromTo string: "+fromTo);
		}
		_from = core.tut.pori.utils.StringUtils.ISOStringToDate(parts[0]);
		_to = core.tut.pori.utils.StringUtils.ISOStringToDate(parts[1]);
	}

	/**
	 * @return the from
	 */
	public Date getFrom() {
		return _from;
	}

	/**
	 * @return the to
	 */
	public Date getTo() {
		return _to;
	}
}
