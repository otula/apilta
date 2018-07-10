package fi.tut.pori.otula.roadroamer.utils;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
final class ApiltaLocationListener implements LocationListener {
    private static final String CLASS_NAME = ApiltaLocationListener.class.getName();
    // The minimum distance to change Updates in meters
    private static final long MIN_LOCATION_DISTANCE_INTERVAL = 5; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_LOCATION_TIME_INTERVAL = 1000 * 1; // 2 seconds

    private List<String> _providers = null;
    private LocationChangedListener _listener = null;

    ApiltaLocationListener(LocationChangedListener listener){
        _providers = new ArrayList<>(1);
        _listener = listener;
    }

    @Override
    public void addProvider(String provider){
        Log.d(CLASS_NAME, "Adding provider updates for " + provider);
        _providers.add(provider);
    }

    @Override
    public List<String> getProviders() {
        return _providers;
    }

    @Override
    public long getMinDistanceInterval() {
        return MIN_LOCATION_DISTANCE_INTERVAL;
    }

    @Override
    public long getMinTimeInterval() {
        return MIN_LOCATION_TIME_INTERVAL;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(_listener != null){
            _listener.locationChanged(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(CLASS_NAME, provider + " status changed: "+status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(CLASS_NAME, provider + " enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(CLASS_NAME, provider + " disabled");
    }
}
