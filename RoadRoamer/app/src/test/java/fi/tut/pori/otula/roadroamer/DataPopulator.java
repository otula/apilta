package fi.tut.pori.otula.roadroamer;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import fi.tut.pori.otula.roadroamer.datatypes.Condition;
import fi.tut.pori.otula.roadroamer.datatypes.DataPoint;
import fi.tut.pori.otula.roadroamer.utils.Definitions;
import fi.tut.pori.otula.roadroamer.datatypes.Measurement;
import fi.tut.pori.otula.roadroamer.datatypes.Output;
import fi.tut.pori.otula.roadroamer.datatypes.SensorTask;
import fi.tut.pori.otula.roadroamer.datatypes.Task;

/**
 * Helper class to populate data
 */

public class DataPopulator {

    private final long _backendIdSeed;
    private final Random _backendIdRandom;
    private final long _dataPointIdSeed;
    private final Random _dataPointRandom;
    private final long _measurementIdSeed;
    private final Random _measurementIdRandom;
    private final long _taskIdSeed;
    private final Random _taskIdRandom;
    private final long _userIdSeed;
    private final Random _userIdRandom;

    /**
     * Rather naive populaattor-instance. All counts and seeds MUST be given.
     * @param backendCount
     * @param dataPointCount
     * @param measurementCount
     * @param taskIdSeed
     * @param userIdSeed
     */
    public DataPopulator(long backendCount, long dataPointCount, long measurementCount, long taskIdSeed, long userIdSeed){
        _backendIdSeed = backendCount;
        _dataPointIdSeed = dataPointCount;
        _measurementIdSeed = measurementCount;
        _taskIdSeed = taskIdSeed;
        _userIdSeed = userIdSeed;
        _backendIdRandom = new Random(_backendIdSeed);
        _dataPointRandom = new Random(_dataPointIdSeed);
        _measurementIdRandom = new Random(_measurementIdSeed);
        _taskIdRandom = new Random(_taskIdSeed);
        _userIdRandom = new Random(_userIdSeed);
    }

    public List<Task> generateTasks(int amount){
        ArrayList<Task> tasks = new ArrayList<>();
        for(int i=0; i<Math.abs(amount); ++i){
            tasks.add(generatePlainTask(Collections.singletonList(_backendIdRandom.nextLong()),
                    Collections.singletonList(_taskIdRandom.nextLong())));
        }
        return tasks;
    }

    public Task generatePlainTask(List<Long> backendIds, List<Long> taskIds){
        Task task = new SensorTask();
        task.setDescription(RandomStringUtils.randomAlphabetic(20));
        task.setName(RandomStringUtils.randomAlphabetic(10));
        task.setTaskIds(taskIds);
        task.setUpdated(new Date());
        task.setUserId(_userIdRandom.nextLong());
        task.setCreated(new Date());
        task.setBackends(backendIds);
        task.setTaskTypes(new HashSet<>(Collections.singletonList("gather")));

        return task;
    }

    public List<SensorTask> generateSensorTasks(List<Task> baseTasks){
        ArrayList<SensorTask> tasks = new ArrayList<>();
        for (Task task: baseTasks) {
            tasks.add(generateSensorTask(task));
        }
        return tasks;
    }

    public SensorTask generateSensorTask(Task baseTask){
        SensorTask task = new SensorTask(baseTask);
        task.setConditions(generateSensibleConditions());
        task.setOutput(generateSensibleOutput());
        task.setMeasurements(generateMeasurements((int)_measurementIdSeed));
        return task;
    }

    public SensorTask generateSensibleSensorTask(){
        SensorTask task = new SensorTask(generateTasks(1).get(0));
        task.setConditions(generateSensibleConditions());
        task.setOutput(generateSensibleOutput());
        return task;
    }

    public List<Condition> generateSensibleConditions(){
        Condition con1 = new Condition();
        HashMap<String, String> terms1 = new HashMap<>();
        terms1.put(Definitions.TERM_LOCATION_POINT, "61.495792,21.802571");
        terms1.put(Definitions.TERM_TIME_VALIDITY_RANGE, "2016-02-27T23:08:58.999Z/2017-12-08T02:40:05.999Z");
        con1.setConditions(terms1);

        Condition con2 = new Condition();
        HashMap<String, String> terms2 = new HashMap<>();
        terms2.put(Definitions.TERM_LOCATION_AREA, "polygon(61.498142,21.803633; 61.500937,21.802292; 61.499175,21.796936)");
        terms2.put(Definitions.TERM_TIME_VALIDITY_RANGE, "2016-02-27T23:08:58.999Z/2017-12-08T02:40:05.999Z");
        con2.setConditions(terms2);

        return Arrays.asList(con1, con2);
    }

    public List<Output> generateSensibleOutput(){
        Output output1 = new Output();
        output1.setFeature(Definitions.FEATURE_SENSOR_LOCATION);
        Output output2 = new Output();
        output2.setFeature(Definitions.FEATURE_SENSOR_CAMERA);
        //Output output3 = new Output();
        //output3.setFeature(Definitions.FEATURE_SENSOR_VELOCITY);

        return Arrays.asList(output1, output2);
    }

    public List<Measurement> generateMeasurements(int amount){
        ArrayList<Measurement> measurements = new ArrayList<>();
        for(int i=0; i<Math.abs(amount); ++i){
            measurements.add(generateMeasurement(_backendIdRandom.nextLong()));
        }
        return measurements;
    }

    public Measurement generateMeasurement(long backendId){
        Measurement measurement = new Measurement();
        measurement.setBackendId(backendId);
        measurement.setMeasurementId(_measurementIdRandom.nextLong());
        measurement.setDataPoints(generateDataPoints(measurement.getMeasurementId(), _dataPointIdSeed));
        return measurement;
    }

    public List<DataPoint> generateDataPoints(long measurementID, long amount){
        ArrayList<DataPoint> dataPoints = new ArrayList<>();
        for(int i=0; i<Math.abs(amount); ++i){
            dataPoints.add(generateDataPoint(measurementID));
        }
        return dataPoints;
    }

    public DataPoint generateDataPoint(long measurementID){
        DataPoint dp = new DataPoint();
        dp.setDataPointId(String.valueOf(_dataPointRandom.nextLong()));
        dp.setMeasurementId(measurementID);
        dp.setCreated(new Date());
        dp.setDescription(RandomStringUtils.randomAlphabetic(20));
        dp.setKey(RandomStringUtils.randomAlphabetic(6));
        dp.setValue(RandomStringUtils.randomAlphabetic(10));
        return dp;
    }
}
