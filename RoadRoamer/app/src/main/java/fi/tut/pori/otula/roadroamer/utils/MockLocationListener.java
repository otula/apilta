package fi.tut.pori.otula.roadroamer.utils;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fi.tut.pori.otula.roadroamer.R;

/**
 * Mock Location Listener, changes the actual value to something that has been given in a JSON file
 */
final class MockLocationListener implements LocationListener {
    private static final String CLASS_NAME = MockLocationListener.class.getName();

    // The minimum distance to change Updates in meters
    private static final long MIN_LOCATION_DISTANCE_INTERVAL = 0; // 0 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_LOCATION_TIME_INTERVAL = 6000; // 6 seconds

    private List<Pair<Double, Double>> _locations = null;
    private Iterator<Pair<Double, Double>> _it = null;
    private List<String> _providers = null;
    private LocationChangedListener _listener = null;

    MockLocationListener(Context ctx, LocationChangedListener listener){
        super();
        _providers = new ArrayList<>(1);
        InputStream inputStream = ctx.getResources().openRawResource(R.raw.mock);
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
        _locations = parseRoute(reader);
        _listener = listener;
    }

    @Override
    public void addProvider(String provider){
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

    private List<Pair<Double, Double>> parseRoute(JsonReader reader){
        List<Pair<Double, Double>> locations = new ArrayList<>();
        try{
            reader.beginArray();
            while (reader.hasNext()) {
                locations.add(readLocation(reader));
            }
            reader.endArray();
        }catch (IOException ex){
            Log.d(CLASS_NAME, ex.getMessage());
        }finally {
            try{
                reader.close();
            }catch (IOException ex2){
                //doh
            }
        }
        return locations;
    }

    private Pair<Double, Double> readLocation(JsonReader reader) throws IOException{
        Double lat = 0.0;
        Double lon = 0.0;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("lat")) {
                lat = reader.nextDouble();
            } else if (name.equals("long")) {
                lon = reader.nextDouble();
            }else{
                reader.skipValue();
            }
        }
        reader.endObject();
        return new ImmutablePair<>(lat, lon);
    }

    @Override
    public void onLocationChanged(Location location) {
        //set location data from predefined file
        if(_it == null || !_it.hasNext()){
            _it = _locations.iterator();
        }
        Pair<Double, Double> latLong = _it.next();
        location.setLatitude(latLong.getLeft());
        location.setLongitude(latLong.getRight());
        location.setSpeed(RandomUtils.nextFloat(5,20));
        //location should have now been overridden
        Log.d(CLASS_NAME, _locations.indexOf(latLong) + ": " + location.toString());

        if(_listener != null){
            _listener.locationChanged(location);
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(CLASS_NAME, provider + " enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(CLASS_NAME, provider + " disabled");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(CLASS_NAME, provider + " status changed: "+status);
    }
}
