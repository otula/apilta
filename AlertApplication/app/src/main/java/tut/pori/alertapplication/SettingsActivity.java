package tut.pori.alertapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import tut.pori.alertapplication.datatypes.Alert;
import tut.pori.alertapplication.datatypes.Settings;
import tut.pori.alertapplication.datatypes.UserIdentity;
import tut.pori.alertapplication.uiutils.DialogUtils;
import tut.pori.alertapplication.utils.HTTPClient;

public class SettingsActivity extends Activity implements View.OnClickListener, DialogUtils.DialogListener {
    private static final String CLASS_NAME = SettingsActivity.class.toString();
    private static final char SEPARATOR_ID = ',';
    private static final int TAG_CONFIRM_SAVE = 1;
    private EditText _inputAlertCheckInterval = null;
    private EditText _inputListenGroupIds = null;
    private EditText _inputMinLocationDistance = null;
    private EditText _inputMinLocationTime = null;
    private EditText _inputPassword = null;
    private EditText _inputRange = null;
    private EditText _inputReportGroupIds = null;
    private EditText _inputServiceURI = null;
    private EditText _inputUsername = null;
    private ProgressDialog _progressDialog = null;
    private Settings _settings = null;
	private Switch _switchAllowMapGestures = null;
	private Switch _switchAllowPhotos = null;
    private Switch _switchHideUsersAlerts = null;
    private Switch _switchKeepFiles = null;
    private Switch _switchUseGPS = null;
    private Switch _switchUseNetworkProvider = null;
    private Switch _switchUseTimeFilter = null;
    private Switch _switchUseTTS = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
        findViewById(R.id.settings_button_save).setOnClickListener(this);
        _inputAlertCheckInterval = (EditText) findViewById(R.id.settings_input_alert_check_interval);
        _inputServiceURI = (EditText) findViewById(R.id.settings_input_alert_service_uri);
        _inputRange = (EditText) findViewById(R.id.settings_input_range);
        _inputUsername = (EditText) findViewById(R.id.settings_input_username);
        _inputPassword = (EditText) findViewById(R.id.settings_input_password);
        _inputListenGroupIds = (EditText) findViewById(R.id.settings_input_listen_group_ids);
        _inputReportGroupIds = (EditText) findViewById(R.id.settings_input_report_group_ids);
        _inputMinLocationDistance = (EditText) findViewById(R.id.settings_input_min_location_distance);
        _inputMinLocationTime = (EditText) findViewById(R.id.settings_input_min_location_time);
        _switchUseGPS = (Switch) findViewById(R.id.settings_switch_use_gps);
        _switchUseNetworkProvider = (Switch) findViewById(R.id.settings_switch_use_network_provider);
        _switchAllowPhotos = (Switch) findViewById(R.id.settings_switch_allow_photos);
        _switchKeepFiles = (Switch) findViewById(R.id.settings_switch_keep_files);
		_switchHideUsersAlerts = (Switch) findViewById(R.id.settings_switch_hide_users_alerts);
		_switchAllowMapGestures = (Switch) findViewById(R.id.settings_switch_allow_map_gestures);
		_switchUseTTS = (Switch) findViewById(R.id.settings_switch_use_tts);
        _switchUseTimeFilter = (Switch) findViewById(R.id.settings_switch_use_time_filter);
	}

	@Override
	protected void onResume() {
		_settings = new Settings(this);

        _inputAlertCheckInterval.setText(String.valueOf(_settings.getAlertCheckInterval()));

        String temp = _settings.getServiceURI();
        if(StringUtils.isBlank(temp)){
            _inputServiceURI.getText().clear();
        }else{
            _inputServiceURI.setText(temp);
        }

        _inputRange.setText(String.valueOf(_settings.getRange()));
        _inputMinLocationDistance.setText(String.valueOf(_settings.getMinLocationDistance()));
        _inputMinLocationTime.setText(String.valueOf(_settings.getMinLocationTime()/1000));

        temp = _settings.getUsername();
        if(StringUtils.isBlank(temp)){
            _inputUsername.getText().clear();
        }else{
            _inputUsername.setText(temp);
        }

        temp = _settings.getPassword();
        if(StringUtils.isBlank(temp)){
            _inputPassword.getText().clear();
        }else{
            _inputPassword.setText(temp);
        }

        Set<String> ids = _settings.getListenAlertGroupIds();
        if(ids == null || ids.isEmpty()){
            _inputListenGroupIds.getText().clear();
        }else{
            _inputListenGroupIds.setText(toString(ids));
        }

        ids = _settings.getReportAlertGroupIds();
        if(ids == null || ids.isEmpty()){
            _inputReportGroupIds.getText().clear();
        }else{
            _inputReportGroupIds.setText(toString(ids));
        }

        _switchAllowPhotos.setChecked(_settings.isAllowPhotos());
        _switchKeepFiles.setChecked(_settings.isKeepFiles());
        _switchUseGPS.setChecked(_settings.isUseGPS());
        _switchUseNetworkProvider.setChecked(_settings.isUseNetworkProvider());
		_switchHideUsersAlerts.setChecked(_settings.isHideUsersAlerts());
		_switchAllowMapGestures.setChecked(_settings.isAllowMapGestures());
		_switchUseTTS.setChecked(_settings.isUseTTS());
        _switchUseTimeFilter.setChecked(_settings.isUseTimeFilter());

        initializeListenTypes();
        initializeReportTypes();

		super.onResume();
	}

    /**
     *
     */
    private void initializeListenTypes() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.settings_layout_listen_alert_types);
        layout.removeAllViews();
        EnumSet<Alert.AlertType> types = _settings.getListenAlertTypes();
        if(types == null){
            types = EnumSet.noneOf(Alert.AlertType.class);
            _settings.setListenAlertTypes(types);
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        for(final Alert.AlertType type : Alert.AlertType.values()){
            if(type != Alert.AlertType.UNKNOWN){
                CheckBox box = new CheckBox(this);
                box.setLayoutParams(params);
                box.setChecked(types.contains(type));
                box.setText(type.toAlertTypeString());
                box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        EnumSet<Alert.AlertType> lTypes =  _settings.getListenAlertTypes();
                        if(isChecked){
                            lTypes.add(type);
                        }else{
                            lTypes.remove(type);
                        }
                    }
                });
                layout.addView(box);
            }
        }
    }

    /**
     *
     */
    private void initializeReportTypes() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.settings_layout_report_alert_types);
        layout.removeAllViews();
        EnumSet<Alert.AlertType> types = _settings.getReportAlertTypes();
        if(types == null){
            types = EnumSet.noneOf(Alert.AlertType.class);
            _settings.setReportAlertTypes(types);
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        for(final Alert.AlertType type : Alert.AlertType.values()){
            if(type != Alert.AlertType.UNKNOWN){
                CheckBox box = new CheckBox(this);
                box.setLayoutParams(params);
                box.setChecked(types.contains(type));
                box.setText(type.toAlertTypeString());
                box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        EnumSet<Alert.AlertType> rTypes =  _settings.getReportAlertTypes();
                        if(isChecked){
                            rTypes.add(type);
                        }else{
                            rTypes.remove(type);
                        }
                    }
                });
                layout.addView(box);
            }
        }
    }

    /**
     *
     * @param ids
     * @return the list of ids converted to a string
     */
    private StringBuilder toString(Set<String> ids) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = ids.iterator();
        sb.append(iter.next());
        while(iter.hasNext()){
            sb.append(SEPARATOR_ID);
            sb.append(iter.next());
        }
        return sb;
    }

    /**
     *
     * @param idValueString
     * @return the values in the string as a set or null if no values
     */
    private Set<String> toSet(String idValueString) {
        String[] values = StringUtils.split(idValueString, SEPARATOR_ID);
        if(ArrayUtils.isEmpty(values)){
            return null;
        }
        HashSet<String> set = new HashSet<>(values.length);
        Collections.addAll(set, values);
		return set;
    }

    @Override
    public void onBackPressed() {
        DialogUtils.showConfirmationDialog(this, R.string.settings_confirm_save, this, false, TAG_CONFIRM_SAVE);
    }

    @Override
    public void onClick(View v) { // only button is the "save" button
        saveSettings(false);
    }

    /**
     *
     * @param closeActivity if true the activity will be closed after successful save by calling "back button"
     */
    private void saveSettings(boolean closeActivity) {
        try {
            _settings.setAlertCheckInterval(Integer.parseInt(_inputAlertCheckInterval.getText().toString()));
            _settings.setRange(Float.parseFloat(_inputRange.getText().toString()));
            _settings.setMinLocationDistance(Integer.parseInt(_inputMinLocationDistance.getText().toString()));
            _settings.setMinLocationTime(Integer.parseInt(_inputMinLocationTime.getText().toString())*1000);
        } catch (NumberFormatException ex){
            Log.e(CLASS_NAME, "Number conversion failed.", ex);
            Toast.makeText(this, R.string.settings_error_save_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        _settings.setServiceURI(_inputServiceURI.getText().toString());
        _settings.setUsername(_inputUsername.getText().toString());
        _settings.setPassword(_inputPassword.getText().toString());
        _settings.setListenAlertGroupIds(toSet(_inputListenGroupIds.getText().toString()));
        _settings.setReportAlertGroupIds(toSet(_inputReportGroupIds.getText().toString()));
        _settings.setAllowPhotos(_switchAllowPhotos.isChecked());
        _settings.setKeepFiles(_switchKeepFiles.isChecked());
        _settings.setUseGPS(_switchUseGPS.isChecked());
        _settings.setUseNetworkProvider(_switchUseNetworkProvider.isChecked());
        _settings.setHideUsersAlerts(_switchHideUsersAlerts.isChecked());
        _settings.setAllowMapGestures(_switchAllowMapGestures.isChecked());
        _settings.setUseTTS(_switchUseTTS.isChecked());
        _settings.setUseTimeFilter(_switchUseTimeFilter.isChecked());

        _progressDialog = ProgressDialog.show(this, getString(R.string.settings_title_loading), getString(R.string.settings_notification_checking_credentials));
        new CredentialCheckTask(closeActivity).execute();
    }

    @Override
    public void dialogClosed(ConfirmationStatus status, int tag) { // tag is always TAG_CONFIRM_SAVE
        if(status == ConfirmationStatus.ACCEPTED){
            saveSettings(true);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public <T> void selectionClosed(T id, String text, int tag) {
        // nothing needed
    }

    /**
     *
     */
    private class CredentialCheckTask extends AsyncTask<Void, Void, UserIdentity> {
        private boolean _closeActivity = false;

        /**
         *
         * @param closeActivity if true the activity will be closed by calling "back button" after successful save
         */
        public CredentialCheckTask(boolean closeActivity) {
            _closeActivity = closeActivity;
        }

        @Override
        protected UserIdentity doInBackground(Void... params) {
            try(HTTPClient client = new HTTPClient(_settings)){
                return client.getUserDetails();
            }
        }

        @Override
        protected void onPostExecute(UserIdentity userId) {
            _progressDialog.dismiss();
            _progressDialog = null;
            if(userId == null){
                DialogUtils.showErrorDialog(SettingsActivity.this, R.string.settings_error_bad_credentials, false);
                return;
            }

            _settings.setUserId(userId.getUserId());
            if(!_settings.isValid()){
                Toast.makeText(SettingsActivity.this, R.string.settings_error_save_failed, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(SettingsActivity.this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
                _settings.setUserId(userId.getUserId());
                _settings.save(SettingsActivity.this);
                if(_closeActivity){
                    SettingsActivity.super.onBackPressed();
                }
            }
        }
    } // class CredentialCheckTask
}
