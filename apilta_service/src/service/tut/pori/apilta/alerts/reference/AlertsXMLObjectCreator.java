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
package service.tut.pori.apilta.alerts.reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.log4j.Logger;

import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.DateIntervalParameter.Interval;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;
import service.tut.pori.apilta.alerts.datatypes.Alert;
import service.tut.pori.apilta.alerts.datatypes.AlertList;
import service.tut.pori.apilta.alerts.datatypes.Location;
import service.tut.pori.apilta.files.TemporaryTokenHandler;
import service.tut.pori.apilta.files.datatypes.Definitions;
import service.tut.pori.apilta.files.datatypes.FileDetails;
import service.tut.pori.apilta.files.datatypes.FileDetailsList;

/**
 * 
 * Class that can be used to created example objects/object lists.
 *
 */
public class AlertsXMLObjectCreator {
	private static final String IMAGE_URL = "http://otula.pori.tut.fi/d2i/leijona_album_art.jpg";
	private static final String IMAGE_URL_MIME_TYPE = "image/jpeg";
	private static final Logger LOGGER = Logger.getLogger(AlertsXMLObjectCreator.class);	
	private static final Integer ALERT_RANGE = 100; // in meters
	private static final int TEXT_LENGTH = 64;
	private Random _random = null;
	private RandomStringGenerator _stringGenerator = null;

	/**
	 * 
	 * @param seed for random generator, or null to use default (system time in nanoseconds)
	 */
	public AlertsXMLObjectCreator(Long seed){
		if(seed == null){
			seed = System.nanoTime();
		}
		_random = new Random(seed);
		_stringGenerator = new RandomStringGenerator.Builder().withinRange('a', 'z').build();
	}

	/**
	 * @return the random
	 */
	public Random getRandom() {
		return _random;
	}

	/**
	 * 
	 * @param alertGroupId 
	 * @param alertType
	 * @param createdFilter 
	 * @param dataGroups 
	 * @param limits
	 * @param location
	 * @param range 
	 * @return alert list
	 */
	public AlertList generateAlertList(long[] alertGroupId, Collection<String> alertType, Set<Interval> createdFilter, DataGroups dataGroups, Limits limits, Location location, Double range) {
		int count = limits.getMaxItems(service.tut.pori.apilta.alerts.datatypes.Definitions.ELEMENT_ALERT_LIST);
		if(count < 1){
			LOGGER.warn("count < 1");
			return null;
		}else if(count >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Count was "+Limits.DEFAULT_MAX_ITEMS+", using 1.");
			count = 1;
		}
		
		ArrayList<Alert> alerts = new ArrayList<>(count);
		for(int i=0;i<count;++i){
			alerts.add(generateAlert(alertGroupId, alertType, createdFilter, dataGroups, limits, location, range));
		}
		
		AlertList list = new AlertList();
		list.setAlerts(alerts);
		return list;
	}

	/**
	 * 
	 * @param alertGroupId 
	 * @param alertType
	 * @param createdFilter 
	 * @param dataGroups 
	 * @param limits 
	 * @param location
	 * @param range 
	 * @return alert
	 */
	public Alert generateAlert(long[] alertGroupId, Collection<String> alertType, Set<Interval> createdFilter, DataGroups dataGroups, Limits limits, Location location, Double range) {
		Alert alert = new Alert();
		
		ArrayList<Long> ids = new ArrayList<>(1);
		if(ArrayUtils.isEmpty(alertGroupId)){
			ids.add(Math.abs(_random.nextLong()));
		}else{
			ids.add(alertGroupId[_random.nextInt(alertGroupId.length)]);
		}
		alert.setAlertGroupIds(ids);
		
		if(alertType != null && !alertType.isEmpty()){
			alert.setAlertType(IterableUtils.get(alertType, _random.nextInt(alertType.size())));
		}else{
			alert.setAlertType(_stringGenerator.generate(TEXT_LENGTH));
		}
		
		alert.setDescription(_stringGenerator.generate(TEXT_LENGTH));
		alert.setUserId(new UserIdentity(Math.abs(_random.nextLong())));
		Date created = generateCreatedTimestamp(createdFilter);
		alert.setCreated(created);
		alert.setValidUntil(new Date(System.currentTimeMillis()+6000000)); // make sure the alert is still valid by creating the timestamp sometime in the future
		alert.setLocation(generateLocation(location, range));
		alert.setAlertId(UUID.randomUUID().toString());
		alert.setRange(ALERT_RANGE);
		
		if(DataGroups.hasDataGroup(DataGroups.DATA_GROUP_ALL, dataGroups)){
			alert.setFiles(generateFileDetailsList(limits));
		}
		
		return alert;
	}
	
	/**
	 * 
	 * @param source if null, random location is returned
	 * @param range 
	 * @return randomly generated location located within the given range of the location source
	 */
	public Location generateLocation(Location source, Double range) {
		Location location = new Location();
		if(source == null){
			if(range != null){
				LOGGER.debug("Ignoring range because of null source.");
			}
			location.setLatitude(_random.nextDouble()*181-90);
			location.setLongitude(_random.nextDouble()*361-180);
		}else{	
			Double sourceHeading = source.getHeading();
			double heading = Math.toRadians(sourceHeading == null ? _random.nextInt(360) : sourceHeading);
			double r = (range == null ? _random.nextInt(service.tut.pori.apilta.alerts.Definitions.DEFAULT_RANGE.intValue()) : range);
			
			double lat = source.getLatitude();
			double lon = source.getLongitude();
			
			lat += Math.cos(heading) * r;
			if(lat < -90){ // simply limit the value in case invalid coordinate is generated
				lat = -90;
			}else if(lat > 90){
				lat = 90;
			}
			
			lon += Math.sin(heading) * r;
			if(lon < -180){ // simply limit the value in case invalid coordinate is generated
				lon = -180;
			}else if(lon > 180){
				lon = 180;
			}
			
			location.setLatitude(lat);
			location.setLongitude(lon);
		}
		return location;
	}
	
	/**
	 * 
	 * @param limits 
	 * @return randomly generated file details list
	 */
	public FileDetailsList generateFileDetailsList(Limits limits) {
		int count = limits.getMaxItems(Definitions.ELEMENT_FILE_DETAILS_LIST);
		if(count < 1){
			LOGGER.warn("count < 1");
			return null;
		}else if(count >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Count was "+Limits.DEFAULT_MAX_ITEMS+", using 1.");
			count = 1;
		}
		
		ArrayList<FileDetails> details = new ArrayList<>(count);
		for(int i=0;i<count;++i){
			details.add(generateFileDetails());
		}
		FileDetailsList list = new FileDetailsList();
		list.setFiles(details);
		return list;
	}
	
	/**
	 * 
	 * @return randomly generated file details
	 */
	public FileDetails generateFileDetails() {
		FileDetails details = new FileDetails();
		details.setGUID(UUID.randomUUID().toString());
		details.setMimeType(IMAGE_URL_MIME_TYPE);
		details.setUrl(IMAGE_URL);
		details.setValidUntil(generateValidUntil());
		return details;
	}
	
	/**
	 * 
	 * @param createdFilter If multiple values are given, the interval will be selected randomly from the given values. If null, a random time between unix epoch and the current time is selected
	 * @return randomly generate date between UNIX epoch and current time
	 */
	public Date generateCreatedTimestamp(Set<Interval> createdFilter) {
		if(createdFilter == null || createdFilter.isEmpty()){
			return new Date(RandomUtils.nextLong(0, System.currentTimeMillis()));
		}else{
			Interval interval = IterableUtils.get(createdFilter, _random.nextInt(createdFilter.size()));
			return new Date(RandomUtils.nextLong(interval.getStart().getTime(), interval.getEnd().getTime()));
		}
	}
	
	/**
	 * 
	 * @return timestamp sometime in the future
	 */
	public Date generateValidUntil() {
		return new Date(TemporaryTokenHandler.CACHE_VALIDITY*1000+System.currentTimeMillis());
	}

	/**
	 * 
	 * @return randomly generated GUID
	 */
	public String createGUID() {
		return UUID.randomUUID().toString();
	}
}
