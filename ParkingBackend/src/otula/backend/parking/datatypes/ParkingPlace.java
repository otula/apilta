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

import java.util.List;

/**
 * details for a single parking place
 * 
 */
public class ParkingPlace {
	private static String BOOLEAN_FALSE = "ei";
	private Coordinate _location = null;
	private String _name = null;
	private String _notice = null;
	private String _owner = null;
	private Boolean _paymentRequired = null;
	private Integer _placeCount = null;
	private Integer _timeLimit = null;
	private String _zone = null;
	
	/**
	 * 
	 * @param data
	 * @param location
	 */
	public ParkingPlace(List<SimpleData> data, Coordinate location) {
		_location = location;
		initialize(data);
	}
	
	/**
	 * 
	 * @param data
	 */
	private void initialize(List<SimpleData> data) {
		if(data == null){
			return;
		}
		
		for(SimpleData sd : data){
			switch (sd.getName()) {
				case Definitions.SIMPLE_DATA_KEY_NAME:
					_name = sd.getValue();
					break;
				case Definitions.SIMPLE_DATA_KEY_NOTICE:
					_notice = sd.getValue();
					break;
				case Definitions.SIMPLE_DATA_KEY_OWNER:
					_owner = sd.getValue();
					break;
				case Definitions.SIMPLE_DATA_KEY_PAYMENT_REQUIRED:
					_paymentRequired = !BOOLEAN_FALSE.equalsIgnoreCase(sd.getValue()); // assume everything else except negative answer is positive
					break;
				case Definitions.SIMPLE_DATA_KEY_TIME_LIMIT:
					_timeLimit = Integer.valueOf(sd.getValue());
					break;
				case Definitions.SIMPLE_DATA_KEY_ZONE:
					_zone = sd.getValue();
					break;
				case Definitions.SIMPLE_DATA_KEY_PLACE_COUNT:
					_placeCount = Integer.valueOf(sd.getValue());
					break;
				default:
					break;
			}
		}
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * @return the notice
	 */
	public String getNotice() {
		return _notice;
	}
	
	/**
	 * @return the owner
	 */
	public String getOwner() {
		return _owner;
	}
	
	/**
	 * @return the paymentRequired
	 */
	public Boolean getPaymentRequired() {
		return _paymentRequired;
	}
	
	/**
	 * @return the placeCount
	 */
	public Integer getPlaceCount() {
		return _placeCount;
	}
	
	/**
	 * @return the timeLimit
	 */
	public Integer getTimeLimit() {
		return _timeLimit;
	}
	
	/**
	 * @return the zone
	 */
	public String getZone() {
		return _zone;
	}

	/**
	 * @return the location
	 */
	public Coordinate getLocation() {
		return _location;
	}
}
