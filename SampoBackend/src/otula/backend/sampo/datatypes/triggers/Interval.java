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
package otula.backend.sampo.datatypes.triggers;

import java.util.Date;

/**
 * Interval condition
 * 
 */
public class Interval {
	private Integer _interval = null;
	private Date _lastTriggered = null;
	
	/**
	 * 
	 * @param interval in seconds
	 */
	public Interval(Integer interval) {
		_interval = interval;
	}

	/**
	 * @return the interval in seconds
	 */
	public Integer getInterval() {
		return _interval;
	}
	
	/**
	 * @return the lastTriggered
	 * @see #setLastTriggered(Date)
	 */
	public Date getLastTriggered() {
		return _lastTriggered;
	}
	
	/**
	 * @param lastTriggered the lastTriggered to set
	 * @see #getLastTriggered()
	 */
	public void setLastTriggered(Date lastTriggered) {
		_lastTriggered = lastTriggered;
	}
}
