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
package otula.backend.parking;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import core.tut.pori.utils.XMLFormatter;
import otula.backend.parking.datatypes.KML;
import otula.backend.parking.datatypes.ParkingConfiguration;
import otula.backend.parking.datatypes.ParkingPlace;
import otula.backend.parking.datatypes.Placemark;

/**
 * Client for retrieving parking place informations
 * 
 */
public class ParkingClient implements Closeable {
	private static final Logger LOGGER = Logger.getLogger(ParkingClient.class);
	private CloseableHttpClient _client = null;
	private ParkingConfiguration _configuration = null;
	private XMLFormatter _formatter = null;
	
	/**
	 * City name enumerations
	 * 
	 */
	public enum City {
		/**
		 * Parking for the City of Pori
		 */
		PORI;
		
		/**
		 * 
		 * @param configuration
		 * @return the URI for retrieving parking details using the provided configuration
		 */
		private String getUri(ParkingConfiguration configuration){
			LOGGER.warn(this+" "+configuration+" "+configuration.getUriPori());
			switch(this){
				case PORI:
					return configuration.getUriPori();
				default: // should never go here
					LOGGER.warn("Unhandeled type: "+this.toString());
					return null;
			}
		}
	} // enum City

	/**
	 * 
	 * @param configuration
	 */
	public ParkingClient(ParkingConfiguration configuration) {
		_client = HttpClients.createDefault();
		_formatter = new XMLFormatter();
		_formatter.setThrowOnError(false);
		_configuration = configuration;
		LOGGER.debug(_configuration.getUriPori());
	}
	
	@Override
	public void close() {
		try {
			_client.close();
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
		}
	}

	/**
	 * 
	 * @param cities for which the details will be retrieved
	 * @return list of parking places or null if none was found
	 */
	public List<ParkingPlace> getParkingPlaces(Set<City> cities) {
		ArrayList<ParkingPlace> places = new ArrayList<>();
		for(City city : cities){
			String uri = city.getUri(_configuration);
			LOGGER.debug("Calling URI: "+uri);
			try(CloseableHttpResponse response = _client.execute(new HttpGet(uri))) {
				int code = response.getStatusLine().getStatusCode();
				if(code < 200 || code >= 300){
					LOGGER.warn("Server responded: "+code);
				}else{
					HttpEntity entity = response.getEntity();
					if(entity == null){
						LOGGER.warn("No entity returned by the server.");
					}else{
						List<ParkingPlace> tempPlaces = null;
						try{
							switch(city){
								case PORI:
									tempPlaces = processPoriResponse(entity.getContent());
									break;
								default:
									LOGGER.warn("Unhandeled city: "+city);
									break;
							}
						} finally {
							EntityUtils.consumeQuietly(entity);
						}
						if(tempPlaces != null){
							places.addAll(tempPlaces);
						}
					}
				}
			} catch (IOException ex) {
				LOGGER.error(ex, ex);
			}
		}
		
		return (places.isEmpty() ? null : places);
	}
	
	/**
	 * 
	 * @param in
	 * @return helper method for converting the Pori parking places list to ParkingPlace objects
	 */
	private List<ParkingPlace> processPoriResponse(InputStream in) {
		List<Placemark> placemarks = _formatter.toObject(in, KML.class).getDocument().getPlacemarks();
		if(placemarks == null || placemarks.isEmpty()){
			LOGGER.debug("No placemarks returned.");
			return null;
		}
				
		List<ParkingPlace> places = new ArrayList<>(placemarks.size());
		for(Placemark pm : placemarks){
			places.add(new ParkingPlace(pm.getExtendedData().getSimpleData(), pm.getCoordinates().calculateAverageCoordinate()));
		}
		
		return places;
	}
}
