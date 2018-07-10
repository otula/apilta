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
package tut.pori.shockapplication.uiutils;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import tut.pori.shockapplication.R;

/**
 * The activity that is going to be attached to this dialog <i>must</i> implement {@link SystematicErrorDialog.SystemErrorDialogListener}
 *
 */
public class SystematicErrorDialog extends DialogFragment implements View.OnClickListener, SensorEventListener, LocationListener {
	/** dialog tag */
	public static final String TAG = SystematicErrorDialog.class.toString();
	private static final long MEAN_UPDATE_INTERVAL = 2000; // in ms
	private SystemErrorDialogListener _listener = null;
	private Sensor _accelerometer = null;
	private TextView _accX = null;
	private TextView _accY = null;
	private TextView _accZ = null;
	private TextView _accXYZ = null;
	private TextView _accXYZMean = null;
	private TextView _locationDelayGPS = null;
	private TextView _locationDelayNetworkProvider = null;
	private LocationManager _locationManager = null;
	private SensorManager _sensorManager = null;
	private double _meanSystematicError = 0;
	private double _meanSystematicErrorSum = 0;
	private int _meanSystematicErrorCount = 0;
	private HandlerThread _handlerThread = null;
	private Handler _handler = null;
	private Runnable _meanUpdater = null;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		stopSensors();
		_listener = (SystemErrorDialogListener) context;

		_sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		_accelerometer = _sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		if(_accelerometer == null){
			Log.w(TAG, "No linear accelerometer available, attempting to use accelerometer.");
			_accelerometer = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			if(_accelerometer == null){
				Log.w(TAG, "No accelerometer available.");
			}
		}

		_locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}

	@Override
	public void onResume() {
		super.onResume();
		startSensors();
	}

	@Override
	public void onPause() {
		stopSensors();
		super.onPause();
	}

	/**
	 *
	 */
	private void startSensors(){
		if(_sensorManager != null && _accelerometer != null){
			if(!_sensorManager.registerListener(this, _accelerometer, SensorManager.SENSOR_DELAY_NORMAL)){
				Log.w(TAG, "Failed to refister accelerometer listener.");
			}
		}
		if(_locationManager != null){
			try {
				_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
				_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
			} catch (SecurityException ex) {
				Log.e(TAG, "Permission denied for location updates.", ex);
				Toast.makeText(getContext(), R.string.toast_permission_denied, Toast.LENGTH_LONG).show();
			}
		}

		_handler.postDelayed(_meanUpdater, MEAN_UPDATE_INTERVAL);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_handlerThread = new HandlerThread("MeanHanderThread");
		_handlerThread.start();
		_handler = new Handler(_handlerThread.getLooper());
		_meanUpdater = new Runnable() {
			@Override
			public void run() {
				updateMean();
				_handler.postDelayed(this, MEAN_UPDATE_INTERVAL);
			}
		};
	}

	@Override
	public void onDestroy() {
		if(_handlerThread != null){
			_handlerThread.quitSafely();
		}
		super.onDestroy();
	}

	/**
	 *
	 */
	private void stopSensors(){
		if(_sensorManager != null){
			_sensorManager.unregisterListener(this);
		}
		if(_locationManager != null){
			_locationManager.removeUpdates(this);
		}

		if(_handler != null){
			_handler.removeCallbacks(_meanUpdater);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.dialog_systematic_error, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Dialog d = getDialog();
		d.setCancelable(false);
		d.setCanceledOnTouchOutside(false);
		d.setTitle(R.string.dialog_systematic_error_title);

		_accX = view.findViewById(R.id.acc_x);
		_accY = view.findViewById(R.id.acc_y);
		_accZ = view.findViewById(R.id.acc_z);
		_accXYZ = view.findViewById(R.id.acc_xyz);
		_accXYZMean = view.findViewById(R.id.acc_xyz_mean);
		_locationDelayGPS = view.findViewById(R.id.location_delay_gps);
		_locationDelayNetworkProvider = view.findViewById(R.id.location_delay_network_provider);

		view.findViewById(R.id.button_save).setOnClickListener(this);
		view.findViewById(R.id.button_discard).setOnClickListener(this);

		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()){
			case R.id.button_discard:
				stopSensors();
				dismiss();
				break;
			case R.id.button_save:
				stopSensors();
				dismiss();
				_listener.systematicErrorSaved(_meanSystematicError);
				break;
			default:
				Log.w(TAG, "Unhandeled view ignored.");
				break;
		}
	}

	/**
	 *
	 */
	private synchronized void updateMean() {
		_meanSystematicError = _meanSystematicErrorSum / _meanSystematicErrorCount;
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		_accX.setText(String.valueOf(sensorEvent.values[0]));
		_accY.setText(String.valueOf(sensorEvent.values[1]));
		_accZ.setText(String.valueOf(sensorEvent.values[2]));

		double systematicError = Math.sqrt(Math.pow(sensorEvent.values[0],2)+Math.pow(sensorEvent.values[1],2)+Math.pow(sensorEvent.values[2],2));
		_accXYZ.setText(String.valueOf(systematicError));

		synchronized (this) {
			++_meanSystematicErrorCount;
			_meanSystematicErrorSum += systematicError;
			_accXYZMean.setText(String.valueOf(_meanSystematicError));
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {
		Log.d(TAG, "Accuracy changed for sensor: "+sensor.getName()+": "+i);
	}

	@Override
	public void onLocationChanged(Location location) {
		String delay = String.valueOf(System.currentTimeMillis()-location.getTime());
		switch(location.getProvider()){
			case LocationManager.GPS_PROVIDER:
				_locationDelayGPS.setText(delay);
				break;
			case LocationManager.NETWORK_PROVIDER:
				_locationDelayNetworkProvider.setText(delay);
				break;
			default:
				Log.w(TAG, "Ignored unknown provider: "+location.getProvider());
				break;
		}
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
	 */
	public interface SystemErrorDialogListener {
		/**
		 * Called when user has saved a new systematic error value (mean)
		 *
		 * @param value
		 */
		public void systematicErrorSaved(double value);
	} // interface SystemErrorDialogListener
}
