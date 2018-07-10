/**
 * Copyright 2014 Tampere University of Technology, Pori Department
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
package core.tut.pori.http;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The base class for HTTP Response data. 
 * 
 * Sub-classes of this class can be used in combination with HTTP Response to provide automatic marshalling of object content to XML output.
 * 
 * Note: implemented as abstract class because JAXB does not handle interfaces well.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class ResponseData {
	private final transient Class<?>[] _defaultClasses;
	
	/**
	 * The default implementation equals to calling getClass(), override this if your class requires other classes
	 * for serialization purposes (which cannot be directly resolved).
	 * 
	 * In general, overriding the default class definitions should not be needed, and in many cases an attempt to do so may be a sign of bad design,
	 * though there might also be valid cases where the functionality is required.
	 * 
	 * @return the list of classes present in this data
	 */
	public Class<?>[] getDataClasses(){
		return _defaultClasses;
	}
	
	/**
	 * no-args constructor, you should provide one for serialization purposes
	 */
	protected ResponseData(){
		_defaultClasses = new Class<?>[]{getClass()};
	}
}
