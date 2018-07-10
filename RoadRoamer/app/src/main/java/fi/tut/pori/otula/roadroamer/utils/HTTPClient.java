/*
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
package fi.tut.pori.otula.roadroamer.utils;

import android.util.Base64;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpMessage;
import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.utils.URIBuilder;
import cz.msebera.android.httpclient.entity.FileEntity;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;

import cz.msebera.android.httpclient.util.EntityUtils;
import fi.tut.pori.otula.roadroamer.datatypes.FileDetails;
import fi.tut.pori.otula.roadroamer.datatypes.SensorTask;
import fi.tut.pori.otula.roadroamer.datatypes.Settings;

/**
 * http client implementation for retrieving / posting sensor tasks, measurement et friends
 */
public class HTTPClient implements Closeable {
	private static final String CLASS_NAME = HTTPClient.class.toString();
	private CloseableHttpClient _client = null;
	private XMLSerializer _serializer = null;
	private Settings _settings = null;
	private XMLParser _xmlParser = null;

	/**
	 *
	 * @param settings
	 */
	public HTTPClient(Settings settings) {
		_xmlParser = new XMLParser();
		_serializer = new XMLSerializer();
		_client = HttpClients.createDefault();
		_settings = settings;
	}

    /**
	 *
	 * @param task
	 * @return response or null on failure
	 */
	public boolean taskFinished(SensorTask task) {
		StringEntity entity = new StringEntity(_serializer.toString(task), Definitions.DEFAULT_ENCODING);
		entity.setContentType(Definitions.CONTENT_TYPE_XML);

        String uriString = task.getCallbackUri();
        if(StringUtils.isBlank(uriString)) {
            Log.d(CLASS_NAME, "Callback uri was not defined, using default");
            URI uri = null;
            try {
                uri = new URIBuilder(_settings.getServiceURI()
                        + Definitions.REST_PATH
                        + Definitions.SERVICE_SENSORS
                        + Definitions.SEPARATOR_URI_PATH
                        + Definitions.METHOD_TASK_FINISHED).build();
            } catch (URISyntaxException e) {
                return false;
            }
            uriString = uri.toString();
        }
		HttpPost post = new HttpPost(uriString);
		post.setEntity(entity);
		setHTTPBasicAuthHeader(post);
        Log.d(CLASS_NAME, uriString);
		try{
            String response = _client.execute(post, new BasicResponseHandler());
            return true;
		} catch (IOException ex){
			Log.e(CLASS_NAME, "Request failed.", ex);
		}
		return false;
	}

	/**
	 *
	 * @return list of tasks or null if none was found
	 */
	public List<SensorTask> getTasks(){
        String backendId = _settings.getStringSetting(Settings.PreferenceKey.BACK_END_ID);
        URI uri;
        try {
            uri = new URIBuilder(_settings.getServiceURI()
                    + Definitions.REST_PATH
                    + Definitions.SERVICE_TASKS
                    + Definitions.SEPARATOR_URI_PATH
                    + Definitions.METHOD_GET_TASKS)
                    .addParameter(Definitions.PARAMETER_BACKEND_ID, backendId).build();
        } catch (URISyntaxException e) {
            Log.e(CLASS_NAME, "Error when building URI");
            return null;
        }
        HttpGet get = new HttpGet(uri);
        setHTTPBasicAuthHeader(get);

		List<SensorTask> tasks = null;
        Log.d(CLASS_NAME, uri.toString());
		try (CloseableHttpResponse response = _client.execute(get)) {
			StatusLine status = response.getStatusLine();
			int code = status.getStatusCode();
			if(code < 200 || code >= 300){
				Log.e(CLASS_NAME, "Failed to retrieve tasks, server responded: "+code+ " "+status.getReasonPhrase());
			}else{
				HttpEntity entity = response.getEntity();
				Set<Long> taskIds = _xmlParser.parseTaskIds(entity.getContent());
				EntityUtils.consumeQuietly(entity);

                if(taskIds != null && !taskIds.isEmpty()){
                    tasks = new ArrayList<>();
                    for(Long taskId : taskIds){
                        SensorTask task = getSensorTask(taskId);
                        if(task != null){
                            tasks.add(task);
                        }
                    }
                }
			}
		} catch (IOException ex){
			Log.e(CLASS_NAME, "Request failed.", ex);
		}
		return tasks;
	}

    /**
     *
     * @return requested task information or null if not found
     */
    private SensorTask getSensorTask(Long taskId){
        String backendId = _settings.getStringSetting(Settings.PreferenceKey.BACK_END_ID);

        URI uri;
        try {
            uri = new URIBuilder(_settings.getServiceURI()
                    + Definitions.REST_PATH
                    + Definitions.SERVICE_SENSORS
                    + Definitions.SEPARATOR_URI_PATH
                    + Definitions.METHOD_GET_TASK_DETAILS)
                    .addParameter(Definitions.PARAMETER_BACKEND_ID, backendId)
                    .addParameter(Definitions.PARAMETER_TASK_ID, taskId.toString()).build();
        } catch (URISyntaxException e) {
            Log.e(CLASS_NAME, "Error when building URI");
            return null;
        }
        HttpGet get = new HttpGet(uri);
        setHTTPBasicAuthHeader(get);

        SensorTask task = null;
        Log.d(CLASS_NAME, uri.toString());
        try (CloseableHttpResponse response = _client.execute(get)) {
            StatusLine status = response.getStatusLine();
            int code = status.getStatusCode();
            if(code < 200 || code >= 300){
                Log.e(CLASS_NAME, "Failed to retrieve tasks, server responded: "+code+ " "+status.getReasonPhrase());
            }else{
                HttpEntity entity = response.getEntity();
                task = _xmlParser.parseSensorTask(entity.getContent());
                EntityUtils.consumeQuietly(entity);
            }
        } catch (IOException ex){
            Log.e(CLASS_NAME, "Request failed.", ex);
        }
        return task;
    }

	@Override
	public void close() {
		try {
			_client.close();
		} catch (IOException ex) {
			Log.e(CLASS_NAME, "Failed to close client.", ex);
		}
	}

	/**
	 * Create and set <a href="http://tools.ietf.org/html/rfc2617">HTTP Basic Authentication</a> header based on the given values.
	 *
	 * @param message the message object to set the header to
	 * @throws IllegalArgumentException on bad values
	 */
	private void setHTTPBasicAuthHeader(HttpMessage message) throws IllegalArgumentException {
		String username = _settings.getStringSetting(Settings.PreferenceKey.USER_NAME);
		if(StringUtils.isBlank(username)){
			throw new IllegalArgumentException("Invalid or null username.");
		}

		String password = _settings.getStringSetting(Settings.PreferenceKey.USER_PASSWORD);
		if(org.apache.commons.lang3.StringUtils.isBlank(password)){ // the RFC does not exactly mention whether the password can or cannot be an empty string, but for possible compatibility reason we'll reject all empty strings
			throw new IllegalArgumentException("Invalid password");
		}

		try {
			message.setHeader("Authorization", "Basic " + Base64.encodeToString((username+":"+password).getBytes(Definitions.DEFAULT_ENCODING), Base64.DEFAULT).trim());
		} catch (UnsupportedEncodingException ex) { // should never happen
			Log.e(CLASS_NAME, "Encoding failed.", ex);
			throw new IllegalArgumentException("Failed to process the header using encoding "+Definitions.DEFAULT_ENCODING);
		}
	}

	/**
	 *
	 * @param file
	 * @return the file details for the uploaded file (as reported by the service) or null on failure
	 */
	public FileDetails uploadFile(File file) {
        String backendId = _settings.getStringSetting(Settings.PreferenceKey.BACK_END_ID);

        URI uri;
        try {
            uri = new URIBuilder(_settings.getServiceURI()
                    + Definitions.REST_PATH
                    + Definitions.SERVICE_SENSORS
                    + Definitions.SEPARATOR_URI_PATH
                    + Definitions.METHOD_CREATE_FILE)
                    .addParameter(Definitions.PARAMETER_BACKEND_ID, backendId)
                    .build();
        } catch (URISyntaxException e) {
            Log.e(CLASS_NAME, "Error when building URI");
            return null;
        }
		HttpPost post = new HttpPost(uri);
		setHTTPBasicAuthHeader(post);
		post.setEntity(new FileEntity(file));

        Log.d(CLASS_NAME, uri.toString());
		try (CloseableHttpResponse response = _client.execute(post)) {
			StatusLine status = response.getStatusLine();
			int code = status.getStatusCode();
			if(code < 200 || code >= 300){
				Log.e(CLASS_NAME, "Failed to upload file, server responded: "+code+ " "+status.getReasonPhrase());
			}else{
				HttpEntity entity = response.getEntity();
				List<FileDetails> details = _xmlParser.parseFileDetails(entity.getContent());
				EntityUtils.consumeQuietly(entity);
				if(details == null || details.isEmpty()){
					Log.w(CLASS_NAME, "No file details in the response.");
				}else{
					return details.iterator().next();
				}
			}
		} catch (IOException ex){
			Log.e(CLASS_NAME, "Request failed.", ex);
		}
		return null;
	}
}
