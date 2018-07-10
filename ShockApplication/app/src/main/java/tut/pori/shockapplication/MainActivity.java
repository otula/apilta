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

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

import tut.pori.shockapplication.datatypes.Definitions;
import tut.pori.shockapplication.datatypes.Settings;
import tut.pori.shockapplication.datatypes.ShockMeasurement;
import tut.pori.shockapplication.uiutils.SystematicErrorDialog;
import tut.pori.shockapplication.utils.SQLiteHandler;
import tut.pori.shockapplication.utils.SensorHandler;

/**
 *
 */
public class MainActivity extends Activity implements View.OnClickListener, SystematicErrorDialog.SystemErrorDialogListener {
	private static final int REQUEST_PERMISSIONS = 1;
	private static final String TAG = MainActivity.class.toString();
	private long _listenStartedTimestamp = 0;
	private TextView _totalCount = null;
	private TextView _log = null;
	private SQLiteHandler _sqLiteHandler = null;
    private Settings _settings = null;
    private EditText _username = null;
    private EditText _password = null;
    private Switch _publicData = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		_sqLiteHandler = new SQLiteHandler(this);
		_totalCount = findViewById(R.id.count_total);
		_log = findViewById(R.id.log);

		findViewById(R.id.button_send).setOnClickListener(this);
		findViewById(R.id.button_print_log).setOnClickListener(this);
		findViewById(R.id.button_start).setOnClickListener(this);
		findViewById(R.id.button_stop).setOnClickListener(this);
		findViewById(R.id.button_grant_permissions).setOnClickListener(this);
        findViewById(R.id.button_save_settings).setOnClickListener(this);
		findViewById(R.id.button_show_systematic_error).setOnClickListener(this);
		_publicData = findViewById(R.id.switch_public_data);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        _settings = new Settings(this);
		/*********** set hardcoded settings **********/
        _settings.setMinMeasurements(10);
        _settings.setServiceURI("http://otula.pori.tut.fi:44446/ApiltaService/rest/");
        _settings.setMinMeasurementLevel(0);
        _settings.setMinMeasurements(10);
		_settings.setMaxMeasurements(1800);
		_settings.setMinimumSpeed(1);
        _settings.save(this);
        /*********** set hardcoded settings **********/

        _username = findViewById(R.id.edit_username);
        _username.setText(_settings.getUsername());
        _password = findViewById(R.id.edit_password);
        _password.setText(_settings.getPassword());
        _publicData.setChecked(Definitions.DATA_VISIBILITY_PUBLIC.equals(_settings.getDataVisibility()));
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch(requestCode){
			case REQUEST_PERMISSIONS:
				if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
					Toast.makeText(this, R.string.toast_permission_granted, Toast.LENGTH_LONG).show();
				}else{
					Log.d(TAG, "Permission not granted.");
				}
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
				break;
		}
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
			case R.id.button_start:
				Intent i = new Intent(this, MeasurementService.class);
				i.setAction(MeasurementService.ACTION_START);
				startService(i);
				_listenStartedTimestamp = System.currentTimeMillis();
				break;
			case R.id.button_stop:
				_totalCount.setText(String.valueOf(_sqLiteHandler.countMeasurements(null))+", not sent: "+String.valueOf(_sqLiteHandler.countMeasurements(false)));
				i = new Intent(this, MeasurementService.class);
				i.setAction(MeasurementService.ACTION_STOP);
				startService(i);
				break;
			case R.id.button_send:
				i = new Intent(this, MeasurementService.class);
				i.setAction(MeasurementService.ACTION_SEND_MEASUREMENTS);
				startService(i);
				Toast.makeText(this, R.string.toast_sending_measurements, Toast.LENGTH_LONG).show();
				break;
			case R.id.button_print_log:
				List<ShockMeasurement> measurements = _sqLiteHandler.getMeasurements(_listenStartedTimestamp, System.currentTimeMillis());
				if(measurements == null){
					Toast.makeText(this, R.string.toast_no_measurements, Toast.LENGTH_LONG).show();
				}else{
					printLog(measurements);
				}
				break;
			case R.id.button_grant_permissions:
				requestPermissions(new String[]{SensorHandler.PERMISSION_LOCATION}, REQUEST_PERMISSIONS);
				break;
            case R.id.button_save_settings:
                saveSettings();
                break;
			case R.id.button_show_systematic_error:
				showSystematicError();
				break;
			default:
				Log.w(TAG, "Unhandeled onclick.");
				break;
		}
	}

	/**
	 *
	 */
	private void showSystematicError() {
		DialogFragment df = new SystematicErrorDialog();
		df.show(getFragmentManager(), SystematicErrorDialog.TAG);
	}

	/**
	 *
	 * @param measurements
	 */
	private void printLog(List<ShockMeasurement> measurements) {
		StringBuilder sb = new StringBuilder();
		for(ShockMeasurement m : measurements){
			sb.append("ShockMeasurement id: ");
			sb.append(m.getMeasurementId());
			sb.append(", lat: ");
			sb.append(m.getLatitude());
			sb.append(", lon: ");
			sb.append(m.getLongitude());
			sb.append(", speed: ");
			sb.append(m.getSpeed());
			sb.append(", time: ");
			sb.append(new Date(m.getTimestamp()));

			ShockMeasurement.AccelerometerData aData = m.getAccelerometerData();
			if(aData != null){
				sb.append("\nx_acc: ");
				sb.append(aData.getxAcceleration());
				sb.append(", y_acc: ");
				sb.append(aData.getyAcceleration());
				sb.append(", z_acc: ");
				sb.append(aData.getzAcceleration());
				sb.append(", time: ");
				sb.append(new Date(aData.getTimestamp()));
			}else{
				sb.append("\nNo accelerometer data.");
			}

			ShockMeasurement.GyroData gData = m.getGyroData();
			if(gData != null){
				sb.append("\nx_speed: ");
				sb.append(gData.getxSpeed());
				sb.append(", y_speed: ");
				sb.append(gData.getySpeed());
				sb.append(", z_speed: ");
				sb.append(gData.getzSpeed());
				sb.append(", time: ");
				sb.append(new Date(gData.getTimestamp()));
			}else{
				sb.append("\nNo gyroscope data.");
			}

			ShockMeasurement.RotationData rData = m.getRotationData();
			if(rData != null){
				sb.append("\nx_sin: ");
				sb.append(rData.getxSin());
				sb.append(", y_sin: ");
				sb.append(rData.getySin());
				sb.append(", z_sin: ");
				sb.append(rData.getzSin());
				sb.append(", cos: "+rData.getCos());
				sb.append(", accuracy: ");
				sb.append(rData.getAccuracy());
				sb.append(", time: "+new Date(rData.getTimestamp()));
			}else{
				sb.append("No rotation vectors.");
			}
			sb.append('\n');
		}

		_log.setText(sb);
	}

	@Override
	protected void onDestroy() {
		if(_sqLiteHandler != null){
			_sqLiteHandler.close();
		}
		super.onDestroy();
	}

	@Override
	public void systematicErrorSaved(double value) {
		_settings.setSystematicError((float) value);
		saveSettings();
	}

	/**
	 *
	 */
	private void saveSettings() {
		String username = _username.getText().toString();
		String password = _password.getText().toString();
		if(StringUtils.isBlank(username) || StringUtils.isBlank(password)){
			Toast.makeText(this, R.string.toast_invalid_credentials, Toast.LENGTH_LONG).show();
		}else{
			_settings.setDataVisibility((_publicData.isChecked() ? Definitions.DATA_VISIBILITY_PUBLIC : Definitions.DATA_VISIBILITY_PRIVATE));
			_settings.setUsername(username);
			_settings.setPassword(password);
			_settings.save(this);
			Intent i = new Intent(this, MeasurementService.class);
			i.setAction(MeasurementService.ACTION_SETTINGS_UPDATED);
			startService(i);
			Toast.makeText(this, R.string.toast_settings_saved, Toast.LENGTH_LONG).show();
		}
	}
}
