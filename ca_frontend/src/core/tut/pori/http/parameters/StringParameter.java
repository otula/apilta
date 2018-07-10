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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;


/**
 * A simple class for String parameters
 *
 */
public class StringParameter extends HTTPParameter {
	private static final Logger LOGGER = Logger.getLogger(StringParameter.class);
	private String _value = null;
	private List<String> _values = null;

	@Override
	public void initialize(List<String> parameterValues) throws IllegalArgumentException {
		_values = parameterValues;
		_value = null;
	}
	
	/**
	 * create empty, non-initializes StringParameter
	 */
	public StringParameter() {
		// nothing needed
	}

	/**
	 * 
	 * @return list of values or null if none
	 */
	public List<String> getValues(){
		if(_value != null){	// lazily initialize the array if requested for a single parameter
			_values = new ArrayList<>(1);
			_values.add(_value);
			_value =  null;
			return _values;
		}else{
			return _values;
		}
	}
	
	/**
	 * 
	 * @return first (or the only one) of the values or null if none available
	 */
	@Override
	public String getValue(){
		if(_value != null){
			return _value;
		}else if(_values != null){
			return _values.get(0);
		}else{
			return null;
		}
	}

	@Override
	public boolean hasValues() {
		if(_values != null || _value != null){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public void initialize(String parameterValue) throws IllegalArgumentException {
		_value = parameterValue;
		_values = null;
	}

	@Override
	public void initialize(InputStream stream) throws IllegalArgumentException {
		try {
			_value = IOUtils.toString(stream, core.tut.pori.http.Definitions.CHARSET_UTF8);
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to read HTTP body.");
		}
	}
}
