/**
 * Copyright 2016 Tampere University of Technology, Pori Department
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package service.tut.pori.apilta.sensors.reference;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.log4j.Logger;

import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.DateIntervalParameter.Interval;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;
import service.tut.pori.apilta.sensors.Definitions;
import service.tut.pori.apilta.sensors.datatypes.Condition;
import service.tut.pori.apilta.sensors.datatypes.DataPoint;
import service.tut.pori.apilta.sensors.datatypes.Measurement;
import service.tut.pori.apilta.sensors.datatypes.MeasurementList;
import service.tut.pori.apilta.sensors.datatypes.Output;
import service.tut.pori.apilta.sensors.datatypes.SensorTask;
import service.tut.pori.tasks.datatypes.Task;
import service.tut.pori.tasks.datatypes.Task.State;
import service.tut.pori.tasks.datatypes.Task.Visibility;
import service.tut.pori.tasks.datatypes.TaskBackend;
import service.tut.pori.tasks.datatypes.TaskBackend.Status;

/**
 * 
 * Class that can be used to created example objects/object lists.
 *
 */
public class SensorsXMLObjectCreator {
	private static final Logger LOGGER = Logger.getLogger(SensorsXMLObjectCreator.class);
	private static final int TEXT_LENGTH = 64;
	private Random _random = null;
	private RandomStringGenerator _stringGenerator = null;
	
	/**
	 * 
	 * @param seed for random generator, or null to use default (system time in nanoseconds)
	 */
	public SensorsXMLObjectCreator(Long seed){
		if(seed == null){
			seed = System.nanoTime();
		}
		_random = new Random(seed);
		_stringGenerator = new RandomStringGenerator.Builder().withinRange('a', 'z').build();
	}

	/**
	 * @return the random
	 */
	public Random getRandom() {
		return _random;
	}
	
	/**
	 * 
	 * @param backendIdFilter 
	 * @param createdFilter 
	 * @param dataGroups 
	 * @param limits
	 * @param measurementIdFilter 
	 * @return list of measurement objects
	 */
	public List<Measurement> generateMeasurementList(long[] backendIdFilter, Set<Interval> createdFilter, DataGroups dataGroups, Limits limits, List<String> measurementIdFilter){
		int count = limits.getMaxItems(service.tut.pori.apilta.sensors.datatypes.Definitions.ELEMENT_MEASUREMENT_LIST);
		if(count < 1){
			LOGGER.warn("count < 1");
			return null;
		}else if(count >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Count was "+Limits.DEFAULT_MAX_ITEMS+", using 1.");
			count = 1;
		}
		boolean hasMeasurementIdFilter = (measurementIdFilter != null && !measurementIdFilter.isEmpty());
		if(hasMeasurementIdFilter){
			int filterSize = measurementIdFilter.size();
			if(hasMeasurementIdFilter && filterSize > count){
				LOGGER.debug("Not enough measurement ids in the filter for limit count, restricting count to: "+filterSize);
				count = filterSize;
			}
		}
		
		List<Measurement> list = new ArrayList<>();
		for(int i=0;i<count;++i) {
			Measurement measurement = generateMeasurementData(backendIdFilter, createdFilter, dataGroups, limits);
			if(hasMeasurementIdFilter){ // if filter given ...
				measurement.setMeasurementId(measurementIdFilter.get(i)); // ... modify ids to match the filter
			}
			list.add(measurement);
		}
		return (list.isEmpty() ? null : list);
	}

	/**
	 * 
	 * @param backendIdFilter 
	 * @param createdFilter 
	 * @param dataGroups 
	 * @param limits
	 * @return pseudo randomly generated measurement data
	 */
	public Measurement generateMeasurementData(long[] backendIdFilter, Set<Interval> createdFilter, DataGroups dataGroups, Limits limits) {
		Measurement data = new Measurement();
		data.setBackendId((ArrayUtils.isEmpty(backendIdFilter) ? Math.abs(_random.nextLong()) : backendIdFilter[_random.nextInt(backendIdFilter.length)]));
		if(DataGroups.hasDataGroup(DataGroups.DATA_GROUP_ALL, dataGroups) || DataGroups.hasDataGroup(Definitions.DATA_GROUP_DATA_POINTS, dataGroups)){
			data.setDataPoints(createDataPointList(data.getMeasurementId(), limits, createdFilter));
		}
		data.setMeasurementId(UUID.randomUUID().toString());

		return data;
	}

	/**
	 * 
	 * @param backendId
	 * @param dataGroups
	 * @param limits
	 * @param taskType
	 * @return pseudo randomly generated task details
	 * @throws IllegalArgumentException on invalid arguments
	 */
	public SensorTask generateTaskDetails(Long backendId, DataGroups dataGroups, Limits limits, String taskType) throws IllegalArgumentException {
		SensorTask task = (SensorTask) setTaskDetails(new SensorTask(), backendId, taskType);
		setSensorTaskDetails(limits, task);
		
		return task;
	}

	/**
	 * 
	 * @param backendId 
	 * @param dataGroups
	 * @param limits
	 * @param taskType
	 * @return pseudo randomly generated task results
	 * @throws IllegalArgumentException on invalid arguments
	 */
	public SensorTask generateTaskResults(Long backendId, DataGroups dataGroups, Limits limits, String taskType) throws IllegalArgumentException
	{
		SensorTask task = (SensorTask) setTaskDetails(new SensorTask(), _random.nextLong(), taskType);
		//erase a few non-needed members for this case
		List<TaskBackend> backends = task.getBackends();
		backends.clear();
		TaskBackend tb = generateTaskBackend(backendId);
		backends.add(tb);
		task.setCreated(null);
		task.setUpdated(null);
		task.setUserId(null);
		task.setName(null);
		task.setDescription(null);
		task.setState(null);
		//generate & set task results
		MeasurementList data = new MeasurementList();
		data.setMeasurements(generateMeasurementList((backendId == null ? new long[]{tb.getBackendId()} : new long[]{backendId}), null, dataGroups, limits, null));
		task.setMeasurements(data);
		
		return task;
	}
	
	/**
	 * 
	 * @param measurementId
	 * @param limits
	 * @param createdFilter 
	 * @return list of datapoint objects
	 */
	public List<DataPoint> createDataPointList(String measurementId, Limits limits, Set<Interval> createdFilter){
		int count = limits.getMaxItems(service.tut.pori.apilta.sensors.datatypes.Definitions.ELEMENT_DATAPOINT_LIST);
		if(count < 1){
			LOGGER.warn("count < 1");
			return null;
		}else if(count >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Count was "+Limits.DEFAULT_MAX_ITEMS+", using 1.");
			count = 1;
		}
		List<DataPoint> list = new ArrayList<>();
		for(int i=0;i<count;++i){
			DataPoint kw = createDataPoint(createdFilter, measurementId);
			if(kw != null){
				list.add(kw);
			}
		}
		return (list.isEmpty() ? null : list);
	}
	
	/**
	 * Return pseudo randomly generated DataPoint
	 * @param createdFilter 
	 * @param measurementId
	 * @return a datapoint
	 */
	public DataPoint createDataPoint(Set<Interval> createdFilter, String measurementId){
		DataPoint dp = new DataPoint();
		Date created = createDate(createdFilter, null);
		dp.setCreated(created);
		dp.setDataPointId(UUID.randomUUID().toString());
		
		if(measurementId == null){
			measurementId = UUID.randomUUID().toString();
		}
		
		dp.setMeasurementId(measurementId);
		dp.setDescription(_stringGenerator.generate(TEXT_LENGTH));
		dp.setKey(_stringGenerator.generate(TEXT_LENGTH));
		dp.setValue(_stringGenerator.generate(TEXT_LENGTH));
		
		return dp;
	}
	
	/**
	 * 
	 * @param intervals if null, the returned date will be random, if given an interval will be randomly selected amongst the given intervals and a new date will be generated that is within the interval
	 * @param start override the start time
	 * @return new date created by the given intervals
	 * @throws IllegalArgumentException on invalid interval and/or start time
	 */
	private Date createDate(Set<Interval> intervals, Date start) throws IllegalArgumentException{
		if(intervals != null && !intervals.isEmpty()){
			Interval interval = IterableUtils.get(intervals, _random.nextInt(intervals.size()));
			if(start == null){
				start = interval.getStart();
			}
			Date end = interval.getEnd();
			if(end.before(start)){
				throw new IllegalArgumentException("Cannot create valid date based on the given start time.");
			}
			
			return new Date(RandomUtils.nextLong(start.getTime(), end.getTime()));
		}else if(start == null){
			return new Date(RandomUtils.nextLong(0, System.currentTimeMillis()));
		}else{
			return new Date(RandomUtils.nextLong(start.getTime(), System.currentTimeMillis()));
		}
	}
	
	/**
	 * 
	 * @param task
	 * @param backendId
	 * @param taskType
	 * @return the populated base class task
	 */
	public Task setTaskDetails(Task task, long backendId, String taskType){
		if(task == null){
			return null;
		}
		
		ArrayList<TaskBackend> backends = new ArrayList<>(1);
		TaskBackend backend = new TaskBackend();
		backend.setBackendId(backendId);
		backends.add(backend);
		task.setBackends(backends);
		
		task.setCreated(new Date(System.currentTimeMillis() - Math.abs(_random.nextLong() % 31536000000L)));
		task.addTaskId(UUID.randomUUID().toString());
		HashSet<String> taskTypes = new HashSet<>(1);
		if(StringUtils.isEmpty(taskType)){
			taskTypes.add(_stringGenerator.generate(TEXT_LENGTH));
		}else{
			taskTypes.add(taskType);
		}
		task.setTaskTypes(taskTypes);
		task.setUpdated(new Date(task.getCreated().getTime() + Math.abs(_random.nextLong() % 31536000000L)));
		task.setUserId(new UserIdentity(Math.abs(_random.nextLong())));
		task.setName(_stringGenerator.generate(TEXT_LENGTH));
		task.setDescription(_stringGenerator.generate(TEXT_LENGTH));
		task.setDataVisibility(generateVisibility());
		task.setState(generateState());
		
		return task;
	}
	
	/**
	 * 
	 * @return random task visibility
	 */
	public Visibility generateVisibility() {
		Visibility[] visibilities = Visibility.values();
		return visibilities[_random.nextInt(visibilities.length)];
	}
	
	/**
	 * 
	 * @return random task state
	 */
	public State generateState() {
		State[] values = State.values();
		return values[_random.nextInt(values.length)];
	}
	
	/**
	 * 
	 * @param backendId if null, random id is generated
	 * @return random task back end
	 */
	public TaskBackend generateTaskBackend(Long backendId) {
		TaskBackend tb = new TaskBackend();
		tb.setBackendId((backendId == null ? Math.abs(_random.nextLong()) : backendId));
		tb.setMessage(_stringGenerator.generate(TEXT_LENGTH));
		Status[] values = Status.values();
		tb.setStatus(values[_random.nextInt(values.length)]);
		return tb;
	}
	
	/**
	 * @param limits 
	 * @param task
	 * @return return populated sensor task
	 */
	public Task setSensorTaskDetails(Limits limits, SensorTask task){
		List<Output> outputs = new ArrayList<>();
		for(int i=0; i<_random.nextInt(4)+1; ++i){
			Output output = new Output();
			output.setFeature(_stringGenerator.generate(5));
			outputs.add(output);
		}
		task.setOutput(outputs);
		
		task.setConditions(createConditionList(limits));
		return task;
	}
	
	/**
	 * 
	 * @param limits
	 * @return list of generated random conditions
	 */
	public List<Condition> createConditionList(Limits limits){
		int count = limits.getMaxItems(service.tut.pori.apilta.sensors.datatypes.Definitions.ELEMENT_WHEN);
		if(count < 1){
			LOGGER.warn("count < 1");
			return null;
		}else if(count >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Count was "+Limits.DEFAULT_MAX_ITEMS+", using 1.");
			count = 1;
		}
		List<Condition> list = new ArrayList<>();
		for(int i=0;i<count;++i){
			Condition condition = createCondition(limits);
			if(condition != null){
				list.add(condition);
			}
		}
		return (list.isEmpty() ? null : list);
	}
	
	/**
	 * 
	 * @param limits 
	 * @return return randomized condition
	 */
	public Condition createCondition(Limits limits){
		int count = limits.getMaxItems(service.tut.pori.apilta.sensors.datatypes.Definitions.ELEMENT_CONDITION);
		if(count < 1){
			LOGGER.warn("count < 1");
			return null;
		}else if(count >= Limits.DEFAULT_MAX_ITEMS){
			LOGGER.debug("Count was "+Limits.DEFAULT_MAX_ITEMS+", using 1.");
			count = 1;
		}
		Condition condition = new Condition();
		TreeMap<String, String> conditions = new TreeMap<>();
		for(int i=0; i<_random.nextInt(2)+1; ++i){
			conditions.put(_stringGenerator.generate(5), _stringGenerator.generate(10));
		}
		conditions.put("time/validFromToRange", Instant.now().minusSeconds(Math.abs(_random.nextLong() % 31536000L)).toString() +"/"+Instant.now().plusSeconds(Math.abs(_random.nextLong() % 31536000L)).toString());
		condition.setConditions(conditions);
		return condition;
	}

	/**
	 * 
	 * @return random GUID
	 */
	public String createGUID() {
		return UUID.randomUUID().toString();
	}
}
