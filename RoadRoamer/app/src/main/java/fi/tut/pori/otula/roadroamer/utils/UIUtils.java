package fi.tut.pori.otula.roadroamer.utils;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import fi.tut.pori.otula.roadroamer.datatypes.Settings;

/**
 *
 */
public class UIUtils {
    private static final String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION};

    private UIUtils(){
        //nothing needed
    }

    /**
     * Show a snackbar in given view
     * @param view
     * @param stringResourceId
     */
    public static void showMessage(final View view, final int stringResourceId){
        if(view != null) {
            Snackbar.make(view, stringResourceId, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    /**
     * Show a snackbar in given view
     * @param view
     * @param text
     */
    public static void showMessage(final View view, final CharSequence text){
        if(view != null) {
            Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    public static Settings.PreferenceKey onRequestLocationPermissionResult(final int requestCode, final String[] permissions, final int[] grantResults){
        Settings.PreferenceKey key;
        if(requestCode == Settings.PreferenceKey.LOCATION_GPS_ALLOWED.ordinal()) {
            key = Settings.PreferenceKey.LOCATION_GPS_ALLOWED;
        }else if (requestCode == Settings.PreferenceKey.LOCATION_NETWORK_ALLOWED.ordinal()) {
            key = Settings.PreferenceKey.LOCATION_NETWORK_ALLOWED;
        }else {
            return null;
        }
        return key;
    }

    public static boolean isLocationAccessAllowed(@NonNull final Context context){
        return context.checkSelfPermission(PERMISSIONS_LOCATION[0]) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean requestLocationAccess(@NonNull final Fragment fragment, final int preferenceKeyOrdinal) {
        Log.d("requestLocationAccess", "requesting location access");
        if (isLocationAccessAllowed(fragment.getContext())) {
            Log.d("requestLocationAccess", "request already granted");
            return true;
        } else {
            Log.d("requestLocationAccess", "requesting");
            fragment.requestPermissions(PERMISSIONS_LOCATION, preferenceKeyOrdinal);
            // The callback method gets the result of the request.
            return false;
        }
    }
}
