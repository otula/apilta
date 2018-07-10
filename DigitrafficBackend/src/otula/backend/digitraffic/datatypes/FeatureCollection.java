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

import java.util.Date;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * A feature collection.
 * 
 * As defined in <a href="https://tie.digitraffic.fi/api/v1/data/documentation/swagger-ui.html#!/metadata/weatherStationsUsingGET">Digitraffic documentation</a>
 * 
 */
public class FeatureCollection {
	@SerializedName(value=Definitions.JSON_NAME_DATA_LAST_CHECKED)
	private Date _dataLastCheckedTime = null;
	@SerializedName(value=Definitions.JSON_NAME_DATA_UPDATED)
	private Date _dataUpdatedTime = null;
	@SerializedName(value=Definitions.JSON_NAME_FEATURES)
	private List<Feature> _features = null;
	@SerializedName(value=Definitions.JSON_NAME_TYPE)
	private String _type = null;
	
	/**
	 * @return the dataLastCheckedTime
	 */
	public Date getDataLastCheckedTime() {
		return _dataLastCheckedTime;
	}
	
	/**
	 * @return the dataUpdatedTime
	 */
	public Date getDataUpdatedTime() {
		return _dataUpdatedTime;
	}
	
	/**
	 * @return the features
	 */
	public List<Feature> getFeatures() {
		return _features;
	}
	
	/**
	 * @return the type
	 */
	public String getType() {
		return _type;
	}
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if this collection contains no features
	 */
	protected boolean isEmpty(){
		return (_features == null || _features.isEmpty());
	}
	
	/**
	 * 
	 * @param collection
	 * @return true if the collection is null or empty
	 */
	public static boolean isEmpty(FeatureCollection collection) {
		return (collection == null || collection.isEmpty());
	}
}
