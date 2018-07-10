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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.ArrayUtils;

import core.tut.pori.context.ServiceInitializer;
import service.tut.pori.apilta.ApiltaProperties;
import service.tut.pori.apilta.shock.datatypes.LocationData;
import service.tut.pori.apilta.shock.datatypes.ShockMeasurement;
import service.tut.pori.apilta.utils.MathUtils;

/**
 * calculate measurement grouping based on the given data
 * 
 */
public class GroupCalculator {
	private static final String GROUP_METHOD_AVERAGE = "AVERAGE";
	private static final String GROUP_METHOD_MEDIAN = "MEDIAN";
	private GroupMethod _method = null;
	private double _range = 0; // in km
	private int[] _levelFilter = null;
	
	/**
	 * method used for grouping
	 * 
	 */
	public enum GroupMethod {
		/**
		 * calculate group value based on average (arithmetic mean)
		 */
		AVERAGE(GROUP_METHOD_AVERAGE),
		/**
		 * calculate group value based on median
		 */
		MEDIAN(GROUP_METHOD_MEDIAN);
		
		private String _value;
		
		/**
		 * 
		 * @param value
		 */
		private GroupMethod(String value) {
			_value = value;
		}
		
		/**
		 * 
		 * @param value
		 * @return group method
		 * @throws IllegalArgumentException
		 */
		public static GroupMethod fromString(String value) throws IllegalArgumentException {
			for(GroupMethod m : values()) {
				if(m._value.equalsIgnoreCase(value)) {
					return m;
				}
			}
			throw new IllegalArgumentException("Invalid value: "+value);
		}
	} // enum GroupMethod
	
	/**
	 * 
	 * @param method
	 * @param range in meters, if null or &lt; 1 default is used
	 */
	public GroupCalculator(GroupMethod method, Integer range) {
		_method = method;
		if(range == null || range < 1) {
			_range = ServiceInitializer.getPropertyHandler().getSystemProperties(ApiltaProperties.class).getShockGroupRange();
		}else {
			_range = range;
			_range /= 1000;
		}
	}
	
	/**
	 * @param levelFilter the levelFilter to set
	 */
	public void setLevelFilter(int[] levelFilter) {
		_levelFilter = levelFilter;
	}

	/**
	 * 
	 * @param measurements note: the passed list might be modified by this method
	 * @return the grouped list
	 */
	public List<ShockMeasurement> group(List<ShockMeasurement> measurements) {
		switch(_method) {
			case AVERAGE:
				return groupAverage(measurements);
			case MEDIAN:
				return groupMedian(measurements);
			default:
				throw new IllegalArgumentException("Unknown method: "+_method);
		}
	}
	
	/**
	 * 
	 * @param measurements
	 * @return values grouped by median
	 */
	private List<ShockMeasurement> groupMedian(List<ShockMeasurement> measurements) {
		ArrayList<ShockMeasurement> grouped = new ArrayList<>();
		
		int levelSum = 0;
		int levelCount = 0;
		ShockMeasurement center = null;
		double centerLat = 0;
		double centerLon = 0;
		Iterator<ShockMeasurement> iter = measurements.iterator();
		while(iter.hasNext()) {
			while(iter.hasNext()) {
				ShockMeasurement m = iter.next();
				Integer level = m.getLevel();
				if(level == null) { // ignore measurements without known level
					iter.remove();
					continue;
				}
				
				LocationData d = m.getLocationData();
				if(center == null) {
					center = m;
					centerLat = d.getLatitude();
					centerLon = d.getLongitude();
					levelSum += level;
					++levelCount;
					iter.remove();
				}else if(MathUtils.haversine(centerLat, centerLon, d.getLatitude(), d.getLongitude()) < _range) {
					levelSum += level;
					++levelCount;
					iter.remove();
				} // if
			} // while
			
			if(levelCount > 0) {
				int level = Math.round(levelSum/levelCount);
				if(_levelFilter == null || ArrayUtils.contains(_levelFilter, level)) {
					center.setLevel(level);
					grouped.add(center);
				}
			}
			center = null;
			levelCount = 0;
			levelSum = 0;
			iter = measurements.iterator();
		} // for
		
		return (grouped.isEmpty() ? null : grouped);
	}
	
	/**
	 * 
	 * @param measurements
	 * @return values grouped by median
	 */
	private List<ShockMeasurement> groupAverage(List<ShockMeasurement> measurements) {
		ArrayList<ShockMeasurement> grouped = new ArrayList<>();
		
		LinkedList<Integer> groupLevels = new LinkedList<>();
		int levelCount = 0;
		ShockMeasurement center = null;
		double centerLat = 0;
		double centerLon = 0;
		Iterator<ShockMeasurement> iter = measurements.iterator();
		while(iter.hasNext()) {
			while(iter.hasNext()) {
				ShockMeasurement m = iter.next();
				Integer level = m.getLevel();
				if(level == null) { // ignore measurements without known level
					iter.remove();
					continue;
				}
				
				LocationData d = m.getLocationData();
				if(center == null) {
					center = m;
					centerLat = d.getLatitude();
					centerLon = d.getLongitude();
					groupLevels.add(level);
					++levelCount;
					iter.remove();
				}else if(MathUtils.haversine(centerLat, centerLon, d.getLatitude(), d.getLongitude()) < _range) {
					int sLevel = level;
					ListIterator<Integer> lIter = groupLevels.listIterator(levelCount);
					while(true) {
						if(sLevel >= lIter.previous()) {
							lIter.add(level);
							break;
						}else if(!lIter.hasPrevious()) {
							groupLevels.addFirst(level);
							break;
						}
					}
					++levelCount;
					iter.remove();
				} // if
			} // while
			
			if(levelCount == 0) {
				continue;
			}else if(levelCount % 2 == 0) {
				int mIndex = levelCount/2;
				int level = Math.round((groupLevels.get(mIndex)+groupLevels.get(mIndex-1))/2);
				if(_levelFilter == null || ArrayUtils.contains(_levelFilter, level)) {
					center.setLevel(level);
					grouped.add(center);
				}
			}else {
				center.setLevel(groupLevels.get(levelCount/2));
				grouped.add(center);
			}
			center = null;
			levelCount = 0;
			groupLevels.clear();
			iter = measurements.iterator();
		} // for
		
		return (grouped.isEmpty() ? null : grouped);
	}
}
