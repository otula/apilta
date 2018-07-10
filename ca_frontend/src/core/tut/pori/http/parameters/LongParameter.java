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
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * A simple class for Long parameters
 */
public class LongParameter extends HTTPParameter {
	private static final Logger LOGGER = Logger.getLogger(LongParameter.class);
	private long[] _values = null;

	@Override
	public void initialize(List<String> parameterValues) throws IllegalArgumentException {
		try{
			int count = parameterValues.size();
			long[] array = new long[count];
			for(int i=0;i<count;++i){
				array[i] = Long.parseLong(parameterValues.get(i));
			}
			_values = array;
		}catch(NumberFormatException ex){
			LOGGER.debug(ex, ex);
			throw new IllegalArgumentException("Invalid value for parameter: "+getParameterName());
		}
	}
	
	/**
	 * 
	 * @return array of values or null if none
	 */
	public long[] getValues(){
		return _values;
	}
	
	@Override
	public Long getValue(){
		return (hasValues() ? _values[0] : null);
	}

	@Override
	public boolean hasValues() {
		return (_values != null);
	}

	@Override
	public void initialize(String parameterValue) throws IllegalArgumentException {
		if(StringUtils.isBlank(parameterValue)){
			_values = null;
			return;	// do nothing on empty value
		}
		try{
			_values = new long[]{Long.parseLong(parameterValue)};
		}catch(NumberFormatException ex){
			LOGGER.debug(ex, ex);
			throw new IllegalArgumentException("Invalid value: "+parameterValue+" for parameter: "+getParameterName());
		}
	}
	
	@Override
	public void initialize(InputStream stream) throws IllegalArgumentException {
		try {
			_values = new long[]{Long.parseLong(IOUtils.toString(stream, core.tut.pori.http.Definitions.CHARSET_UTF8))};
		} catch (IOException | NumberFormatException ex) {
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to read HTTP body.");
		}
	}
}
