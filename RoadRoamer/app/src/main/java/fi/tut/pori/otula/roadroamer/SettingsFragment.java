package fi.tut.pori.otula.roadroamer;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Log;
import android.webkit.URLUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import fi.tut.pori.otula.roadroamer.datatypes.Settings;
import fi.tut.pori.otula.roadroamer.utils.TaskPerformer;
import fi.tut.pori.otula.roadroamer.utils.UIUtils;

/**
 *
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {

    public SettingsFragment(){
        //nothing needed
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // set summary texts
        Map<String, ?> prefs = getPreferenceManager().getSharedPreferences().getAll();
        for (String key : prefs.keySet())  {
            Preference pref = findPreference(key);
            if(pref != null) {
                pref.setOnPreferenceChangeListener(this);
                setSummary(pref);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        Log.d("onResume", "registering");
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        Log.d("onPause", "unRegistering");
    }

    private void setSummary(String key) {
        Preference pref = findPreference(key);
        setSummary(pref);
    }

    private void setSummary(Preference pref){
        if(pref == null){
            return;
        }
        CharSequence summary = null;
        boolean shouldSetSummary = false;
        if(pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            summary = listPref.getEntry();
            shouldSetSummary = true;
        }else if(pref instanceof EditTextPreference){
            EditTextPreference editPref = (EditTextPreference) pref;
            if((editPref.getEditText().getInputType() & InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    == InputType.TYPE_TEXT_VARIATION_PASSWORD){
                if(!StringUtils.isEmpty(editPref.getText())) {
                    summary = StringUtils.repeat("*", 8);
                }
            }else{
                summary = editPref.getText();
            }
            shouldSetSummary = true;
        }
        if(shouldSetSummary) {
            if (StringUtils.isEmpty(summary)) {
                summary = getString(R.string.preference_undefined);
            }
            pref.setSummary(summary);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
        setSummary(key);

        //restart location updates...
        if(key.equalsIgnoreCase(Settings.PreferenceKey.LOCATION_GPS_ALLOWED.toString()) ||
                key.equalsIgnoreCase(Settings.PreferenceKey.LOCATION_NETWORK_ALLOWED.toString()) ||
                key.equalsIgnoreCase(Settings.PreferenceKey.DEMO_MODE.toString())){
            TaskPerformer.getInstance().restartLocationUpdates();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(StringUtils.equalsIgnoreCase(permissions[0], Manifest.permission.ACCESS_FINE_LOCATION)){
            Settings.PreferenceKey key = UIUtils.onRequestLocationPermissionResult(requestCode, permissions, grantResults);
            if(key == Settings.PreferenceKey.LOCATION_GPS_ALLOWED ||
                    key == Settings.PreferenceKey.LOCATION_NETWORK_ALLOWED){
                SharedPreferences sPrefs = getPreferenceManager().getSharedPreferences();
                boolean grantStatus = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
                //update on/off status of the corresponding switch
                sPrefs.edit().putBoolean(key.toString(), grantStatus).apply();
                ((SwitchPreference)findPreference(key.toString())).setChecked(grantStatus);
                onSharedPreferenceChanged(sPrefs, key.toString());
            }
        }else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        //performs validation on selected preference keys
        //returns true when change of the value is allowed
        //returns false when change was not allowed
        Settings.PreferenceKey key = Settings.PreferenceKey.fromString(preference.getKey());
        switch (key){
            case USER_NAME:
            case USER_PASSWORD:
            case BACK_END_ID:
                if(StringUtils.isBlank((String)newValue)){
                    UIUtils.showMessage(this.getView(), R.string.preference_no_empty);
                    return false;
                }
                break;
            case SERVICE_CUSTOM_URI:
                if(!URLUtil.isValidUrl((String) newValue)){
                    UIUtils.showMessage(this.getView(), R.string.preference_malformed_uri);
                    return false;
                }
                break;
            case LOCATION_GPS_ALLOWED:
            case LOCATION_NETWORK_ALLOWED:
                if((Boolean)newValue){  //when turned on
                    return UIUtils.requestLocationAccess(this, key.ordinal());  //returns true if permission to access location is already granted
                }
                break;
            case UNKNOWN:
                return false;
            default:
                return true;
        }
        //preference should be valid if we get down here
        return true;
    }
}
