/*
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
package tut.pori.alertapplication.utils;

import android.location.Location;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import tut.pori.alertapplication.datatypes.Alert;
import tut.pori.alertapplication.datatypes.FileDetails;

/**
 * This class is NOT thread-safe
 */
public class XMLSerializer {
	private static final String CLASS_NAME = XMLSerializer.class.toString();
	private XmlSerializer _serializer = null;

	/**
	 *
	 */
	public XMLSerializer() {
		_serializer = Xml.newSerializer();
	}

	/**
	 *
	 * @param alert
	 * @return the list of alerts in an xml string or null on failure
	 */
	public String toString(Alert alert){
		StringWriter writer = new StringWriter();
		try {
			_serializer.setOutput(writer);
			_serializer.startDocument(Definitions.DEFAULT_ENCODING, true);
			_serializer.startTag(null, Definitions.ELEMENT_ALERT);

			_serializer.startTag(null, Definitions.ELEMENT_ALERT_TYPE);
			_serializer.text(alert.getAlertType().toAlertTypeString());
			_serializer.endTag(null, Definitions.ELEMENT_ALERT_TYPE);

			_serializer.startTag(null, Definitions.ELEMENT_CREATED_TIMESTAMP);
			_serializer.text(StringUtils.dateToISOString(alert.getCreated()));
			_serializer.endTag(null, Definitions.ELEMENT_CREATED_TIMESTAMP);

			String description = alert.getDescription();
			if(description != null && !description.isEmpty()){
				_serializer.startTag(null, Definitions.ELEMENT_DESCRIPTION);
				_serializer.text(description);
				_serializer.endTag(null, Definitions.ELEMENT_DESCRIPTION);
			}

			toString(alert.getFiles());
			toString(alert.getLocation());

			_serializer.endTag(null, Definitions.ELEMENT_ALERT);
			_serializer.endDocument();
		} catch (IOException | IllegalArgumentException | IllegalStateException ex) { // should not happen
			Log.e(CLASS_NAME, "Failed to write stream.", ex);
			return null;
		}

		return writer.toString();
	}

	/**
	 *
	 * @param fileDetails if null or empty, tis method does nothing
	 * @throws IOException
	 */
	private void toString(List<FileDetails> fileDetails) throws IOException, IllegalArgumentException, IllegalStateException {
		if(fileDetails == null || fileDetails.isEmpty()){
			return;
		}

		_serializer.startTag(null, Definitions.ELEMENT_FILE_DETAILS_LIST);
		for(FileDetails details : fileDetails){
			_serializer.startTag(null, Definitions.ELEMENT_FILE_DETAILS);
			_serializer.startTag(null, Definitions.ELEMENT_GUID);
			_serializer.text(details.getGUID());
			_serializer.endTag(null, Definitions.ELEMENT_GUID);
			_serializer.endTag(null, Definitions.ELEMENT_FILE_DETAILS);
		}
		_serializer.endTag(null, Definitions.ELEMENT_FILE_DETAILS_LIST);
	}

	/**
	 *
	 * @param location
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 */
	private void toString(Location location) throws IOException, IllegalArgumentException, IllegalStateException {
		_serializer.startTag(null, Definitions.ELEMENT_LOCATION);
		_serializer.startTag(null, Definitions.ELEMENT_LATITUDE);
		_serializer.text(String.valueOf(location.getLatitude()));
		_serializer.endTag(null, Definitions.ELEMENT_LATITUDE);
		_serializer.startTag(null, Definitions.ELEMENT_LONGITUDE);
		_serializer.text(String.valueOf(location.getLongitude()));
		_serializer.endTag(null, Definitions.ELEMENT_LONGITUDE);
		_serializer.endTag(null, Definitions.ELEMENT_LOCATION);
	}
}
