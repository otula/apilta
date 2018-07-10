/**
 * Copyright 2018 Tampere University of Technology, Pori Department
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
package tut.pori.shockapplication.utils;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;

import tut.pori.shockapplication.datatypes.ShockMeasurement;

/**
 *
 */
public class XMLSerializer {
	private static final String TAG = XMLSerializer.class.toString();

	/**
	 *
	 * @param measurements
	 * @return the list as an xml string
	 */
	public String toString(Collection<ShockMeasurement> measurements) {
		StringWriter writer = new StringWriter();
		try {
			XmlSerializer serializer = Xml.newSerializer();

			serializer.setOutput(writer);
			serializer.startDocument(Definitions.DEFAULT_ENCODING, true);
			serializer.startTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_MEASUREMENT_LIST);

			for(ShockMeasurement m : measurements){
				toString(m, serializer);
			}

			serializer.endTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_MEASUREMENT_LIST);
			serializer.endDocument();
		} catch (IOException | IllegalArgumentException | IllegalStateException ex) { // should not happen
			Log.e(TAG, "Failed to write stream.", ex);
			return null;
		}

		return writer.toString();
	}

	/**
	 *
	 * @param measurement
	 * @param serializer
	 * @throws IOException on IOException
	 */
	private void toString(ShockMeasurement measurement, XmlSerializer serializer) throws IOException {
		serializer.startTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_MEASUREMENT);

		serializer.startTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_LEVEL);
		serializer.text(String.valueOf(measurement.getLevel()));
		serializer.endTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_LEVEL);

		serializer.startTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_TIMESTAMP);
		String timestamp = StringUtils.dateToISOString(new Date(measurement.getTimestamp()));
		serializer.text(timestamp);
		serializer.endTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_TIMESTAMP);

		serializer.startTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_DATA_VISIBILITY);
		serializer.text(measurement.getDataVisibility());
		serializer.endTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_DATA_VISIBILITY);

		serializer.startTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_LOCATION_DATA);

		serializer.startTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_TIMESTAMP);
		serializer.text(timestamp);
		serializer.endTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_TIMESTAMP);

		serializer.startTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_LATITUDE);
		serializer.text(String.valueOf(measurement.getLatitude()));
		serializer.endTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_LATITUDE);

		serializer.startTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_LONGITUDE);
		serializer.text(String.valueOf(measurement.getLongitude()));
		serializer.endTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_LONGITUDE);

		Float speed = measurement.getSpeed();
		if(speed != null) {
			serializer.startTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_SPEED);
			serializer.text(String.valueOf(speed));
			serializer.endTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_SPEED);
		}

		Float heading = measurement.getHeading();
		if(heading != null) {
			serializer.startTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_HEADING);
			serializer.text(String.valueOf(heading));
			serializer.endTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_HEADING);
		}

		serializer.endTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_LOCATION_DATA);

		toString(measurement.getAccelerometerData(), serializer);

		serializer.endTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_MEASUREMENT);
	}

	/**
	 *
	 * @param accelerometerData
	 * @param serializer
	 * @throws IOException on IOException
	 */
	private void toString(ShockMeasurement.AccelerometerData accelerometerData, XmlSerializer serializer) throws IOException {
		serializer.startTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_ACCELEROMETER_DATA);

		serializer.startTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_TIMESTAMP);
		serializer.text(StringUtils.dateToISOString(new Date(accelerometerData.getTimestamp())));
		serializer.endTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_TIMESTAMP);

		serializer.startTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_X_ACCELERATION);
		serializer.text(Float.toString(accelerometerData.getxAcceleration()));
		serializer.endTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_X_ACCELERATION);

		serializer.startTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_Y_ACCELERATION);
		serializer.text(Float.toString(accelerometerData.getyAcceleration()));
		serializer.endTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_Y_ACCELERATION);

		serializer.startTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_Z_ACCELERATION);
		serializer.text(Float.toString(accelerometerData.getzAcceleration()));
		serializer.endTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_Z_ACCELERATION);

		Float systematicError = accelerometerData.getSystematicError();
		if(systematicError != null){
			serializer.startTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_SYSTEMATIC_ERROR);
			serializer.text(String.valueOf(systematicError));
			serializer.endTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_SYSTEMATIC_ERROR);
		}

		serializer.endTag(null, tut.pori.shockapplication.datatypes.Definitions.ELEMENT_ACCELEROMETER_DATA);
	}
}
