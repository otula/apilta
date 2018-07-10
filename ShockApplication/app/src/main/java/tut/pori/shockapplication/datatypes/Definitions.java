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
package tut.pori.shockapplication.datatypes;

/**
 * 
 *
 */
public final class Definitions {
	/* elements */
	/** xml element declaration */
	public static final String ELEMENT_ACCELEROMETER_DATA = "accelerometerData";
	/** xml element declaration */
	public static final String ELEMENT_DATA_VISIBILITY = "dataVisibility";
	/** xml element declaration */
	public static final String ELEMENT_LATITUDE = "latitude";
	/** xml element declaration */
	public static final String ELEMENT_LEVEL = "level";
	/** xml element declaration */
	public static final String ELEMENT_LOCATION_DATA = "locationData";
	/** xml element declaration */
	public static final String ELEMENT_LONGITUDE = "longitude";
	/** xml element declaration */
	public static final String ELEMENT_HEADING = "heading";
	/** xml element declaration */
	public static final String ELEMENT_MEASUREMENT = "measurement";
	/** xml element declaration */
	public static final String ELEMENT_MEASUREMENT_LIST = "measurementList";
	/** xml element declaration */
	public static final String ELEMENT_SPEED = "speed";
	/** xml element declaration */
	public static final String ELEMENT_SYSTEMATIC_ERROR = "systematicError";
	/** xml element declaration */
	public static final String ELEMENT_TIMESTAMP = "timestamp";
	/** xml element declaration */
	public static final String ELEMENT_X_ACCELERATION = "x_acc";
	/** xml element declaration */
	public static final String ELEMENT_Y_ACCELERATION = "y_acc";
	/** xml element declaration */
	public static final String ELEMENT_Z_ACCELERATION = "z_acc";

	/* common */
	/** visibility private */
	public static final String DATA_VISIBILITY_PRIVATE = "PRIVATE";
	/** visibility public */
	public static final String DATA_VISIBILITY_PUBLIC = "PUBLIC";
	
	/**
	 */
	private Definitions() {
		// nothing needed
	}
}
