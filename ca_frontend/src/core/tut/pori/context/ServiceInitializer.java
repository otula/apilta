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
package core.tut.pori.context;

import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import core.tut.pori.properties.PropertyHandler;
import core.tut.pori.utils.StringUtils;

/**
 * Initializes and destroys the web application (service) context, and loads all other handlers during context initialization.
 * 
 * This class can be used to retrieve instances of various handlers and classes instantiated by the handlers.
 * 
 * One should never close the handlers or the instances returned by the handlers manually, as they are managed automatically.
 * The handlers themselves are generally immutable and no modifications to the initialized contexts are possible.
 *
 */
public class ServiceInitializer implements ServletContextListener{
	private static ConfigHandler CONFIG_HANDLER = null;
	private static DAOHandler DAO_HANDLER = null;
	private static EventHandler EVENT_HANDLER = null;
	private static ExecutorHandler EXECUTOR_HANDLER = null;
	private static Logger LOGGER = null;
	private static PropertyHandler PROPERTY_HANDLER = null;
	private static ServiceHandler SERVICE_HANDLER = null;
	
	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		if(EXECUTOR_HANDLER != null){	// on failed initialization this may be null
			EXECUTOR_HANDLER.close();
			EXECUTOR_HANDLER = null;
		}
		if(SERVICE_HANDLER != null){	// on failed initialization this may be null
			SERVICE_HANDLER.close();
			SERVICE_HANDLER = null;
		}
		if(DAO_HANDLER != null){	// on failed initialization this may be null
			DAO_HANDLER.close();
			DAO_HANDLER = null;
		}
		if(PROPERTY_HANDLER != null){	// on failed initialization this may be null
			PROPERTY_HANDLER.close();
			PROPERTY_HANDLER = null;
		}
		if(EVENT_HANDLER != null){	// on failed initialization this may be null
			EVENT_HANDLER.close(); // close as last the last one in case the other handler send something on destruction
			EVENT_HANDLER = null;
		}
		LOGGER.info("Context destroyed.");
		LOGGER = null;
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		Date started = new Date();
		ServletContext context = servletContextEvent.getServletContext();
		CONFIG_HANDLER = new ConfigHandler(context);	//config handler must be first
		LOGGER = Logger.getLogger(ServiceInitializer.class);
		PROPERTY_HANDLER = new PropertyHandler(context);
		DAO_HANDLER = new DAOHandler();
		SERVICE_HANDLER = new ServiceHandler();
		EXECUTOR_HANDLER = new ExecutorHandler();
		EVENT_HANDLER = new EventHandler();
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
	 * @return Property handler instance. Note that you should NOT close the instance, it will be automatically closed.
	 */
	public static PropertyHandler getPropertyHandler(){
		return PROPERTY_HANDLER;
	}
	
	/**
	 * 
	 * @return Service handler instance. Note that you should NOT close the instance, it will be automatically closed.
	 */
	public static ServiceHandler getServiceHandler(){
		return SERVICE_HANDLER;
	}
	
	/**
	 * 
	 * @return DAO handler instance. Note that you should NOT close the instance, it will be automatically closed.
	 */
	public static DAOHandler getDAOHandler(){
		return DAO_HANDLER;
	}
	
	/**
	 * 
	 * @return scheduler handler instance. Note: you should not close the instance, it will be closed automatically.
	 */
	public static ExecutorHandler getExecutorHandler(){
		return EXECUTOR_HANDLER;
	}
	
	/**
	 * 
	 * @return event handler instance. Note: you should not close the instance, it will be closed automatically.
	 */
	public static EventHandler getEventHandler(){
		return EVENT_HANDLER;
	}
	
	/**
	 * 
	 * @return session handler instance. Note: you should not close the instance, it will be closed automatically.
	 */
	public static SessionHandler getSessionHandler(){
		return SessionHandler.getSessionHandler();
	}
}
