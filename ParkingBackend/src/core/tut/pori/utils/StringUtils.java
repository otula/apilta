/**
 * Copyright 2014 Tampere University of Technology, Pori Department
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
package core.tut.pori.utils;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.log4j.Logger;


/**
 * A thread-safe utility class for processing Strings.
 */
public final class StringUtils {
	private static final FastDateFormat ISO_DATE = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssX", TimeZone.getTimeZone("UTC"));
	private static final Logger LOGGER = Logger.getLogger(StringUtils.class);

	/**
	 * 
	 */
	private StringUtils(){
		// nothing needed
	}

	/**
	 * 
	 * @param started
	 * @param finished
	 * @return string representation of the time interval between the given dates
	 */
	public static String getDurationString(Date started, Date finished){		
		long duration_ms = finished.getTime() - started.getTime();
		long ms = ((duration_ms % 86400000) % 3600000) % 1000;
		long s = (((duration_ms % 86400000) % 3600000) % 60000) /1000;
		long min = ((duration_ms % 86400000) % 3600000)/60000;
		long h = (duration_ms % 86400000)/3600000;
		StringBuilder sb = new StringBuilder();
		if(h > 0){
			sb.append(String.valueOf(h));
			sb.append(" h");
		}
		if(min > 0){
			if(sb.length() > 0)
				sb.append(" ");
			sb.append(String.valueOf(min));
			sb.append(" min");
		}
		if(s > 0){
			if(sb.length() > 0)
				sb.append(" ");
			sb.append(String.valueOf(s));
			sb.append(" s");
		}
		if(ms > 0){
			if(sb.length() > 0)
				sb.append(" ");
			sb.append(String.valueOf(ms));
			sb.append(" ms");
		}
		if(sb.length() < 1){
			sb.append(" less than 1 ms");
		}
		return sb.toString();
	}

	/**
	 * This method is synchronized for the conversion
	 * 
	 * @param date
	 * @return null if null passed, otherwise the passed string in format yyyy-MM-dd'T'HH:mm:ssZ
	 */
	public static String dateToISOString(Date date){
		return (date == null ? null : ISO_DATE.format(date));
	}
	
	/**
	 * Note: this method will accept microseconds, but the actual microsecond values will be ignored,
	 * passing 2012-05-23T10:32:20.000000Z equals to passing 2012-05-23T10:32:20Z. 
	 * 
	 * Both Z and +XXXX are accepted timezone formats.
	 * 
	 * @param date the passed string in format yyyy-MM-dd'T'HH:mm:ssZ
	 * @return the given string as ISODate or null if null or empty string was passed
	 * @throws IllegalArgumentException on invalid string
	 */
	public static Date ISOStringToDate(String date) throws IllegalArgumentException{
		if(org.apache.commons.lang3.StringUtils.isBlank(date)){
			LOGGER.debug("Empty string passed.");
			return null;
		}
		try {
			return ISO_DATE.parse(date);	
		} catch (ParseException ex) {//+0300
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Invalid date string: "+date);
		}
	}
	
	/**
	 * 
	 * @param sb
	 * @param array array of items to append, if empty or null, this is a no-op
	 * @param separator separator used for the join operation, if empty or null the values will appended without separator
	 * @return the passed builder
	 * @throws IllegalArgumentException on bad data
	 */
	public static StringBuilder append(StringBuilder sb, int[] array, String separator) throws IllegalArgumentException{
		if(sb == null){
			throw new IllegalArgumentException("null builder.");
		}
		if(ArrayUtils.isEmpty(array)){
			LOGGER.debug("Ignored empty array.");
			return sb;
		}
		
		boolean hasSeparator = !org.apache.commons.lang3.StringUtils.isBlank(separator);
		
		sb.append(array[0]);
		for(int i=1;i<array.length;++i){
			if(hasSeparator){
				sb.append(separator);
			}
			sb.append(array[i]);
		}
		return sb;
	}
	
	/**
	 * 
	 * @param sb
	 * @param array array of items to append, if empty or null, this is a no-op
	 * @param separator separator used for the join operation, if empty or null the values will appended without separator
	 * @return the passed builder
	 * @throws IllegalArgumentException on bad data
	 */
	public static StringBuilder append(StringBuilder sb, long[] array, String separator) throws IllegalArgumentException{
		if(sb == null){
			throw new IllegalArgumentException("null builder.");
		}
		if(ArrayUtils.isEmpty(array)){
			LOGGER.debug("Ignored empty array.");
			return sb;
		}
		
		boolean hasSeparator = !org.apache.commons.lang3.StringUtils.isBlank(separator);
		
		sb.append(array[0]);
		for(int i=1;i<array.length;++i){
			if(hasSeparator){
				sb.append(separator);
			}
			sb.append(array[i]);
		}
		return sb;
	}
}
