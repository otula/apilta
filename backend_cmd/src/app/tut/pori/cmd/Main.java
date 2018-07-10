/**
 * Copyright 2017 Tampere University of Technology, Pori Department
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
package app.tut.pori.cmd;


/**
 * Main class for command line
 * 
 */
public class Main {
	private static final String LOG4J_CONFIGURATION_FILE = "log4j2.xml";
	private static final String LOG4J_SYSTEM_PROPERTY = "log4j.configurationFile";
	

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.setProperty(LOG4J_SYSTEM_PROPERTY, LOG4J_CONFIGURATION_FILE);	// Load logger configuration
		int retval = CMDApplication.CODE_OK;
		try(CMDApplication ap = new CMDApplication()){
			retval = ap.execute(args);
		}
		if(retval != CMDApplication.CODE_OK){ // if error code was received, exit with error
			System.exit(retval);
		}
	}
}
