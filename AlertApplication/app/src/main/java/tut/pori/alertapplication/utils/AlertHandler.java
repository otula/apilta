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
package tut.pori.alertapplication.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.LocationSource;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import tut.pori.alertapplication.datatypes.Alert;
import tut.pori.alertapplication.datatypes.FileDetails;
import tut.pori.alertapplication.datatypes.Settings;

import tut.pori.alertapplication.R;

/**
 * tracks alerts around the user's location
 *
 * remember to call {@link #onPause()} to store the correct states.
 */
public class AlertHandler implements Closeable, LocationListener, LocationSource {
    private static final String CLASS_NAME = AlertHandler.class.toString();
   	private static final float BEARING_LIMIT = 60; // how far (in degrees) the bearing to target location can be from the current bearing
	private static final float DEFAULT_ALERT_ON_DISTANCE = 200f; // when is alert shown to user - within this distance (in meters), used when speed is unknown (cannot use time)
	private static final float DEFAULT_ALERT_ON_TIME = 20f; // when is alert shown to the user - the alert will be met within this time (in seconds)
    private Context _context = null;
	private final LinkedList<Alert> _alertCache;
	private Runnable _activeGetAlertTask = null;
	private HTTPClient _client = null;
    private Handler _handler = null;
	private Date _lastAccess = null;
	private Location _lastLocation = null;
	private AlertListener _listener = null;
	private LocationManager _locationManager = null;
	private OnLocationChangedListener _onLocationChangedListener = null;

	/**
	 *
	 * @param context
	 * @param listener
	 * @param settings
	 */
	public AlertHandler(Context context, AlertListener listener, Settings settings){
		_listener = listener;
		_context = context;
		_handler = new Handler();
		_client = new HTTPClient(settings);
		_locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
       _alertCache = new LinkedList<>();
	}

	/**
	 * report new alert to the service
	 *
	 * @param alert
	 */
	public void addAlert(Alert alert) {
        new AddAlertTask().execute(alert);
	}

	@Override
	public void close() {
		stopLocationUpdates();
		_client.close();
	}

	/**
	 * start location updated
	 *
	 * @throws SecurityException if user has denied location permissions
	 */
	public void startLocationUpdates() throws SecurityException {
        Settings settings = _client.getSettings();
        if(settings.isUseGPS()) {
            Log.d(CLASS_NAME, "Starting updates for "+LocationManager.GPS_PROVIDER);
            _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, settings.getMinLocationTime(), settings.getMinLocationDistance(), this);
        }
        if(settings.isUseNetworkProvider()) {
            Log.d(CLASS_NAME, "Starting updates for "+LocationManager.NETWORK_PROVIDER);
            _locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, settings.getMinLocationTime(), settings.getMinLocationDistance(), this);
        }
        if(_activeGetAlertTask != null){
            _handler.removeCallbacks(_activeGetAlertTask);
            _handler.post(_activeGetAlertTask);
        }
	}

    /**
	 * stop location updates
	 *
	 * @throws SecurityException if user has denied location permissions
	 */
	public void stopLocationUpdates() throws SecurityException{
        Log.d(CLASS_NAME, "Stopping all location updates.");
		_locationManager.removeUpdates(this);
		if(_activeGetAlertTask != null){
			_handler.removeCallbacks(_activeGetAlertTask);
		}
	}

	/**
	 *
	 * save the state of this handler to the provided settings, this will also automatically stop the location updates
	 */
	public void onPause() {
		stopLocationUpdates();
        Settings settings = _client.getSettings();
        if(settings != null){
            settings.save(_context);
        }
	}

	/**
	 *
	 * @return currently active settings
	 * @see #setSettings(Settings)
	 */
	public Settings getSettings() {
		return _client.getSettings();
	}

	/**
	 *
	 * @return copy of the alert cache or null if no alerts in the cache
	 */
	public ArrayList<Alert> getAlertCache() {
		synchronized (_alertCache) {
			if(_alertCache.isEmpty()){
				return null;
			}else{
				return new ArrayList<>(_alertCache);
			}
		}
	}

	/**
	 *
	 * @param settings the currently active settings
	 * @see #getSettings()
	 */
	public void setSettings(Settings settings) {
		_client.setSettings(settings);
	}

	@Override
	public void onLocationChanged(Location location) {
		_lastLocation = location;

		HashSet<Alert.AlertType> inRange = new HashSet<>();
		HashSet<Alert.AlertType> ahead = new HashSet<>();

		synchronized (_alertCache) {
			if(!_alertCache.isEmpty()){
				float speed = location.getSpeed();
				float distanceThreshold = (speed < 1 ? DEFAULT_ALERT_ON_DISTANCE : speed * DEFAULT_ALERT_ON_TIME);
				float maxDistance = _client.getSettings().getRange();
				boolean hasBearing = location.hasBearing();
				float maxBearing = 0;
				float minBearing = 0;
				if(hasBearing){ // calculate "bearing cone" for accepted bearing values
					float bearing = location.getBearing();
					maxBearing = bearing + BEARING_LIMIT;
					if(maxBearing > 360){
						maxBearing -= 360;
					}
					minBearing = bearing - BEARING_LIMIT;
					if(minBearing < 0){
						minBearing += 360;
					}
				}

				for(Iterator<Alert> iter = _alertCache.iterator(); iter.hasNext();) {
					Alert alert = iter.next();
					Location aLocation = alert.getLocation();
					float distance = location.distanceTo(aLocation);
					if(distance > maxDistance){ // alert has gone out of the maximum retrieval range
						iter.remove();
						continue;
					}

					Integer range = alert.getRange();
					if((range != null && distance < range) || (distance < distanceThreshold)){
						inRange.add(alert.getAlertType());
					}else if(hasBearing){
						float alertBearing = location.bearingTo(aLocation);
						if(minBearing > alertBearing && alertBearing < maxBearing){ // check that the alert is really ahead
							ahead.add(alert.getAlertType());
						}
					}else{ // if bearing is not known, report all alerts around the user
						ahead.add(alert.getAlertType());
					}
				}
			} // if
		} // synchronized

		Log.d(CLASS_NAME, "Signalling listener for "+inRange.size()+" alerts in range.");
		_listener.alertsInRange(inRange.isEmpty() ? null : inRange);

		Log.d(CLASS_NAME, "Signalling listener for "+ahead.size()+" alerts ahead.");
		_listener.alertsAhead(ahead.isEmpty() ? null : ahead);

		if(_activeGetAlertTask == null){ // start the get alerts task when location is resolved for the first time
			_activeGetAlertTask = new GetAlertsTask();
			_handler.post(_activeGetAlertTask);
		}

		if(_onLocationChangedListener != null){
			_onLocationChangedListener.onLocationChanged(location);
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(CLASS_NAME, "Provider "+provider+" status changed to: "+status);
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(CLASS_NAME, "Provider "+provider+" was enabled.");
	}

	@Override
	public void onProviderDisabled(String provider) {
        Log.d(CLASS_NAME, "Provider "+provider+" was disabled.");
	}

    /**
     *
     * @return last known location or null if none
     */
    public Location getLastLocation(){
        return _lastLocation;
    }

	@Override
	public void activate(OnLocationChangedListener onLocationChangedListener) {
		_onLocationChangedListener = onLocationChangedListener;
		Log.d(CLASS_NAME, "Location changed listener enabled.");
	}

	@Override
	public void deactivate() {
		_onLocationChangedListener = null;
		Log.d(CLASS_NAME, "Location changed listener disabled.");
	}

	/**
	 * this method is <i>not</i> synchronized for the alert cache
	 *
	 * @param alert
	 * @return true if the alert is already in the cache
	 */
	private boolean alertInCache(Alert alert){
		for(Alert a : _alertCache){
			if(a.getAlertId().equals(alert.getAlertId())){
				return true;
			}
		}
		return false;
	}

	/**
	 * listener for alert handler events
	 */
	public interface AlertListener {
		/**
		 *
		 * @param alerts set of alert types currently in range, null if no alerts in range
		 */
		public void alertsInRange(Set<Alert.AlertType> alerts);

		/**
		 * Note: if the user's current heading is not known, this may also report alerts that are around the user in any direction, null if no alerts ahead
		 *
		 * @param alerts set of alerts coming up ahead
		 */
		public void alertsAhead(Set<Alert.AlertType> alerts);
	} // interface AlertListener

    /**
     * asynchronous task for posting new alert to the service
     */
    private class AddAlertTask extends AsyncTask<Alert, Void, String> {

        @Override
        protected String doInBackground(Alert... params) {
            List<FileDetails> details = params[0].getFiles();
            if(details != null){
				Log.d(CLASS_NAME, "Uploading files...");
				for(FileDetails d : details){
					String path = d.getFilePath();
                    FileDetails file = _client.uploadFile(FileUtils.getFile(path));
					if(file == null){
						Log.e(CLASS_NAME, "Failed to upload file: "+path);
						return null;
					}else{
                        d.setGUID(file.getGUID());
                    }
				}

                Log.d(CLASS_NAME, "Uploading files, if any...");
            }

            Log.d(CLASS_NAME, "Adding new alert...");
            String alertId = _client.addAlert(params[0]);
            if(alertId == null){
                Log.w(CLASS_NAME, "Failed to add new alert.");
            }else{
                Log.d(CLASS_NAME, "Added new alert, id: "+alertId);
            }

            if(details != null && !_client.getSettings().isKeepFiles()){
                Log.d(CLASS_NAME, "Deleting uploaded files...");
                for(FileDetails file : details){
                    FileUtils.deleteFile(file.getFilePath());
                }
            }

            return alertId;
        }

		@Override
		protected void onPostExecute(String alertId) {
			if(org.apache.commons.lang3.StringUtils.isBlank(alertId)){
				Toast.makeText(_context, R.string.error_add_alert_failed, Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(_context, R.string.notification_alert_added, Toast.LENGTH_SHORT).show();
			}
		}
	} // class AddAlertTask

    /**
     * runnable used by the handler for (re)scheduling alert retrieval
     */
    private class GetAlertsTask implements Runnable {

		@Override
		public void run() {
            new GetAlertsATask().execute();
		}
	} // class GetAlertsTask

	/**
	 * asynchronous task for retrieving alerts
	 */
	private class GetAlertsATask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            int interval = _client.getSettings().getAlertCheckInterval();
            Log.d(CLASS_NAME, "Scheduling new alert retrieval in "+interval+" seconds...");
            _handler.postDelayed(_activeGetAlertTask, interval*1000);
        }

        @Override
		protected Void doInBackground(Void... params) {
            Log.d(CLASS_NAME, "Retrieving new alerts...");
            Settings settings = _client.getSettings();
            Date lastAccess = (settings.isUseTimeFilter() ? _lastAccess : null); // only use time filter if enabled in settings
			List<Alert> alerts = _client.getAlerts(_lastLocation, lastAccess);
			_lastAccess = new Date(); // update the last access date;
			if(alerts == null){
				Log.d(CLASS_NAME, "No new alerts received for user's current location.");
			}else{
				Log.d(CLASS_NAME, "Received "+alerts.size()+" new alerts for user's current location.");
				Long userId = settings.getUserId();
				synchronized (_alertCache) {
					for(Alert alert : alerts){
						if((!settings.isHideUsersAlerts() || !alert.getUserId().getUserId().equals(userId)) && !alertInCache(alert)){ // ignore user's own alerts, and alerts that are already in the cache
							_alertCache.add(alert);
						} // if
					} // for
				} // synchornized
			} // else
			return null;
		}
	} // class GetAlertsATask
}
