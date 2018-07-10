/**
 * Copyright 2018 Tampere University of Technology, Pori Department
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
package service.tut.pori.apilta.shock.datatypes;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import core.tut.pori.http.ResponseData;

/**
 * 
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_SHOCK_HIGHLIGHT_LIST)
@XmlAccessorType(XmlAccessType.NONE)
public class ShockHighlightList extends ResponseData {
	@XmlElementRef
	private List<ShockHighlight> _shockHighlights = null;
	
	/**
	 * @return the shockHighlights
	 */
	public List<ShockHighlight> getShockHighlights() {
		return _shockHighlights;
	}

	/**
	 * @param shockHighlights the shockHighlights to set
	 */
	public void setShockHighlights(List<ShockHighlight> shockHighlights) {
		_shockHighlights = shockHighlights;
	}

	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if list is empty
	 */
	protected boolean isEmpty() {
		return (_shockHighlights == null || _shockHighlights.isEmpty());
	}
	
	/**
	 * 
	 * @param list
	 * @return true if the list is null or empty
	 */
	public static boolean isEmpty(ShockHighlightList list) {
		return (list == null || list.isEmpty());
	}
	
	/**
	 * 
	 * @param highlights
	 * @return return list object containing the given list or null if null or empty list was passed
	 */
	public static ShockHighlightList getShockMeasurementList(List<ShockHighlight> highlights) {
		if(highlights == null || highlights.isEmpty()) {
			return null;
		}
		ShockHighlightList list = new ShockHighlightList();
		list._shockHighlights = highlights;
		return list;
	}
}
