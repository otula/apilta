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
package otula.backend.parking;

/**
 * definitions for the parking package
 * 
 */
public final class Definitions {
	/* features */
	/** features */
	public static final String FEATURE_PARKING = "parking";
	/** features */
	public static final String FEATURE_DETAILS = FEATURE_PARKING+"/details";
	/** features */
	public static final String FEATURE_LOCATION = FEATURE_PARKING+"/location";
	
	/* condition entry types */
	/** condition entry key */
	public static final String CONDITION_ENTRY_KEY_LOCATION = "location/point";

	/* output data keys */
	/** output data key */
	public static final String DATA_KEY_LOCATION = CONDITION_ENTRY_KEY_LOCATION;
	/** output data key */
	public static final String DATA_KEY_NAME = FEATURE_PARKING+"/name";
	/** output data key */
	public static final String DATA_KEY_PLACE_COUNT = FEATURE_PARKING+"/placeCount";
	/** output data key */
	public static final String DATA_KEY_NOTICE = FEATURE_PARKING+"/notice";
	/** output data key */
	public static final String DATA_KEY_OWNER = FEATURE_PARKING+"/owner";
	/** output data key */
	public static final String DATA_KEY_PAYMENT_REQUIRED = FEATURE_PARKING+"/paymentRequired";
	/** output data key */
	public static final String DATA_KEY_TIME_LIMIT = FEATURE_PARKING+"/timeLimit";
	/** output data key */
	public static final String DATA_KEY_ZONE = FEATURE_PARKING+"/zone";
	
	/**
	 * 
	 */
	private Definitions() {
		// nothing needed
	}
}
