package fi.tut.pori.otula.roadroamer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fi.tut.pori.otula.roadroamer.datatypes.Condition;
import fi.tut.pori.otula.roadroamer.datatypes.DataPoint;
import fi.tut.pori.otula.roadroamer.datatypes.FileDetails;
import fi.tut.pori.otula.roadroamer.datatypes.Measurement;
import fi.tut.pori.otula.roadroamer.datatypes.Output;
import fi.tut.pori.otula.roadroamer.datatypes.SensorTask;
import fi.tut.pori.otula.roadroamer.datatypes.Settings;
import fi.tut.pori.otula.roadroamer.utils.Definitions;
import fi.tut.pori.otula.roadroamer.utils.HTTPClient;
import fi.tut.pori.otula.roadroamer.utils.XMLSerializer;

/**
 *
 */
public class NetworkCore {
    private static final String CLASS_NAME = NetworkCore.class.getName();

    private Handler _handler = null;
    private HTTPClient _client = null;
    private List<SensorTask> _taskCache = null;
    private Map<String, File> _fileCache = null;
    private Map<Condition, SensorTask> _dataCache = null;
    private Runnable _valueSenderThread = null;
    private boolean _senderThreadScheduled = false;

    public NetworkCore(Context context){

        _fileCache = new HashMap<>();
        _dataCache = new HashMap<>();
        _client = new HTTPClient(new Settings(context));
        _handler = new Handler();
    }

    public boolean getTasks(){
        return _handler.post(new GetSensorTask());
    }

    public boolean sendTaskDataAndFiles(){
        if(_valueSenderThread == null){ // start the sending of data when the data is available for the first time
            _valueSenderThread = new AddMeasurementsTask();
        }
        return scheduleDataSender();
    }

    public boolean isConditionInDataCache(Condition condition){
        return _dataCache.containsKey(condition);
    }

    public SensorTask addToDataCache(Condition condition, SensorTask task) {
        return _dataCache.put(condition, task);
    }

    public SensorTask getFromDataCache(Condition condition){
        return _dataCache.get(condition);
    }

    public synchronized void stopDataSenderThread(){
        if(_valueSenderThread != null){
            _handler.removeCallbacks(_valueSenderThread);
        }
        _senderThreadScheduled = false;
    }

    private synchronized boolean scheduleDataSender(){
        if(!_senderThreadScheduled) {
            _senderThreadScheduled = _handler.postDelayed(_valueSenderThread, Definitions.TIME_BETWEEN_CONNECTION_ATTEMPTS);
        }
        return _senderThreadScheduled;
    }

    /**
     *
     * @param file
     * @return key to the file if the file was new or null if already in the cache.
     */
    public synchronized String addFileToQueue(File file) {
        String fileCacheKey = file.getAbsolutePath();
        File addedFile = _fileCache.put(fileCacheKey, file);
        if(addedFile == null){  //file was new, return true
            return fileCacheKey;
        }else{  //file already existed in file cache, return false
            return null;
        }
    }

    public List<SensorTask> getTaskCache() {
        if(_taskCache == null){
            return new ArrayList<>(0);  //for null safety
        }
        return _taskCache;
    }

    public void purgeFileCache(){

    }

    /**
     * runnable used by the handler for (re)scheduling sensor task retrieval
     */
    private class GetSensorTask implements Runnable {

        @Override
        public void run() {
            new GetSensorATask().execute();
        }
    } // class GetSensorTask

    /**
     * asynchronous task for retrieving sensor tasks
     */
    private class GetSensorATask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            //_handler.postDelayed(_activeGetAlertTask, _client.getSettings().getAlertCheckInterval()*1000);
            //TODO should probably check for new tasks once in a while
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<SensorTask> tasks = null;
            try{
                tasks = _client.getTasks();
            }catch(IllegalArgumentException ex){
                Log.d(CLASS_NAME, ex.getMessage());
                return null;
            }

            if(tasks != null) {
                _taskCache = tasks;
                for (SensorTask task : tasks) {
                    Log.d(CLASS_NAME, "Condition#"+ XMLSerializer.nullToEmpty(task.getConditions()).size()
                            + " Output#"+XMLSerializer.nullToEmpty(task.getOutput()).size());
                    String outStr = "";
                    for(Output out : XMLSerializer.nullToEmpty(task.getOutput())){
                        outStr += out.getFeature()+",";
                    }
                    Log.d(CLASS_NAME, "Outputs ("+outStr+")");
                    for(Condition cond : XMLSerializer.nullToEmpty(task.getConditions())){
                        StringBuilder str = new StringBuilder();
                        str.append("Terms#");
                        str.append(cond.getConditions().size());
                        for (Map.Entry<String, String> entry : cond.getConditions().entrySet()) {
                            String conditionName = entry.getKey();
                            String conditionTrigger = entry.getValue();
                            str.append("\t");
                            str.append(conditionName);
                            str.append("='");
                            str.append(conditionTrigger);
                            str.append("'");
                        }
                        Log.d(CLASS_NAME, str.toString());
                    }
                }
            }
            return null;
        }
    } // class GetSensorATask

    /**
     * runnable used by the handler for (re)scheduling sensor task retrieval
     */
    private class AddMeasurementsTask implements Runnable {

        @Override
        public void run() {
            //find items that are "ready" i.e. older than *one minute*
            List<SensorTask> tasks = null;
            Iterator<Map.Entry<Condition, SensorTask>> it = _dataCache.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Condition, SensorTask> entry = it.next();
                SensorTask task = entry.getValue();
                if (System.currentTimeMillis() - task.getUpdated().getTime() > (Definitions.TIME_BETWEEN_CONNECTION_ATTEMPTS/2)) {   //wait for 30 seconds after last addition to make sure all data has been collected to this task
                    if(tasks == null){
                        tasks = new ArrayList<>();
                    }
                    tasks.add(task);
                    it.remove();
                }
            }
            if(tasks != null){
                new AddMeasurementsATask().execute(tasks.toArray(new SensorTask[tasks.size()]));
                if(!_dataCache.isEmpty()){
                    Log.d(CLASS_NAME, "Data cache still not empty, reschedule send task.");
                    scheduleDataSender();  //trying again in a minute
                }
            }else if(_dataCache.isEmpty()) {
                Log.d(CLASS_NAME, "No data to send, sleeping.");
            }else{
                Log.d(CLASS_NAME, "Time interval not exceeded on any item, trying again.");
                scheduleDataSender();  //trying again in a minute
            }
        }
    } // class AddMeasurementsTask

    /**
     * asynchronous task for posting new measurements and photos/files to the service
     */
    private class AddMeasurementsATask extends AsyncTask<SensorTask, Void, Boolean> {

        @Override
        protected Boolean doInBackground(SensorTask... params) {
            Log.d(CLASS_NAME, "Sending tasks, amount " + params.length);
            for(int i=0; i<params.length; ++i){
                SensorTask task = params[i];
                Measurement measurement = task.getMeasurements().get(0);    //there really should be only one measurement per task
                List<DataPoint> dataPoints = measurement.getDataPoints();
                boolean isReady = true;
                Iterator<DataPoint> it = dataPoints.iterator();
                DataPoint dp = null;
                while (it.hasNext()) {
                    dp = it.next();
                    //files should be uploaded
                    if (Definitions.DATA_POINT_KEY_FILE_TEMP.equalsIgnoreCase(dp.getKey())) {
                        String fileCacheKey = dp.getValue();
                        Log.d(CLASS_NAME, "Sending file: "+fileCacheKey);

                        FileDetails details = _client.uploadFile(_fileCache.get(fileCacheKey));
                        if (details != null) {
                            //file uploaded, update data point information
                            dp.setKey(Definitions.DATA_POINT_KEY_FILE_GUID);
                            dp.setValue(details.getGUID());
                            isReady = true;
                        } else {
                            isReady = false;
                            break;
                            //failed... do not remove from send queue yet, try again next time
                        }
                    }
                }
                if(isReady){
                    Log.d(CLASS_NAME, "Sending task.");
                    _client.taskFinished(task);
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            _senderThreadScheduled = false;
            if(success){
                Log.d(CLASS_NAME, "Sending measurements succeeded.");
            }else{
                Log.w(CLASS_NAME, "Sending measurements failed.");
            }
        }
    } // class AddMeasurementsTask
}
