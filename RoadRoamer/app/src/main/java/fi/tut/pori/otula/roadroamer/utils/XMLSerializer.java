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

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import fi.tut.pori.otula.roadroamer.datatypes.DataPoint;
import fi.tut.pori.otula.roadroamer.datatypes.Measurement;
import fi.tut.pori.otula.roadroamer.datatypes.SensorTask;


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
	 * Method to serialize task from task. Expects to have properly created task object.
	 * @param task
	 * @return the sensor task in an xml string or null on failure
	 */
	public String toString(SensorTask task){
		StringWriter writer = new StringWriter();
		try {
			_serializer.setOutput(writer);
			_serializer.startDocument(Definitions.DEFAULT_ENCODING, true);
			_serializer.startTag(null, Definitions.ELEMENT_TASK);

			_serializer.startTag(null, Definitions.ELEMENT_TASK_ID_LIST);
            for(Long taskId : nullToEmpty(task.getTaskIds())){
                _serializer.startTag(null, Definitions.ELEMENT_TASK_ID);
                _serializer.text(taskId.toString());
                _serializer.endTag(null, Definitions.ELEMENT_TASK_ID);
            }
			_serializer.endTag(null, Definitions.ELEMENT_TASK_ID_LIST);

            _serializer.startTag(null, Definitions.ELEMENT_MEASUREMENT_LIST);
            for(Measurement measurement : nullToEmpty(task.getMeasurements())){
                measurementSerializer(_serializer, measurement);
            }
            _serializer.endTag(null, Definitions.ELEMENT_MEASUREMENT_LIST);

            _serializer.startTag(null, Definitions.ELEMENT_BACKEND_LIST);
            for(Long backendId : nullToEmpty(task.getBackends())){
                _serializer.startTag(null, Definitions.ELEMENT_BACKEND);
                _serializer.startTag(null, Definitions.ELEMENT_BACKEND_ID);
                _serializer.text(backendId.toString());
                _serializer.endTag(null, Definitions.ELEMENT_BACKEND_ID);
                _serializer.startTag(null, Definitions.ELEMENT_ENABLED);
                _serializer.text(Boolean.TRUE.toString());
                _serializer.endTag(null, Definitions.ELEMENT_ENABLED);
                _serializer.startTag(null, Definitions.ELEMENT_TASK_STATUS);
                _serializer.text(Definitions.TASKSTATUS_COMPLETED);
                _serializer.endTag(null, Definitions.ELEMENT_TASK_STATUS);
                _serializer.endTag(null, Definitions.ELEMENT_BACKEND);
            }
            _serializer.endTag(null, Definitions.ELEMENT_BACKEND_LIST);

            _serializer.startTag(null, Definitions.ELEMENT_UPDATED_TIMESTAMP);
            _serializer.text(StringUtils.dateToISOString(task.getUpdated()));
            _serializer.endTag(null, Definitions.ELEMENT_UPDATED_TIMESTAMP);

			_serializer.endTag(null, Definitions.ELEMENT_TASK);
			_serializer.endDocument();
		} catch (IOException | IllegalArgumentException | IllegalStateException ex) { // should not happen
			Log.e(CLASS_NAME, "Failed to write stream.", ex);
			return null;
		}

		return writer.toString();
	}

	private void measurementSerializer(org.xmlpull.v1.XmlSerializer serializer, Measurement measurement) throws IOException{
        serializer.startTag(null, Definitions.ELEMENT_MEASUREMENT);

        serializer.startTag(null, Definitions.ELEMENT_MEASUREMENT_ID);
        serializer.text(measurement.getMeasurementId().toString());
        serializer.endTag(null, Definitions.ELEMENT_MEASUREMENT_ID);
        serializer.startTag(null, Definitions.ELEMENT_BACKEND_ID);
        serializer.text(measurement.getBackendId().toString());
        serializer.endTag(null, Definitions.ELEMENT_BACKEND_ID);
        serializer.startTag(null, Definitions.ELEMENT_DATAPOINT_LIST);
        for(DataPoint dp : nullToEmpty(measurement.getDataPoints())){
            datapointSerializer(serializer, dp);
        }
        serializer.endTag(null, Definitions.ELEMENT_DATAPOINT_LIST);

        serializer.endTag(null, Definitions.ELEMENT_MEASUREMENT);
    }

    private void datapointSerializer(org.xmlpull.v1.XmlSerializer serializer, DataPoint dataPoint) throws IOException{
        serializer.startTag(null, Definitions.ELEMENT_DATAPOINT);

        serializer.startTag(null, Definitions.ELEMENT_CREATED_TIMESTAMP);
        serializer.text(StringUtils.dateToISOString(dataPoint.getCreated()));
        serializer.endTag(null, Definitions.ELEMENT_CREATED_TIMESTAMP);
        serializer.startTag(null, Definitions.ELEMENT_DATAPOINT_ID);
        serializer.text(dataPoint.getDataPointId());
        serializer.endTag(null, Definitions.ELEMENT_DATAPOINT_ID);
        serializer.startTag(null, Definitions.ELEMENT_DESCRIPTION);
        serializer.text(dataPoint.getDescription());
        serializer.endTag(null, Definitions.ELEMENT_DESCRIPTION);
        serializer.startTag(null, Definitions.ELEMENT_KEY);
        serializer.text(dataPoint.getKey());
        serializer.endTag(null, Definitions.ELEMENT_KEY);
        serializer.startTag(null, Definitions.ELEMENT_VALUE);
        serializer.text(dataPoint.getValue());
        serializer.endTag(null, Definitions.ELEMENT_VALUE);

        serializer.endTag(null, Definitions.ELEMENT_DATAPOINT);
    }

    public static <E> List<E> nullToEmpty(List<E> list){
        if(list == null){
            return Collections.emptyList();
        }else{
            return list;
        }
    }
}
