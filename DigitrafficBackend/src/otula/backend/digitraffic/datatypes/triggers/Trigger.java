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

/**
 * when this task should trigger
 *
 */
public class Trigger {
	private Interval _interval = null;
	private Location _location = null;
	private ValidFromTo _validFromTo = null;
	
	/**
	 * @return the location
	 * @see #setLocation(Location)
	 */
	public Location getLocation() {
		return _location;
	}
	
	/**
	 * @param location the location to set
	 * @see #getLocation()
	 */
	public void setLocation(Location location) {
		_location = location;
	}
	
	/**
	 * @return the validFromTo
	 * @see #setValidFromTo(ValidFromTo)
	 */
	public ValidFromTo getValidFromTo() {
		return _validFromTo;
	}
	
	/**
	 * @param validFromTo the validFromTo to set
	 * @see #getValidFromTo()
	 */
	public void setValidFromTo(ValidFromTo validFromTo) {
		_validFromTo = validFromTo;
	}

	/**
	 * @return the interval
	 * @see #setInterval(Interval)
	 */
	public Interval getInterval() {
		return _interval;
	}

	/**
	 * @param interval the interval to set
	 * @see #getInterval()
	 */
	public void setInterval(Interval interval) {
		_interval = interval;
	}
} 
