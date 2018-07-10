/**
 * Copyright 2014 Tampere University of Technology, Pori Department
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
package service.tut.pori.tasks;

import java.util.Date;

import org.apache.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import otula.backend.core.ServiceInitializer;
import core.tut.pori.utils.StringUtils;

/**
 * Handles system executors. 
 * 
 * This class can be used to retrieve instances of the quartz scheduler.
 * 
 * One should not initialize this handler directly, as an instantiated version is available from ServiceInitializer.
 */
public class ExecutorHandler {
	private static final Logger LOGGER = Logger.getLogger(ExecutorHandler.class);
	private static final String QUARTZ_CONFIGURATION_FILE = "quartz.properties";
	private static final String QUARTZ_SYSTEM_PROPERTY = "org.quartz.properties";
	private Scheduler _scheduler = null;

	/**
	 * 
	 */
	public void close() {
		if(_scheduler != null){
			try {
				_scheduler.shutdown(true);
			} catch (SchedulerException ex) {
				LOGGER.error(ex, ex);
			}
		}
	}

	/**
	 * 
	 * @throws IllegalArgumentException
	 */
	public ExecutorHandler() throws IllegalArgumentException{
		LOGGER.debug("Initializing handler...");
		Date started = new Date();
		System.setProperty(QUARTZ_SYSTEM_PROPERTY, ServiceInitializer.getConfigHandler().getPropertyFilePath()+QUARTZ_CONFIGURATION_FILE);	// load quartz configuration
		try {
			_scheduler  = StdSchedulerFactory.getDefaultScheduler();
			_scheduler.start();
		} catch (SchedulerException ex) {
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to initialize Scheduler.");
		}
		
		LOGGER.debug("Executor Handler initialized in "+StringUtils.getDurationString(started, new Date()));
	}
	
	/**
	 * Note: you should not run long-running background tasks, which may reserve the scheduler for a long time.
	 * If you need to do multiple functions, split the task to separate Jobs, and use triggers to launch new jobs.
	 * Especially, do NOT use sleep(), if you need to wait, schedule your job to re-run again at a later date.
	 * 
	 * Do NOT close or cleanup the instances returned by this method, the initialization and destruction is handled automatically.
	 * 
	 * @return quartz scheduler for background jobs
	 */
	public Scheduler getScheduler(){
		return _scheduler;
	}
}
