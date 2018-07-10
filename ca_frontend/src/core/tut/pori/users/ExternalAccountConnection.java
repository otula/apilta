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
package core.tut.pori.users;

import java.util.Collection;
import java.util.EnumSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

/**
 * A class, which contains details of a single account connection belonging to a user.
 */
@XmlRootElement(name=Definitions.ELEMENT_EXTERNAL_ACCOUNT_CONNECTION)
@XmlAccessorType(XmlAccessType.NONE)
public class ExternalAccountConnection {
	@XmlElement(name=Definitions.ELEMENT_EXTERNAL_ID)
	private String _externalId = null;
	@XmlElement(name=Definitions.ELEMENT_SERVICE_TYPE)
	private UserServiceType _serviceType = null;
	
	/**
	 * Service types for external account connections.
	 *
	 */
	@XmlEnum
	public enum UserServiceType{
		/** Service type for Google */
		@XmlEnumValue(value=Definitions.SERVICE_TYPE_GOOGLE)
		GOOGLE(1),
		/** Service type for Facebook */
		@XmlEnumValue(value=Definitions.SERVICE_TYPE_FACEBOOK)
		FACEBOOK(2),
		/** Service type Twitter */
		@XmlEnumValue(value=Definitions.SERVICE_TYPE_TWITTER)
		TWITTER(3),
		/** Service type Other/unknown */
		@XmlEnumValue(value=Definitions.SERVICE_TYPE_OTHER)
		OTHER(3);
		
		private int _userServiceId;
		
		/**
		 * 
		 * @param userServiceId
		 */
		private UserServiceType(int userServiceId){
			_userServiceId = userServiceId;
		}
		
		/**
		 * 
		 * @return this service type as integer
		 */
		public int toInt(){
			return _userServiceId;
		}
		
		/**
		 * 
		 * @param value
		 * @return the value converted to user service type
		 * @throws IllegalArgumentException on bad value
		 */
		public static UserServiceType fromInt(int value) throws IllegalArgumentException{
			for(UserServiceType t : UserServiceType.values()){
				if(t._userServiceId == value){
					return t;
				}
			}
			throw new IllegalArgumentException("Bad "+UserServiceType.class.toString()+" : "+value);
		}
		
		/**
		 * 
		 * @param types
		 * @return the types as integers or null if null or empty list was passed
		 */
		public static int[] toInt(EnumSet<UserServiceType> types){
			if(types == null || types.isEmpty()){
				return null;
			}
			int[] values = new int[types.size()];
			int index = -1;
			for(UserServiceType t : types){
				values[++index] = t.toInt();
			}
			return values;
		}
		
		/**
		 * 
		 * @return this type as service type string
		 */
		public String toUserServiceTypeString() {
			switch(this){
				case FACEBOOK:
					return Definitions.SERVICE_TYPE_FACEBOOK;
				case GOOGLE:
					return Definitions.SERVICE_TYPE_GOOGLE;
				case TWITTER:
					return Definitions.SERVICE_TYPE_TWITTER;
				case OTHER:
					return Definitions.SERVICE_TYPE_OTHER;
				default:
					throw new UnsupportedOperationException("Unhandeled service type : "+name());
			}
		}
		
		/**
		 * 
		 * @param value
		 * @return the value converted to user service type
		 * @throws IllegalArgumentException on bad value
		 */
		public static UserServiceType fromUserServiceTypeString(String value) throws IllegalArgumentException {
			if(!StringUtils.isBlank(value)){
				for(UserServiceType t : UserServiceType.values()){
					if(t.toUserServiceTypeString().equalsIgnoreCase(value)){
						return t;
					}
				}
			}
			throw new IllegalArgumentException("Bad "+UserServiceType.class.toString()+" : "+value);
		}
		
		/**
		 * 
		 * @param values
		 * @return a set containing all of the given values converted to user service types or null if null or empty collection was given
		 * @throws IllegalArgumentException on bad value
		 */
		public static EnumSet<UserServiceType> fromUserServiceTypeStrings(Collection<String> values) throws IllegalArgumentException {
			if(values == null || values.isEmpty()){
				return null;
			}
			EnumSet<UserServiceType> set = EnumSet.noneOf(UserServiceType.class);
			for(String value : values){
				set.add(fromUserServiceTypeString(value));
			}
			return set;
		}
	} // enum UserServiceType

	/**
	 * 
	 */
	public ExternalAccountConnection(){
		// nothing needed
	}

	/**
	 * 
	 * @param externalId
	 * @param serviceType
	 */
	public ExternalAccountConnection(String externalId, UserServiceType serviceType) {
		_serviceType = serviceType;
		_externalId = externalId;
	}

	/**
	 * @return the serviceType
	 */
	public UserServiceType getServiceType() {
		return _serviceType;
	}

	/**
	 * @param serviceType the serviceType to set
	 */
	public void setServiceType(UserServiceType serviceType) {
		_serviceType = serviceType;
	}

	/**
	 * @return the externalId
	 */
	public String getExternalId() {
		return _externalId;
	}

	/**
	 * @param externalId the externalId to set
	 */
	public void setExternalId(String externalId) {
		_externalId = externalId;
	}
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if this external account connection object is valid
	 */
	protected boolean isValid(){
		if(_serviceType == null || StringUtils.isBlank(_externalId)){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * 
	 * @param connection
	 * @return false if connection is null or invalid
	 */
	public static boolean isValid(ExternalAccountConnection connection){
		return (connection == null ? false : connection.isValid());
	}
}
