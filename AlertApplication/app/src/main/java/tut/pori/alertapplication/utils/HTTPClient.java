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
package tut.pori.alertapplication.utils;

import android.location.Location;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpMessage;
import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.FileEntity;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;

import cz.msebera.android.httpclient.util.EntityUtils;
import tut.pori.alertapplication.datatypes.Alert;
import tut.pori.alertapplication.datatypes.FileDetails;
import tut.pori.alertapplication.datatypes.Settings;
import tut.pori.alertapplication.datatypes.UserIdentity;

/**
 * http client implementation for retrieving / posting alerts
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
		_settings =  settings;
	}

	/**
	 * this will use the (report) alert group ids found in the settings
	 *
	 * @param alert
	 * @return id for the added alert or null on failure
	 * @throws IllegalStateException if required settings are missing
	 */
	public String addAlert(Alert alert) throws IllegalStateException {
		Collection<String> alertGroupIds = _settings.getReportAlertGroupIds();
		if(alertGroupIds == null || alertGroupIds.isEmpty()){
			throw new IllegalStateException("Invalid or missing alert group ids for reporting new alerts.");
		}
        
		StringEntity entity = new StringEntity(_serializer.toString(alert), Definitions.DEFAULT_ENCODING);
		entity.setContentType(Definitions.CONTENT_TYPE_XML);

		StringBuilder uri = new StringBuilder();
		uri.append(_settings.getServiceURI());
        uri.append(Definitions.SERVICE_ALERTS);
		uri.append(Definitions.SEPARATOR_URI_PATH);
		uri.append(Definitions.METHOD_ADD_ALERT);

		uri.append(Definitions.SEPARATOR_URI_METHOD_PARAMS);
		uri.append(Definitions.PARAMETER_ALERT_GROUP_ID);
		uri.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR);
		Iterator<String> iterator = alertGroupIds.iterator();
		uri.append(iterator.next());
		while(iterator.hasNext()){
			uri.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
			uri.append(iterator.next());
		}

        String uriString = uri.toString();
		HttpPost post = new HttpPost(uriString);
		post.setEntity(entity);
		setHTTPBasicAuthHeader(post);
        Log.d(CLASS_NAME, uriString);
		try(CloseableHttpResponse response = _client.execute(post)){
			StatusLine status = response.getStatusLine();
			int code = status.getStatusCode();
			if(code < 200 || code >= 300){
				Log.e(CLASS_NAME, "Failed to add alert, server responded: "+code+ " "+status.getReasonPhrase());
			}else{
				HttpEntity responseEntity = response.getEntity();
				List<Alert> alerts = _xmlParser.parseAlerts(responseEntity.getContent());
				EntityUtils.consumeQuietly(entity);
				if(alerts == null){
					Log.w(CLASS_NAME, "Status "+code+" returned no alerts.");
				}else{
					return alerts.iterator().next().getAlertId();
				}
			}
		} catch (IOException ex){
			Log.e(CLASS_NAME, "Request failed.", ex);
		}

		return null;
	}

	/**
	 * this will use the range, (listen) alert type and (listen) group ids found in the settings (if present)
	 *
	 * @param location
	 * @param since if given, only alerts created after the given timestamp are returned
	 * @return list of alerts or null if none was found
	 */
	public List<Alert> getAlerts(Location location, Date since){
		StringBuilder uri = new StringBuilder();
		uri.append(_settings.getServiceURI());
        uri.append(Definitions.SERVICE_ALERTS);
		uri.append(Definitions.SEPARATOR_URI_PATH);
		uri.append(Definitions.METHOD_GET_ALERTS);

		uri.append(Definitions.SEPARATOR_URI_METHOD_PARAMS);
		uri.append(Definitions.PARAMETER_LOCATION);
		uri.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR);
		uri.append(Definitions.PARAMETER_LOCATION_COORDINATE);
		uri.append(Definitions.SEPARATOR_URI_QUERY_TYPE_VALUE);
		uri.append(location.getLatitude());
		uri.append(Definitions.SEPARATOR_COORDINATES);
		uri.append(location.getLongitude());
		if(location.hasBearing()){
			uri.append(Definitions.SEPARATOR_URI_QUERY_TYPE_VALUE);
			uri.append(Definitions.PARAMETER_LOCATION_HEADING);
			uri.append(Definitions.SEPARATOR_URI_QUERY_TYPE_VALUE);
			uri.append(location.getBearing());
		}

		uri.append(Definitions.SEPARATOR_URI_QUERY_PARAMS);
		uri.append(Definitions.PARAMETER_RANGE);
		uri.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR);
		uri.append(_settings.getRange());

        uri.append(Definitions.SEPARATOR_URI_QUERY_PARAMS);
        uri.append(Definitions.PARAMETER_DATA_GROUPS);
        uri.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR);
        uri.append(Definitions.DATA_GROUPS_ALL);

		Collection<String> alertGroupIds = _settings.getListenAlertGroupIds();
		if(alertGroupIds != null && !alertGroupIds.isEmpty()){
			uri.append(Definitions.SEPARATOR_URI_QUERY_PARAMS);
			uri.append(Definitions.PARAMETER_ALERT_GROUP_ID);
			uri.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR);
			Iterator<String> iterator = alertGroupIds.iterator();
			uri.append(iterator.next());
			while(iterator.hasNext()){
				uri.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
				uri.append(iterator.next());
			}
		}

		Collection<Alert.AlertType> alertTypes = _settings.getListenAlertTypes();
		if(alertTypes != null && !alertTypes.isEmpty()){
			uri.append(Definitions.SEPARATOR_URI_QUERY_PARAMS);
			uri.append(Definitions.PARAMETER_ALERT_TYPE);
			uri.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR);
			Iterator<Alert.AlertType> iterator = alertTypes.iterator();
			uri.append(iterator.next().toAlertTypeString());
			while(iterator.hasNext()){
				uri.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);
				uri.append(iterator.next().toAlertTypeString());
			}
		}

		if(since != null){
			uri.append(Definitions.SEPARATOR_URI_QUERY_PARAMS);
			uri.append(Definitions.PARAMETER_CREATED);
			uri.append(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE_SEPARATOR);
			uri.append(tut.pori.alertapplication.utils.StringUtils.dateToISOString(since).replace("+","%2B"));
		}

        String uriString = uri.toString();
		HttpGet get = new HttpGet(uriString);
		setHTTPBasicAuthHeader(get);
		List<Alert> alerts = null;
        Log.d(CLASS_NAME,uriString);
		try (CloseableHttpResponse response = _client.execute(get)) {
			StatusLine status = response.getStatusLine();
			int code = status.getStatusCode();
			if(code < 200 || code >= 300){
				Log.e(CLASS_NAME, "Failed to retrieve alerts, server responded: "+code+ " "+status.getReasonPhrase());
			}else{
				HttpEntity entity = response.getEntity();
				alerts = _xmlParser.parseAlerts(entity.getContent());
				EntityUtils.consumeQuietly(entity);
			}
		} catch (IOException ex){
			Log.e(CLASS_NAME, "Request failed.", ex);
		}

		return alerts;
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
		String username = _settings.getUsername();
		if(StringUtils.isBlank(username)){
			throw new IllegalArgumentException("Invalid or null username.");
		}

		String password = _settings.getPassword();
		if(org.apache.commons.lang3.StringUtils.isBlank(password)){ // the RFC does not exactly mention whether the password can or cannot be an empty string, but for possible compatibility reason we'll reject all empty strings
			throw new IllegalArgumentException("Invalid password : "+password);
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
	 * @return currently active settings
	 * @see #setSettings(Settings)
	 */
	public Settings getSettings() {
		return _settings;
	}

	/**
	 *
	 * @param settings set currently active settings
	 * @see #getSettings()
	 */
	public void setSettings(Settings settings) {
		_settings = settings;
	}

    /**
     *
     * @return user details
     */
	public UserIdentity getUserDetails() {
        StringBuilder uri = new StringBuilder(_settings.getServiceURI());
        uri.append(Definitions.SERVICE_USER);
        uri.append(Definitions.SEPARATOR_URI_PATH);
        uri.append(Definitions.METHOD_GET_USER_DETAILS);
        String uriString = uri.toString();
        HttpGet get = new HttpGet(uriString);
        setHTTPBasicAuthHeader(get);

        UserIdentity userId = null;
        Log.d(CLASS_NAME, uriString);
        try (CloseableHttpResponse response = _client.execute(get)) {
            StatusLine status = response.getStatusLine();
            int code = status.getStatusCode();
            if(code < 200 || code >= 300){
                Log.e(CLASS_NAME, "Failed to retrieve user details, server responded: "+code+ " "+status.getReasonPhrase());
            }else{
                HttpEntity entity = response.getEntity();
                userId = _xmlParser.parseUserIdentity(entity.getContent());
                EntityUtils.consumeQuietly(entity);
            }
        } catch (IOException ex){
            Log.e(CLASS_NAME, "Request failed.", ex);
        }
        return userId;
    }

	/**
	 *
	 * @param file
	 * @return the file details for the uploaded file (as reported by the service) or null on failure
	 */
	public FileDetails uploadFile(File file) {
		StringBuilder uri = new StringBuilder(_settings.getServiceURI());
		uri.append(Definitions.SERVICE_ALERTS);
		uri.append(Definitions.SEPARATOR_URI_PATH);
		uri.append(Definitions.METHOD_CREATE_FILE);
        String uriString = uri.toString();
		HttpPost post = new HttpPost(uriString);
		setHTTPBasicAuthHeader(post);
		post.setEntity(new FileEntity(file));

        Log.d(CLASS_NAME, uriString);
		try (CloseableHttpResponse response = _client.execute(post)) {
			StatusLine status = response.getStatusLine();
			int code = status.getStatusCode();
			if(code < 200 || code >= 300){
				Log.e(CLASS_NAME, "Failed to retrieve alerts, server responded: "+code+ " "+status.getReasonPhrase());
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
