package service.tut.pori.apilta.shock.datatypes;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import core.tut.pori.http.parameters.HTTPParameter;

/**
 * For parsing latitude/longitude limit parameters
 * 
 * Format:
 * {@value service.tut.pori.apilta.shock.datatypes.LocationLimits#PARAMETER_DEFAULT_NAME}=lat{@value core.tut.pori.http.Definitions#SEPARATOR_URI_QUERY_TYPE_VALUE}lon{@value core.tut.pori.http.Definitions#SEPARATOR_URI_QUERY_PARAM_VALUES}lat{@value core.tut.pori.http.Definitions#SEPARATOR_URI_QUERY_TYPE_VALUE}lon
 * 
 * The decimal separator for latitude and longitude is .
 * 
 * The first coordinate is the lower left corner of the bounding box, the second coordinate is the upper right corner.
 * 
 */
public class LocationLimits extends HTTPParameter {
	/** parameter default name */
	public static final String PARAMETER_DEFAULT_NAME = "location_limits";
	private static final Logger LOGGER = Logger.getLogger(LocationLimits.class);
	private LatLng _lowerLeft = null;
	private LatLng _upperRight = null;

	@Override
	public void initialize(List<String> parameterValues) throws IllegalArgumentException {
		if(parameterValues.size() != 2){
			throw new IllegalArgumentException("Two parameters are required.");
		}
		
		try {
		Iterator<String> iter = parameterValues.iterator();
		String value = iter.next();
		String[] parts = StringUtils.split(value, core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_TYPE_VALUE);
		_lowerLeft = new LatLng(Double.valueOf(parts[0]), Double.valueOf(parts[1]));
		
		value = iter.next();
		parts = StringUtils.split(value, core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_TYPE_VALUE);
		_upperRight = new LatLng(Double.valueOf(parts[0]), Double.valueOf(parts[1]));
		
		} catch (NumberFormatException ex) {
			LOGGER.debug(ex, ex);
			throw new IllegalArgumentException("Invalid value for parameter: "+getParameterName());
		}
	}

	@Override
	public void initialize(String parameterValue) throws IllegalArgumentException {
		throw new IllegalArgumentException("Two parameters are required.");
	}

	@Override
	public boolean hasValues() {
		return (_lowerLeft != null && _upperRight != null);
	}

	/**
	 * Return Pair of lower left/upper right coordinates
	 * 
	 * @see #getLowerLeft()
	 * @see #getUpperRight()
	 */
	@Override
	public Pair<LatLng, LatLng> getValue() {
		if(hasValues()){
			return Pair.of(getLowerLeft(), getUpperRight());
		}else{
			return null;
		}
	}
	
	/**
	 * 
	 * @return lower left corner bounding box coordinate
	 */
	public LatLng getLowerLeft() {
		return _lowerLeft;
	}
	
	/**
	 * 
	 * @return upper right corner bounding box coordinate
	 */
	public LatLng getUpperRight() {
		return _upperRight;
	}
	
	/**
	 * 
	 * 
	 */
	public static class LatLng {
		private Double _latitude = null;
		private Double _longitude = null;
		
		/**
		 * 
		 * @param latitude
		 * @param longitude
		 */
		public LatLng(Double latitude, Double longitude) {
			_latitude = latitude;
			_longitude = longitude;
		}

		/**
		 * @return the latitude
		 */
		public Double getLatitude() {
			return _latitude;
		}

		/**
		 * @return the longitude
		 */
		public Double getLongitude() {
			return _longitude;
		}
	} // class LatLng
}
