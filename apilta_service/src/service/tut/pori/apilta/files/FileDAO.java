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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.jclouds.io.Payload;
import org.jclouds.io.Payloads;
import org.jclouds.openstack.swift.v1.domain.SwiftObject;
import org.springframework.beans.factory.DisposableBean;

import core.tut.pori.dao.DAO;
import service.tut.pori.apilta.files.datatypes.FileDetails;
import service.tut.pori.apilta.files.datatypes.SignedURL;

/**
 * Content storage driver for accessing OpenStack Swift Object Storage.
 */
public class FileDAO implements DAO, DisposableBean {
	private static final Logger LOGGER = Logger.getLogger(FileDAO.class);
	private static final String METADATA_MIME_TYPE = "mimeType";
	private SwiftTemplate _swiftTemplate = null;
	
	/**
	 * 
	 */
	public FileDAO() {
		_swiftTemplate = new SwiftTemplate();
	}
	
	/**
	 * 
	 * @param file
	 * @return guid of the created file or null on failure
	 */
	public String addFile(InputStream file) {
		String guid = UUID.randomUUID().toString();
		try(Payload payload = Payloads.newInputStreamPayload(file)){			
			_swiftTemplate.put(guid, payload);
		} catch (IOException ex) {
			LOGGER.error(ex, ex);
			return null;
		}
		return guid;
	}
	
	/**
	 * 
	 * @param details
	 * @return true on success
	 */
	public boolean updateFile(FileDetails details) {
		HashMap<String, String> metadata = new HashMap<>(1);
		metadata.put(METADATA_MIME_TYPE, details.getMimeType());
		_swiftTemplate.update(details.getGUID(), metadata); // TODO how to know if this has succeeded?
		return true;
	}

	/**
	 * 
	 * @param guid
	 * @return file details for the content or null if no content matching the guid was found
	 */
	public FileDetails getFileDetails(String guid) {
		SwiftObject object = _swiftTemplate.get(guid);
		if(object == null){
			LOGGER.debug("Could not find object for guid: "+guid);
			return null;
		}
		
		FileDetails details = new FileDetails();
		details.setGUID(guid);
		Map<String, String> metadata = object.getMetadata();
		details.setMimeType(metadata.get(METADATA_MIME_TYPE));
		SignedURL signedURL = _swiftTemplate.signURL(object);
		details.setUrl(signedURL.getUrl());
		details.setValidUntil(signedURL.getValidUntil());
		return details; // the only metadata that is currently used
	}

	/**
	 * 
	 * @param guid
	 */
	public void removeFile(String guid) {
		_swiftTemplate.delete(guid);
	}

	@Override
	public void destroy() {	
		_swiftTemplate.close();
		_swiftTemplate = null;
	}
}
