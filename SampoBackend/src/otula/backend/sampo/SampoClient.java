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
package otula.backend.sampo;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import otula.backend.tasks.TaskClient;

/**
 * Client for accessing the <a href="http://sampo.fmi.fi">sampo satellite images</a>
 * 
 */
public class SampoClient implements Closeable {
	private static final HashMap<Pair<String, String>, String> LOCATION_FEATURE_MAP; // location+feature / url map
	static{
		LOCATION_FEATURE_MAP = new HashMap<>(12); // there is no way to retrieve the URL map dynamically, so for simplicity, initialize it in a static fashion. We could also load the values from a configuration file.
		
		LOCATION_FEATURE_MAP.put(Pair.of(Definitions.LOCATION_SODANKYLA, Definitions.FEATURE_AEROSOL+Definitions.FEATURE_SEPARATOR+Definitions.FEATURE_COMPOSITE), "http://sampo.fmi.fi/kuvat/images/uvai_composite_1.png");
		LOCATION_FEATURE_MAP.put(Pair.of(Definitions.LOCATION_SODANKYLA, Definitions.FEATURE_AEROSOL+Definitions.FEATURE_SEPARATOR+Definitions.FEATURE_INDIVIDUAL), "http://sampo.fmi.fi/kuvat/images/AerosolIndexUV_1.png");
		
		LOCATION_FEATURE_MAP.put(Pair.of(Definitions.LOCATION_SODANKYLA, Definitions.FEATURE_CLOUD_COVER+Definitions.FEATURE_SEPARATOR+Definitions.FEATURE_COMPOSITE), "http://sampo.fmi.fi/kuvat/images/cloud_composite_1.png");
		LOCATION_FEATURE_MAP.put(Pair.of(Definitions.LOCATION_SODANKYLA, Definitions.FEATURE_CLOUD_COVER+Definitions.FEATURE_SEPARATOR+Definitions.FEATURE_INDIVIDUAL), "http://sampo.fmi.fi/kuvat/images/EffectiveCloudFraction_1.png");
		
		LOCATION_FEATURE_MAP.put(Pair.of(Definitions.LOCATION_SODANKYLA, Definitions.FEATURE_UV_DAILY+Definitions.FEATURE_SEPARATOR+Definitions.FEATURE_COMPOSITE), "http://sampo.fmi.fi/kuvat/images/dailydose_composite_1.png");
		LOCATION_FEATURE_MAP.put(Pair.of(Definitions.LOCATION_SODANKYLA, Definitions.FEATURE_UV_DAILY+Definitions.FEATURE_SEPARATOR+Definitions.FEATURE_INDIVIDUAL), "http://sampo.fmi.fi/kuvat/images/ErythemalDailyDose_1.png");
		
		LOCATION_FEATURE_MAP.put(Pair.of(Definitions.LOCATION_SODANKYLA, Definitions.FEATURE_UV_INDEX+Definitions.FEATURE_SEPARATOR+Definitions.FEATURE_COMPOSITE), "http://sampo.fmi.fi/kuvat/images/uvindex_composite_1.png");
		LOCATION_FEATURE_MAP.put(Pair.of(Definitions.LOCATION_SODANKYLA, Definitions.FEATURE_UV_INDEX+Definitions.FEATURE_SEPARATOR+Definitions.FEATURE_INDIVIDUAL), "http://sampo.fmi.fi/kuvat/images/UVindex_1.png");
		
		LOCATION_FEATURE_MAP.put(Pair.of(Definitions.LOCATION_SODANKYLA, Definitions.FEATURE_O3+Definitions.FEATURE_SEPARATOR+Definitions.FEATURE_COMPOSITE), "http://sampo.fmi.fi/kuvat/images/o3_composite_1.png");
		LOCATION_FEATURE_MAP.put(Pair.of(Definitions.LOCATION_SODANKYLA, Definitions.FEATURE_O3+Definitions.FEATURE_SEPARATOR+Definitions.FEATURE_INDIVIDUAL), "http://sampo.fmi.fi/kuvat/images/ColumnAmountO3_1.png");
		
		LOCATION_FEATURE_MAP.put(Pair.of(Definitions.LOCATION_SODANKYLA, Definitions.FEATURE_SO2+Definitions.FEATURE_SEPARATOR+Definitions.FEATURE_COMPOSITE), "http://sampo.fmi.fi/kuvat/images/so2_composite_1.png");
		LOCATION_FEATURE_MAP.put(Pair.of(Definitions.LOCATION_SODANKYLA, Definitions.FEATURE_SO2+Definitions.FEATURE_SEPARATOR+Definitions.FEATURE_INDIVIDUAL), "http://sampo.fmi.fi/kuvat/images/ColumnAmountSO2_1.png");
	}
	private static final Logger LOGGER = Logger.getLogger(SampoClient.class);
	private CloseableHttpClient _client = null;
	
	/**
	 * 
	 */
	public SampoClient() {
		_client = HttpClients.createDefault();
	}

	@Override
	public void close() {
		try {
			_client.close();
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
		_client = null;
	}
	
	/**
	 * 
	 * @param feature the requested feature
	 * @param location the location/area of the image
	 * @param path the path where to store the retrieved file
	 * @return File object representing the retrieved file or null on failure
	 */
	public File retrieveImage(String feature, String location, String path) {
		String uri = LOCATION_FEATURE_MAP.get(Pair.of(location, feature));
		if(uri == null){
			LOGGER.warn("Could not find applicaple URI for location: "+location+" and feature: "+feature);
			return null;
		}
		
		File file = null;
		try {
			file = File.createTempFile(String.valueOf(System.currentTimeMillis()), null, new File(path));
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			return null;
		}
		
		LOGGER.debug("Retrieving image from URI: "+uri);
		try(CloseableHttpResponse response = _client.execute(new HttpGet(uri)); FileOutputStream output = new FileOutputStream(file)) {
			if(!TaskClient.checkForError(response)){
				LOGGER.warn("Failed to retrieve image from URI: "+uri);
				return null;
			}
			HttpEntity entity = response.getEntity();
			try{
				IOUtils.copy(entity.getContent(), output);
			} finally {
				EntityUtils.consumeQuietly(entity);
			}
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			file.delete();
			return null;
		}
		
		return file;
	}
	
	/**
	 * 
	 * @param area
	 * @param feature
	 * @return true if the area/feature pair is valid and can be used with this client
	 */
	public static boolean validate(String area, String feature) {
		return LOCATION_FEATURE_MAP.containsKey(Pair.of(area, feature));
	}
}
