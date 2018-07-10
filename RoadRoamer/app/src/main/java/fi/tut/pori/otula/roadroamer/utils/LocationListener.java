package fi.tut.pori.otula.roadroamer.utils;

import java.util.List;

/**
 *
 */
public interface LocationListener extends android.location.LocationListener {

    void addProvider(String provider);
    List<String> getProviders();
    long getMinDistanceInterval();
    long getMinTimeInterval();
}
