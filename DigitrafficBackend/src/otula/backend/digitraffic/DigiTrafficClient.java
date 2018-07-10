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
package otula.backend.digitraffic;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import core.tut.pori.http.Definitions;
import core.tut.pori.utils.JSONFormatter;
import otula.backend.digitraffic.datatypes.FeatureCollection;
import otula.backend.digitraffic.datatypes.Properties;
import otula.backend.digitraffic.datatypes.WeatherData;
import otula.backend.digitraffic.datatypes.WeatherStationProperties;

/**
 * 
 * client for accessing the Digitraffic service
 */
public class DigiTrafficClient implements Closeable { 
	private static final Logger LOGGER = Logger.getLogger(DigiTrafficClient.class);
	private static final String SERVICE_ADDRESS = "https://tie.digitraffic.fi";
	private static final String SERVICE_URI_DATA = SERVICE_ADDRESS+"/api/v1/data";
	private static final String SERVICE_URI_METADATA = SERVICE_ADDRESS+"/api/v1/metadata";
	private static final String SERVICE_URI_METHOD_WEATHER_DATA = SERVICE_URI_DATA+"/weather-data";
	private static final String SERVICE_URI_METHOD_WEATHER_STATIONS = SERVICE_URI_METADATA+"/weather-stations";
	private CloseableHttpClient _client = null;
	private Gson _weatherDeserializer = null;
	
	/**
	 * 
	 */
	public DigiTrafficClient(){
		initialize();
	}
	
	/**
	 * initialize member variables
	 */
	private void initialize() {
		_client = HttpClients.createDefault();
		GsonBuilder builder = new GsonBuilder();
		JSONFormatter.registerDateAdapters(builder);
		builder.registerTypeAdapter(Properties.class, new JsonDeserializer<WeatherStationProperties>() {

			@Override
			public WeatherStationProperties deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				return context.deserialize(json, WeatherStationProperties.class);
			}
		});
		_weatherDeserializer = builder.create();
	}

	@Override
	public void close() {
		try {
			_client.close();
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
		_client = null;
		_weatherDeserializer = null;
	}
	
	/**
	 * 
	 * @return list of features for the weather stations or null if no stations found
	 */
	public FeatureCollection getWeatherStations() {
		LOGGER.debug("Calling GET "+SERVICE_URI_METHOD_WEATHER_STATIONS);
		try(CloseableHttpResponse response = _client.execute(new HttpGet(SERVICE_URI_METHOD_WEATHER_STATIONS))){
			int code = response.getStatusLine().getStatusCode();
			if(code < 200 || code >= 300){
				LOGGER.warn("Server responded with code: "+code);
				return null;
			}
			
			HttpEntity entity = response.getEntity();
			if(entity == null){
				LOGGER.warn("Server did not return response entity.");
				return null;
			}
			
			try (InputStreamReader in = new InputStreamReader(entity.getContent())){
				return _weatherDeserializer.fromJson(in, FeatureCollection.class);
			} finally {
				EntityUtils.consumeQuietly(entity);
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
		return null;
	}
	
	/**
	 * 
	 * @param roadStationId if null, returns data for all stations in the service
	 * @return the weather data or null if nothing was found
	 */
	public WeatherData getWeatherData(Integer roadStationId){
		String uri = SERVICE_URI_METHOD_WEATHER_DATA;
		if(roadStationId != null){
			StringBuilder sb = new StringBuilder();
			sb.append(SERVICE_URI_METHOD_WEATHER_DATA);
			sb.append(Definitions.SEPARATOR_URI_PATH);
			sb.append(roadStationId);
			uri = sb.toString();
		}
		
		LOGGER.debug("Calling GET "+uri);
		try(CloseableHttpResponse response = _client.execute(new HttpGet(uri))){
			int code = response.getStatusLine().getStatusCode();
			if(code < 200 || code >= 300){
				LOGGER.warn("Server responded with code: "+code);
				return null;
			}
			
			HttpEntity entity = response.getEntity();
			if(entity == null){
				LOGGER.warn("Server did not return response entity.");
				return null;
			}
			
			try (InputStreamReader in = new InputStreamReader(entity.getContent())){
				WeatherData data = _weatherDeserializer.fromJson(in, WeatherData.class);
				return (WeatherData.isEmpty(data) ? null : data);
			} finally {
				EntityUtils.consumeQuietly(entity);
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
		return null;
	}
}
