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
 * Converts parameter value String to boolean performing a case insensitive comparison, 
 * values "no", "0" and "false", empty String and null are interpreted as false, everything else as true
 *
 */
public class BooleanParameter extends HTTPParameter{
	private static final Logger LOGGER = Logger.getLogger(BooleanParameter.class);
	private static String FALSE_NO = "no";
	private static String FALSE_0 = "0";
	private static String FALSE_FALSE = "false";
	private boolean[] _values = null;

	@Override
	public void initialize(List<String> parameterValues) throws IllegalArgumentException {
		int count = parameterValues.size();
		_values = new boolean[count];
		for(int i=0;i<count;++i){
			_values[i] = toBoolean(parameterValues.get(i));
		}
	}

	/**
	 * 
	 * @return the values of this parameter or null if none
	 */
	public boolean[] getValues(){
		return _values;
	}
	
	/**
	 * 
	 * @return true if true value was given to this parameter, otherwise false
	 */
	@Override
	public Boolean getValue(){
		return (hasValues() ? _values[0] : Boolean.FALSE);
	}

	@Override
	public boolean hasValues() {
		return (_values != null);
	}

	@Override
	public void initialize(String parameterValue) throws IllegalArgumentException {
		_values = new boolean[]{toBoolean(parameterValue)};
	}
	
	/**
	 * 
	 * @return true if the value is true, false if value is false or does not exist
	 */
	public boolean isTrue(){
		return (hasValues() ? _values[0] : false);
	}
	
	/**
	 * 
	 * @param s
	 * @return the string converted to boolean
	 */
	public static boolean toBoolean(String s){
		if(StringUtils.isBlank(s) || FALSE_0.equalsIgnoreCase(s) || FALSE_FALSE.equalsIgnoreCase(s) || FALSE_NO.equalsIgnoreCase(s)){
			return false;
		}else{
			return true;
		}
	}
	
	@Override
	public void initialize(InputStream stream) throws IllegalArgumentException {
		try {
			initialize(IOUtils.toString(stream, core.tut.pori.http.Definitions.CHARSET_UTF8));
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to read HTTP body.");
		}
	}
}
