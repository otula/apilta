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
package service.tut.pori.apilta.files.datatypes;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import core.tut.pori.utils.ISODateAdapter;

/**
 * Contains details of a single file.
 */
@XmlRootElement(name=Definitions.ELEMENT_FILE_DETAILS)
@XmlAccessorType(XmlAccessType.NONE)
public class FileDetails{
	private static final Logger LOGGER = Logger.getLogger(FileDetails.class);
	@XmlElement(name=Definitions.ELEMENT_GUID)
	private String _guid = null;
	@XmlElement(name=Definitions.ELEMENT_MIME_TYPE)
	private String _mimeType = null;
	@XmlElement(name=Definitions.ELEMENT_URL)
	private String _url = null;
	@XmlJavaTypeAdapter(ISODateAdapter.class)
	@XmlElement(name=Definitions.ELEMENT_VALID_UNTIL)
	private Date _validUntil = null;

	/**
	 * @return the guid
	 * @see #setGUID(String)
	 */
	public String getGUID() {
		return _guid;
	}
	
	/**
	 * @param guid the guid to set
	 * @see #getGUID()
	 */
	public void setGUID(String guid) {
		_guid = guid;
	}

	/**
	 * @return the mimeType
	 * @see #setMimeType(String)
	 */
	public String getMimeType() {
		return _mimeType;
	}

	/**
	 * @param mimeType the mimeType to set
	 * @see #getMimeType()
	 */
	public void setMimeType(String mimeType) {
		_mimeType = mimeType;
	}

	/**
	 * @return the url
	 * @see #setUrl(String)
	 */
	public String getUrl() {
		return _url;
	}

	/**
	 * @param url the url to set
	 * @see #getUrl()
	 */
	public void setUrl(String url) {
		_url = url;
	}
	
	/**
	 * @return the last date/time the urls provided in this file details object are valid or null if the date is unknown
	 * @see #setValidUntil(Date)
	 */
	public Date getValidUntil() {
		return _validUntil;
	}

	/**
	 * @param validUntil the validUntil to set
	 * @see #getValidUntil()
	 */
	public void setValidUntil(Date validUntil) {
		_validUntil = validUntil;
	}

	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if valid
	 * @see #isValid(FileDetails)
	 */
	protected boolean isValid() {
		if(StringUtils.isBlank(_guid)){
			LOGGER.debug("GUID was null or empty.");
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * 
	 * @param details
	 * @return false if details is null or contains invalid data
	 */
	public static boolean isValid(FileDetails details) {
		if(details == null){
			return false;
		}else{
			return details.isValid();
		}
	}
}
