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
package tut.pori.alertapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import tut.pori.alertapplication.datatypes.Alert;
import tut.pori.alertapplication.datatypes.FileDetails;
import tut.pori.alertapplication.datatypes.Settings;
import tut.pori.alertapplication.uiutils.AlertsAheadAdapter;
import tut.pori.alertapplication.uiutils.AlertsInRangeAdapter;
import tut.pori.alertapplication.utils.AlertHandler;
import tut.pori.alertapplication.utils.FileUtils;
import tut.pori.alertapplication.utils.SoundHandler;

/**
 * the main activity for posting new alerts
 */
public class ReportActivity extends Activity implements AlertHandler.AlertListener, View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private static final String CLASS_NAME = ReportActivity.class.toString();
    private static final String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int REQUEST_PERMISSIONS = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;
	private AlertHandler _alertHandler = null;
	private AlertsAheadAdapter _alertsAheadAdapter = null;
	private AlertsInRangeAdapter _alertsInRangeAdapter = null;
	private Button _buttonReportPhoto = null;
    private int _currentAlertIndex = 0;
	private ImageView _imageAlert = null;
	private LinearLayout _layoutMain = null;
	private LinearLayout _layoutReport = null;
	private GoogleMap _map = null;
	private MapFragment _mapFragment = null;
	private Alert.AlertType[] _reportAlertTypes = null;
	private SoundHandler _soundHandler = null;
    private TextView _textReport = null;
    private TextView _textCounter = null;
    private AlertCountDownTimer _timer = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_soundHandler = new SoundHandler(this);
		_alertHandler = new AlertHandler(this, this, null); // settings will be updated in onResume()

		setContentView(R.layout.activity_report);
		_mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.report_map);
		_mapFragment.getMapAsync(this);
		_textReport = (TextView) findViewById(R.id.report_text);
		_textCounter = (TextView) findViewById(R.id.report_counter);
		_imageAlert = (ImageView) findViewById(R.id.report_image_alert);
		_buttonReportPhoto = (Button) findViewById(R.id.report_button_photo);
		_buttonReportPhoto.setOnClickListener(this);
		_layoutReport = (LinearLayout) findViewById(R.id.report_layout_alerts);
        _layoutReport.setOnClickListener(this);
		_layoutMain = (LinearLayout) findViewById(R.id.report_layout_main);
		_layoutMain.setOnClickListener(this);

		_alertsAheadAdapter = new AlertsAheadAdapter(this, (LinearLayout) findViewById(R.id.report_alerts_ahead));
		_alertsInRangeAdapter = new AlertsInRangeAdapter(this, (LinearLayout) findViewById(R.id.report_alerts_in_range), _soundHandler);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
			case R.id.menu_map:
				showMapLayout();
				return true;
            case R.id.menu_main:
                showMainLayout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
	protected void onPause() {
		stopLocationUpdates();
		_alertHandler.onPause();
		super.onPause();
	}

	/**
	 * stop location updates
	 *
	 * this will also remove visible marker and cooldown task
	 */
	private void stopLocationUpdates(){
		try {
			_alertHandler.stopLocationUpdates();
		} catch (SecurityException ex) { // there isn't much we can do if this happens
			Log.e(CLASS_NAME, "Failed to stop location updates for alert handler.", ex);
		}
		if(_map != null && _map.isMyLocationEnabled()){
			try {
				_map.setMyLocationEnabled(false);
			} catch (SecurityException ex) { // there isn't much we can do if this happens
				Log.e(CLASS_NAME, "Failed to stop location updates for map layer.", ex);
			}
		}

		stopTasks();
	}

	/**
	 * stop active tasks if any
	 */
	private void stopTasks() {
		if(_timer != null){
            _timer.cancel();
        }
	}

	/**
	 * ask user permissions and attempt to start location updates
	 */
	private void startLocationUpdates(){
		if(PackageManager.PERMISSION_GRANTED == checkSelfPermission(PERMISSIONS_LOCATION[0])){
			Log.d(CLASS_NAME, "Starting location updates...");
			_alertHandler.startLocationUpdates();
		}else{
			Log.d(CLASS_NAME, "Requesting permissions from user...");
			requestPermissions(PERMISSIONS_LOCATION, REQUEST_PERMISSIONS);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Settings settings = new Settings(this);
		if(!settings.isValid()){
			startActivity(new Intent(this, SettingsActivity.class));
			return; // return to prevent initialization of layouts with invalid values
		}

		_alertHandler.setSettings(settings);

		EnumSet<Alert.AlertType> types = settings.getReportAlertTypes();
		_reportAlertTypes = new Alert.AlertType[types.size()];
		types.toArray(_reportAlertTypes);

		if(settings.isAllowPhotos()){
			_buttonReportPhoto.setVisibility(View.VISIBLE);
		}else{
			_buttonReportPhoto.setVisibility(View.GONE);
		}

		_soundHandler.setEnableTTS(settings.isUseTTS());

		showMainLayout();

        startLocationUpdates();
	}

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode){
            case REQUEST_PERMISSIONS:
                startLocationUpdates();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

	@Override
	protected void onDestroy() {
		_alertHandler.close();
		_alertHandler = null;
		_soundHandler.close();
		_soundHandler = null;
		super.onDestroy();
	}

	/**
	 *
	 */
	private void showMapLayout() {
		stopTasks(); // stop all previous tasks

		if(_map == null){
			Log.w(CLASS_NAME, "Show called before map is ready.");
			return;
		}

		_map.clear();
		List<Alert> alerts = _alertHandler.getAlertCache();
		if(alerts != null){
			_soundHandler.play(SoundHandler.EventType.NEW_ALERTS_IN_RANGE);

			for(Alert alert : alerts) {
				Location location = alert.getLocation();
				MarkerOptions options = new MarkerOptions();
				LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
				options.position(latlng);
				String title = alert.getDescription();
				if(!StringUtils.isBlank(title)){
					options.title(title);
				}

				Marker marker = _map.addMarker(options);
				Alert.AlertType type = alert.getAlertType();
				marker.setIcon(BitmapDescriptorFactory.fromResource(type.getIconImageResource()));

				Integer range = alert.getRange();
				if(range != null){
					int color = getColor(type.getColorResource());
					Circle circle = _map.addCircle(new CircleOptions()
							.center(latlng)
							.radius(range)
							.strokeColor(color)
							.fillColor(color));
					marker.setTag(circle);
				}
			}
		}


		UiSettings uiSettings = _map.getUiSettings();
		if(_alertHandler.getSettings().isAllowMapGestures()){
			uiSettings.setAllGesturesEnabled(true);
			_map.setOnMapClickListener(null);
		}else{
			uiSettings.setAllGesturesEnabled(false);
			_map.setOnMapClickListener(this);
		}
		_layoutReport.setVisibility(View.GONE);
		_layoutMain.setVisibility(View.GONE);
		_mapFragment.getView().setVisibility(View.VISIBLE);

		try {
			_map.setMyLocationEnabled(true); // attempt to enable current location updates for the map
		} catch (SecurityException ex){ // should not happen here, simply ignore if it happens
			Log.e(CLASS_NAME, "Failed to enable my location layer for the map.", ex);
		}
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.report_layout_alerts:
                reportClicked();
                break;
			case R.id.report_button_photo:
				takePhotoClicked();
				break;
			case R.id.report_layout_main: // essentially the same as clicking the report view
				_currentAlertIndex = -1; // make sure processing report clicked moves to the first report of the list
				reportClicked();
				break;
            default:
                Log.w(CLASS_NAME, "Ignored unknown view.");
                break;
        }
    }

	/**
	 *
	 */
	private void takePhotoClicked() {
		stopTasks(); // stop all timers
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (intent.resolveActivity(getPackageManager()) != null) { // Ensure that there's a camera activity to handle the intent
			Pair<String, Uri> paths = FileUtils.createFile(this);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, paths.getRight());
			Settings settings = _alertHandler.getSettings(); // save to settings in case this activity gets reset/re-created during camera activity
			Alert data = settings.getAlertData();
			FileDetails details = new FileDetails();
			details.setFilePath(paths.getLeft());
			data.addFile(details); // update the data with file details
			settings.setAlertData(data); // make sure the object is saved
			startActivityForResult(intent, REQUEST_TAKE_PHOTO);
		}else{
			Log.w(CLASS_NAME, "Failed to resolve intent for camera activity.");
		}
		showMainLayout(); // go to no active view
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            sendAddAlert();
        }else {
            Log.d(CLASS_NAME, "Photo capture failed/aborted, with code: "+resultCode);
        }
    }

	/**
	 *
	 */
	private void showMainLayout() {
        stopTasks(); // stop all previous tasks
		_currentAlertIndex = -1;
		_imageAlert.setImageResource(R.drawable.no_active);
		_textReport.setText(R.string.report_no_active);
		_textCounter.setVisibility(View.INVISIBLE);
		_soundHandler.play(SoundHandler.EventType.MAIN_VIEW);
		_layoutReport.setVisibility(View.GONE);
		_mapFragment.getView().setVisibility(View.GONE);
		_layoutMain.setVisibility(View.VISIBLE);
	}

    /**
     *
     */
    private void reportClicked() {
        stopTasks();
        ++_currentAlertIndex;
        if(_currentAlertIndex >= _reportAlertTypes.length){ // no active alert
            showMainLayout();
        }else{
            Location location = _alertHandler.getLastLocation();
            if(location == null){
                Toast.makeText(this, R.string.report_error_location, Toast.LENGTH_SHORT).show();
                showMainLayout();
                return;
            }

            Alert.AlertType type = _reportAlertTypes[_currentAlertIndex];
			_soundHandler.play(type);
            _imageAlert.setImageResource(type.getImageResource());
            _textReport.setText(type.getStringResource());

			_layoutReport.setVisibility(View.VISIBLE);
			_mapFragment.getView().setVisibility(View.GONE);
			_layoutMain.setVisibility(View.GONE);

            _timer = new AlertCountDownTimer(location, type);
            _timer.startTimer();
        }
    }

	@Override
	public void onMapReady(GoogleMap googleMap) {
		Log.d(CLASS_NAME, "Map initialized...");
		_map = googleMap;
		_map.setLocationSource(_alertHandler);
	}

	/**
	 * send alert to server
	 */
	private void sendAddAlert() {
        Settings settings = _alertHandler.getSettings();
		Alert alert = settings.getAlertData();
		Toast.makeText(ReportActivity.this, R.string.report_adding_alert, Toast.LENGTH_SHORT).show();
		_alertHandler.addAlert(alert);
        settings.setAlertData(null);
	}

	/**
	 * (locally) save the currently active alert data
	 *
	 * @param location
	 * @param type
	 */
	private void saveAlertData(Location location, Alert.AlertType type) {
        Alert alert = new Alert();
        alert.setLocation(location);
        alert.setAlertType(type);
        alert.setCreated(new Date());
		_alertHandler.getSettings().setAlertData(alert);
	}

	@Override
	public void alertsInRange(Set<Alert.AlertType> alerts) {
		_alertsInRangeAdapter.setAlerts(alerts);
	}

	@Override
	public void alertsAhead(Set<Alert.AlertType> alerts) {
		_alertsAheadAdapter.setAlerts(alerts);
	}

	@Override
	public void onMapClick(LatLng latLng) {
		showMainLayout(); // ignore click, go to main view (close map)
	}

	/**
     * updates the counter and posting of an alert
     */
    private class AlertCountDownTimer extends CountDownTimer {
        private static final int COUNTDOWN_DURATION = 10000; // in ms
        private static final int COUNTDOWN_INTERVAL = 1000; // in ms

        /**
         *
         * @param location
         */
        public AlertCountDownTimer(Location location, Alert.AlertType type){
            super(COUNTDOWN_DURATION, COUNTDOWN_INTERVAL);
            saveAlertData(location, type);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            _textCounter.setText(String.valueOf(millisUntilFinished/1000));
        }

        @Override
        public void onFinish() {
            sendAddAlert();
			_currentAlertIndex = _reportAlertTypes.length; // set to max length to go to "no active alert"
            reportClicked(); // go to "no active"
        }

        /**
         * start time and set the timer view visible
         */
        public void startTimer(){
            _textCounter.setText(String.valueOf(COUNTDOWN_DURATION/1000));
            _textCounter.setVisibility(View.VISIBLE);
            start();
        }
    } // class AlertCountDownTimer
}
