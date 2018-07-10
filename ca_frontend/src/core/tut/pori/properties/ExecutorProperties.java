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
package core.tut.pori.properties;

import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Settings for the system executor and quartz scheduler.
 */
public class ExecutorProperties extends SystemProperty {
	/* properties */
	private static final String PROPERTY_CORE_PORI_EXECUTOR_CORE_COUNT = PROPERTY_CORE_PORI_EXECUTOR+".core_count";
	private static final String PROPERTY_CORE_PORI_EXECUTOR_KEEP_ALIVE = PROPERTY_CORE_PORI_EXECUTOR+".thread_keep_alive";
	private static final String PROPERTY_CORE_PORI_EXECUTOR_QUEUE_SIZE = PROPERTY_CORE_PORI_EXECUTOR+".queue_size";
	private static final String PROPERTY_CORE_PORI_EXECUTOR_POOL_SIZE = PROPERTY_CORE_PORI_EXECUTOR+".thread_count";
	private int _coreCount = 1;
	private long _keepAlive = 60;
	private int _queueSize = 10;
	private int _poolSize = 10;
	
	@Override
	public void initialize(Properties properties) throws IllegalArgumentException {
		try{
			_coreCount = Integer.parseInt(properties.getProperty(PROPERTY_CORE_PORI_EXECUTOR_CORE_COUNT));
			_keepAlive = Long.parseLong(properties.getProperty(PROPERTY_CORE_PORI_EXECUTOR_KEEP_ALIVE));
			_queueSize = Integer.parseInt(properties.getProperty(PROPERTY_CORE_PORI_EXECUTOR_QUEUE_SIZE));
			_poolSize = Integer.parseInt(properties.getProperty(PROPERTY_CORE_PORI_EXECUTOR_POOL_SIZE));
		}catch (NumberFormatException ex){
			Logger.getLogger(getClass()).error(ex, ex);
			throw new IllegalArgumentException("Bad "+PROPERTY_CORE_PORI_EXECUTOR_POOL_SIZE+", "+PROPERTY_CORE_PORI_EXECUTOR_QUEUE_SIZE+", "+PROPERTY_CORE_PORI_EXECUTOR_CORE_COUNT+" OR "+PROPERTY_CORE_PORI_EXECUTOR_KEEP_ALIVE);
		}
	}

	/**
	 * @return the coreCount
	 */
	public int getCoreCount() {
		return _coreCount;
	}

	/**
	 * @return the keepAlive, in seconds
	 */
	public long getKeepAlive() {
		return _keepAlive;
	}

	/**
	 * @return the queueSize
	 */
	public int getQueueSize() {
		return _queueSize;
	}

	/**
	 * @return the poolSize
	 */
	public int getPoolSize() {
		return _poolSize;
	}
}
