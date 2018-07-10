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
package service.tut.pori.apilta.shock;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.DateIntervalParameter;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;
import service.tut.pori.apilta.ApiltaProperties;
import service.tut.pori.apilta.shock.GroupCalculator.GroupMethod;
import service.tut.pori.apilta.shock.datatypes.LocationData;
import service.tut.pori.apilta.shock.datatypes.LocationLimits;
import service.tut.pori.apilta.shock.datatypes.ShockHighlight;
import service.tut.pori.apilta.shock.datatypes.ShockHighlightList;
import service.tut.pori.apilta.shock.datatypes.ShockMeasurement;
import service.tut.pori.apilta.shock.datatypes.ShockMeasurementList;
import service.tut.pori.apilta.utils.MathUtils;

/**
 * 
 *
 */
public final class ShockCore {
	private static final DataGroups DATAGROUP_LOCATION_DATA = new DataGroups(Definitions.DATA_GROUP_LOCATION_DATA);
	private static final Logger LOGGER = Logger.getLogger(ShockCore.class);
	
	/**
	 * 
	 */
	private ShockCore() {
		// nothing needed
	}

	/**
	 * 
	 * @param userIdentity
	 * @param list
	 * @throws IllegalArgumentException on invalid data
	 */
	public static void createMeasurement(UserIdentity userIdentity, ShockMeasurementList list) throws IllegalArgumentException {
		if(!ShockMeasurementList.isValid(list)){
			throw new IllegalArgumentException("Empty or invalid measurement list provided.");
		}
		
		ShockDAO dao = ServiceInitializer.getDAOHandler().getDAO(ShockDAO.class);
		for(ShockMeasurement measurement : list.getShockMeasurements()){
			measurement.setUserId(userIdentity); // set the authenticated user as the owner for all measurements
			
			if(measurement.getVisibility() == null){
				LOGGER.debug("Empty visibility for measurement provided by user, id: "+userIdentity.getUserId()+", defaulting to: "+Definitions.DEFAULT_VISIBILITY);
				measurement.setVisibility(Definitions.DEFAULT_VISIBILITY);
			}
			
			dao.createMeasurement(measurement);
		}
	}

	/**
	 * 
	 * @param userIdentity
	 * @param locationLimits
	 * @param dataGroups
	 * @param dateInterval timestamp interval
	 * @param levelFilter 
	 * @param limits
	 * @param groupMethod 
	 * @param groupRange in meters
	 * @param userIdFilter 
	 * @return measurement list or null if nothing was found
	 */
	public static ShockMeasurementList getMeasurements(UserIdentity userIdentity, LocationLimits locationLimits, DataGroups dataGroups, DateIntervalParameter dateInterval, int[] levelFilter, Limits limits, GroupMethod groupMethod, Integer groupRange, long[] userIdFilter) {
		ShockMeasurementList measurements = ServiceInitializer.getDAOHandler().getDAO(ShockDAO.class).getMeasurements(userIdentity, locationLimits, dataGroups, dateInterval, levelFilter, limits, userIdFilter);
		if(!ShockMeasurementList.isEmpty(measurements) && groupMethod != null) {
			GroupCalculator gc = new GroupCalculator(groupMethod, groupRange);
			gc.setLevelFilter(levelFilter);
			List<ShockMeasurement> list = gc.group(measurements.getShockMeasurements());
			if(list == null) {
				return null;
			}
			measurements.setShockMeasurements(list);
		}
		return measurements;
	}

	/**
	 * 
	 * @param userIdentity
	 * @param minMeasurements the highlight group's minimum number of measurements
	 * @param range maximum range (in km) around a central point when calculating groups
	 * @param locationLimits
	 * @param dateInterval timestamp interval
	 * @param levelFilter
	 * @param limits
	 * @param userIdFilter
	 * @return highlight list of null if nothing was found
	 */
	public static ShockHighlightList getHighlights(UserIdentity userIdentity, int minMeasurements, double range, LocationLimits locationLimits, DateIntervalParameter dateInterval, int[] levelFilter, Limits limits, long[] userIdFilter) {
		ShockMeasurementList list = getMeasurements(userIdentity, locationLimits, DATAGROUP_LOCATION_DATA, dateInterval, levelFilter, limits, null, null, userIdFilter);
		if(ShockMeasurementList.isEmpty(list)) {
			LOGGER.debug("No measurements found with the given values.");
			return null;
		}
		
		ArrayList<ShockHighlight> highlights = new ArrayList<>();
		HashSet<Long> userIds = new HashSet<>();
		long minTimeDifference = ServiceInitializer.getPropertyHandler().getSystemProperties(ApiltaProperties.class).getShockGroupTimeDifference();
		List<ShockMeasurement> measurements = list.getShockMeasurements();
		int size = 0;
		while((size = measurements.size()) > minMeasurements) {
			ShockMeasurement center = measurements.remove(size-1);
			Date from = center.getTimestamp();
			Date to = from;
			long centerTimestamp = center.getTimestamp().getTime();
			int minLevel = center.getLevel();
			int maxLevel = minLevel;
			double maxRange = 0;
			int measurementCount = 0;
			userIds.clear();
			Long userId = center.getUserId().getUserId();
			userIds.add(userId);
			LocationData location = center.getLocationData();
			double lat = location.getLatitude();
			double lon = location.getLongitude();
			
			for(Iterator<ShockMeasurement> iter = measurements.iterator(); iter.hasNext();){
				ShockMeasurement m = iter.next();
				Date mt = m.getTimestamp();
				UserIdentity mUserId = m.getUserId();
				if(UserIdentity.equals(mUserId, userId) && Math.abs(mt.getTime()-centerTimestamp) < minTimeDifference){ // if the measurements are from the same user, check that the measurements are from different sessions
					continue;
				}
				location = m.getLocationData();
				double tRange = MathUtils.haversine(lat, lon, location.getLatitude(), location.getLongitude());
				if(tRange < range){
					userIds.add(mUserId.getUserId());
					++measurementCount;
					if(tRange > maxRange){
						maxRange = tRange;
					}
					int level = m.getLevel();
					if(level < minLevel){
						minLevel = level;
					}else if(level > maxLevel){
						maxLevel = level;
					}
					
					if(mt.before(from)) {
						from = mt;
					}else if(mt.after(to)) {
						to = mt;
					}
					iter.remove();
				} // if
			} // for
			
			if(measurementCount > minMeasurements) {
				ShockHighlight hl = new ShockHighlight();
				hl.setLatitude(lat); // naively use the center point even though it may not be the exact center
				hl.setLongitude(lon); // naively use the center point even though it may not be the exact center
				hl.setFrom(from);
				hl.setTo(to);
				hl.setMaxLevel(maxLevel);
				hl.setMinLevel(minLevel);
				hl.setUserCount(userIds.size());
				hl.setMaxRange(maxRange);
				hl.setMeasurementCount(measurementCount);
				highlights.add(hl);
			}
		}
		
		return ShockHighlightList.getShockMeasurementList(highlights);
	}
}
