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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import tut.pori.alertapplication.datatypes.Alert;
import tut.pori.alertapplication.datatypes.FileDetails;
import tut.pori.alertapplication.datatypes.UserIdentity;

/**
 * xml parser for converting input stream to alert object
 *
 * Note: this class is NOT thread-safe
 */
public class XMLParser {
	private static final String CLASS_NAME = XMLParser.class.toString();
	private XmlPullParser _parser = null;

	/**
	 *
	 */
	public XMLParser(){
		_parser = Xml.newPullParser();
		try {
			_parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		} catch (XmlPullParserException ex) { // this should not happen
			Log.e(CLASS_NAME, "Initialization failed.", ex);
		}
	}

	/**
	 *
	 * @param in the input stream, you must manually close the stream (not closed by this method)
	 * @return list of alerts from the given stream or null if none was found
	 */
	public List<Alert> parseAlerts(InputStream in) {
		if(in == null){
			Log.w(CLASS_NAME, "Ignored null stream.");
			return null;
		}
		try {
			_parser.setInput(in, null);
			_parser.nextTag();
		} catch (XmlPullParserException | IOException ex) {
			Log.e(CLASS_NAME, "Failed to set input.", ex);
			return null;
		}
		return parseAlertsList();
	}

	/**
	 *
	 * @param in the input stream, you must manually close the stream (not closed by this method)
	 * @return user identity from the given stream or null if none was found
	 */
	public UserIdentity parseUserIdentity(InputStream in) {
		if(in == null){
			Log.w(CLASS_NAME, "Ignored null stream.");
			return null;
		}
		try {
			_parser.setInput(in, null);
			_parser.nextTag();

			while(_parser.next() != XmlPullParser.END_TAG){
				if (_parser.getEventType() != XmlPullParser.START_TAG) {
					continue;
				}

				switch(_parser.getName()){
                    case Definitions.ELEMENT_USER_IDENTITY_LIST:
                        break;
					case Definitions.ELEMENT_USER_IDENTITY:
						return parseUserIdentity();
					default:
						skip();
						break;
				}
			}
		} catch (XmlPullParserException | IOException | NumberFormatException ex) {
			Log.e(CLASS_NAME, "Failed to set input.", ex);
		}
		return null;
	}

	/**
	 *
	 * @param in the input stream, you must manually close the stream (not closed by this method)
	 * @return file details from the given stream or null if none was found
	 */
	public List<FileDetails> parseFileDetails(InputStream in) {
		if(in == null){
			Log.w(CLASS_NAME, "Ignored null stream.");
			return null;
		}
		try {
			_parser.setInput(in, null);
			_parser.nextTag();
		} catch (XmlPullParserException | IOException | NumberFormatException ex) {
			Log.e(CLASS_NAME, "Failed to set input.", ex);
			return null;
		}
		return parseFileDetailsList();
	}

	/**
	 *
	 * @return list of file details or null if none was found in the active parser
	 */
	private List<FileDetails> parseFileDetailsList() {
		ArrayList<FileDetails> details = new ArrayList<>();
		try {
			while(_parser.next() != XmlPullParser.END_TAG){
				if (_parser.getEventType() != XmlPullParser.START_TAG) {
					continue;
				}

				switch(_parser.getName()){
					case Definitions.ELEMENT_FILE_DETAILS_LIST: // valid tag, details are inside this tag, so do not skip
						break;
					case Definitions.ELEMENT_FILE_DETAILS:
						details.add(parseFileDetails());
						break;
					default:
						skip();
						break;
				}
			}
		} catch (XmlPullParserException | IOException | NumberFormatException ex) {
			Log.e(CLASS_NAME, "Failed to parse input.", ex);
			return null;
		}
		return (details.isEmpty() ? null : details);
	}

	/**
	 *
	 * @return list of alerts or null if none was found in the active parser
	 */
	private List<Alert> parseAlertsList() {
		ArrayList<Alert> alerts = new ArrayList<>();
		try {
			while(_parser.next() != XmlPullParser.END_TAG){
				if (_parser.getEventType() != XmlPullParser.START_TAG) {
					continue;
				}

				switch(_parser.getName()){
					case Definitions.ELEMENT_ALERT_LIST: // valid tag, alerts are inside this tag, so do not skip
						break;
					case Definitions.ELEMENT_ALERT:
						alerts.add(parseAlert());
						break;
					default:
						skip();
						break;
				}
			}
		} catch (XmlPullParserException | IOException | NumberFormatException ex) {
			Log.e(CLASS_NAME, "Failed to parse input.", ex);
			return null;
		}
		return (alerts.isEmpty() ? null : alerts);
	}

	/**
	 *
	 * @return alert
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private Alert parseAlert() throws XmlPullParserException, IOException {
		Alert alert = new Alert();
		while (_parser.next() != XmlPullParser.END_TAG) {
			if (_parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			switch(_parser.getName()) {
				case Definitions.ELEMENT_ALERT_ID:
					alert.setAlertId(readText());
					break;
				case Definitions.ELEMENT_ALERT_TYPE:
					alert.setAlertType(Alert.AlertType.fromAlertTypeString(readText()));
					break;
				case Definitions.ELEMENT_CREATED_TIMESTAMP:
					alert.setCreated(StringUtils.ISOStringToDate(readText()));
					break;
				case Definitions.ELEMENT_DESCRIPTION:
					alert.setDescription(readText());
					break;
				case Definitions.ELEMENT_LOCATION:
					alert.setLocation(parseLocation());
					break;
				case Definitions.ELEMENT_RANGE:
					alert.setRange(readInteger());
					break;
				case Definitions.ELEMENT_USER_IDENTITY:
					alert.setUserId(parseUserIdentity());
					break;
				case Definitions.ELEMENT_FILE_DETAILS_LIST:
					alert.setFiles(parseFileDetailsList());
					break;
				default:
					skip();
					break;
			}
		}
		return alert;
	}

	/**
	 *
	 * @return user identity
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	private UserIdentity parseUserIdentity() throws XmlPullParserException, IOException, NumberFormatException {
		UserIdentity userId = new UserIdentity();
		while (_parser.next() != XmlPullParser.END_TAG) {
			if (_parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			switch(_parser.getName()) {
				case Definitions.ELEMENT_USER_ID:
					userId.setUserId(readLong());
					break;
				default:
					skip();
					break;
			}
		}
		return userId;
	}

	/**
	 *
	 * @return file details
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private FileDetails parseFileDetails() throws XmlPullParserException, IOException {
		FileDetails details = new FileDetails();
		while (_parser.next() != XmlPullParser.END_TAG) {
			if (_parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			switch(_parser.getName()) {
				case Definitions.ELEMENT_GUID:
					details.setGUID(readText());
					break;
				default:
					skip();
					break;
			}
		}
		return details;
	}

	/**
	 *
	 * @return location
	 * @throws XmlPullParserException
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	private Location parseLocation() throws XmlPullParserException, IOException, NumberFormatException {
		Location location = new Location(Definitions.LOCATION_PROVIDER_SERVICE);
		while (_parser.next() != XmlPullParser.END_TAG) {
			if (_parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}

			switch(_parser.getName()) {
				case Definitions.ELEMENT_LATITUDE:
					location.setLatitude(Double.parseDouble(readText()));
					break;
				case Definitions.ELEMENT_LONGITUDE:
					location.setLongitude(Double.parseDouble(readText()));
					break;
				default:
					skip();
					break;
			}
		}
		return location;
	}

	/**
	 *
	 * @return text value for the xml element
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String readText() throws IOException, XmlPullParserException {
		String result = null;
		if (_parser.next() == XmlPullParser.TEXT) {
			result = _parser.getText();
			_parser.nextTag();
		}
		return result;
	}

	/**
	 *
	 * @return integer value for the xml element
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private Integer readInteger() throws IOException, XmlPullParserException, NumberFormatException {
		String result = null;
		if (_parser.next() == XmlPullParser.TEXT) {
			result = _parser.getText();
			_parser.nextTag();
		}
		return Integer.valueOf(result);
	}

	/**
	 *
	 * @return long value for the xml element
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private Long readLong() throws IOException, XmlPullParserException, NumberFormatException {
		String result = null;
		if (_parser.next() == XmlPullParser.TEXT) {
			result = _parser.getText();
			_parser.nextTag();
		}
		return Long.valueOf(result);
	}

	/**
	 *
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void skip() throws XmlPullParserException, IOException {
		int depth = 1;
		while (depth != 0) {
			switch (_parser.next()) {
				case XmlPullParser.END_TAG:
					--depth;
					break;
				case XmlPullParser.START_TAG:
					++depth;
					break;
			}
		}
	}
}
