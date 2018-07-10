/**
 * Copyright 2018 Tampere University of Technology, Pori Department
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
package tut.pori.shockapplication.utils;

import android.util.Base64;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import cz.msebera.android.httpclient.HttpMessage;
import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import tut.pori.shockapplication.datatypes.Settings;
import tut.pori.shockapplication.datatypes.ShockMeasurement;

/**
 *
 */
public class ConnectionHandler implements Closeable {
	private static final String TAG = ConnectionHandler.class.toString();
	private CloseableHttpClient _client = null;
	private XMLSerializer _serializer = null;
	private Settings _settings = null;

	/**
	 *
	 * @param settings
	 */
	public ConnectionHandler(Settings settings) {
		_client = HttpClients.createDefault();
		_serializer = new XMLSerializer();
		_settings = settings;
	}

	@Override
	public void close() {
		try {
			_client.close();
		} catch (IOException ex) {
			Log.e(TAG, "Failed to close client.", ex);
		}
	}

    /**
     *
     * @return currently active settings
     */
    public Settings getSettings() {
        return _settings;
    }

    /**
     *
     * @param settings
     */
    public void setSettings(Settings settings) {
        _settings = settings;
    }

    /**
	 *
	 * @param measurements
	 * @return true on success
	 * @throws IllegalArgumentException on bad input data
	 */
	public boolean send(Collection<ShockMeasurement> measurements) throws IllegalArgumentException {
		if(measurements == null || measurements.isEmpty()){
			throw new IllegalArgumentException("Null or empty measurement list.");
		}

		StringEntity entity = new StringEntity(_serializer.toString(measurements), Definitions.DEFAULT_ENCODING);
		entity.setContentType(Definitions.CONTENT_TYPE_XML);
		String uri = _settings.getServiceURI()+Definitions.SERVICE_SHOCK+Definitions.SEPARATOR_URI_PATH+Definitions.METHOD_CREATE_MEASUREMENT;
		Log.d(TAG, "Calling POST "+uri);
		HttpPost post = new HttpPost(uri);
		post.setEntity(entity);
		setHTTPBasicAuthHeader(post);

		try(CloseableHttpResponse response = _client.execute(post)){
			StatusLine status = response.getStatusLine();
			int code = status.getStatusCode();
			if(code < 200 || code >= 300){
				Log.e(TAG, "Failed to send measurements, server responded: "+code+ " "+status.getReasonPhrase());
				return false;
			}else{
				Log.d(TAG, "Measurements sent.");
			}
		} catch (IOException ex){
			Log.e(TAG, "Request failed.", ex);
			return false;
		}

		return true;
	}

	/**
	 * Create and set <a href="http://tools.ietf.org/html/rfc2617">HTTP Basic Authentication</a> header based on the given values.
	 *
	 * @param message the message object to set the header to
	 * @throws IllegalArgumentException on bad values
	 */
	private void setHTTPBasicAuthHeader(HttpMessage message) throws IllegalArgumentException {
		String username = _settings.getUsername();
		if(org.apache.commons.lang3.StringUtils.isBlank(username)){
			throw new IllegalArgumentException("Invalid or null username.");
		}

		String password = _settings.getPassword();
		if(org.apache.commons.lang3.StringUtils.isBlank(password)){ // the RFC does not exactly mention whether the password can or cannot be an empty string, but for possible compatibility reason we'll reject all empty strings
			throw new IllegalArgumentException("Invalid password : "+password);
		}

		try {
			message.setHeader("Authorization", "Basic " + Base64.encodeToString((username+":"+password).getBytes(Definitions.DEFAULT_ENCODING), Base64.DEFAULT).trim());
		} catch (UnsupportedEncodingException ex) { // should never happen
			Log.e(TAG, "Encoding failed.", ex);
			throw new IllegalArgumentException("Failed to process the header using encoding "+Definitions.DEFAULT_ENCODING);
		}
	}
}