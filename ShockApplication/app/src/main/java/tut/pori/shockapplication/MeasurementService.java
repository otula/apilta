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
package tut.pori.shockapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import tut.pori.shockapplication.datatypes.Settings;
import tut.pori.shockapplication.datatypes.ShockMeasurement;
import tut.pori.shockapplication.utils.ConnectionHandler;
import tut.pori.shockapplication.utils.SQLiteHandler;
import tut.pori.shockapplication.utils.SensorHandler;


/**
 * measurement foreground service
 */
public class MeasurementService extends Service {
	/** intent action for starting the service */
	public static final String ACTION_START = "tut.pori.shockapplication.START_MEASUREMENTS";
	/** intent action for stopping the service */
	public static final String ACTION_STOP = "tut.pori.shockapplication.STOP_MEASUREMENTS";
	/** intent action for sending measurements immediately */
	public static final String ACTION_SEND_MEASUREMENTS = "tut.pori.shockapplication.SEND_MEASUREMENTS";
    /** intent action notifying the service about updated settings */
    public static final String ACTION_SETTINGS_UPDATED = "tut.pori.shockapplication.SETTINGS_UPDATED";
	private static final String TAG = MeasurementService.class.toString();
	private static final String NOTIFICATION_CHANNEL_ID = "tut.pori.shockapplication.NOTIFICATION_CHANNEL";
	private static final String NOTIFICATION_CHANNEL_NAME = NOTIFICATION_CHANNEL_ID;
	private static final int NOTIFICATION_ID = 100;
	private static final String NOTIFICATION_TITLE = "Measurement Service";
	private static final String NOTIFICATION_TEXT_MEASURING = "Measuring...";
	private static final String NOTIFICATION_TEXT_PERMISSION_DENIED = "Permission denied, you must allow location permissions.";
	private static final String NOTIFICATION_TEXT_SENDING = "Sending measurements...";
    private static final String NOTIFICATION_TEXT_MEASUREMENTS_SENT = "Measurements sent.";
    private static final String NOTIFICATION_TEXT_NO_MEASUREMENTS_SENT = "No measurements were sent.";
	private static final long UPDATE_NOTIFICATION_INTERVAL = 5000; // how often the notification text is updated, in ms
	private static final long SEND_MEASUREMENTS_INTERVAL = 1800000; // how often the measurements are sent to the server, in ms
	private ConnectionHandler _connectionHandler = null;
	private Settings _settings = null;
	private SensorHandler _sensorHandler = null;
	private SQLiteHandler _sqLiteHandler = null;
	private SendMeasurementTask _sendInProgress = null;
	private Notification.Builder _notificationBuilder = null;
	private Handler _handler = null;
	private Runnable _countUpdater = null;
	private Runnable _measurementSender = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
    	if(intent == null){
			Log.w(TAG, "Received null intent.");
			return super.onStartCommand(intent, flags, startId);
		}
    	String action = intent.getAction();
        Log.d(TAG, "Received action :"+action);
		if(ACTION_START.equals(action)){
			_notificationBuilder.setContentText(NOTIFICATION_TEXT_MEASURING);
			_notificationBuilder.setOngoing(true);
			startForeground(NOTIFICATION_ID, _notificationBuilder.build());
			startMeasurements();
		}else if(ACTION_STOP.equals(action)){
			stopMeasurements();
			stopForeground(true);
		}else if(ACTION_SEND_MEASUREMENTS.equals(action)) {
            sendMeasurements();
        }else if(ACTION_SETTINGS_UPDATED.equals(action)) {
		    _settings.load(this);
		}else{
			throw new IllegalArgumentException("Invalid action: "+action);
		}

		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 *
	 */
	private void sendMeasurements() {
		if(_sendInProgress != null){
			Log.d(TAG, "Send already in progress.");
			return;
		}

		_sendInProgress = new SendMeasurementTask();
		_sendInProgress.execute();

		_notificationBuilder.setContentText(NOTIFICATION_TEXT_SENDING);
		_notificationBuilder.setOngoing(true);
		startForeground(NOTIFICATION_ID, _notificationBuilder.build());
	}

	/**
	 *
	 */
	private void startMeasurements() {
		if(_sensorHandler.startSensors()){
			startHandlerCallbacks();
			Log.d(TAG, "Measurements started.");
		}else{
			Log.w(TAG, "Failed to start sensors, attempting to request permissions.");

			_notificationBuilder.setContentText(NOTIFICATION_TEXT_PERMISSION_DENIED);
			_notificationBuilder.setOngoing(false);
			startForeground(NOTIFICATION_ID, _notificationBuilder.build());
		}
	}

	/**
	 *
	 */
	private void stopHandlerCallbacks() {
		_handler.removeCallbacks(_countUpdater);
		_handler.removeCallbacks(_measurementSender);
	}

	/**
	 *
	 */
	private void startHandlerCallbacks() {
		stopHandlerCallbacks(); // remove existing callbacks
		_handler.postDelayed(_countUpdater, UPDATE_NOTIFICATION_INTERVAL);
		_handler.postDelayed(_measurementSender, 2000); // wait a couple of seconds
	}

	/**
	 *
	 */
	private void stopMeasurements() {
		stopHandlerCallbacks();
		_sensorHandler.stopSensors();
		Log.d(TAG, "Measurements stopped.");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O && mgr.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
			mgr.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT));
		}

		_settings = new Settings(this);
		_sqLiteHandler = new SQLiteHandler(this);
		_sensorHandler = new SensorHandler(this, _sqLiteHandler, _settings);
		_sensorHandler.initialize();
		_connectionHandler = new ConnectionHandler(_settings);

		_notificationBuilder = new Notification.Builder(this);
		_notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground);
		_notificationBuilder.setContentTitle(NOTIFICATION_TITLE);
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
			_notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
		}
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		_notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, intent, 0));

		_handler = new Handler(getMainLooper());
		_countUpdater = new Runnable() {
			@Override
			public void run() {
			    if(_sendInProgress == null) { // only override the text if "sending measurements" notification is not active
                    _notificationBuilder.setContentText(NOTIFICATION_TEXT_MEASURING + " " + _sensorHandler.getMeasurementCount());
                }
				_notificationBuilder.setOngoing(true);
				startForeground(NOTIFICATION_ID, _notificationBuilder.build());
				_handler.postDelayed(this, UPDATE_NOTIFICATION_INTERVAL);
			}
		};
		_measurementSender = new Runnable() {
			@Override
			public void run() {
				sendMeasurements();
			}
		};

		Log.d(TAG, "Created.");
	}

	@Override
	public void onDestroy() {
		stopMeasurements();

		if(_sqLiteHandler != null){
			_sqLiteHandler.close();
		}
		if(_connectionHandler != null){
			_connectionHandler.close();
		}

		super.onDestroy();
	}

	/**
	 *
	 */
	private class SendMeasurementTask extends AsyncTask<Void, Void, Boolean> {

		/**
		 *
		 * @param measurements
		 */
		private void processLevels(List<ShockMeasurement> measurements) {
			float accLow = 10000; // any high enough value is OK
			float accHigh = 0;

			Float systematicError = _settings.getSystematicError();
			float systematicErrorValue = (systematicError == null ? 0 : systematicError);
			for(ShockMeasurement m : measurements){
				ShockMeasurement.AccelerometerData aData = m.getAccelerometerData();
				float rVector = Math.abs((float) Math.sqrt(Math.pow(aData.getxAcceleration(),2)+Math.pow(aData.getyAcceleration(),2)+Math.pow(aData.getzAcceleration(),2))-systematicErrorValue);
				aData.setxyzAcceleration(rVector);
				aData.setSystematicError(systematicError);
				if(accHigh < rVector){
					accHigh = rVector;
				}else if(accLow > rVector){
					accLow = rVector;
				}
			}

			float step1 = accLow+(accHigh-accLow)/5;
			float step2 = step1*2;
			float step3 = step2+step1;
			float step4 = step3+step1;

			int minLevel = _settings.getMinMeasurementLevel();
			for(Iterator<ShockMeasurement> iter = measurements.iterator(); iter.hasNext();) {
			    ShockMeasurement m = iter.next();
				float rVector = m.getAccelerometerData().getxyzAcceleration();
				int level = 4;
				if(rVector < step1){
					level = 0;
				}else if(rVector < step2){
					level = 1;
				}else if(rVector < step3){
					level = 2;
				}else if(rVector < step4){
					level = 3;
				} // else level = 4

                if(level < minLevel){ // remove measurements that are below the set level
				    iter.remove();
                }else {
                    m.setLevel(level);
                }
			}
		}

		@Override
		protected Boolean doInBackground(Void args[]) {
			boolean retval = true;
			do {
				List<ShockMeasurement> list = _sqLiteHandler.getUnsentMeasurements(_settings.getMaxMeasurements());
				if(list == null){
					Log.d(TAG, "No measurements to send.");
					return Boolean.FALSE;
				}

				if(list.size() < _settings.getMinMeasurements()){
					Log.d(TAG, "Minimum measurement amount not reached.");
					return Boolean.FALSE;
				}
				processLevels(list);
				retval = _connectionHandler.send(list);
				if(retval){
					int count = list.size(); // get new size, the list might have gotten shorter if {@link processLevels(List)} removed items
					ArrayList<String> ids = new ArrayList<>(count);
					for(ShockMeasurement m : list) {
						ids.add(m.getMeasurementId());
					}
					_sqLiteHandler.setSent(ids);
					Log.d(TAG, "Measurements sent: "+count);
				}
			} while(retval);
			return retval;
		}

		@Override
		protected void onPostExecute(Boolean aBoolean) {
			if(aBoolean){
				Log.d(TAG, "Measurements sent.");
                _notificationBuilder.setContentText(NOTIFICATION_TEXT_MEASUREMENTS_SENT);
			}else{
				Log.w(TAG, "Failed to send measurements.");
                _notificationBuilder.setContentText(NOTIFICATION_TEXT_NO_MEASUREMENTS_SENT);
			}
            _notificationBuilder.setOngoing(false);
            startForeground(NOTIFICATION_ID, _notificationBuilder.build());
			_sendInProgress = null;

			Log.d(TAG, "Scheduling next measurements send in "+SEND_MEASUREMENTS_INTERVAL/1000+" seconds.");
			_handler.postDelayed(_measurementSender, SEND_MEASUREMENTS_INTERVAL);
		}
	} // class SendMeasurementTask
}
