package fi.tut.pori.otula.roadroamer.utils;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import org.apache.commons.lang3.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fi.tut.pori.otula.roadroamer.NetworkCore;
import fi.tut.pori.otula.roadroamer.datatypes.Condition;
import fi.tut.pori.otula.roadroamer.datatypes.DataPoint;
import fi.tut.pori.otula.roadroamer.datatypes.Measurement;
import fi.tut.pori.otula.roadroamer.datatypes.SensorTask;
import fi.tut.pori.otula.roadroamer.datatypes.Settings;

/**
 * Adapted from http://stackoverflow.com/questions/2566350/how-to-always-run-a-service-in-the-background
 */
public class TaskPerformer extends SimpleCamera2ServicePublish implements LocationChangedListener {
    private static final String CLASS_NAME = TaskPerformer.class.getSimpleName();

    private static TaskPerformer _instance = null;
    private LocationManager _locationManager = null;
    private LocationListener _locationListener = null;
    private Location _lastLocation = null;

    private Map<Condition, SensorTask> _cameraTaskQueue = new HashMap<>();
    private NetworkCore _network = null;

    @Override
    public void onCreate() {
        Log.d(CLASS_NAME, "onCreate " + _instance);
        super.onCreate();
        if(_instance != null){
            _instance.stopLocationUpdates();
            _instance = null;
        }
        _instance = this;
        _network = new NetworkCore(this);
    }

    public TaskPerformer() {
        Log.d(CLASS_NAME, "class created");
    }

    public static TaskPerformer getInstance(){
        return _instance;
        /*
         * change to this model?
         * http://stackoverflow.com/questions/2463175/how-to-have-android-service-communicate-with-activity
         */
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;    //nothing else needed
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(CLASS_NAME, "Received start id " + startId + ": " + intent);
        _locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        startLocationUpdates();

        //get some data from network
        _network.getTasks();

        if(new Settings(this).getBooleanSetting(Settings.PreferenceKey.BACKGROUND_PROCESSING)){
            return START_STICKY;
        }else{
            return START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        _network.stopDataSenderThread();

        /*TODO check if files are purged...
        for(File file: _fileCache){
            file.delete();
        }
        */

        stopLocationUpdates();
        super.onDestroy();
    }

    /**
     * start location updated
     *
     * @throws SecurityException if user has denied location permissions
     */
    public void startLocationUpdates() throws SecurityException {
        Settings settings = new Settings(this);
        LocationListener listener = null;

        if(settings.getBooleanSetting(Settings.PreferenceKey.DEMO_MODE)){
            Log.d(CLASS_NAME, "Starting updates for Mock Location Provider");
            listener = new MockLocationListener(this, this);
            listener.addProvider(LocationManager.PASSIVE_PROVIDER);
        }else if(UIUtils.isLocationAccessAllowed(this)) {
            listener = new ApiltaLocationListener(this);
            if (settings.getBooleanSetting(Settings.PreferenceKey.LOCATION_GPS_ALLOWED) &&
                    _locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                listener.addProvider(LocationManager.GPS_PROVIDER);
            }
            if (settings.getBooleanSetting(Settings.PreferenceKey.LOCATION_NETWORK_ALLOWED) &&
                    _locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                listener.addProvider(LocationManager.NETWORK_PROVIDER);
            }
        }
        startUpdates(listener);
    }

    private void startUpdates(LocationListener listener) throws SecurityException{
        if(listener != null){
            stopLocationUpdates();
            _locationListener = listener;
            List<String> providers = _locationListener.getProviders();
            for (String provider: providers) {  //start updates for each of the defined providers
                _locationManager.requestLocationUpdates(provider, _locationListener.getMinTimeInterval(), _locationListener.getMinDistanceInterval(), _locationListener);
            }
        }
    }

    /**
     * stop location updates
     *
     */
    public void stopLocationUpdates(){
        if(_locationManager != null && _locationListener != null){
            Log.d(CLASS_NAME, "Stopping all location updates.");
            _locationManager.removeUpdates(_locationListener);
            _locationListener = null;
        }
    }

    public void restartLocationUpdates(){
        Log.d(CLASS_NAME, "start-stop");
        stopLocationUpdates();
        startLocationUpdates();
    }

    @Override
    public void locationChanged(Location location) {
        StringBuilder stringBuilder = new StringBuilder();
        if(_lastLocation != null){
            stringBuilder.append("Distance from last: ");
            stringBuilder.append(location.distanceTo(_lastLocation));
            stringBuilder.append(" meters\t\t");
        }
        stringBuilder.append(location.toString());
        Log.d(CLASS_NAME, stringBuilder.toString());

        _lastLocation = location;
        checkMatchingTasks(_lastLocation, Definitions.FEATURE_SENSOR_CAMERA, _network.getTaskCache());
    }

    @Override
    protected void processImage(Image image){
        FileOutputStream output = null;
        File file = null;
        try {
            //save file temporarily
            file = File.createTempFile(Definitions.PREFIX_PHOTO + System.currentTimeMillis() + ".", Definitions.SUFFIX_PHOTO, this.getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            output = new FileOutputStream(file);
            output.write(bytes);
            file.deleteOnExit();

            //attach file to each task in queue
            attachFileToMeasurement(file);
            Log.d(CLASS_NAME, file.getAbsolutePath());
        } catch (IOException ex){
            ex.printStackTrace();
            if(file != null){
                file.delete();
            }
        }finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void checkMatchingTasks(Location location, String feature, List<SensorTask> tasks){
        if(tasks == null){
            return;
        }
        //traverse thru the tasks list and determine which conditions are matching to this location
        for(SensorTask task: tasks) {
            for (Condition condition : task.getConditions()) {
                boolean conditionTriggered = false;
                //check that *all* of the terms are matching for a condition
                for (Map.Entry<String, String> entry : condition.getConditions().entrySet()) {
                    String conditionName = entry.getKey();
                    String conditionTrigger = entry.getValue();
                    conditionTriggered = checkForTrigger(conditionName, conditionTrigger, location);
                    if(!conditionTriggered){
                        break;  //this condition is not eligible
                    }
                }

                if (conditionTriggered) {   //process if condition was triggered
                    if (!_network.isConditionInDataCache(condition)) {  //check if the condition was new, and construct new Task&Measurement object with relevant data as needed
                        long backendId = Long.valueOf(new Settings(this).getStringSetting(Settings.PreferenceKey.BACK_END_ID));

                        SensorTask sensorTask = new SensorTask();
                        sensorTask.setBackends(Collections.singletonList(backendId));
                        sensorTask.setTaskIds(task.getTaskIds());
                        sensorTask.setCallbackUri(task.getCallbackUri());

                        Measurement measurement = new Measurement();
                        measurement.setBackendId(backendId);
                        measurement.setMeasurementId(RandomUtils.nextLong());
                        measurement.addDataPoint(createLocationDataPoint(location, measurement.getMeasurementId()));
                        List<Measurement> measurements = new ArrayList<>(1);
                        measurements.add(measurement);
                        sensorTask.setMeasurements(measurements);

                        _network.addToDataCache(condition, sensorTask);
                    }
                    //moves task to the requested processing queue
                    switch (feature) {
                        case Definitions.FEATURE_SENSOR_CAMERA:
                            Log.d(CLASS_NAME, "Conditions match, taking a photo. Condition: " + org.apache.commons.lang3.StringUtils.join(condition.getConditions().values()));
                            _cameraTaskQueue.put(condition, _network.getFromDataCache(condition));
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        if (!_cameraTaskQueue.isEmpty() && isCameraReady()) {
            takeSingleImage();
        }
    }

    private boolean checkForTrigger(String triggerName, String triggerValue, Location location){
        switch(triggerName) {
            case Definitions.TERM_LOCATION_POINT:
                float[] results = new float[1];
                String[] triggerLatLong = triggerValue.split(",");  //parses the value to [Lat, Long] array
                //TODO add check for bearingTo, for determining we are approaching the target point...
                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                        Double.valueOf(triggerLatLong[0]), Double.valueOf(triggerLatLong[1]),
                        results);
                //by definition of Location.distanceBetween, results[0] will have the distance between points in meters
                if (results[0] < compensateDistanceForSpeed(location.getSpeed())) {
                    Log.d(CLASS_NAME, triggerName + " matched, distance was " + results[0] + " meters");
                    return true;
                }
                break;
            case Definitions.TERM_LOCATION_AREA:
                //TODO see
                //http://stackoverflow.com/questions/18199082/point-in-polygon-algortim-using-location-class-in-android
                //http://stackoverflow.com/questions/13950062/checking-if-a-longitude-latitude-coordinate-resides-inside-a-complex-polygon-in
                Log.d(CLASS_NAME, triggerName + " never matches in this implementation");
                return false;
            case Definitions.TERM_SENSOR_VELOCITY:
            case Definitions.TERM_TIME_VALIDITY_RANGE:
                //TODO these terms are ignored for the time being
                Log.d(CLASS_NAME, triggerName + " has been ignored");
                return false;
            case Definitions.TERM_TEXT_DESCRIPTION:
                //no need to process this one, skip and return true
                return true;
            default:
                break;
        }
        return false;
    }

    private synchronized void attachFileToMeasurement(File file){
        String fileKey = _network.addFileToQueue(file);
        if(fileKey == null){
            return;
        }

        Date currentTime = new Date();
        Iterator<Map.Entry<Condition, SensorTask>> it = _cameraTaskQueue.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<Condition, SensorTask> entry = it.next();
            SensorTask task = entry.getValue();
            Measurement measurement = task.getMeasurements().get(0);

            //update modification time
            task.setUpdated(currentTime);
            //create new data point
            DataPoint dp = new DataPoint();
            dp.setKey(Definitions.DATA_POINT_KEY_FILE_TEMP);     //"file" needs to be sent to the server before _dataCache is resolved
            dp.setMeasurementId(measurement.getMeasurementId());
            dp.setValue(fileKey);
            dp.setCreated(currentTime);
            dp.setDataPointId(UUID.randomUUID().toString());
            dp.setDescription("Camera");
            measurement.addDataPoint(dp);
            //camera "measurement" added, this condition can be removed from the task queue
            //data will be retained in the _dataCache map
            it.remove();
        }
        _network.sendTaskDataAndFiles();
    }

    /**
     * Helper method to set creation location to a new measurement
     * @param location the current location
     * @return constructed data point data
     */
    private DataPoint createLocationDataPoint(Location location, Long measurementId){
        DataPoint dp = new DataPoint();
        dp.setKey(Definitions.DATA_POINT_KEY_SENSOR_LOCATION);
        dp.setMeasurementId(measurementId);
        dp.setValue(location.getLatitude()+","+location.getLongitude());
        dp.setCreated(new Date());
        dp.setDataPointId(UUID.randomUUID().toString());
        dp.setDescription("Location");
        return dp;
    }

    private float compensateDistanceForSpeed(float speed){
        if(speed < 2.5){
            return -1;
        }else if(speed < 5){//going quite slowly
            return Definitions.MINIMUM_DISTANCE_TO_ACTIVATE;
        }else if(speed > 40){
            //we are going too fast, return something too small to ever match
            Log.w(CLASS_NAME, "we are going too fast, do not compute");
            return -1;
        }else{
            return 7 * speed;   //allow approximately 6-5 seconds before reaching the target
        }
    }
}
