/**
 * Copyright 2016 Tampere University of Technology, Pori Department
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
package core.tut.pori.http.parameters;

import java.io.InputStream;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * A class for parsing time (date) intervals.
 * 
 * Any number of intervals can be given using the format:
 * 
 * ?INTERVAL_PARAMETER_NAME=ISO_DATE_START1/ISO_DATE_END1,ISO_DATE_START2/ISO_DATE_END2, ...
 * 
 * If start value is not given, the earliest possible date is used ({@link core.tut.pori.http.parameters.DateIntervalParameter#DEFAULT_START_DATE}, the unix epoch, January 1, 1970, 00:00:00 GMT).
 * If end value is not given a predefined date in the future is used ({@link core.tut.pori.http.parameters.DateIntervalParameter#DEFAULT_END_DATE}, January 1, 3000, 00:00:00 GMT).
 * 
 * That is,
 * 
 * ?INTERVAL_PARAMETER_NAME=ISO_DATE_START1,/ISO_DATE_END2
 * 
 * would equal to passing
 * 
 * ?INTERVAL_PARAMETER_NAME=ISO_DATE_START/DEFAULT_END_DATE,DEFAULT_START_DATE/ISO_DATE_END2
 * 
 * All dates must be in ISO date format (see {@link core.tut.pori.utils.StringUtils#ISOStringToDate(String)} and <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a>)
 */
public class DateIntervalParameter extends HTTPParameter {
	private static final Logger LOGGER = Logger.getLogger(DateIntervalParameter.class);
	/** default end date if value is not given. January 1, 3000, 00:00:00 GMT */
	public static final long DEFAULT_END_DATE = 32503680000000l;
	/** default start date if value is not given. January 1, 1970, 00:00:00 GMT */
	public static final long DEFAULT_START_DATE = 0;
	/** start and end date separator */
	public static final char SEPARATOR_DATES = '/';
	private LinkedHashSet<Interval> _intervals = null;

	@Override
	public void initialize(List<String> parameterValues) throws IllegalArgumentException {
		_intervals = new LinkedHashSet<>(parameterValues.size());
		for(String parameter : parameterValues) {
			initialize(parameter);
		}
	}

	@Override
	public void initialize(String parameterValue) throws IllegalArgumentException {
		if(StringUtils.isBlank(parameterValue)){
			LOGGER.debug("Ignored empty string.");
			return;
		}
		if(_intervals == null){
			_intervals = new LinkedHashSet<>(1);
		}
		
		Date start = null;
		Date end = null;
		if(!StringUtils.contains(parameterValue, SEPARATOR_DATES)){ // only start date
			start = core.tut.pori.utils.StringUtils.ISOStringToDate(parameterValue);
			end = new Date(DEFAULT_END_DATE);
		}else{
			String[] dates = StringUtils.split(parameterValue, SEPARATOR_DATES);
			if(dates.length < 1 || dates.length > 2){
				throw new IllegalArgumentException("Invalid value for parameter: "+getParameterName());
			}else if(dates.length == 1){ // only end date
				start = new Date(DEFAULT_START_DATE);
				end = core.tut.pori.utils.StringUtils.ISOStringToDate(dates[0]);
			}else{
				start = core.tut.pori.utils.StringUtils.ISOStringToDate(dates[0]);
				end = core.tut.pori.utils.StringUtils.ISOStringToDate(dates[1]);
			}
		}
		
		_intervals.add(new Interval(start, end));
	}

	/**
	 * @return true if at least one interval has been given
	 */
	@Override
	public boolean hasValues() {
		return (_intervals != null && !_intervals.isEmpty());
	}

	/**
	 * @return the first interval or null if no intervals have been given.
	 */
	@Override
	public Interval getValue() {
		return (hasValues() ? _intervals.iterator().next() : null);
	}
	
	/**
	 * 
	 * @return the intervals or null if none given
	 */
	public Set<Interval> getValues() {
		return (hasValues() ? _intervals : null);
	}
	
	@Override
	public void initialize(InputStream parameterValue) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("The use of HTTP Body is not implemented for this parameter.");
	}
	
	/**
	 * Time interval with start end end date.
	 * 
	 * An interval can have both start and end dates, or either one.
	 * If only 
	 */
	public class Interval {
		private Date _end = null;
		private Date _start = null;
		
		/**
		 * 
		 * @param start if not given, the value of {@link core.tut.pori.http.parameters.DateIntervalParameter#DEFAULT_START_DATE} is used for the date initializer.
		 * @param end if not given, the value of {@link core.tut.pori.http.parameters.DateIntervalParameter#DEFAULT_END_DATE} is used for the date initializer.
		 * @throws IllegalArgumentException if start if after end
		 */
		private Interval(Date start, Date end) throws IllegalArgumentException{
			if(end == null){
				LOGGER.debug("No end date, using default.");
				_end = new Date(DEFAULT_END_DATE);
			}else{
				_end = end;
			}
			
			if(start == null){
				LOGGER.debug("No start date, using default.");
				_start = new Date(DEFAULT_START_DATE);
			}else{
				_start = start;
			}

			if(_start.after(_end)){
				throw new IllegalArgumentException("Invalid value for parameter: "+getParameterName()+": start date must be before end date.");
			}
		}

		/**
		 * @return the end date or {@value DateIntervalParameter#DEFAULT_END_DATE}} if not given
		 */
		public Date getEnd() {
			return _end;
		}

		/**
		 * @return the start date or {@value DateIntervalParameter#DEFAULT_START_DATE}} if not given
		 */
		public Date getStart() {
			return _start;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((_end == null) ? 0 : _end.hashCode());
			result = prime * result + ((_start == null) ? 0 : _start.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Interval other = (Interval) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (_end == null) {
				if (other._end != null)
					return false;
			} else if (!_end.equals(other._end))
				return false;
			if (_start == null) {
				if (other._start != null)
					return false;
			} else if (!_start.equals(other._start))
				return false;
			return true;
		}

		/**
		 * 
		 * @return outer type
		 */
		private DateIntervalParameter getOuterType() {
			return DateIntervalParameter.this;
		}		
	} // class Interval
}
