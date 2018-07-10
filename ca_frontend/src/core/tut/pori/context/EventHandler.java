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

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.ResolvableType;

import core.tut.pori.utils.StringUtils;

/**
 * This class can be used to submit application events to the defined listeners. 
 * 
 * Listeners are defined by event context configuration and the defined annotations.
 * 
 * One should not initialize this handler directly, as an instantiated version is available from ServiceInitializer.
 *
 */
public class EventHandler {
	private static final Logger LOGGER = Logger.getLogger(EventHandler.class);
	private static final String SERVLET_CONFIGURATION_FILE = "event-context.xml";
	private ClassPathXmlApplicationContext _context = null;

	/**
	 * 
	 * @throws BeansException
	 */
	public EventHandler() throws BeansException{
		initialize();
	}
	
	/**
	 * 
	 * @throws BeansException on failure
	 */
	private void initialize() throws BeansException{
		LOGGER.debug("Initializing handler...");
		Date started = new Date();
		_context = new ClassPathXmlApplicationContext(ServiceInitializer.getConfigHandler().getConfigFilePath()+SERVLET_CONFIGURATION_FILE);

		LOGGER.debug("Event Handler initialized in "+StringUtils.getDurationString(started, new Date()));
	}

	/**
	 * close this Service handler and release are resources associated with it
	 */
	public void close(){
		_context.close();
		_context = null;
	}
	
	/**
	 * 
	 * @param event
	 */
	public void publishEvent(ApplicationEvent event){
		_context.publishEvent(event);
	}
	
	/**
	 * Implementation of an asynchronous event multicaster.
	 * 
	 * The default event delivery system will block whilst each event listener is in progress. 
	 * This class sends all events simultaneously to all receivers, though in practice the performance of this method is limited by the current load of the scheduler service.
	 */
	public static class EventMulticaster extends SimpleApplicationEventMulticaster {

		@Override
		public void multicastEvent(final ApplicationEvent event) {
			multicastEvent(event, null);
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void multicastEvent(final ApplicationEvent event, ResolvableType type) {
			Collection<ApplicationListener<?>> listeners = getApplicationListeners(event, (type == null ? ResolvableType.forClass(event.getClass()) : type));
			if(listeners.isEmpty()){
				LOGGER.debug("No listeners for event "+event.getClass().toString());
				return;
			}
			ExecutorHandler handler = ServiceInitializer.getExecutorHandler();
			if(handler == null){
				LOGGER.debug("No executor handler available. Ignoring multicast.");
				syncCast(listeners, event);
				return;
			}
			ExecutorService executor = handler.getExecutor();
			if(executor == null){
				LOGGER.debug("No executor available. Ignoring multicast.");
				syncCast(listeners, event);
				return;
			}
			for(final ApplicationListener listener : listeners){
				executor.execute(new Runnable() {
					@SuppressWarnings("unchecked")
					@Override
					public void run() {
						try {
							listener.onApplicationEvent(event);
						} catch (Throwable ex){
							LOGGER.error(ex, ex);
						}
					}
				});
			}
		}

		/**
		 * blocking fallback method for cases when executor is not available
		 * 
		 * @param listeners list of listeners applicable for the given event type
		 * @param event the event to send
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private void syncCast(Collection<ApplicationListener<?>> listeners, ApplicationEvent event) {
			LOGGER.debug("Using synchronous fallback method for event delivery...");
			for(ApplicationListener listener : listeners){
				try {
					listener.onApplicationEvent(event);
				} catch (Throwable ex){
					LOGGER.error(ex, ex);
				}
			}
		}	
	} //  class EventMulticaster
}
