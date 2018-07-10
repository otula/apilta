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
package service.tut.pori.backends.datatypes;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import core.tut.pori.http.parameters.DataGroups;
import service.tut.pori.backends.Definitions;

/**
 * representation of a back end
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_BACKEND)
@XmlAccessorType(XmlAccessType.NONE)
public class Backend {
	private static final Logger LOGGER = Logger.getLogger(Backend.class);
	@XmlElement(name = Definitions.ELEMENT_ANALYSIS_URI)
	private String _analysisUri = null;
	@XmlElement(name = Definitions.ELEMENT_BACKEND_ID)
	private Long _backendId = null;
	private Set<String> _capabilities = null;
	private DataGroups _defaultTaskDataGroups = null;
	@XmlElement(name = Definitions.ELEMENT_DESCRIPTION)
	private String _description = null;
	@XmlElement(name = Definitions.ELEMENT_ENABLED)
	private Boolean _enabled = null;
	@XmlElement(name = Definitions.ELEMENT_NAME)
	private String _name = null;

	/**
	 * 
	 * @param capabilities
	 * @see #getCapabilities()
	 */
	public void setCapabilities(Set<String> capabilities) {
		_capabilities = capabilities;
	}

	/**
	 * 
	 * @param capability
	 * @return true if the back-end has the given capability
	 */
	public boolean hasCapability(String capability){
		if(_capabilities != null){
			return _capabilities.contains(capability);
		}else{
			return false;
		}
	}
	
	/**
	 * 
	 * @param capabilities
	 * @return true if this back-end has all of the given capabilities
	 */
	public boolean hasCapabilities(Set<String> capabilities){
		if(capabilities == null || capabilities.isEmpty() || _capabilities == null){
			return false;
		}else{
			return _capabilities.containsAll(capabilities);
		}
	}

	/**
	 * 
	 * @param capability
	 */
	public void addCapability(String capability){
		if(_capabilities == null){
			_capabilities = new HashSet<>();
		}
		_capabilities.add(capability);
	}

	/**
	 * 
	 * @return back-ends capabilities
	 * @see #setCapabilities(Set)
	 */
	@XmlElementWrapper(name = Definitions.ELEMENT_CAPABILITY_LIST)
	@XmlElement(name = Definitions.ELEMENT_CAPABILITY)
	public Set<String> getCapabilities() {
		return _capabilities;
	}

	/**
	 * 
	 * @return back-end description
	 * @see #setDescription(String)
	 */
	public String getDescription() {
		return _description;
	}

	/**
	 * 
	 * @param description
	 * @see #getDescription()
	 */
	public void setDescription(String description) {
		_description = description;
	}

	/**
	 * 
	 * @return true if this back-end is enabled
	 * @see #setEnabled(Boolean)
	 */
	public Boolean isEnabled() {
		return _enabled;
	}

	/**
	 * 
	 * @param enabled
	 * @see #isEnabled()
	 */
	public void setEnabled(Boolean enabled) {
		_enabled = enabled;
	}

	/**
	 * 
	 * @return the analysis service URI or null if this back end cannot receive add task calls
	 * @see #setAnalysisUri(String)
	 */
	public String getAnalysisUri() {
		return _analysisUri;
	}

	/**
	 * 
	 * @param analysisUri  the analysis service URI or null if this back end cannot receive add task calls
	 * @see #getAnalysisUri()
	 */
	public void setAnalysisUri(String analysisUri) {
		_analysisUri = analysisUri;
	}

	/**
	 * 
	 * @return back-end id
	 * @see #setBackendId(Long)
	 */
	public Long getBackendId() {
		return _backendId;
	}

	/**
	 * 
	 * @param backendId
	 * @see #getBackendId()
	 */
	public void setBackendId(Long backendId) {
		_backendId = backendId;
	}

	/**
	 * @return the default data groups taht should be used when populating tasks for this back end
	 * @see #setDefaultTaskDataGroups(DataGroups)
	 */
	public DataGroups getDefaultTaskDataGroups() {
		return _defaultTaskDataGroups;
	}

	/**
	 * @param defaultTaskDataGroups the default data groups that should be used when populating tasks for this back end
	 * @see #getDefaultTaskDataGroups()
	 */
	public void setDefaultTaskDataGroups(DataGroups defaultTaskDataGroups) {
		_defaultTaskDataGroups = defaultTaskDataGroups;
	}

	/**
	 * 
	 */
	public Backend(){
		// nothing needed
	}
	
	/**
	 * @return the name
	 * @see #setName(String)
	 */
	public String getName() {
		return _name;
	}

	/**
	 * @param name the name to set
	 * @see #getName()
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * for sub classing, use the static
	 * 
	 * @return true if valid
	 * @see #isValid(Backend)
	 */
	protected boolean isValid() {
		if(_enabled == null){
			LOGGER.debug("Invalid enabled value.");
			return false;
		}else if(DataGroups.isEmpty(_defaultTaskDataGroups)){
			LOGGER.debug("Invalid default data groups.");
			return false;
		}else if(StringUtils.isBlank(_name)){
			LOGGER.debug("Invalid name.");
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param backend
	 * @return true if the object is a valid back end, false if not (or if null was passed)
	 */
	public static boolean isValid(Backend backend) {
		return (backend == null ? false : backend.isValid());
	}
}
