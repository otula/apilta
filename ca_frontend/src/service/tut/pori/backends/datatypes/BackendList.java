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
 * container for back ends usable with response objects
 */
@XmlRootElement(name=Definitions.ELEMENT_BACKEND_LIST)
@XmlAccessorType(XmlAccessType.NONE)
public class BackendList extends ResponseData{
	@XmlElement(name = Definitions.ELEMENT_BACKEND)
	private List<Backend> _backends = null;

	/**
	 * @return the back ends
	 * @see #setBackends(List)
	 */
	public List<Backend> getBackends() {
		return _backends;
	}
	
	/**
	 * for serialization
	 */
	public BackendList() {
		// nothing needed
	}
	
	/**
	 * 
	 * @param backends
	 */
	public BackendList(List<Backend> backends) {
		_backends = backends;
	}
	
	/**
	 * 
	 * @param backend
	 */
	public BackendList(Backend backend) {
		_backends = new ArrayList<>(1);
		_backends.add(backend);
	}

	/**
	 * @param backends the back ends to set
	 * @see #getBackends()
	 */
	public void setBackends(List<Backend> backends) {
		_backends = backends;
	}
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if this list is empty
	 * @see #isEmpty(BackendList)
	 */
	protected boolean isEmpty() {
		return (_backends == null || _backends.isEmpty());
	}
	
	/**
	 * 
	 * @param backends
	 * @return true if the given list is null or empty
	 */
	public static boolean isEmpty(BackendList backends) {
		if(backends ==  null){
			return true;
		}else{
			return backends.isEmpty();
		}
	}
}
