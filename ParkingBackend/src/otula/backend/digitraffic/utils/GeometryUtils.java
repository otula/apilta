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
package otula.backend.digitraffic.utils;

/**
 * basic geometry utils
 * 
 */
public final class GeometryUtils {
	private static final double EARTH_RADIUS = 6372.8; // In kilometers
    
	/**
	 * 
	 */
	private GeometryUtils(){
		// nothing needed
	}
	
	/**
	 * Use the Haversine formula to calculate the distance between two points
	 * 
	 * See <a href="https://rosettacode.org/wiki/Haversine_formula#Java">Haversine formula</a>
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return the distance in kilometers
	 */
	public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
 
        double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return EARTH_RADIUS * c;
    }
}
