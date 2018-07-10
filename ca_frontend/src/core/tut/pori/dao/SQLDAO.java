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
package core.tut.pori.dao;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.mysql.jdbc.AbandonedConnectionCleanupThread;

/**
 * Base class for SQL DAOs.
 * 
 * http://dev.mysql.com/doc/refman/5.0/en/connector-j-usagenotes-spring-config.html
 * http://dev.mysql.com/doc/refman/5.0/en/connector-j-usagenotes-spring-config-jdbctemplate.html
 * 
 * Subclassing this class will automatically add the new class to DAOHandler, and will be retrievable
 * run-time from ServiceInitializer.getDAOHandler().getDAO(...)
 */
public abstract class SQLDAO implements DAO {
	/* databases */
	/** default database schema */
	protected static final String DATABASE = "ca_frontend";
	/* common columns */
	/** COUNT(*) definition */
	protected static final String COLUMN_COUNT = "COUNT(*)";
	/** default column name for GUIDs */
	protected static final String COLUMN_GUID = "guid";
	/** default column name for row created */
	protected static final String COLUMN_ROW_CREATED = "row_created";
	/** default column name for row updated */
	protected static final String COLUMN_ROW_UPDATED = "row_updated";
	/** default column name for user id */
	protected static final String COLUMN_USER_ID = "user_id";
	/* sql strings */
	/** SQL string for retrieving the last generated row id */
	protected static final String SQL_SELECT_LAST_INSERT_ID = "SELECT LAST_INSERT_ID()";
	/* member variables */
	private JdbcTemplate _jdbcTemplate = null;
	private TransactionTemplate _transactionTemplate = null;
	
	/**
	 * 
	 * @param dataSource
	 */
	@Autowired
	public void setDataSource(DataSource dataSource){
		_jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	/**
	 * 
	 * @param tnxManager
	 */
	@Autowired
	public void setTnxManager(DataSourceTransactionManager tnxManager){
		_transactionTemplate = new TransactionTemplate(tnxManager);
	}

	/**
	 * @return the jdbcTemplate
	 */
	protected JdbcTemplate getJdbcTemplate() {
		return _jdbcTemplate;
	}
	
	/**
	 * Note: the template is shared amongst all instances that subclass SQLDAO. It is thread-safe, though you should not
	 * change template settings if you are not absolutely sure what you are doing. If a more specific configuration instance
	 * is required, you should instantiate new TransactionTemplate.
	 * 
	 * @return the transactionTemplate
	 */
	public TransactionTemplate getTransactionTemplate() {
		return _transactionTemplate;
	}

	/**
	 * Helper method for checking if the given column is a COUNT(*) column
	 * 
	 * @param columnName
	 * @param value
	 * @return the value of the count column or < 0 if not an count column
	 */
	protected long checkCountColumn(String columnName, Object value){
		if(!COLUMN_COUNT.equalsIgnoreCase(columnName)){
			return -1;
		}
		return (Long)value;
	}
	
	/**
	 * Cleans up the SQL driver after context has been closed
	 *
	 * Automatically instantiated by Spring as a bean.
	 */
	@SuppressWarnings("unused")
	private static class ContextClosedEventListener implements ApplicationListener<ContextClosedEvent>{
		private static final Logger LOGGER = Logger.getLogger(ContextClosedEventListener.class);
		
		@Override
		public void onApplicationEvent(ContextClosedEvent event) {
			LOGGER.debug("Cleaning up SQL connections...");
			
			try {
				AbandonedConnectionCleanupThread.shutdown(); //MySQL specific clean up http://bugs.mysql.com/bug.php?id=65909
			} catch (InterruptedException ex) {
				 LOGGER.error(ex, ex);
			}
			
			Enumeration<Driver> drivers = DriverManager.getDrivers();
	        while (drivers.hasMoreElements()) { // terminate the driver
	            Driver driver = drivers.nextElement();
	            try {
	            	LOGGER.debug("Deregistering SQL driver: "+driver.getClass());
	                DriverManager.deregisterDriver(driver);
	            } catch (SQLException ex) {
	               LOGGER.error(ex, ex);
	            }
	        }
		}
	} // class ContextClosedEventListener
}
