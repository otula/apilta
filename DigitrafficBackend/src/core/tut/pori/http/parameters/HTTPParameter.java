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
package core.tut.pori.http.parameters;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import core.tut.pori.http.Definitions;

/**
 * Base class for custom HTTP Parameters.
 * 
 * Even though the built-in parameter types should work for most cases, there might rise a need for a case-specific way to parse a given method parameter, this class is for those needs.
 */
public abstract class HTTPParameter {
	private String _parameterName = null;

	/**
	 * The inherited class should have no-args default constructor
	 */
	public HTTPParameter(){
		// nothing needed
	}

	/**
	 * Initialize the parameter by raw URL encoded string.
	 * 
	 * This can be overridden to intercept the URL encoded parameter, the default implementation decodes the string and calls initialize()
	 * 
	 * @param parameterValue
	 * @throws IllegalArgumentException
	 */
	public void initializeRaw(String parameterValue) throws IllegalArgumentException {
		try {
			initialize(URLDecoder.decode(parameterValue, Definitions.ENCODING_UTF8));
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(HTTPParameter.class).error(ex);	// use local variable for logger, as this exception should never be thrown
			throw new IllegalArgumentException("Failed to decode input for parameter: "+getParameterName());	// if it does happen, abort here
		}
	}

	/**
	 * Initialize the parameter by raw URL encoded string list.
	 * 
	 * This can be overridden to intercept the URL encoded parameter, the default implementation decodes the string and calls initialize()
	 * 
	 * @param parameterValues
	 * @throws IllegalArgumentException
	 */
	public void initializeRaw(List<String> parameterValues) throws IllegalArgumentException {
		List<String> retval = new ArrayList<>(parameterValues.size());	// preserve the original
		try {
			for(Iterator<String> iter = parameterValues.iterator();iter.hasNext();){
				retval.add(URLDecoder.decode(iter.next(), Definitions.ENCODING_UTF8));
			}
			initialize(retval);
		} catch (UnsupportedEncodingException ex) {	// this should not happen
			Logger.getLogger(HTTPParameter.class).error(ex);	// use local variable for logger, as this exception should never be thrown
			throw new IllegalArgumentException("Failed to decode input for parameter: "+getParameterName());	// if it does happen, abort here
		}
	}

	/**
	 * Initialize this parameter based on the given parameter values
	 * 
	 * @param parameterValues can NOT be null
	 * @throws IllegalArgumentException on bad input data
	 */
	public abstract void initialize(List<String> parameterValues) throws IllegalArgumentException;

	/**
	 * Initialize this parameter based on the given parameter value
	 * 
	 * @param parameterValue can be null
	 * @throws IllegalArgumentException on bad input data
	 */
	public abstract void initialize(String parameterValue) throws IllegalArgumentException;

	/**
	 * Initialize this parameter based on input stream
	 * 
	 * Override this method if you want to accept HTTP Body data.
	 * 
	 * The default implementation only throws an exception.
	 * 
	 * @param parameterValue can be null
	 * @throws IllegalArgumentException on bad input data
	 */
	public void initialize(InputStream parameterValue) throws IllegalArgumentException{
		throw new UnsupportedOperationException("The use of HTTP Body is not implemented for this parameter.");
	}

	/**
	 * 
	 * @return true if this parameter has one or more values
	 */
	public abstract boolean hasValues();

	/**
	 * 
	 * @return a single value object or null if none available
	 */
	public abstract Object getValue();

	/**
	 * @return the parameterName
	 */
	public String getParameterName() {
		return _parameterName;
	}

	/**
	 * @param parameterName the parameterName to set
	 */
	public void setParameterName(String parameterName) {
		_parameterName = parameterName;
	}
}
