package fi.tut.pori.otula.roadroamer.datatypes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.apache.commons.lang3.StringUtils;

/**
 * This class should be used for accessing settings.
 */
public class Settings {

    private static final String DEFAULT_SERVICE_URI = "http://example.org/ApiltaService";
    private final SharedPreferences _preferences;

    public Settings(Context context){
        _preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public enum PreferenceKey{
        BACKGROUND_PROCESSING("background_processing_allowed"),
        LOCATION_GPS_ALLOWED("location_gps_allowed"),
        LOCATION_NETWORK_ALLOWED("location_network_allowed"),
        DEMO_MODE("demo_mode"),
        USER_NAME("user_name"),
        USER_PASSWORD("user_password"),
        BACK_END_ID("back_end_id"),
        SERVICE_USE_CUSTOM("server_use_custom"),
        SERVICE_CUSTOM_URI("server_custom_uri"),
        NOTIFICATIONS_ALLOWED("notifications_allowed"),
        //NOTIFICATIONS_RINGTONE("notifications_new_message_ringtone"),
        //NOTIFICATIONS_VIBRATE("notifications_new_message_vibrate"),
        UNKNOWN("");

        private String _value = null;

        PreferenceKey(String value){
            _value = value;
        }

        @Override
        public String toString(){
            return _value;
        }

        /**
         *
         * @param value the value to be evaluated
         * @return the preference key, or UNKNOWN if not supported
         */
        public static PreferenceKey fromString(String value) {
            for(PreferenceKey key : values()){
                if(StringUtils.equals(key._value, value)){
                    return key;
                }
            }
            return UNKNOWN;
        }
    }

    public String getServiceURI(){
        if(_preferences.getBoolean(PreferenceKey.SERVICE_USE_CUSTOM.toString(), false)){
            return _preferences.getString(PreferenceKey.SERVICE_CUSTOM_URI.toString(), DEFAULT_SERVICE_URI);
        }else{
            return DEFAULT_SERVICE_URI;
        }
    }

    public boolean getBooleanSetting(PreferenceKey key){
        switch (key){
            case BACKGROUND_PROCESSING:
            case DEMO_MODE:
            case LOCATION_GPS_ALLOWED:
            case LOCATION_NETWORK_ALLOWED:
            case NOTIFICATIONS_ALLOWED:
            //case NOTIFICATIONS_VIBRATE:
                return _preferences.getBoolean(key.toString(), false);
            default:
                return false;
        }
    }

    public String getStringSetting(PreferenceKey key){
        switch (key){
            case USER_NAME:
            case USER_PASSWORD:
            case BACK_END_ID:
                return _preferences.getString(key.toString(), "");
            case SERVICE_CUSTOM_URI:
                return getServiceURI();
            default:
                return "";
        }
    }
}
