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
package tut.pori.alertapplication.datatypes;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tut.pori.alertapplication.utils.Definitions;


/**
 * settings for the alert application
 */
public class Settings {
	private static final String CLASS_NAME = Settings.class.toString();
    private static final String SETTING_ALERT_CHECK_INTERVAL = "alertCheckInterval";
    private static final String SETTING_ALERT_DATA_ALERT_TYPE = "alertDataAlertType";
    private static final String SETTING_ALERT_DATA_FILE_PATH = "alertDataFilePath";
    private static final String SETTING_ALERT_DATA_LOCATION_LATITUDE = "alertDataLocationLatitude";
    private static final String SETTING_ALERT_DATA_LOCATION_LONGITUDE = "alertDataLocationLongitude";
    private static final String SETTING_ALERT_DATA_TIMESTAMP = "alertDataTimestamp";
	private static final String SETTING_ALLOW_MAP_GESTURES = "allowMapGestures";
	private static final String SETTING_ALLOW_PHOTOS = "allowPhotos";
	private static final String SETTING_HIDE_USERS_ALERTS = "hideUsersAlert";
	private static final String SETTING_KEEP_FILES = "keepFiles";
	private static final String SETTING_LISTEN_ALERT_GROUP_IDS = "listenAlertGroupIds";
	private static final String SETTING_LISTEN_ALERT_TYPES = "listenAlertTypes";
	private static final String SETTING_MIN_LOCATION_DISTANCE = "minLocationDistance";
    private static final String SETTING_MIN_LOCATION_TIME = "minLocationTime";
	private static final String SETTING_PASSWORD = "password";
	private static final String SETTING_RANGE = "range";
	private static final String SETTING_REPORT_ALERT_GROUP_IDS = "reportAlertGroupIds";
	private static final String SETTING_REPORT_ALERT_TYPES = "reportAlertTypes";
    private static final String SETTING_SERVICE_URI = "serviceUri";
    private static final String SETTING_USE_GPS = "useGPS";
    private static final String SETTING_USE_NETWORK_PROVIDER = "useNetworkProvider";
	private static final String SETTING_USE_TIME_FILTER = "useTimeFilter";
    private static final String SETTING_USE_TTS = "useTTS";
	private static final String SETTING_USER_ID = "userId";
	private static final String SETTING_USERNAME = "username";
	private int _alertCheckInterval = 30; // in seconds
    private Alert _alertData = null;
	private boolean _allowMapGestures = false;
	private boolean _allowPhotos = false;
	private boolean _hideUsersAlerts = true;
	private boolean _keepFiles = true;
	private Set<String> _listenAlertGroupIds = null;
	private EnumSet<Alert.AlertType> _listenAlertTypes = null;
    private int _minLocationDistance = 2; // in meters
    private int _minLocationTime = 2000; // in ms
	private String _password = null;
	private float _range = 5f; // in kilometers
	private Set<String> _reportAlertGroupIds = null;
	private EnumSet<Alert.AlertType> _reportAlertTypes = null;
	private String _serviceURI = null;
    private boolean _useGPS = true;
    private boolean _useNetworkProvider = true;
	private Long _userId = null;
	private String _username = null;
    private boolean _useTimeFilter = true;
	private boolean _useTTS = false;

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
	 * @return true if gestures are allowed on the map
	 * @see #setAllowMapGestures(boolean)
	 */
	public boolean isAllowMapGestures() {
		return _allowMapGestures;
	}

	/**
	 *
	 * @param allowMapGestures
	 * @see #isAllowPhotos()
	 */
	public void setAllowMapGestures(boolean allowMapGestures) {
		_allowMapGestures = allowMapGestures;
	}

	/**
	 *
	 * @return true if text-to-speech is used for sound effects
	 * @see #setUseTTS(boolean)
	 */
	public boolean isUseTTS() {
		return _useTTS;
	}

	/**
	 *
	 * @param useTTS
	 * @see #isUseTTS()
	 */
	public void setUseTTS(boolean useTTS) {
		_useTTS = useTTS;
	}

    /**
     *
     * @return true if time filter (created) is used with get alerts
     * @see #setUseTimeFilter(boolean)
     */
    public boolean isUseTimeFilter() {
        return _useTimeFilter;
    }

    /**
     *
     * @return min location check distance
     * @see #setMinLocationDistance(int)
     */
    public int getMinLocationDistance() {
        return _minLocationDistance;
    }

    /**
     *
     * @param minLocationDistance
     * @see #getMinLocationDistance()
     */
    public void setMinLocationDistance(int minLocationDistance) {
        _minLocationDistance = minLocationDistance;
    }

    /**
     *
     * @return the minimum location check interval
     * @see #setMinLocationTime(int)
     */
    public int getMinLocationTime() {
        return _minLocationTime;
    }

    /**
     *
     * @param minLocationTime in ms
     * @see #getMinLocationTime()
     */
    public void setMinLocationTime(int minLocationTime) {
        _minLocationTime = minLocationTime;
    }

    /**
     *
     * @param useTimeFilter
     * @see #isUseTimeFilter()
     */
    public void setUseTimeFilter(boolean useTimeFilter) {
        _useTimeFilter = useTimeFilter;
    }

    /**
	 *
	 * @return range in km
	 * @see #setRange(float)
	 */
	public float getRange() {
		return _range;
	}

	/**
	 *
	 * @param range in km
	 * @see #getRange()
	 */
	public void setRange(float range) {
		_range = range;
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
	 * @return true if the user's own alerts should be hidden from the alert lists
	 * @see #setHideUsersAlerts(boolean)
	 */
	public boolean isHideUsersAlerts() {
		return _hideUsersAlerts;
	}

	/**
	 *
	 * @param hideUsersAlerts
	 * @see #isHideUsersAlerts()
	 */
	public void setHideUsersAlerts(boolean hideUsersAlerts) {
		_hideUsersAlerts = hideUsersAlerts;
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
	 * @return alert group ids the user is listening for new alerts
	 * @see #setListenAlertGroupIds(Set)
	 */
	public Set<String> getListenAlertGroupIds() {
		return _listenAlertGroupIds;
	}

	/**
	 *
	 * @param listenAlertGroupIds the alert groups the user is listening for new alerts
	 * @see #getListenAlertGroupIds()
	 */
	public void setListenAlertGroupIds(Set<String> listenAlertGroupIds) {
		_listenAlertGroupIds = listenAlertGroupIds;
	}

	/**
	 *
	 * @return the alert group ids used when reporting new alerts
	 * @see #setReportAlertGroupIds(Set)
	 */
	public Set<String> getReportAlertGroupIds() {
		return _reportAlertGroupIds;
	}

	/**
	 *
	 * @param reportAlertGroupIds the alert group ids used when reporting new alerts
	 * @see #getReportAlertGroupIds()
	 */
	public void setReportAlertGroupIds(Set<String> reportAlertGroupIds) {
		_reportAlertGroupIds = reportAlertGroupIds;
	}

	/**
	 *
	 * @return alert types the user is listening for new alerts
	 * @see #setListenAlertTypes(EnumSet)
	 */
	public EnumSet<Alert.AlertType> getListenAlertTypes() {
		return _listenAlertTypes;
	}

	/**
	 *
	 * @param listenAlertTypes the alert types the user is listening for new alerts
	 * @see #getListenAlertTypes()
	 */
	public void setListenAlertTypes(EnumSet<Alert.AlertType> listenAlertTypes) {
		_listenAlertTypes = listenAlertTypes;
	}

	/**
	 *
	 * @return the alert types for the user is submitting new alerts
	 * @see #getReportAlertTypes()
	 */
	public EnumSet<Alert.AlertType> getReportAlertTypes() {
		return _reportAlertTypes;
	}

	/**
	 *
	 * @param reportAlertTypes the alert types for the user is submitting new alerts
	 * @see #getReportAlertTypes()
	 */
	public void setReportAlertTypes(EnumSet<Alert.AlertType> reportAlertTypes) {
		_reportAlertTypes = reportAlertTypes;
	}

	/**
	 *
	 * @return location check interval in seconds
	 * @see #setAlertCheckInterval(int)
	 */
	public int getAlertCheckInterval() {
		return _alertCheckInterval;
	}

	/**
	 *
	 * @param alertCheckInterval in seconds
	 * @see #setAlertCheckInterval(int)
	 */
	public void setAlertCheckInterval(int alertCheckInterval) {
		_alertCheckInterval = alertCheckInterval;
	}

    /**
     *
     * @return true if GPS use is enabled
     * @see #setUseGPS(boolean)
     */
    public boolean isUseGPS() {
        return _useGPS;
    }

    /**
     *
     * @param useGPS
     * @see #isUseGPS()
     */
    public void setUseGPS(boolean useGPS) {
        _useGPS = useGPS;
    }

    /**
     *
     * @return true if use of network provider is enabled
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
	 * @return true if attaching photos to alerts, and viewing photos in the map view is allowed
	 * @see #setAllowPhotos(boolean)
	 */
	public boolean isAllowPhotos() {
		return _allowPhotos;
	}

	/**
	 *
	 * @param allowPhotos
	 * @see #isAllowPhotos()
	 */
	public void setAllowPhotos(boolean allowPhotos) {
		_allowPhotos = allowPhotos;
	}

	/**
	 *
	 * @return true if temporary files (e.g. photos) are kept on the device after uploading the files to the service
	 * @see #setKeepFiles(boolean)
	 */
	public boolean isKeepFiles() {
		return _keepFiles;
	}

	/**
	 *
	 * @param keepFiles
	 * @see #isKeepFiles()
	 */
	public void setKeepFiles(boolean keepFiles) {
		_keepFiles = keepFiles;
	}

	/**
	 *
	 * @return user id
	 * @see #setUserId(Long)
	 */
	public Long getUserId() {
		return _userId;
	}

	/**
	 *
	 * @param userId
	 * @see #getUserId()
	 */
	public void setUserId(Long userId) {
		_userId = userId;
	}

    /**
     *
     * @return alert data, because of possible serialization/save process this may not be the same object as passed to @{@link #setAlertData(Alert)}
     * @see #setAlertData(Alert)
     */
    public Alert getAlertData() {
        return _alertData;
    }

    /**
     * Note: this is not guaranteed to store the alert completely, only timestamp, location, alert type and local file paths file details (if given) are stored in the (permanent) settings
     *
     * @param alertData
     * @see #getAlertData()
     */
    public void setAlertData(Alert alertData) {
        _alertData = alertData;
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

		editor.putInt(SETTING_ALERT_CHECK_INTERVAL, _alertCheckInterval);

		if(_listenAlertGroupIds == null || _listenAlertGroupIds.isEmpty()){
			editor.remove(SETTING_LISTEN_ALERT_GROUP_IDS);
		}else{
			editor.putStringSet(SETTING_LISTEN_ALERT_GROUP_IDS, _listenAlertGroupIds);
		}

		if(_reportAlertGroupIds == null || _reportAlertGroupIds.isEmpty()){
			editor.remove(SETTING_REPORT_ALERT_GROUP_IDS);
		}else{
			editor.putStringSet(SETTING_REPORT_ALERT_GROUP_IDS, _reportAlertGroupIds);
		}

		if(_listenAlertTypes == null || _listenAlertTypes.isEmpty()){
			editor.remove(SETTING_LISTEN_ALERT_TYPES);
		}else{
			editor.putStringSet(SETTING_LISTEN_ALERT_TYPES, toStringSet(_listenAlertTypes));
		}

		if(_reportAlertTypes == null || _reportAlertTypes.isEmpty()){
			editor.remove(SETTING_REPORT_ALERT_TYPES);
		}else{
			editor.putStringSet(SETTING_REPORT_ALERT_TYPES, toStringSet(_reportAlertTypes));
		}

		editor.putBoolean(SETTING_ALLOW_PHOTOS, _allowPhotos);
		editor.putBoolean(SETTING_KEEP_FILES, _keepFiles);
        editor.putBoolean(SETTING_USE_GPS, _useGPS);
        editor.putBoolean(SETTING_USE_NETWORK_PROVIDER, _useNetworkProvider);
		editor.putFloat(SETTING_RANGE, _range);
		editor.putBoolean(SETTING_HIDE_USERS_ALERTS, _hideUsersAlerts);
		editor.putBoolean(SETTING_USE_TTS, _useTTS);
        editor.putBoolean(SETTING_USE_TIME_FILTER, _useTimeFilter);
		editor.putBoolean(SETTING_ALLOW_MAP_GESTURES, _allowMapGestures);
        editor.putInt(SETTING_MIN_LOCATION_DISTANCE, _minLocationDistance);
        editor.putInt(SETTING_MIN_LOCATION_TIME, _minLocationTime);

		if(_userId == null){
			editor.remove(SETTING_USER_ID);
		}else{
			editor.putLong(SETTING_USER_ID, _userId);
		}

		if(_alertData == null){
            editor.remove(SETTING_ALERT_DATA_ALERT_TYPE);
            editor.remove(SETTING_ALERT_DATA_LOCATION_LATITUDE);
            editor.remove(SETTING_ALERT_DATA_LOCATION_LONGITUDE);
            editor.remove(SETTING_ALERT_DATA_TIMESTAMP);
        }else{
            Location location = _alertData.getLocation();
            editor.putLong(SETTING_ALERT_DATA_LOCATION_LATITUDE, Double.doubleToLongBits(location.getLatitude()));
            editor.putLong(SETTING_ALERT_DATA_LOCATION_LONGITUDE, Double.doubleToLongBits(location.getLongitude()));
            editor.putLong(SETTING_ALERT_DATA_TIMESTAMP, _alertData.getCreated().getTime());
            editor.putString(SETTING_ALERT_DATA_ALERT_TYPE, _alertData.getAlertType().toAlertTypeString());
            List<FileDetails> files = _alertData.getFiles();
            if(files != null && !files.isEmpty()){
                Set<String> paths = new HashSet<>(files.size());
                for(FileDetails details : files){
                    paths.add(details.getFilePath());
                }
                editor.putStringSet(SETTING_ALERT_DATA_FILE_PATH, paths);
            }
        }

		editor.commit();
	}

	/**
	 *
	 * @param types
	 * @return the types converted to string
	 */
	private Set<String> toStringSet(EnumSet<Alert.AlertType> types){
		HashSet<String> strings = new HashSet<>(types.size());
		for(Alert.AlertType type : types){
			strings.add(type.toAlertTypeString());
		}
		return strings;
	}

	/**
	 * load/reset the settings to previously saved values
	 *
	 * @param context
	 */
	public void load(Context context){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		_serviceURI = preferences.getString(SETTING_SERVICE_URI, null);
		_alertCheckInterval = preferences.getInt(SETTING_ALERT_CHECK_INTERVAL, _alertCheckInterval);
		_password = preferences.getString(SETTING_PASSWORD, null);
		_username = preferences.getString(SETTING_USERNAME, null);
		_listenAlertGroupIds = preferences.getStringSet(SETTING_LISTEN_ALERT_GROUP_IDS, null);
		_reportAlertGroupIds = preferences.getStringSet(SETTING_REPORT_ALERT_GROUP_IDS, null);
        _useGPS = preferences.getBoolean(SETTING_USE_GPS, _useGPS);
        _useNetworkProvider = preferences.getBoolean(SETTING_USE_NETWORK_PROVIDER, _useNetworkProvider);
		_allowPhotos = preferences.getBoolean(SETTING_ALLOW_PHOTOS, _allowPhotos);
		_keepFiles = preferences.getBoolean(SETTING_KEEP_FILES, _keepFiles);
		_hideUsersAlerts = preferences.getBoolean(SETTING_HIDE_USERS_ALERTS, _hideUsersAlerts);
		_allowMapGestures = preferences.getBoolean(SETTING_ALLOW_MAP_GESTURES, _allowMapGestures);
		_useTTS = preferences.getBoolean(SETTING_USE_TTS, _useTTS);
        _useTimeFilter = preferences.getBoolean(SETTING_USE_TIME_FILTER, _useTimeFilter);
        _minLocationDistance = preferences.getInt(SETTING_MIN_LOCATION_DISTANCE, _minLocationDistance);
        _minLocationTime = preferences.getInt(SETTING_MIN_LOCATION_TIME, _minLocationTime);

		if(preferences.contains(SETTING_LISTEN_ALERT_TYPES)){
			Set<String> types = preferences.getStringSet(SETTING_LISTEN_ALERT_TYPES, null);
			_listenAlertTypes = EnumSet.noneOf(Alert.AlertType.class);
			for(String type : types){
				_listenAlertTypes.add(Alert.AlertType.fromAlertTypeString(type));
			}
		}else{
			_listenAlertTypes = null;
		}
		_range = preferences.getFloat(SETTING_RANGE, _range);
		if(preferences.contains(SETTING_REPORT_ALERT_TYPES)){
			Set<String> types = preferences.getStringSet(SETTING_REPORT_ALERT_TYPES, null);
			_reportAlertTypes = EnumSet.noneOf(Alert.AlertType.class);
			for(String type : types){
				_reportAlertTypes.add(Alert.AlertType.fromAlertTypeString(type));
			}
		}else{
			_reportAlertTypes = null;
		}

		if(preferences.contains(SETTING_ALERT_DATA_ALERT_TYPE)){
            _alertData = new Alert();
            Location location = new Location(Definitions.LOCATION_PROVIDER_SETTINGS);
            location.setLatitude(Double.longBitsToDouble(preferences.getLong(SETTING_ALERT_DATA_LOCATION_LATITUDE, 0l)));
            location.setLongitude(Double.longBitsToDouble(preferences.getLong(SETTING_ALERT_DATA_LOCATION_LONGITUDE, 0l)));
            _alertData.setLocation(location);
            _alertData.setCreated(new Date(preferences.getLong(SETTING_ALERT_DATA_TIMESTAMP, 0l)));
            _alertData.setAlertType(Alert.AlertType.fromAlertTypeString(preferences.getString(SETTING_ALERT_DATA_ALERT_TYPE, null)));

            Set<String> filePaths = preferences.getStringSet(SETTING_ALERT_DATA_FILE_PATH, null);
            if(filePaths != null){
                for(String path : filePaths){
                    FileDetails details = new FileDetails();
                    details.setFilePath(path);
                    _alertData.addFile(details);
                }
            }
		}else{
            _alertData = null;
        }

		if(preferences.contains(SETTING_USER_ID)){
			_userId = preferences.getLong(SETTING_USER_ID, 0l);
		}else{
			_userId = null;
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
		if(_alertCheckInterval < 1){
			Log.d(CLASS_NAME, SETTING_ALERT_CHECK_INTERVAL+" is invalid.");
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

		if(_reportAlertGroupIds == null || _reportAlertGroupIds.isEmpty()){
			Log.d(CLASS_NAME, SETTING_REPORT_ALERT_GROUP_IDS+" is invalid.");
			return false;
		}

		if(_range < 1){
			Log.d(CLASS_NAME, SETTING_RANGE+" is invalid.");
			return false;
		}

		if(_reportAlertTypes == null || _reportAlertTypes.isEmpty()){
			Log.d(CLASS_NAME, SETTING_REPORT_ALERT_TYPES+" is invalid.");
			return false;
		}

        if(!_useNetworkProvider && !_useGPS){
            Log.d(CLASS_NAME, "Either "+SETTING_USE_NETWORK_PROVIDER+" or "+SETTING_USE_GPS+" must be enabled.");
            return false;
        }

		if(_userId == null){
			Log.d(CLASS_NAME, SETTING_USER_ID+" is invalid.");
			return false;
		}

		if(_minLocationDistance < 0){
            Log.d(CLASS_NAME, SETTING_MIN_LOCATION_DISTANCE+" is invalid.");
            return false;
        }

        if(_minLocationTime < 0) {
            Log.d(CLASS_NAME, SETTING_MIN_LOCATION_TIME+" is invalid.");
            return false;
        }

		return true;
	}
}
