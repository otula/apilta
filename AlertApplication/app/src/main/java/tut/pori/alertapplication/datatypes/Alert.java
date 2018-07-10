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
package tut.pori.alertapplication.datatypes;

import android.location.Location;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tut.pori.alertapplication.R;


/**
 * a single alert
 * 
 */
public class Alert {
	private String _alertId = null;
	private AlertType _alertType = null;
	private Date _created = null;
	private String _description = null;
	private List<FileDetails> _files = null;
	private Location _location = null;
	private Integer _range = null;
	private UserIdentity _userId = null;

	/**
	 * TODO: proper types
	 *
	 * Alert Type enumeration
	 */
	public enum AlertType {
		UNKNOWN,
		ANIMAL_ON_ROAD,
		BROKEN_ROAD_SIGN,
		GENERAL_ACCIDENT,
        TRAFFIC_JAM;

		/**
		 *
		 * @return this alert type as a string
		 * @throws IllegalArgumentException if this Alert is of type {@link AlertType#UNKNOWN}
		 */
		public String toAlertTypeString() throws IllegalArgumentException {
			if(this == AlertType.UNKNOWN){
				throw new IllegalArgumentException(name()+" does not a have a valid alert type string.");
			}else{
				return name();
			}
		}

        /**
         *
         * @return string resource (string id)
         * @throws IllegalArgumentException if no image/resource is known for this type.
         */
        public int getStringResource() throws IllegalArgumentException {
            switch(this) {
                case ANIMAL_ON_ROAD:
                    return R.string.animal_on_road;
                case BROKEN_ROAD_SIGN:
                    return R.string.broken_road_sign;
                case GENERAL_ACCIDENT:
                    return R.string.general_accident;
                case TRAFFIC_JAM:
                    return R.string.traffic;
                case UNKNOWN:
                    return R.string.unknown;
                default:
                    throw new IllegalArgumentException(name()+" does not a have a valid string resource.");
            }
        }

		/**
		 *
		 * @param value
		 * @return alert type for the value or {@link AlertType#UNKNOWN}
		 */
		public static AlertType fromAlertTypeString(String value) {
			for(AlertType t : AlertType.values()){
                if(t == UNKNOWN){
                    continue;
                }
				if(t.toAlertTypeString().equalsIgnoreCase(value)){
					return t;
				}
			}
			return UNKNOWN;
		}

        /**
         *
         * @return image resource (drawable id) of the large image
         */
        public int getImageResource() {
            switch(this) {
                case ANIMAL_ON_ROAD:
                    return R.drawable.animal_on_road;
                case BROKEN_ROAD_SIGN:
                    return R.drawable.broken_road_sign;
                case GENERAL_ACCIDENT:
                    return R.drawable.general_accident;
                case TRAFFIC_JAM:
                    return R.drawable.traffic;
                case UNKNOWN:
					return R.drawable.unknown;
                default:
                    throw new IllegalArgumentException(name()+" does not a have a valid image resource.");
            }
        }

        /**
         *
         * @return image resource (drawable id) of the icon image
         */
        public int getIconImageResource() {
            switch(this) {
                case ANIMAL_ON_ROAD:
                    return R.drawable.icon_animal_on_road;
                case BROKEN_ROAD_SIGN:
                    return R.drawable.icon_broken_road_sign;
                case GENERAL_ACCIDENT:
                    return R.drawable.icon_general_accident;
                case TRAFFIC_JAM:
                    return R.drawable.icon_traffic;
                case UNKNOWN:
                    return R.drawable.unknown;
                default:
                    throw new IllegalArgumentException(name()+" does not a have a valid image resource.");
            }
        }

		/**
		 *
		 * @return image resource (drawable id) for the image used in alert lists
		 */
		public int getListImageResource() {
			switch(this) {
				case ANIMAL_ON_ROAD:
					return R.drawable.list_animal_on_road;
				case BROKEN_ROAD_SIGN:
					return R.drawable.list_broken_road_sign;
				case GENERAL_ACCIDENT:
					return R.drawable.list_general_accident;
				case TRAFFIC_JAM:
					return R.drawable.list_traffic;
				case UNKNOWN:
					return R.drawable.unknown;
				default:
					throw new IllegalArgumentException(name()+" does not a have a valid image resource.");
			}
		}

		/**
		 *
		 * @return audio resource (raw id)
		 */
		public int getAudioResource() {
			switch(this) {
				case ANIMAL_ON_ROAD:
					return R.raw.animal_on_road;
				case BROKEN_ROAD_SIGN:
					return R.raw.broken_road_sign;
				case GENERAL_ACCIDENT:
					return R.raw.general_accident;
				case TRAFFIC_JAM:
					return R.raw.traffic;
				case UNKNOWN:
					return R.raw.unknown;
				default:
					throw new IllegalArgumentException(name()+" does not a have a valid image resource.");
			}
		}

		/**
		 *
		 * @return color resource (id)
		 */
		public int getColorResource() {
			switch(this) {
				case ANIMAL_ON_ROAD:
                    return R.color.colorAlertAnimalOnRoad;
				case BROKEN_ROAD_SIGN:
					return R.color.colorAlertBrokenRoadSign;
				case GENERAL_ACCIDENT:
					return R.color.colorAlertGeneralAccident;
				case TRAFFIC_JAM:
					return R.color.colorAlertTrafficJam;
				case UNKNOWN:
				default:
					return R.color.colorAlertUnknown;
			}
		}
    } // enum AlertType

	/**
	 *
	 * @return created
	 * @see #setCreated(Date)
     */
	public Date getCreated() {
		return _created;
	}

	/**
	 *
	 * @param created
	 * @see #getCreated()
     */
	public void setCreated(Date created) {
		_created = created;
	}
	
	/**
	 * @return the alertType
	 * @see #setAlertType(AlertType)
	 */
	public AlertType getAlertType() {
		return _alertType;
	}
	
	/**
	 * @param alertType the alertType to set
	 * @see #getAlertType()
	 */
	public void setAlertType(AlertType alertType) {
		_alertType = alertType;
	}
	
	/**
	 * @return the description
	 * @see #setDescription(String)
	 */
	public String getDescription() {
		return _description;
	}
	
	/**
	 * @param description the description to set
	 * @see #getDescription()
	 */
	public void setDescription(String description) {
		_description = description;
	}
	
	/**
	 * @return the files
	 * @see #setFiles(List)
	 */
	public List<FileDetails> getFiles() {
		return _files;
	}
	
	/**
	 * @param files the files to set
	 * @see #getFiles()
	 */
	public void setFiles(List<FileDetails> files) {
		_files = files;
	}

    /**
     *
     * @param file
     * @see #getFiles()
     */
	public void addFile(FileDetails file) {
        if(_files == null){
            _files = new ArrayList<>();
        }
        _files.add(file);
    }
	
	/**
	 * @return the location
	 * @see #setLocation(Location)
	 */
	public Location getLocation() {
		return _location;
	}
	
	/**
	 * @param location the location to set
	 * @see #getLocation()
	 */
	public void setLocation(Location location) {
		_location = location;
	}

	/**
	 *
	 * @return alert id
	 * @see #setAlertId(String)
	 */
	public String getAlertId() {
		return _alertId;
	}

	/**
	 *
	 * @param alertId
	 * @see #getAlertId()
	 */
	public void setAlertId(String alertId) {
		_alertId = alertId;
	}

	/**
	 *
	 * @return range (in meters)
	 * @see #setRange(Integer)
	 */
	public Integer getRange() {
		return _range;
	}

	/**
	 *
	 * @param range in meters
	 * @see #getRange()
	 */
	public void setRange(Integer range) {
		_range = range;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		Alert alert = (Alert) o;

		return _alertId != null ? _alertId.equals(alert._alertId) : alert._alertId == null;

	}

	@Override
	public int hashCode() {
		return _alertId != null ? _alertId.hashCode() : 0;
	}

	/**
	 *
	 * @return the creator/owner of this alert
	 * @see #setUserId(UserIdentity)
	 */
	public UserIdentity getUserId() {
		return _userId;
	}

	/**
	 *
	 * @param userId
	 * @see #getUserId()
	 */
	public void setUserId(UserIdentity userId) {
		_userId = userId;
	}
}
