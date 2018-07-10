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
package fi.tut.pori.otula.roadroamer.utils;

import android.util.Log;
import android.util.Xml;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.tut.pori.otula.roadroamer.datatypes.Condition;
import fi.tut.pori.otula.roadroamer.datatypes.FileDetails;
import fi.tut.pori.otula.roadroamer.datatypes.Output;
import fi.tut.pori.otula.roadroamer.datatypes.SensorTask;

/**
 * xml parser for converting input stream to sensor task object
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

    /**
     *
     * @param in the input stream, you must manually close the stream (not closed by this method)
     * @return sensor task from the given stream or null if none was found
     */
    public Set<Long> parseTaskIds(InputStream in) {
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
        return parseTaskIds();
    }

    private Set<Long> parseTaskIds(){
        Set<Long> taskIds = null;
        try {
            while(_parser.next() != XmlPullParser.END_TAG){
                if (_parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                switch(_parser.getName()){
                    case Definitions.ELEMENT_TASK_LIST:
                        taskIds = new HashSet<>();    //the root element for task, create array list instance
                        break;
                    case Definitions.ELEMENT_TASK:
                        List<Long> taskIdList = parseTaskIdList();
                        if(taskIdList != null && !taskIdList.isEmpty()){
                            taskIds.addAll(taskIdList);
                        }
                        skip(); //go to next (possible) task element
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
        return taskIds;
    }

    /**
     *
     * @param in the input stream, you must manually close the stream (not closed by this method)
     * @return sensor task from the given stream or null if none was found
     */
    public SensorTask parseSensorTask(InputStream in) {
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
        return parseSensorTask();
    }

    private SensorTask parseSensorTask(){
        SensorTask task = null;
        try {
            while(_parser.next() != XmlPullParser.END_TAG){
                if (_parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                switch(_parser.getName()){
                    case Definitions.ELEMENT_TASK:
                        task = new SensorTask();    //the root element for task, create sensor task instance
                        break;
                    case Definitions.ELEMENT_CREATED_TIMESTAMP:
                        task.setCreated(StringUtils.ISOStringToDate(readText()));
                        break;
                    case Definitions.ELEMENT_TASK_ID_LIST:
                        task.setTaskIds(parseTaskIdList());
                        break;
                    case Definitions.ELEMENT_UPDATED_TIMESTAMP:
                        task.setUpdated(StringUtils.ISOStringToDate(readText()));
                        break;
                    case Definitions.ELEMENT_CALLBACK_URI:
                        task.setCallbackUri(readText());
                        break;
                    case Definitions.ELEMENT_WHAT:
                        task.setOutput(parseOutputList());
                        break;
                    case Definitions.ELEMENT_WHEN:
                        task.setConditions(parseConditionList());
                        break;
                    //the following elements are not handled
                    case Definitions.ELEMENT_DESCRIPTION:
                    case Definitions.ELEMENT_NAME:
                    case Definitions.ELEMENT_USER_IDENTITY:
                    default:
                        skip();
                        break;
                }
            }
        } catch (XmlPullParserException | IOException | NumberFormatException ex) {
            Log.e(CLASS_NAME, "Failed to parse input.", ex);
            return null;
        }
        return task;
    }

    private List<Long> parseTaskIdList(){
        ArrayList<Long> taskIds = new ArrayList<>();
        try {
            while(_parser.next() != XmlPullParser.END_TAG){
                if (_parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                switch(_parser.getName()){
                    case Definitions.ELEMENT_TASK_ID_LIST:
                        break;
                    case Definitions.ELEMENT_TASK_ID:
                        taskIds.add(readLong());
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
        return (taskIds.isEmpty() ? null : taskIds);
    }

    /**
     *
     * @return list of output details or null if none was found in the active parser
     */
    private List<Output> parseOutputList() {
        ArrayList<Output> outputs = new ArrayList<>();
        try {
            while(_parser.next() != XmlPullParser.END_TAG){
                if (_parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                switch(_parser.getName()){
                    case Definitions.ELEMENT_OUTPUT:
                        outputs.add(parseOutput());
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
        return (outputs.isEmpty() ? null : outputs);
    }

    /**
     *
     * @return output (features)
     * @throws XmlPullParserException
     * @throws IOException
     */
    private Output parseOutput() throws XmlPullParserException, IOException {
        Output output = new Output();
        while (_parser.next() != XmlPullParser.END_TAG) {
            if (_parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            switch(_parser.getName()) {
                case Definitions.ELEMENT_FEATURE:
                    output.setFeature(readText());
                    break;
                default:
                    skip();
                    break;
            }
        }
        return output;
    }

    /**
     *
     * @return list of conditions, or null if none was found in the active parser
     */
    private List<Condition> parseConditionList() {
        ArrayList<Condition> conditions = new ArrayList<>();
        try {
            while(_parser.next() != XmlPullParser.END_TAG){
                if (_parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                switch(_parser.getName()){
                    case Definitions.ELEMENT_CONDITION:
                        conditions.add(parseConditionTerms());
                        skip();
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
        return (conditions.isEmpty() ? null : conditions);
    }

    /**
     *
     * @return returns condition, or null if none was found in the active parser
     */
    private Condition parseConditionTerms() {
        Map<String, String> terms = new HashMap<>();
        try {
            while(_parser.next() != XmlPullParser.END_TAG){
                if (_parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                switch(_parser.getName()){
                    case Definitions.ELEMENT_TERMS: // valid tag, details are inside this tag, so do not skip
                        break;
                    case Definitions.ELEMENT_ENTRY:
                        Pair<String, String> term = parseConditionEntry();
                        terms.put(term.getLeft(), term.getRight());
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
        if(terms.isEmpty()){
            return null;
        }else {
            Condition condition = new Condition();
            condition.setConditions(terms);
            return condition;
        }
    }

    /**
     *
     * @return key-value pair
     * @throws XmlPullParserException
     * @throws IOException
     */
    private Pair<String, String> parseConditionEntry() throws XmlPullParserException, IOException {
        String key = null;
        String value = null;
        while (_parser.next() != XmlPullParser.END_TAG) {
            if (_parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            switch(_parser.getName()) {
                case Definitions.ELEMENT_KEY:
                    key = readText();
                    break;
                case Definitions.ELEMENT_VALUE:
                    value = readText();
                    break;
                default:
                    skip();
                    break;
            }
        }
        return new ImmutablePair<>(key, value);
    }
}
