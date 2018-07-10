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
import java.util.List;

/**
 * Input stream parameter for reading data as a stream.
 * 
 * This only supports data received as an InputStream (e.g. http body)
 *
 */
public class InputStreamParameter extends HTTPParameter{
	/** Default parameter name to use for a body parameter. If the parameter is defined as being a body parameter, the name has no effect. */
	public static final String PARAMETER_DEFAULT_NAME = "body";
	private InputStream _stream = null;

	@Override
	public void initialize(List<String> parameterValues) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Parameter value list not accepted for this class.");
	}

	@Override
	public void initialize(String parameterValue) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Parameter value not accepted for this class.");
	}

	@Override
	public boolean hasValues() {
		return (_stream != null);
	}

	@Override
	public void initialize(InputStream stream) {
		_stream = stream;
	}
	
	/**
	 *  Note: if this class is used with default \@HttpServiceMethod annotation designated as a body parameter,
	 *  you do not need to close the stream manually, it will be automatically closed when the connection disconnects.
	 * 
	 * @return the input stream
	 */
	@Override
	public InputStream getValue(){
		return _stream;
	}
}
