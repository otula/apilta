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
package otula.backend.core;

import java.util.Date;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import service.tut.pori.tasks.ExecutorHandler;
import core.tut.pori.context.ConfigHandler;
import core.tut.pori.utils.StringUtils;

/**
 * Initializes and destroys the web application context
 *
 */
public class ServiceInitializer implements ServletContextListener {
	private static ConfigHandler CONFIG_HANDLER = null;
	private static ExecutorHandler EXECUTOR_HANDLER = null;
	private static Logger LOGGER = null;

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		if(EXECUTOR_HANDLER != null){	// on failed initialization this may be null
			EXECUTOR_HANDLER.close();
			EXECUTOR_HANDLER = null;
		}
			
		LOGGER.info("Context destroyed.");
		LOGGER = null;
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		Date started = new Date();
		CONFIG_HANDLER = new ConfigHandler(event.getServletContext());	//config handler must be first
		LOGGER = Logger.getLogger(ServiceInitializer.class);
		EXECUTOR_HANDLER = new ExecutorHandler();
		LOGGER.info("Context initialized in "+StringUtils.getDurationString(started, new Date()));
	}
	
	/**
	 * 
	 * @return Config handler instance.
	 */
	public static ConfigHandler getConfigHandler(){
		return CONFIG_HANDLER;
	}
	
	/**
	 * 
	 * @return scheduler handler instance. Note: you should not close the instance, it will be closed automatically.
	 */
	public static ExecutorHandler getExecutorHandler(){
		return EXECUTOR_HANDLER;
	}
}
