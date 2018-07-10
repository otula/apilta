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
package tut.pori.shockapplication.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import tut.pori.shockapplication.datatypes.Settings;
import tut.pori.shockapplication.datatypes.ShockMeasurement;

/**
 *
 */
public class SensorHandler implements SensorEventListener, LocationListener {
	public static final String PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
	private static final long EVENT_VALIDITY = 2000; // in ms, Note: the sensor events in general, arrive in timely manner, but the location data passed to onLocationUpdated() may be over 1s in the past
	private static final String TAG = SensorHandler.class.toString();
	private Sensor _accelerometer = null;
	private Context _context = null;
	private SQLiteHandler _database = null;
	private Sensor _gyro = null;
	private Sensor _rotation = null;
	private SensorEvent _lastAcceleratorEvent = null;
	private SensorEvent _lastGyroEvent = null;
	private SensorEvent _lastRotationEvent = null;
	private LocationManager _locationManager = null;
	private int _measurementCount = 0; // count of measurements in the active count cycle (since previous start call)
	private SensorManager _sensorManager = null;
	private Settings _settings = null;

	/**
	 *
	 * @param context
	 * @param sqLiteHandler to use for saving data, note: the handler is NOT closed by this class
	 * @param settings
	 */
	public SensorHandler(Context context, SQLiteHandler sqLiteHandler, Settings settings) {
		_context = context;
		_database = sqLiteHandler;
		_sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		_locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		_settings = settings;
	}

	/**
	 *
	 * initialize using the provided settings
	 */
	public void initialize() {
		if(_settings.isUseAccelerometer()){
			_accelerometer = _sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
			if(_accelerometer == null){
				Log.w(TAG, "No linear accelerometer available, attempting to use accelerometer.");
				_accelerometer = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				if(_accelerometer == null){
					Log.w(TAG, "No accelerometer available.");
				}
			}
		}

		if(_settings.isUseGyro()){
			_gyro = _sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
			if(_gyro == null){
				Log.w(TAG, "No gyroscope available.");
			}
		}

		if(_settings.isUseRotation()){
			_rotation = _sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
			if(_rotation == null){
				Log.w(TAG, "No rotation vector available.");
			}
		}
	}

	/**
	 *
	 * @return true on success, false on failure (generally, permission was denied)
	 */
	public boolean startSensors() {
		Log.d(TAG, "Starting sensors...");
		_measurementCount = 0;

		if (_context.checkSelfPermission(PERMISSION_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
			Log.e(TAG, "Permission denied for location updates.");
			return false;
		}

		if(_gyro != null && !_sensorManager.registerListener(this, _gyro, SensorManager.SENSOR_DELAY_NORMAL)){
			Log.w(TAG, "Failed to register gyro listener.");
		}

		if(_accelerometer != null && !_sensorManager.registerListener(this, _accelerometer, SensorManager.SENSOR_DELAY_NORMAL)){
			Log.w(TAG, "Failed to refister accelerometer listener.");
		}

		if(_rotation != null && !_sensorManager.registerListener(this, _rotation, SensorManager.SENSOR_DELAY_NORMAL)){
			Log.w(TAG, "Failed to register rotation listener.");
		}

		_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		if(_settings.isUseNetworkProvider()){
			Log.d(TAG, "Using network provider.");
			_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		}
		return true;
	}

	/**
	 *
	 */
	public void stopSensors() {
		Log.d(TAG, "Stopping sensors...");
		_sensorManager.unregisterListener(this);
		_locationManager.removeUpdates(this);
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		sensorEvent.timestamp = System.currentTimeMillis(); // the original timestamp is in arbitrary ns since boot, which is useless, replace with current time
		if(sensorEvent.sensor == _accelerometer){
			_lastAcceleratorEvent = sensorEvent;
		}else if(sensorEvent.sensor == _gyro){
			_lastGyroEvent = sensorEvent;
		}else if(sensorEvent.sensor == _rotation){
			_lastRotationEvent = sensorEvent;
		}else{
			Log.w(TAG, "Ignored unknown sensor event...");
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {
		Log.d(TAG, "Accuracy changed for sensor: "+sensor.getName()+": "+i);
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "Location changed.");

		Float speed = null;
		float minimumSpeed = _settings.getMinimumSpeed();
		if(minimumSpeed > 0){ // if minimum speed is set
			if(!location.hasSpeed()){ // ignore locations without speed data
				Log.w(TAG, "Ignored location with unknown speed.");
				return;
			}
			speed = location.getSpeed();

			if(speed < minimumSpeed){
				return;
			}
		}else if(location.hasSpeed()){
			speed = location.getSpeed();
		}

		long locationTimestamp = location.getTime(); // just in case use the timestamps to check that the events are not too old

		SensorEvent ae = _lastAcceleratorEvent;
		ShockMeasurement.AccelerometerData aData = null;
		if(ae != null){
			if(Math.abs(locationTimestamp - ae.timestamp) < EVENT_VALIDITY){
				float minimumAcceleration = _settings.getMinimumAcceleration();
				if(minimumAcceleration > 0 && Math.abs(ae.values[0]) < minimumAcceleration && Math.abs(ae.values[1]) < minimumAcceleration && Math.abs(ae.values[2]) < minimumAcceleration){ // if minimum acceleration is set, use it
					Log.d(TAG, "Below minimum acceleration threshold...");
					return;
				}
				aData = new ShockMeasurement.AccelerometerData(ae.values[0], ae.values[1], ae.values[2], ae.timestamp);
			}else{
				Log.d(TAG, "No up-to-date accelerometer data available.");
				return; // accelerometer data is required
			}
		}else {
			Log.d(TAG, "No accelerometer data available.");
			return; // accelerometer data is required
		}

		SensorEvent ge = _lastGyroEvent;
		ShockMeasurement.GyroData gData = null;
		if(ge != null){
			if(Math.abs(locationTimestamp - ge.timestamp) < EVENT_VALIDITY){
				gData = new ShockMeasurement.GyroData(ge.values[0], ge.values[1], ge.values[2], ge.timestamp);
			}
		}

		SensorEvent re = _lastRotationEvent;
		ShockMeasurement.RotationData rData = null;
		if(re != null){
			if(Math.abs(locationTimestamp - re.timestamp) < EVENT_VALIDITY){
				rData = new ShockMeasurement.RotationData(re.values[0], re.values[1], re.values[2], re.values[3], re.values[4], re.timestamp);
			}
		}

		Float heading = null;
		if(location.hasBearing()){
			heading = location.getBearing();
		}

		Log.d(TAG, "Creating new measurement...");
		ShockMeasurement measurement = new ShockMeasurement(aData, _settings.getDataVisibility(), gData, heading, location.getLatitude(), location.getLongitude(), rData, speed, locationTimestamp);
		_database.createMeasurement(measurement); // Note: this will block if database read (in result send) is in progress causing gaps in the result set, this is not a huge problem as such, but if OS responsiveness checks start causing problems, this needs to be moved to a separate thread
		++_measurementCount;
	}

	@Override
	public void onStatusChanged(String s, int i, Bundle bundle) {
		Log.d(TAG, "Status changed: "+s+", "+i);
	}

	@Override
	public void onProviderEnabled(String s) {
		Log.d(TAG, "Provider enabled: "+s);
	}

	@Override
	public void onProviderDisabled(String s) {
		Log.d(TAG, "Provider disabled: "+s);
	}

	/**
	 *
	 * @return number of measurements since previous stop/start
	 * @see #startSensors()
	 */
	public int getMeasurementCount() {
		return _measurementCount;
	}
}
