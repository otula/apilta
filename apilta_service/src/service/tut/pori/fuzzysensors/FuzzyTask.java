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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.quartz.JobBuilder;

import core.tut.pori.context.ServiceInitializer;
import service.tut.pori.apilta.sensors.datatypes.SensorTask;

/**
 * fuzzy fuzzy task representation
 * 
 */
@XmlRootElement(name=service.tut.pori.tasks.Definitions.ELEMENT_TASK)
@XmlAccessorType(XmlAccessType.NONE)
public class FuzzyTask extends SensorTask {

	@Override
	public String getCallbackUri() {
		return ServiceInitializer.getPropertyHandler().getRESTBindContext()+Definitions.SERVICE_FUZZY_SENSORS_FRONTEND+"/"+service.tut.pori.tasks.Definitions.METHOD_TASK_FINISHED;
	}

	@Override
	public FuzzyTaskDAO getTaskDao() {
		return ServiceInitializer.getDAOHandler().getDAO(FuzzyTaskDAO.class);
	}

	@Override
	public JobBuilder getBuilder() {
		throw new UnsupportedOperationException("No builder for "+getClass().toString()); // prevent scheduling fuzzy tasks
	}
}
