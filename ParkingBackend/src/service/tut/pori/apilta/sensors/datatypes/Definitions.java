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
package service.tut.pori.apilta.sensors.datatypes;

/**
 * definitions for sensor data types
 * 
 * This class has been modified from the original version:
 * - Dependencies to Solr removed (core.tut.pori.dao.SolrDAO class)
 */
public final class Definitions {
	/* elements */
	/** xml element declaration */
	public static final String ELEMENT_BACKEND_ID = "backendId";
	/** xml element declaration */
	public static final String ELEMENT_CONDITION = "condition";
	/** xml element declaration */
	public static final String ELEMENT_CREATED_TIMESTAMP = "createdTimestamp";
	/** xml element declaration */
	public static final String ELEMENT_DATAPOINT = "dataPoint";
	/** xml element declaration */
	public static final String ELEMENT_DATAPOINT_ID = "dataPointId";
	/** xml element declaration */
	public static final String ELEMENT_DATAPOINT_LIST = "dataPointList";
	/** xml element declaration */
	public static final String ELEMENT_DATAPOINT_TYPE = "dataPointType";
	/** xml element declaration */
	public static final String ELEMENT_DESCRIPTION = "description";
	/** xml element declaration */
	public static final String ELEMENT_FEATURE = "feature";
	/** xml element declaration */
	public static final String ELEMENT_KEY = "key";
	/** xml element declaration */
	public static final String ELEMENT_MEASUREMENT = "measurement";
	/** xml element declaration */
	public static final String ELEMENT_MEASUREMENT_ID = "measurementId";
	/** xml element declaration */
	public static final String ELEMENT_MEASUREMENT_LIST = "measurementList";
	/** xml element declaration */
	public static final String ELEMENT_NAME = "name";
	/** xml element declaration */
	public static final String ELEMENT_OUTPUT = "output";
	/** xml element declaration */
	public static final String ELEMENT_TERMS = "terms";
	/** xml element declaration */
	public static final String ELEMENT_VALUE = "value";
	/** xml element declaration */
	public static final String ELEMENT_WHAT = "what";
	/** xml element declaration */
	public static final String ELEMENT_WHEN = "when";
	
	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
