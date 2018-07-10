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
package otula.backend.digitraffic.datatypes;

import com.google.gson.annotations.SerializedName;

/**
 * A single feature.
 * 
 * As defined in <a href="https://tie.digitraffic.fi/api/v1/data/documentation/swagger-ui.html#!/metadata/weatherStationsUsingGET">Digitraffic documentation</a>
 * 
 */
public class Feature {
	@SerializedName(Definitions.JSON_NAME_GEOMETRY)
	private Geometry _geometry = null;
	@SerializedName(Definitions.JSON_NAME_ID)
	private Integer _id = null;
	@SerializedName(Definitions.JSON_NAME_PROPERTIES)
	private Properties _properties = null;
	@SerializedName(Definitions.JSON_NAME_TYPE)
	private String _type = null;
	
	/**
	 * @return the geometry
	 */
	public Geometry getGeometry() {
		return _geometry;
	}
	
	/**
	 * @return the id
	 */
	public Integer getId() {
		return _id;
	}
	
	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		return _properties;
	}
	
	/**
	 * @return the type
	 */
	public String getType() {
		return _type;
	}
}
