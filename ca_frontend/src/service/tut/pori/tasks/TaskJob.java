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
package service.tut.pori.tasks;

import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import core.tut.pori.context.ServiceInitializer;
import service.tut.pori.tasks.datatypes.Task;
import service.tut.pori.tasks.datatypes.TaskBackend;

/**
 * abstract base class that can be used to initialize basic variables of a generic task job (task identifier, task dao and back ends)
 *
 */
public abstract class TaskJob implements Job {
	private static final String JOB_KEY_TASK_ID = "taskJobTaskId";
	private static final Logger LOGGER = Logger.getLogger(TaskJob.class);
	private List<TaskBackend> _backends = null;
	private TaskDAO _taskDAO = null;
	private String _taskId = null;
	
	/**
	 * @return the backends
	 */
	protected List<TaskBackend> getBackends() {
		return _backends;
	}
	
	/**
	 * @return the taskDAO
	 */
	protected TaskDAO getTaskDAO() {
		return _taskDAO;
	}
	
	/**
	 * @return the taskId
	 */
	protected String getTaskId() {
		return _taskId;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		if(!initialize(context)){
			LOGGER.warn("Failed to start task, id: "+_taskId);
			return;
		}
		LOGGER.debug("Starting task, id: "+_taskId);
		executeTask();
		LOGGER.debug("Task started, task id: "+_taskId);
	}
	
	/**
	 * called to initialize this task, remember to call this method (super) if you override it
	 * 
	 * @param context
	 * @return true on success
	 */
	protected boolean initialize(JobExecutionContext context) {
		_taskId = context.getMergedJobDataMap().getString(JOB_KEY_TASK_ID);
		Class<? extends TaskDAO> daoClass = ServiceInitializer.getDAOHandler().getDAO(TaskDAO.class).getDAOClass(_taskId);
		if(daoClass == null){
			LOGGER.warn("Failed to start task, id: "+_taskId+". Task does not exist.");
			return false;
		}
		
		_taskDAO = ServiceInitializer.getDAOHandler().getDAO(daoClass);
		
		_backends = _taskDAO.getBackends(null, _taskId);
		if(_backends == null){
			LOGGER.warn("No back ends for task, id: "+_taskId);
			return false;
		}
		
		return true;
	}
	
	/**
	 * called after task initialization
	 */
	abstract protected void executeTask();
	
	/**
	 * This method will return a builder initialized with the default values (a single task identifier) set in the task. 
	 * 
	 * This basic implementation only sets the task identifier.
	 * 
	 * Note: the given task must have a single unique identifier.
	 * 
	 * @param task the task used for initialization
	 * @param taskClass class of the task
	 * @return job builder
	 * @throws IllegalArgumentException if the task contains invalid data for builder initialization
	 */
	protected static JobBuilder getBuilder(Task task, Class<? extends TaskJob> taskClass)  throws IllegalArgumentException {
		List<String> taskIds = task.getTaskIds();
		if(taskIds == null || taskIds.size() != 1){
			throw new IllegalArgumentException("Task id is missing, or multiple identifiers.");
		}
		JobBuilder jb = JobBuilder.newJob(taskClass);
		JobDataMap data = new JobDataMap();
		data.put(JOB_KEY_TASK_ID, taskIds.iterator().next());
		jb.setJobData(data);
		return jb;
	}
}
