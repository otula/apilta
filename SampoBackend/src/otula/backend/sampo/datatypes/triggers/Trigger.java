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

/**
 * Trigger condition for the task
 * 
 */
public class Trigger {
	private String _area = null;
	private Interval _interval = null;
	private ValidFromTo _valid = null;
	
	/**
	 * @return the area
	 */
	public String getArea() {
		return _area;
	}
	
	/**
	 * @param area the area to set
	 */
	public void setArea(String area) {
		_area = area;
	}
	
	/**
	 * @return the interval
	 */
	public Interval getInterval() {
		return _interval;
	}
	
	/**
	 * @param interval the interval to set
	 */
	public void setInterval(Interval interval) {
		_interval = interval;
	}
	
	/**
	 * @return the valid
	 */
	public ValidFromTo getValidFromTo() {
		return _valid;
	}
	
	/**
	 * @param valid the valid to set
	 */
	public void setValidFromTo(ValidFromTo valid) {
		_valid = valid;
	}
}
