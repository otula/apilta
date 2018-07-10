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
package service.tut.pori.fuzzysensors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import service.tut.pori.apilta.sensors.SensorTaskDAO;
import service.tut.pori.apilta.sensors.reference.SensorsXMLObjectCreator;
import service.tut.pori.backends.Definitions;
import service.tut.pori.fuzzysensors.frontend.FuzzyFrontendCore;
import service.tut.pori.tasks.datatypes.Task;
import service.tut.pori.tasks.datatypes.Task.State;
import service.tut.pori.tasks.datatypes.TaskBackend;
import service.tut.pori.tasks.datatypes.TaskList;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.DateIntervalParameter.Interval;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.XMLFormatter;

/**
 * very fuzzy fuzzy task dao
 * 
 */
public class FuzzyTaskDAO extends SensorTaskDAO {
	private static final Logger LOGGER = Logger.getLogger(FuzzyTaskDAO.class);
	private XMLFormatter _formatter = new XMLFormatter();
	private SensorsXMLObjectCreator _creator = new SensorsXMLObjectCreator(null);

	@Override
	public String createTask(Task task) throws IllegalArgumentException {
		LOGGER.debug(_formatter.toString(task));
		String taskId = UUID.randomUUID().toString();
		LOGGER.debug("Returning random task id: "+taskId);
		return taskId;
	}

	@Override
	public boolean updateTask(Task task) throws IllegalArgumentException {
		LOGGER.debug("Ignoring task update.");
		return true;
	}

	@Override
	public FuzzyTask getTask(Long backendId, DataGroups dataGroups, Limits limits, String taskId) {
		LOGGER.debug("Returning random task.");
		return FuzzyFrontendCore.queryTaskDetails(null, backendId, taskId, dataGroups, limits);
	}

	@Override
	public List<TaskBackend> getBackends(Limits limits, String taskId) {
		LOGGER.debug("Returning random back ends.");
		ArrayList<TaskBackend> backends = new ArrayList<>();
		int count = 0;
		if(limits == null || (count = limits.getEndItem(Definitions.ELEMENT_BACKEND_LIST)) == Limits.DEFAULT_MAX_ITEMS){
			backends.add(_creator.generateTaskBackend(null));
		}else{
			for(int i=0;i<count;++i){
				backends.add(_creator.generateTaskBackend(null));
			}			
		}
		return (backends.isEmpty() ? null : backends);
	}

	@Override
	public void removeTask(String taskId) {
		LOGGER.debug("Ignoring remove task, for task id: "+taskId);
	}

	@Override
	public boolean statusUpdated(TaskBackend backend, String taskId) {
		LOGGER.debug("Ignoring status update for back end, id: "+backend.getBackendId()+", task, id: "+taskId);
		return true;
	}

	@Override
	protected List<String> getTaskIds(UserIdentity userId) {
		LOGGER.debug("Returning null task list for user, id: "+userId.getUserId());
		return null;
	}

	@Override
	public TaskList getTaskList(long[] backendIdFilter, Collection<Interval> createdFilter, Limits limits, Collection<State> stateFilter,	long[] userIdfilter) {
		LOGGER.debug("Returning null task list.");
		return null;
	}

	@Override
	protected boolean updateTaskTimestamp(String taskId) {
		LOGGER.debug("Ignoring timestamp update for task, id: "+taskId);
		return true;
	}
}
