/*
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
package tut.pori.shockapplication.datatypes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;


/**
 * settings for the alert application
 */
public class Settings {
	private static final String CLASS_NAME = Settings.class.toString();
	private static final String SETTING_DATA_VISIBILITY = "dataVisibility";
	private static final String SETTING_MAX_MEASUREMENTS = "maxMeasurements";
	private static final String SETTING_MIN_ACCELERATION = "minAcceleration";
    private static final String SETTING_MIN_MEASUREMENT_LEVEL = "minMeasurementLevel";
	private static final String SETTING_MIN_MEASUREMENTS = "minMeasurements";
	private static final String SETTING_MIN_SPEED = "minSpeed";
	private static final String SETTING_PASSWORD = "password";
    private static final String SETTING_SERVICE_URI = "serviceUri";
	private static final String SETTING_SYSTEMATIC_ERROR = "systematicError";
	private static final String SETTING_USE_ACCELEROMETER = "useAccelerometer";
	private static final String SETTING_USE_GYRO = "useGyro";
	private static final String SETTING_USE_NERWORK_PROVIDER = "useNetworkProvider";
	private static final String SETTING_USE_ROTATION = "useRotation";
	private static final String SETTING_USERNAME = "username";
	private String _dataVisibility = null;
	private int _maxMeasurements = 10000;
	private float _minimumAcceleration = -1;
    private int _minMeasurementLevel = 0;
	private int _minMeasurements = 0;
	private float _minimumSpeed = -1; // minimum speed in m/s
	private String _password = null;
	private String _serviceURI = null;
	private Float _systematicError = null;
	private boolean _useAccelerometer = true;
	private boolean _useGyro = false;
	private boolean _useNetworkProvider = false;
	private String _username = null;
	private boolean _useRotation = false;

	/**
	 * this will automatically load the settings for the given context
	 *
	 * @param context
	 */
	public Settings(Context context){
		load(context);
	}

	/**
	 *
	 * @return password
	 * @see #setPassword(String)
	 */
	public String getPassword() {
		return _password;
	}

	/**
	 *
	 * @param password
	 * @see #getPassword()
	 */
	public void setPassword(String password) {
		_password = password;
	}

	/**
	 *
	 * @return service uri without method names
	 * @see #setServiceURI(String)
	 */
	public String getServiceURI() {
		return _serviceURI;
	}

	/**
	 *
	 * @param serviceURI service uri without method names
	 * @see #getServiceURI()
	 */
	public void setServiceURI(String serviceURI) {
		_serviceURI = serviceURI;
	}

	/**
	 *
	 * @return username
	 * @see #setUsername(String)
	 */
	public String getUsername() {
		return _username;
	}

	/**
	 *
	 * @param username
	 * @see #getUsername()
	 */
	public void setUsername(String username) {
		_username = username;
	}

	/**
	 *
	 * @return minimum amount of measurements to send
	 * @see #setMinMeasurements(int)
	 */
	public int getMinMeasurements() {
		return _minMeasurements;
	}

	/**
	 *
	 * @param minMeasurements
	 * @see #setMinMeasurements(int)
	 */
	public void setMinMeasurements(int minMeasurements) {
		_minMeasurements = minMeasurements;
	}

	/**
	 *
	 * @return dataVisibility
	 * @see #setDataVisibility(String)
	 */
	public String getDataVisibility() {
		return _dataVisibility;
	}

	/**
	 *
	 * @param dataVisibility
	 * @see #getDataVisibility()
	 */
	public void setDataVisibility(String dataVisibility) {
		_dataVisibility = dataVisibility;
	}

	/**
	 *
	 * @param context
	 */
	public void save(Context context){
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

		if(StringUtils.isBlank(_serviceURI)){
			editor.remove(SETTING_SERVICE_URI);
		}else{
			editor.putString(SETTING_SERVICE_URI, _serviceURI);
		}

		if(StringUtils.isBlank(_password)){
			editor.remove(SETTING_PASSWORD);
		}else{
			editor.putString(SETTING_PASSWORD, _password);
		}

		if(StringUtils.isBlank(_username)){
			editor.remove(SETTING_USERNAME);
		}else{
			editor.putString(SETTING_USERNAME, _username);
		}

		if(StringUtils.isBlank(_dataVisibility)){
			editor.remove(SETTING_DATA_VISIBILITY);
		}else{
			editor.putString(SETTING_DATA_VISIBILITY, _dataVisibility);
		}

		editor.putInt(SETTING_MAX_MEASUREMENTS, _maxMeasurements);
		editor.putInt(SETTING_MIN_MEASUREMENTS, _minMeasurements);
        editor.putInt(SETTING_MIN_MEASUREMENT_LEVEL, _minMeasurementLevel);
		editor.putFloat(SETTING_MIN_ACCELERATION, _minimumAcceleration);
		editor.putFloat(SETTING_MIN_SPEED, _minimumSpeed);

		if(_systematicError == null){
			editor.remove(SETTING_SYSTEMATIC_ERROR);
		}else{
			editor.putFloat(SETTING_SYSTEMATIC_ERROR, _systematicError);
		}

        editor.putBoolean(SETTING_USE_NERWORK_PROVIDER, _useNetworkProvider);
		editor.putBoolean(SETTING_USE_ACCELEROMETER, _useAccelerometer);
		editor.putBoolean(SETTING_USE_GYRO, _useGyro);
		editor.putBoolean(SETTING_USE_ROTATION, _useRotation);

		editor.commit();
	}

	/**
	 * load/reset the settings to previously saved values
	 *
	 * @param context
	 */
	public void load(Context context){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		_serviceURI = preferences.getString(SETTING_SERVICE_URI, null);
		_password = preferences.getString(SETTING_PASSWORD, null);
		_username = preferences.getString(SETTING_USERNAME, null);
		_dataVisibility = preferences.getString(SETTING_DATA_VISIBILITY, Definitions.DATA_VISIBILITY_PRIVATE);
		_minMeasurements = preferences.getInt(SETTING_MIN_MEASUREMENTS, _minMeasurements);
        _minMeasurementLevel = preferences.getInt(SETTING_MIN_MEASUREMENT_LEVEL, _minMeasurementLevel);
        _useNetworkProvider = preferences.getBoolean(SETTING_USE_NERWORK_PROVIDER, _useNetworkProvider);
        _minimumAcceleration = preferences.getFloat(SETTING_MIN_ACCELERATION, _minimumAcceleration);
        _minimumSpeed = preferences.getFloat(SETTING_MIN_SPEED, _minimumSpeed);
        _maxMeasurements = preferences.getInt(SETTING_MAX_MEASUREMENTS, _maxMeasurements);
		_useAccelerometer = preferences.getBoolean(SETTING_USE_ACCELEROMETER, _useAccelerometer);
		_useGyro = preferences.getBoolean(SETTING_USE_GYRO, _useGyro);
		_useRotation = preferences.getBoolean(SETTING_USE_ROTATION, _useRotation);

        if(preferences.contains(SETTING_SYSTEMATIC_ERROR)){
			_systematicError = preferences.getFloat(SETTING_SYSTEMATIC_ERROR, 0);
		}else{
        	_systematicError = null;
		}
	}

	/**
	 *
	 * @return true if all required settings are given
	 */
	public boolean isValid(){
		if(StringUtils.isBlank(_serviceURI)){
			Log.d(CLASS_NAME, SETTING_SERVICE_URI +" is invalid.");
			return false;
		}

		if(StringUtils.isEmpty(_password)){
			Log.d(CLASS_NAME, SETTING_PASSWORD+" is invalid.");
			return false;
		}

		if(StringUtils.isEmpty(_username)){
			Log.d(CLASS_NAME, SETTING_USERNAME+" is invalid.");
			return false;
		}

		return true;
	}

    /**
     *
     * @return minimum measurement level to send to the service
     * @see #setMinMeasurementLevel(int)
     */
    public int getMinMeasurementLevel() {
        return _minMeasurementLevel;
    }

    /**
     *
     * @param minMeasurementLevel
     * @see #getMinMeasurementLevel()
     */
    public void setMinMeasurementLevel(int minMeasurementLevel) {
        _minMeasurementLevel = minMeasurementLevel;
    }

	/**
	 *
	 * @return true if network provider can be used
	 * @see #setUseNetworkProvider(boolean)
	 */
	public boolean isUseNetworkProvider() {
		return _useNetworkProvider;
	}

	/**
	 *
	 * @param useNetworkProvider
	 * @see #isUseNetworkProvider()
	 */
	public void setUseNetworkProvider(boolean useNetworkProvider) {
		_useNetworkProvider = useNetworkProvider;
	}

	/**
	 *
	 * @return minimum acceleration
	 * @see #setMinimumAcceleration(float)
	 */
	public float getMinimumAcceleration() {
		return _minimumAcceleration;
	}

	/**
	 *
	 * @param minimumAcceleration
	 * @see #getMinimumAcceleration()
	 */
	public void setMinimumAcceleration(float minimumAcceleration) {
		_minimumAcceleration = minimumAcceleration;
	}

	/**
	 *
	 * @return minimum speed
	 * @see #setMinimumSpeed(float)
	 */
	public float getMinimumSpeed() {
		return _minimumSpeed;
	}

	/**
	 *
	 * @param minimumSpeed
	 * @see #getMinimumSpeed()
	 */
	public void setMinimumSpeed(float minimumSpeed) {
		_minimumSpeed = minimumSpeed;
	}

	/**
	 *
	 * @return max measurement amount to send at a time
	 * @see #setMaxMeasurements(int)
	 */
	public int getMaxMeasurements() {
		return _maxMeasurements;
	}

	/**
	 *
	 * @param maxMeasurements
	 * @see #getMaxMeasurements()
	 */
	public void setMaxMeasurements(int maxMeasurements) {
		_maxMeasurements = maxMeasurements;
	}

	/**
	 *
	 * @return the absolute value of xyz systematic error
	 * @see #setSystematicError(Float)
	 */
	public Float getSystematicError() {
		return _systematicError;
	}

	/**
	 *
	 * @param systematicError
	 * @see #getSystematicError()
	 */
	public void setSystematicError(Float systematicError) {
		_systematicError = systematicError;
	}

	/**
	 *
	 * @return true if accelerometer is enabled
	 * @see #setUseAccelerometer(boolean)
	 */
	public boolean isUseAccelerometer() {
		return _useAccelerometer;
	}

	/**
	 *
	 * @param useAccelerometer
	 * @see #isUseAccelerometer()
	 */
	public void setUseAccelerometer(boolean useAccelerometer) {
		_useAccelerometer = useAccelerometer;
	}

	/**
	 *
	 * @return true if gyro is enabled
	 * @see #setUseGyro(boolean)
	 */
	public boolean isUseGyro() {
		return _useGyro;
	}

	/**
	 *
	 * @param useGyro
	 * @see #isUseGyro()
	 */
	public void setUseGyro(boolean useGyro) {
		_useGyro = useGyro;
	}

	/**
	 *
	 * @return true if roation sensor is enabled
	 * @see #setUseRotation(boolean)
	 */
	public boolean isUseRotation() {
		return _useRotation;
	}

	/**
	 *
	 * @param useRotation
	 * @see #isUseRotation()
	 */
	public void setUseRotation(boolean useRotation) {
		_useRotation = useRotation;
	}
}
