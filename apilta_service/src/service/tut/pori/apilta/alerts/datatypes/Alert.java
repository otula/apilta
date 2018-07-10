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
package service.tut.pori.apilta.alerts.datatypes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.beans.Field;

import core.tut.pori.dao.SolrDAO;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.ISODateAdapter;
import service.tut.pori.apilta.files.datatypes.FileDetails;
import service.tut.pori.apilta.files.datatypes.FileDetailsList;

/**
 * a single alert
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_ALERT)
@XmlAccessorType(XmlAccessType.NONE)
public class Alert {
	private static final Logger LOGGER = Logger.getLogger(Alert.class);
	@Field(Definitions.SOLR_FIELD_ALERT_GROUP_ID)
	private List<Long> _alertGroupIds = null;
	@Field(core.tut.pori.dao.SolrDAO.SOLR_FIELD_ID)
	@XmlElement(name=Definitions.ELEMENT_ALERT_ID)
	private String _alertId = null;
	@Field(Definitions.SOLR_FIELD_ALERT_TYPE)
	@XmlElement(name=Definitions.ELEMENT_ALERT_TYPE)
	private String _alertType = null;
	@Field(SolrDAO.SOLR_FIELD_CREATED)
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	@XmlElement(name=Definitions.ELEMENT_CREATED_TIMESTAMP)
	private Date _created = null;
	@Field(Definitions.SOLR_FIELD_DESCRIPTION)
	@XmlElement(name=Definitions.ELEMENT_DESCRIPTION)
	private String _description = null;
	@XmlElement(name=service.tut.pori.apilta.files.datatypes.Definitions.ELEMENT_FILE_DETAILS_LIST)
	private FileDetailsList _files = null;
	@XmlElement(name=Definitions.ELEMENT_LOCATION)
	private Location _location = null;
	@Field(Definitions.SOLR_FIELD_RANGE)
	@XmlElement(name=Definitions.ELEMENT_RANGE)
	private Integer _range = null;
	@XmlElement(name=core.tut.pori.users.Definitions.ELEMENT_USER_IDENTITY)
	private UserIdentity _userId = null;
	@Field(Definitions.SOLR_FIELD_VALID)
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	@XmlElement(name=Definitions.ELEMENT_VALID_TIMESTAMP)
	private Date _validUntil = null;
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if the alert is valid
	 * @see #isValid(Alert)
	 */
	protected boolean isValid() {
		if(StringUtils.isBlank(_alertType)){
			LOGGER.debug("Invalid alert type.");
			return false;
		}else if(!UserIdentity.isValid(_userId)){
			LOGGER.debug("Invalid user for the alert.");
			return false;
		}else if(!Location.isValid(_location)){
			LOGGER.debug("Invalid location.");
			return false;
		}else if(!FileDetailsList.isEmpty(_files) && !FileDetailsList.isValid(_files)){
			LOGGER.debug("Invalid file details.");
			return false;
		}else if(_range != null && _range < 1){
			LOGGER.debug("Invalid range.");
			return false;
		}
		
		if(_validUntil != null){
			if(_created == null){
				LOGGER.debug("Valid until timestamp was given, but no created.");
				return false;
			}else if(_validUntil.before(_created)){
				LOGGER.debug("Valid until timestamp is before created timestamp.");
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param alert
	 * @return false if the alert is null or invalid
	 */
	public static boolean isValid(Alert alert) {
		return (alert != null && alert.isValid());
	}
	
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
	 * @return the alertGroupIds
	 * @see #setAlertGroupIds(List)
	 */
	public List<Long> getAlertGroupIds() {
		return _alertGroupIds;
	}
	
	/**
	 * @param alertGroupIds the alertGroupId to set
	 * @see #getAlertGroupIds()
	 */
	public void setAlertGroupIds(List<Long> alertGroupIds) {
		_alertGroupIds = alertGroupIds;
	}
	
	/**
	 * @return the alertType
	 * @see #setAlertType(String)
	 */
	public String getAlertType() {
		return _alertType;
	}
	
	/**
	 * @param alertType the alertType to set
	 * @see #getAlertType()
	 */
	public void setAlertType(String alertType) {
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
	 * @see #setFiles(FileDetailsList)
	 */
	public FileDetailsList getFiles() {
		return _files;
	}
	
	/**
	 * @param files the files to set
	 * @see #getFiles()
	 */
	public void setFiles(FileDetailsList files) {
		_files = files;
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
	 * @return the userId
	 * @see #setUserId(UserIdentity)
	 */
	public UserIdentity getUserId() {
		return _userId;
	}
	
	/**
	 * @param userId the userId to set
	 * @see #getUserId()
	 */
	public void setUserId(UserIdentity userId) {
		_userId = userId;
	}
	
	/**
	 * for solr
	 * 
	 * @param userId
	 * @see #setUserId(UserIdentity)
	 */
	@Field(Definitions.SOLR_FIELD_USER_ID)
	private void setUserIdValue(Long userId){
		_userId = (userId == null ? null : new UserIdentity(userId));
	}
	
	/**
	 * for solr
	 * 
	 * @return user identity value
	 * @see #getUserId()
	 */
	public Long getUserIdValue(){
		return (_userId == null ? null : _userId.getUserId());
	}
	
	/**
	 * for solr
	 * 
	 * @param fileGUIDs
	 * @see #setFiles(FileDetailsList)
	 */
	@Field(Definitions.SOLR_FIELD_FILE_GUIDS)
	private void setFileGUIDs(List<String> fileGUIDs) {
		if(fileGUIDs == null || fileGUIDs.isEmpty()){
			_files = null;
			return;
		}
		ArrayList<FileDetails> details = new ArrayList<>(fileGUIDs.size());
		for(String guid : fileGUIDs){
			FileDetails d = new FileDetails();
			d.setGUID(guid);
			details.add(d);
		}
		_files = new FileDetailsList();
		_files.setFiles(details);
	}
	
	/**
	 * for solr
	 * 
	 * @return lists of file GUIDs or null if no files
	 * @see #getFiles()
	 */
	public List<String> getFileGUIDs() {
		if(FileDetailsList.isEmpty(_files)){
			return null;
		}else{
			List<FileDetails> details = _files.getFiles();
			ArrayList<String> guids = new ArrayList<>(details.size());
			for(FileDetails d : details){
				guids.add(d.getGUID());
			}
			return guids;
		}
	}
	
	/**
	 * for solr
	 * 
	 * @param location
	 * @see #setLocation(Location)
	 */
	@Field(Definitions.SOLR_FIELD_LOCATION)
	private void setLocationString(String location) {
		if(StringUtils.isBlank(location)){
			_location = null;
		}else{
			_location = new Location();
			String[] parts = StringUtils.split(location);
			_location.setLongitude(Double.valueOf(parts[0]));
			_location.setLatitude(Double.valueOf(parts[1]));
		}
	}
	
	/**
	 * for solr
	 * 
	 * @return location as a string
	 * @see #getLocation()
	 */
	public String getLocationString() {
		if(Location.isValid(_location)){
			return _location.getLongitude().toString()+' '+_location.getLatitude().toString();
		}else{
			return null;
		}
	}
	
	/**
	 * 
	 * @return the Solr database id
	 * @see #setAlertId(String)
	 */
	public String getAlertId() {
		return _alertId;
	}
	
	/**
	 * 
	 * @param id
	 * @see #getAlertId()
	 */
	public void setAlertId(String id) {
		_alertId = id;
	}

	/**
	 * @return the validUntil
	 * @see #setValidUntil(Date)
	 */
	public Date getValidUntil() {
		return _validUntil;
	}

	/**
	 * @param validUntil the validUntil to set
	 * @see #getValidUntil()
	 */
	public void setValidUntil(Date validUntil) {
		_validUntil = validUntil;
	}

	/**
	 * @return the range this alert is valid from the center coordinate (in meters)
	 * @see #setRange(Integer)
	 */
	public Integer getRange() {
		return _range;
	}

	/**
	 * Note: this is not used when calculating range queries, this is an optional field that can be specified to give more information about the alert
	 * 
	 * @param range the range this alert is valid from the center coordinate (in meters)
	 * @see #getRange()
	 */
	public void setRange(Integer range) {
		_range = range;
	}
}
