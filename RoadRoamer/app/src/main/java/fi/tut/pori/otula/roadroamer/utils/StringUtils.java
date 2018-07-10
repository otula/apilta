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
package fi.tut.pori.otula.roadroamer.utils;

import android.util.Log;

import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;


/**
 * A thread-safe utility class for processing Strings.
 */
public final class StringUtils {
	private static final String CLASS_NAME = StringUtils.class.toString();
	private static final FastDateFormat ISO_DATE = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssX", TimeZone.getTimeZone("UTC"));
	private static final String ZULU = "Z";

	/**
	 * 
	 */
	private StringUtils(){
		// nothing needed
	}

	/**
	 * This method is synchronized for the conversion
	 * 
	 * @param date
	 * @return null if null passed, otherwise the passed string in format yyyy-MM-dd'T'HH:mm:ssZ
	 */
	public static synchronized String dateToISOString(Date date){
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
	public static synchronized Date ISOStringToDate(String date) throws IllegalArgumentException{
		if(org.apache.commons.lang3.StringUtils.isBlank(date)){
			Log.d(CLASS_NAME, "Empty string passed.");
			return null;
		}
		try {
            /*
			int pointIndex = date.indexOf('.');
			if(pointIndex > 0){ // strip milliseconds 2012-05-23T10:32:20.XXXXXXZ
				if(date.endsWith(ZULU)){ // strip the tailing Z as it creates issues with simple date format
					date = date.substring(0, pointIndex)+"+0000";
				}else{
					int timeZoneIndex = date.indexOf('+', pointIndex);
					if(timeZoneIndex < 0) {  //plus sign not found, try minus just in case
                        timeZoneIndex = date.indexOf('-', pointIndex);
                    }
                    if(timeZoneIndex < 0){
						throw new IllegalArgumentException("Invalid date string: "+date);
					}
					date = date.substring(0, pointIndex) + date.substring(timeZoneIndex);
				}
			}else if(date.endsWith(ZULU)){ // strip the tailing Z as it creates issues with simple date format
				date = date.substring(0, date.length()-1)+"+0000";
			}
			*/
			return ISO_DATE.parse(date);	
		} catch (ParseException ex) {//+0300
			Log.e(CLASS_NAME, "Failed to parse date.", ex);
			throw new IllegalArgumentException("Invalid date string: "+date);
		}
	}
}
