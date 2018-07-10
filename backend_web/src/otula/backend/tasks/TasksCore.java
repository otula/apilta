/**
 * Copyright 2017 Tampere University of Technology, Pori Department
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
package otula.backend.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;

import core.tut.pori.utils.XMLFormatter;
import otula.backend.core.ServiceInitializer;
import otula.backend.tasks.datatypes.Configuration;
import otula.backend.tasks.datatypes.SensorTask;
import service.tut.pori.apilta.sensors.datatypes.DataPoint;
import service.tut.pori.apilta.sensors.datatypes.Measurement;
import service.tut.pori.apilta.sensors.datatypes.MeasurementList;
import service.tut.pori.tasks.datatypes.Task;
import service.tut.pori.tasks.datatypes.Task.State;
import service.tut.pori.tasks.datatypes.TaskBackend.Status;
import service.tut.pori.tasks.datatypes.TaskBackend;

/**
 * Core methods for tasks
 *
 */
public final class TasksCore {
	private static final Logger LOGGER = Logger.getLogger(TasksCore.class);
	
	/**
	 * 
	 */
	private TasksCore() {
		// nothing needed
	}
	
	/**
	 * 
	 * @param task
	 */
	private static void scheduleTask(SensorTask task) {
		if(!Task.isValid(task) || task.getConditions() == null){ // it is enough to check either conditions or outputs
			throw new IllegalArgumentException("Invalid task.");
		}
		
		/* TODO do things here, throw an exception on error, the code below will reply the caller with a set of example data using the quartz scheduler */
		Logger.getLogger(TasksCore.class).debug(new XMLFormatter().toString(task)); // print the entire task to debug, for example
		
		JobDataMap map = new JobDataMap();
		map.put("taskId", task.getTaskIds().iterator().next());
		map.put("callbackUri", task.getCallbackUri());
		Iterator<TaskBackend> iter = task.getBackends().iterator();
		StringBuilder ids = new StringBuilder();
		ids.append(iter.next().getBackendId());
		while(iter.hasNext()){
			ids.append(',');
			ids.append(iter.next().getBackendId());
		}
		map.put("backendIds", ids.toString());
		
		try {
			ServiceInitializer.getExecutorHandler().getScheduler().scheduleJob(JobBuilder.newJob(ExampleJob.class).setJobData(map).build(), TriggerBuilder.newTrigger().startNow().build());
		} catch (UnsupportedOperationException | SchedulerException ex) {
			LOGGER.error(ex, ex);
		}
	}
	
	/**
	 * 
	 * @param task
	 */
	private static void unscheduleTask(SensorTask task) {
		/* TODO we will simply ignore tasks without active state, we could also remove this task from the schedule */
		LOGGER.debug("Ignored task, id: "+task.getTaskIds().iterator().next()+" with state: "+task.getState());
	}
	
	/**
	 * Called when a new task is received
	 * 
	 * @param task
	 * @throws IllegalArgumentException on bad data
	 */
	public static void newTask(SensorTask task) throws IllegalArgumentException {
		String taskId = task.getTaskIds().iterator().next();
		State state = task.getState();
		if(state != State.ACTIVE){ // remove tasks that are not in the active state
			if(taskId == null){
				throw new IllegalArgumentException("Invalid task id: "+taskId);
			}
			unscheduleTask(task);
		}else{
			scheduleTask(task);
		}
	}
	
	/**
	 * An example job
	 * 
	 */
	public static class ExampleJob implements Job {

		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			/* get the data from the context and initialize the task */
			JobDataMap map = context.getMergedJobDataMap();
			SensorTask task = new SensorTask();
			task.setTaskIds(Arrays.asList(map.getString("taskId")));
			task.setCallbackUri(map.getString("callbackUri"));
			String[] ids = StringUtils.split(map.getString("backendIds"), ',');
			ArrayList<TaskBackend> ends = new ArrayList<>(ids.length);
			for(String backendId : ids){
				TaskBackend end = new TaskBackend();
				end.setBackendId(Long.valueOf(backendId));
				end.setStatus(Status.COMPLETED);
				ends.add(end);
			}
			task.setBackends(ends);
			
			/* create some example data */
			MeasurementList data = new MeasurementList();
			Measurement m = new Measurement();
			m.setBackendId(ends.iterator().next().getBackendId());
			DataPoint dp = new DataPoint();
			dp.setCreated(new Date());
			dp.setDescription("Test point");
			dp.setKey("location/point");
			dp.setValue("61.4927406,21.8008332");
			m.addDataPoint(dp);
			dp = new DataPoint();
			dp.setCreated(new Date());
			dp.setDescription("Test temperature");
			dp.setKey("sensor/temperature");
			dp.setValue("22");
			m.addDataPoint(dp);
			data.addMeasurement(m);
			task.setMeasurements(data);
			
			LOGGER.debug(new XMLFormatter().toString(task));
			
			try(TaskClient client = new TaskClient(new Configuration().initialize())){
				client.taskFinished(task);
			}
		}	
	} // class ExampleJob
}
