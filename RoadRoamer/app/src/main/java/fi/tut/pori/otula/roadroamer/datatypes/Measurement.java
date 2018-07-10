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
package fi.tut.pori.otula.roadroamer.datatypes;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * representation of measurement data
 * 
 */
public class Measurement {
    private static final String CLASS_NAME = Measurement.class.toString();
	private Long _backendId = null;
	private List<DataPoint> _dataPoints = null;
	private Long _measurementId = null; // generated id
	
	/**
	 * @return the backendId
	 * @see #setBackendId(Long)
	 */
	public Long getBackendId() {
		return _backendId;
	}

	/**
	 * @param backendId the backendId to set
	 * @see #getBackendId()
	 */
	public void setBackendId(Long backendId) {
		_backendId = backendId;
	}

	/**
	 * @return the data points
	 * @see #setDataPoints(List)
	 */
	public List<DataPoint> getDataPoints() {
		return _dataPoints;
	}

	/**
	 * @param dataPoints the dataPoints to set
	 * @see #getDataPoints()
	 */
	public void setDataPoints(List<DataPoint> dataPoints) {
		_dataPoints = dataPoints;
	}

	public void addDataPoint(DataPoint dataPoint){
        if(_dataPoints==null){
            _dataPoints = new ArrayList<>();
        }
        _dataPoints.add(dataPoint);
    }

	/**
	 * @return the measurementId
	 * @see #setMeasurementId(Long)
	 */
	public Long getMeasurementId() {
		return _measurementId;
	}
	
	/**
	 * @param measurementId the measurementId to set
	 * @see #getMeasurementId()
	 */
	public void setMeasurementId(Long measurementId) {
		_measurementId = measurementId;
	}
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if valid
	 * @see #isValid(Measurement)
	 */
	protected boolean isValid() {
		if(_backendId == null) {
            Log.d(CLASS_NAME, "Invalid back end id.");
			return false;
		}
		
		if(_dataPoints == null || _dataPoints.isEmpty()) {
            Log.d(CLASS_NAME, "No data points.");
			return false;
		}
		
		for(DataPoint dp : _dataPoints){
			if(!DataPoint.isValid(dp)){
                Log.d(CLASS_NAME, "Measurement for back end, id: "+_backendId+" contained an invalid data point.");
				return false;
			}
		}
		
		return true;
	}

	/**
	 * 
	 * @param measurement
	 * @return false if the measurement is null or invalid
	 */
	public static boolean isValid(Measurement measurement) {
		if(measurement == null){
			return false;
		}else{
			return measurement.isValid();
		}
	}
}
