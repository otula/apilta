/**
 * Copyright 2015 Tampere University of Technology, Pori Department
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
package core.tut.pori.utils;

import java.time.Instant;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.StringUtils;

/**
 * JAXB adapter for java.time.Instant class.
 * <br>
 * usage:
 * <dl>Usage:
 * 	<dd>annotate the class' member variable with<br/>
 * 		&#64;XmlJavaTypeAdapter(InstantAdapter.class)</dd>
 * </dl>
 * 
 * @see java.time.Instant
 */
public class InstantAdapter extends XmlAdapter<String, Instant>{

	@Override
	public Instant unmarshal(String instant) throws Exception {
		if(StringUtils.isEmpty(instant)){
			return null;
		}
		return Instant.parse(instant);
	}

	@Override
	public String marshal(Instant instant) throws Exception {
		if(instant == null){
			return null;
		}
		return instant.toString();
	}

}
