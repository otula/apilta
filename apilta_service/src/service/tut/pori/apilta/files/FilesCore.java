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

import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import service.tut.pori.apilta.files.datatypes.FileDetails;
import core.tut.pori.context.ServiceInitializer;

/**
 * Core methods for content handling
 */
public final class FilesCore {
	private static final Logger LOGGER = Logger.getLogger(FilesCore.class);
	private static final TemporaryTokenHandler TOKEN_HANDLER = new TemporaryTokenHandler();
	
	/**
	 * 
	 */
	private FilesCore(){
		// nothing needed
	}
	
	/**
	 * 
	 * @param file
	 * @return details for the created file or null on failure
	 * @throws IllegalArgumentException on bad input data
	 */
	public static FileDetails createFile(InputStream file) throws IllegalArgumentException{
		if(file == null){
			throw new IllegalArgumentException("Invalid file.");
		}
		
		FileDAO fDao = ServiceInitializer.getDAOHandler().getDAO(FileDAO.class);
		if(fDao == null){
			throw new RuntimeException("FileDAO was not initialized");
		}
		String guid = fDao.addFile(file);
		if(guid == null){
			LOGGER.debug("Failed to create new file.");
			return null;
		}
		
		//TODO schedule background process for resolving mimetype & generating thumbnail (when applicable) ?
		
		FileDetails details = new FileDetails();
		details.setGUID(guid);
		return details;
	}
	
	/**
	 * This can be used to change the ownership (backend id) of the file, but not to change the url. If url is given, it is simply ignored.
	 * 
	 * @param details
	 * @return true on success
	 * @throws IllegalArgumentException on invalid details
	 */
	public static boolean updateFile(FileDetails details) throws IllegalArgumentException {
		if(!FileDetails.isValid(details)){
			throw new IllegalArgumentException("Invalid file details.");
		}
		
		return ServiceInitializer.getDAOHandler().getDAO(FileDAO.class).updateFile(details);
	}
	
	/**
	 * 
	 * @param guid
	 * @return details for the file or null if not found
	 */
	public static FileDetails getFileDetails(String guid) {
		return ServiceInitializer.getDAOHandler().getDAO(FileDAO.class).getFileDetails(guid);
	}
	
	/**
	 * 
	 * @param guid
	 */
	public static void removeFile(String guid) {
		ServiceInitializer.getDAOHandler().getDAO(FileDAO.class).removeFile(guid);
	}

	/**
	 * 
	 * @param token
	 * @return details for the token or null if token is not valid
	 */
	public static FileDetails getFileDetailsForToken(String token) {
		String guid = TOKEN_HANDLER.getGUID(token);
		if(StringUtils.isBlank(guid)){
			LOGGER.warn("Attempted to access file details for invalid token.");
			return null;
		}
		
		return getFileDetails(guid);
	}

	/**
	 * Note: this does NOT validate the GUID, it is assumed to exist (using the generated url to access non-existent resource will result in an error)
	 * 
	 * @param guid
	 * @return currently valid temporary url for the given guid/user pair
	 */
	public static String generateTemporaryUrl(String guid) {
		return ServiceInitializer.getPropertyHandler().getRESTBindContext()+Definitions.SERVICE_FILES+core.tut.pori.http.Definitions.SEPARATOR_URI_PATH+Definitions.METHOD_GET_FILE_DETAILS+core.tut.pori.http.Definitions.SEPARATOR_URI_METHOD_PARAMS+Definitions.PARAMETER_TEMPORARY_TOKEN+core.tut.pori.http.Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE+generateTemporaryToken(guid);
	}
	
	/**
	 * Note: this does NOT validate the GUID, it is assumed to exist
	 * 
	 * @param guid
	 * @return currently valid temporary access token for the given guid/user pair
	 */
	protected static String generateTemporaryToken(String guid) {
		return TOKEN_HANDLER.getToken(guid);
	}
}
