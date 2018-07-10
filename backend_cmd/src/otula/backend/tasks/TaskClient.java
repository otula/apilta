/**
 * Copyright 2017 Tampere University of Technology, Pori Department
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
package otula.backend.tasks;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import core.tut.pori.http.Definitions;
import core.tut.pori.http.Response;
import core.tut.pori.http.parameters.DataGroups;
import core.tut.pori.utils.XMLFormatter;
import otula.backend.tasks.datatypes.Configuration;
import otula.backend.tasks.datatypes.SensorTask;
import otula.backend.tasks.datatypes.SensorTaskList;
import service.tut.pori.apilta.files.datatypes.FileDetails;
import service.tut.pori.apilta.files.datatypes.FileDetailsList;
import service.tut.pori.tasks.datatypes.Task;

/**
 * HTTP client for performing the basic task related actions
 *
 */
public class TaskClient implements Closeable {
	private static final String CONTENT_TYPE = "text/xml; charset=utf-8";
	private static final String HEADER_AUTHORIZATION = "Authorization";
	private static final String HEADER_AUTHORIZATION_PREFIX = "Basic ";
	private static final String HEADER_CONTENT_TYPE = "Content-Type";
	private static final Logger LOGGER = Logger.getLogger(TaskClient.class);
	private static final char SEPARATOR_DATES = '/';
	private CloseableHttpClient _client = null;
	private Configuration _configuration = null;
	private XMLFormatter _formatter = null;
	
	/**
	 * 
	 * @param configuration
	 */
	public TaskClient(Configuration configuration) {
		_configuration = configuration;
		initialize();
	}
	
	/**
	 * helper method for initializing the member variables
	 */
	private void initialize(){
		_formatter = new XMLFormatter();
		_client = HttpClients.custom().useSystemProperties().setRetryHandler(new DefaultHttpRequestRetryHandler(1, false)).build();
	}

	@Override
	public void close() {
		try {
			_client.close();
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
		_client = null;
		_formatter = null;
	}
	
	/**
	 * 
	 * @param task
	 * @return true if request finished successfully
	 * @throws IllegalArgumentException on invalid task
	 */
	public boolean taskFinished(Task task) throws IllegalArgumentException {
		if(!Task.isValid(task)){
			throw new IllegalArgumentException("Invalid Task.");
		}
		
		String uri = task.getCallbackUri();
		HttpPost post = new HttpPost(uri);
		post.setEntity(new StringEntity(_formatter.toString(task), core.tut.pori.http.Definitions.CHARSET_UTF8));
		post.setHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE);
		setAuthenticationHeader(post);
		LOGGER.debug("Calling URI: "+uri);
		try (CloseableHttpResponse response = _client.execute(post)) {
			if(!checkForError(response)){
				LOGGER.warn("Failed to execute request: POST "+uri);
				return false;
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @param backendId 
	 * @param file
	 * @return the details of the file created on the service or null on failure
	 */
	public FileDetails createFile(Long backendId, File file) {
		String uri = _configuration.getServiceAddress()+service.tut.pori.apilta.sensors.Definitions.SERVICE_SENSORS+Definitions.SEPARATOR_URI_PATH+service.tut.pori.apilta.sensors.Definitions.METHOD_CREATE_FILE+Definitions.SEPARATOR_URI_METHOD_PARAMS+service.tut.pori.tasks.Definitions.PARAMETER_BACKEND_ID+Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE+backendId;
		HttpPost post = new HttpPost(uri);
		post.setEntity(new FileEntity(file));
		setAuthenticationHeader(post);
		LOGGER.debug("Calling URI: "+uri);
		try(CloseableHttpResponse response = _client.execute(post)) {
			if(checkForError(response)){	
				HttpEntity entity = response.getEntity();
				try{
					Response r = _formatter.toResponse(entity.getContent(), FileDetailsList.class);
					FileDetailsList list = (FileDetailsList) r.getResponseData();
					if(FileDetailsList.isEmpty(list)){
						LOGGER.warn("Empty file details list returned by the service.");
					}else{
						return list.getFiles().iterator().next(); // return the first one
					}
				}finally{
					EntityUtils.consumeQuietly(entity);
				}
			}else{
				LOGGER.warn("Failed to execute request: POST "+uri);
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
		return null;
	}
	
	/**
	 * This is a helper method for checked the error condition of a response.
	 * This method will also print the response to warning log (consuming the payload) if an error is found.
	 * 
	 * @param response
	 * @return true if the response is clean (no error condition)
	 */
	public static boolean checkForError(HttpResponse response) {
		int code = response.getStatusLine().getStatusCode();
		if(code < 200 || code >= 300){
			String content = null;
			HttpEntity entity = response.getEntity();
			if(entity != null){
				try {
					content = IOUtils.toString(entity.getContent(), Definitions.CHARSET_UTF8);
				} catch (UnsupportedOperationException | IOException ex) {
					LOGGER.error(ex, ex);
				} finally {
					EntityUtils.consumeQuietly(entity);
				}
			}
			LOGGER.warn("Server responded with code: "+code+"\n Content: \n"+content);
			return false;
		}else{
			LOGGER.debug("Server responded with code: "+code);
			return true;
		}
	}
	
	/**
	 * generate and set the HTTP basic header in the request
	 * 
	 * <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986</a>
	 * 
	 * @param request
	 */
	protected void setAuthenticationHeader(HttpRequest request) {
		request.setHeader(HEADER_AUTHORIZATION, HEADER_AUTHORIZATION_PREFIX+javax.xml.bind.DatatypeConverter.printBase64Binary((_configuration.getUsername()+":"+_configuration.getPassword()).getBytes()));
	}
	
	/**
	 * 
	 * @param backendId
	 * @param state optional state filter
	 * @param since optional created filter
	 * @param until optional created filter
	 * @return possibly empty list of tasks or null on failure (e.g. HTTP or network error)
	 */
	public SensorTaskList getTasks(Long backendId, String state, Date since, Date until) {
		StringBuilder uri = new StringBuilder(); 
		uri.append(_configuration.getServiceAddress());
		uri.append(service.tut.pori.tasks.Definitions.SERVICE_TASKS);
		uri.append(Definitions.SEPARATOR_URI_PATH);
		uri.append(service.tut.pori.tasks.Definitions.METHOD_RETRIEVE_TASKS);
		
		uri.append(Definitions.SEPARATOR_URI_METHOD_PARAMS);
		uri.append(service.tut.pori.tasks.Definitions.PARAMETER_BACKEND_ID);
		uri.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE);
		uri.append(backendId);
		
		if(!StringUtils.isBlank(state)){
			LOGGER.debug("Adding state filter...");
			uri.append(Definitions.SEPARATOR_URI_QUERY_PARAMS);
			uri.append(service.tut.pori.tasks.Definitions.PARAMETER_TASK_STATE);
			uri.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE);
			uri.append(state);
		}
		
		if(since != null || until != null){
			LOGGER.debug("Adding created filter...");
			uri.append(Definitions.SEPARATOR_URI_QUERY_PARAMS);
			uri.append(service.tut.pori.tasks.Definitions.PARAMETER_CREATED);
			uri.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE);
			
			if(since != null){
				uri.append(core.tut.pori.utils.StringUtils.dateToISOString(since));
			}
			if(until != null){
				uri.append(SEPARATOR_DATES);
				uri.append(core.tut.pori.utils.StringUtils.dateToISOString(until));
			}
		}

		String uriString = uri.toString();
		HttpGet get = new HttpGet(uriString);
		setAuthenticationHeader(get);
		LOGGER.debug("Calling URI: "+uriString);
		try(CloseableHttpResponse response = _client.execute(get)) {
			if(checkForError(response)){	
				HttpEntity entity = response.getEntity();
				try{
					Response r = _formatter.toResponse(entity.getContent(), SensorTaskList.class);
					SensorTaskList list = (SensorTaskList) r.getResponseData();
					if(list == null){
						list = new SensorTaskList();
					}
					return list;
				}finally{
					EntityUtils.consumeQuietly(entity);
				}
			}else{
				LOGGER.warn("Failed to execute request: GET "+uri);
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
		return null;
	}
	
	/**
	 * 
	 * @param backendId
	 * @param taskId
	 * @param dataGroups optional data groups
	 * @return the sensor task or null if not found (or error occurred)
	 */
	public SensorTask queryTaskDetails(Long backendId, String taskId, DataGroups dataGroups) {
		StringBuilder uri = new StringBuilder(); 
		uri.append(_configuration.getServiceAddress());
		uri.append(service.tut.pori.apilta.sensors.Definitions.SERVICE_SENSORS);
		uri.append(Definitions.SEPARATOR_URI_PATH);
		uri.append(service.tut.pori.tasks.Definitions.METHOD_QUERY_TASK_DETAILS);
		
		uri.append(Definitions.SEPARATOR_URI_METHOD_PARAMS);
		uri.append(service.tut.pori.tasks.Definitions.PARAMETER_BACKEND_ID);
		uri.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE);
		uri.append(backendId);
		
		uri.append(Definitions.SEPARATOR_URI_QUERY_PARAMS);
		uri.append(service.tut.pori.tasks.Definitions.PARAMETER_TASK_ID);
		uri.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE);
		uri.append(taskId);
		
		if(!DataGroups.isEmpty(dataGroups)){
			LOGGER.debug("Adding data group filter...");
			uri.append(Definitions.SEPARATOR_URI_QUERY_PARAMS);
			uri.append(DataGroups.PARAMETER_DEFAULT_NAME);
			uri.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE);
			uri.append(dataGroups.toDataGroupString());
		}

		String uriString = uri.toString();
		HttpGet get = new HttpGet(uriString);
		setAuthenticationHeader(get);
		LOGGER.debug("Calling URI: "+uriString);
		try(CloseableHttpResponse response = _client.execute(get)) {
			if(checkForError(response)){	
				HttpEntity entity = response.getEntity();
				try{
					return (SensorTask) _formatter.toResponse(entity.getContent(), SensorTask.class).getResponseData();
				}finally{
					EntityUtils.consumeQuietly(entity);
				}
			}else{
				LOGGER.warn("Failed to execute request: GET "+uri);
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
		return null;
	}
}
