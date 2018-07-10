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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.quartz.JobBuilder;
import org.quartz.JobExecutionContext;

import core.tut.pori.http.Response;
import core.tut.pori.utils.XMLFormatter;
import service.tut.pori.tasks.datatypes.Task;
import service.tut.pori.tasks.datatypes.TaskBackend;
import service.tut.pori.tasks.datatypes.TaskBackend.Status;

/**
 * job for sending task to back ends and updating the back end status into the database
 * 
 */
public class BackendTaskJob extends TaskJob {
	private static final Logger LOGGER = Logger.getLogger(BackendTaskJob.class);
	private XMLFormatter _formatter = null;
	
	@Override
	protected boolean initialize(JobExecutionContext context) {
		_formatter = new XMLFormatter();
		return super.initialize(context);
	}
	
	@Override
	protected void executeTask() {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			String taskId = getTaskId();
			TaskDAO taskDAO = getTaskDAO();
			for(TaskBackend backend : getBackends()){
				Long backendId = backend.getBackendId();
				if(!backend.isEnabled()){
					LOGGER.warn("Ignoring disabled back end, id: "+backendId+" for task, id: "+taskId);
					continue;
				}
				String analysisUri = backend.getAnalysisUri();
				if(StringUtils.isBlank(analysisUri)){ // this is not necessarily an error (or a problem)
					LOGGER.debug("Ignored back end that does not have analysis uri defined, id: "+backendId+" for task, id: "+taskId);
					continue;
				}

				Task task = taskDAO.getTask(backendId, backend.getDefaultTaskDataGroups(), null, taskId);
				String url = analysisUri+Definitions.METHOD_ADD_TASK;
				HttpPost taskRequest = new HttpPost(url);
				taskRequest.setHeader("Content-Type", "text/xml; charset=UTF-8");
				taskRequest.setEntity(new StringEntity((_formatter).toString(task), core.tut.pori.http.Definitions.ENCODING_UTF8));
				
				LOGGER.debug("Task, id: "+taskId+", back-end id: "+backendId+". Sending "+Definitions.METHOD_ADD_TASK+" to URL: "+url);
				try(CloseableHttpResponse response = client.execute(taskRequest)){
					StatusLine sl = response.getStatusLine();
					int sc = sl.getStatusCode();
					LOGGER.debug("Back end, id: "+backendId+" responded to task, id: "+taskId+" : "+sc+" "+sl.getReasonPhrase());
					
					if(sc < 200 || sc >= 300){
						backend.setStatus(Status.ERROR);
					}else{
						backend.setStatus(Status.EXECUTING);
					}
					
					Response r = parseResponse(response);
					if(r == null){
						LOGGER.warn("Failed to parse response from back end, id: "+backendId+", task, id: "+taskId);
						backend.setStatus(Status.UNKNOWN);
					}else{
						core.tut.pori.http.Response.Status status = r.getStatus();
						backend.setMessage((status == null ? "null : " : status.name()+ " : ")+r.getMessage());
					}			
				} catch (IOException ex) {
					LOGGER.error(ex, ex);
					backend.setStatus(Status.ERROR);
					backend.setMessage(Definitions.METHOD_ADD_TASK+" failed for url: "+url);
				}
				taskDAO.statusUpdated(backend, taskId); // update new status for the back end
			}
		}catch (Throwable ex) {	// catch all exceptions to prevent re-scheduling on error
			LOGGER.error(ex, ex);
		}
	}
	
	/**
	 * 
	 * @param response
	 * @return the response parsed from the given response object or null if the response does not contain valid data
	 */
	protected Response parseResponse(CloseableHttpResponse response) {
		HttpEntity entity = response.getEntity();
		if(entity == null){
			LOGGER.debug("Did not receive response body for task, id: "+getTaskId());
			return null;
		}
		try(InputStream in = entity.getContent()){
			return _formatter.toObject(in, Response.class);
		} catch (UnsupportedOperationException | IOException | IllegalArgumentException ex) {
			LOGGER.error(ex, ex);
		} finally {
			EntityUtils.consumeQuietly(entity);
		}
		return null;
	}

	/**
	 * This method will return a builder initialized with the default values set in the task. 
	 * 
	 * This basic implementation only sets the task identifier.
	 * 
	 * Note: the given task must have a single unique identifier.
	 * 
	 * @param task the task used for initialization
	 * @return job builder
	 * @throws IllegalArgumentException if the task contains invalid data for builder initialization
	 */
	public static JobBuilder getBuilder(Task task)  throws IllegalArgumentException {
		return getBuilder(task, BackendTaskJob.class);
	}
}
