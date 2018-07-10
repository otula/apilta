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

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import core.tut.pori.dao.DAO;
import core.tut.pori.utils.StringUtils;

/**
 * The DAO handler.
 * 
 * This class can be used to retrieve instances of the DAOs defined by the database context configuration and sub-classed DAO class implementations.
 *
 */
public class DAOHandler {
	private static final String DATABASE_CONFIGURATION_FILE = "database-context.xml";
	private static final Logger LOGGER = Logger.getLogger(DAOHandler.class);
	private ClassPathXmlApplicationContext _context = null;
	
	/**
	 * 
	 * @throws BeansException
	 */
	public DAOHandler() throws BeansException{
		initialize();
	}
	
	/**
	 * 
	 * @throws BeansException
	 */
	private void initialize() throws BeansException{
		LOGGER.debug("Initializing handler...");
		Date started = new Date();
		_context = new ClassPathXmlApplicationContext(ServiceInitializer.getConfigHandler().getConfigFilePath()+DATABASE_CONFIGURATION_FILE);

		LOGGER.debug("DAO Handler initialized in "+StringUtils.getDurationString(started, new Date()));
	}
	
	/**
	 * close the handler and release all resources
	 */
	public void close(){
		if(_context != null){
			_context.close();
			_context = null;
		}
	}
	
	/**
	 * Note: the comparison is done using exactly the given class, no super or sub class of the type will be returned.
	 * 
	 * Do NOT close or cleanup the instances returned by this method, the initialization and destruction is handled automatically.
	 * 
	 * @param cls
	 * @return the dao or null if none exists
	 */
	public <T extends DAO> T getDAO(Class<T> cls){
		try{
			for(T candidate : _context.getBeansOfType(cls).values()){
				if(candidate.getClass().equals(cls)){
					return candidate;
				}
			}
		} catch (BeansException ex){
			LOGGER.warn(ex, ex);		
		}
		return null;
	}
}
