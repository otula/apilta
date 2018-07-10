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
package app.tut.pori.cmd.datatypes;

import java.util.Date;

/**
 * 
 * details for a single file
 */
public class SqliteFile {
	private String _guid = null;
	private String _measurementId = null;
	private String _path = null;
	private Date _timestamp = null;
	
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
	 * @return the measurementId
	 * @see #setMeasurementId(String)
	 */
	public String getMeasurementId() {
		return _measurementId;
	}
	
	/**
	 * @param measurementId the measurementId to set
	 * @see #getMeasurementId()
	 */
	public void setMeasurementId(String measurementId) {
		_measurementId = measurementId;
	}
	
	/**
	 * @return the path
	 * @see #setPath(String)
	 */
	public String getPath() {
		return _path;
	}
	
	/**
	 * @param path the path to set
	 * @see #getPath()
	 */
	public void setPath(String path) {
		_path = path;
	}
	
	/**
	 * @return the timestamp
	 * @see #setTimestamp(Date)
	 */
	public Date getTimestamp() {
		return _timestamp;
	}
	
	/**
	 * @param timestamp the timestamp to set
	 * @see #getTimestamp()
	 */
	public void setTimestamp(Date timestamp) {
		_timestamp = timestamp;
	}
}
