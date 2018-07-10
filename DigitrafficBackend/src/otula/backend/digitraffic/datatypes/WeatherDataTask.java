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
package otula.backend.digitraffic.datatypes;

import java.util.List;

import otula.backend.digitraffic.datatypes.triggers.Trigger;

/**
 * details for a weather data task
 * 
 */
public class WeatherDataTask {
	private Long _backendId = null;
	private String _callbackUri = null;
	private List<String> features = null;
	private boolean _includeLocation = false;
	private String _taskId = null;
	private List<Trigger> _triggers = null;
	
	/**
	 * @return the backendId
	 */
	public Long getBackendId() {
		return _backendId;
	}
	
	/**
	 * @param backendId the backendId to set
	 */
	public void setBackendId(Long backendId) {
		_backendId = backendId;
	}

	/**
	 * @return the callbackUri
	 */
	public String getCallbackUri() {
		return _callbackUri;
	}

	/**
	 * @param callbackUri the callbackUri to set
	 */
	public void setCallbackUri(String callbackUri) {
		_callbackUri = callbackUri;
	}

	/**
	 * @return the features
	 */
	public List<String> getFeatures() {
		return features;
	}

	/**
	 * @param features the features to set
	 */
	public void setFeatures(List<String> features) {
		this.features = features;
	}

	/**
	 * @return the taskId
	 */
	public String getTaskId() {
		return _taskId;
	}

	/**
	 * @param taskId the taskId to set
	 */
	public void setTaskId(String taskId) {
		_taskId = taskId;
	}
	
	/**
	 * @return the triggers
	 */
	public List<Trigger> getTriggers() {
		return _triggers;
	}

	/**
	 * @param triggers the triggers to set
	 */
	public void setTriggers(List<Trigger> triggers) {
		_triggers = triggers;
	}

	/**
	 * @return the includeLocation
	 */
	public boolean isIncludeLocation() {
		return _includeLocation;
	}

	/**
	 * @param includeLocation the includeLocation to set
	 */
	public void setIncludeLocation(boolean includeLocation) {
		_includeLocation = includeLocation;
	}
}
