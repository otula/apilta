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
package tut.pori.alertapplication.datatypes;

/**
 * Contains details of a single file.
 */
public class FileDetails{
    private String _filePath = null;
	private String _guid = null;


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
     *
     * @return the local file path
     * @see #setFilePath(String)
     */
    public String getFilePath() {
        return _filePath;
    }

    /**
     *
     * @param filePath
     * @see #getFilePath()
     */
    public void setFilePath(String filePath) {
        _filePath = filePath;
    }
}
