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
package service.tut.pori.apilta.files;

/**
 * definitions for files service
 * 
 */
public final class Definitions {
	/* methods */
	/** implemented by front end */
	public static final String METHOD_GET_FILE_DETAILS = "getFileDetails";
	
	/* parameters */
	/** service method parameter declaration */
	public static final String PARAMETER_TEMPORARY_TOKEN = "temp_token";
	
	/* services */
	/** service name declaration */
	public static final String SERVICE_FILES = "files";

	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
