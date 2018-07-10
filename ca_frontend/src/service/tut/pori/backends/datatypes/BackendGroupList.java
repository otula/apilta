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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import core.tut.pori.http.ResponseData;
import service.tut.pori.backends.Definitions;

/**
 * container for back end groups usable with response objects
 */
@XmlRootElement(name=Definitions.ELEMENT_BACKEND_GROUP_LIST)
@XmlAccessorType(XmlAccessType.NONE)
public class BackendGroupList extends ResponseData{
	@XmlElement(name = Definitions.ELEMENT_BACKEND_GROUP)
	private List<BackendGroup> _backendGroups = null;

	/**
	 * @return the back ends
	 * @see #setBackendGroups(List)
	 */
	public List<BackendGroup> getBackendGroups() {
		return _backendGroups;
	}
	
	/**
	 * for serialization
	 */
	public BackendGroupList() {
		// nothing needed
	}
	
	/**
	 * 
	 * @param backendGroup
	 */
	public BackendGroupList(List<BackendGroup> backendGroup) {
		_backendGroups = backendGroup;
	}
	
	/**
	 * 
	 * @param backendGroup
	 */
	public BackendGroupList(BackendGroup backendGroup) {
		_backendGroups = new ArrayList<>(1);
		_backendGroups.add(backendGroup);
	}

	/**
	 * @param backendGroups the back end groups to set
	 * @see #getBackendGroups()
	 */
	public void setBackendGroups(List<BackendGroup> backendGroups) {
		_backendGroups = backendGroups;
	}
	
	/**
	 * for sub-classing, use the static.
	 * 
	 * @return true if empty
	 * @see #isEmpty(BackendGroupList)
	 */
	protected boolean isEmpty() {
		return (_backendGroups == null || _backendGroups.isEmpty());
	}
	
	/**
	 * 
	 * @param backendGroupList
	 * @return true if null or empty list is passed
	 */
	public static boolean isEmpty(BackendGroupList backendGroupList) {
		if(backendGroupList == null){
			return true;
		}else{
			return backendGroupList.isEmpty();
		}
	}
}
