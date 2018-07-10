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
package service.tut.pori.fuzzysensors.backend;

import java.io.IOException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import core.tut.pori.context.ServiceInitializer;
import core.tut.pori.http.Definitions;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.http.parameters.Limits;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.XMLFormatter;
import service.tut.pori.apilta.sensors.datatypes.SensorTask;
import service.tut.pori.apilta.sensors.reference.SensorsReferenceCore;
import service.tut.pori.apilta.sensors.reference.SensorsXMLObjectCreator;
import service.tut.pori.fuzzysensors.FuzzyTask;

/**
 * Fuzzy core methods for the fuzzy sensors test back end
 *
 */
public final class FuzzyBackendCore {
	private static final SensorsXMLObjectCreator CREATOR = new SensorsXMLObjectCreator(null);
	private static final DataGroups DATAGROUPS = new DataGroups(DataGroups.DATA_GROUP_ALL);
	private static final XMLFormatter FORMATTER = new XMLFormatter();
	private static final Limits LIMITS = new Limits(0, 5);
	private static final Logger LOGGER = Logger.getLogger(FuzzyBackendCore.class);
	
	/**
	 * 
	 */
	private FuzzyBackendCore() {
		// nothing needed
	}

	/**
	 * 
	 * @param authenticatedUser 
	 * @param task
	 */
	public static void addTask(UserIdentity authenticatedUser, FuzzyTask task) {
		SensorsReferenceCore.addTask(authenticatedUser, task);
		
		SensorTask results = CREATOR.generateTaskResults(task.getBackends().iterator().next().getBackendId(), DATAGROUPS, LIMITS, task.getTaskTypes().iterator().next());
		ServiceInitializer.getExecutorHandler().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try(CloseableHttpClient client = HttpClients.createDefault()){
					StringEntity content = new StringEntity(FORMATTER.toString(results), Definitions.ENCODING_UTF8);
					content.setContentType(Definitions.CONTENT_TYPE_XML);
					String uri = task.getCallbackUri();
					HttpPost post = new HttpPost(uri);
					post.setEntity(content);
					
					LOGGER.info(uri+" responded "+client.execute(post, new BasicResponseHandler()));
				} catch (IOException ex) {
					LOGGER.warn(ex, ex);
				}
			}
		});
	}
}
