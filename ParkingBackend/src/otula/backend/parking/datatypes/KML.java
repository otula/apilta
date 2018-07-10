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
package otula.backend.parking.datatypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * KML document root
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_KML, namespace = Definitions.NAMESPACE_KML)
@XmlAccessorType(XmlAccessType.NONE)
public class KML {
	@XmlElement(name = Definitions.ELEMENT_DOCUMENT, namespace = Definitions.NAMESPACE_KML)
	private Document _document = null;

	/**
	 * @return the document
	 */
	public Document getDocument() {
		return _document;
	}

	/**
	 * @param document the document to set
	 */
	public void setDocument(Document document) {
		_document = document;
	}
}
