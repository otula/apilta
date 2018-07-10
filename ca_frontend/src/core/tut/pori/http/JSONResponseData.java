/**
 * Copyright 2014 Tampere University of Technology, Pori Department
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
package core.tut.pori.http;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

import core.tut.pori.http.Response.Status;

/**
 * Extends the basic XML output provided by JAXB with support for GSON/JSON output.
 * 
 * Note that when using JSONResponseData, one MUST use the JSONResponse class instead of Response to achieve properly formatted output.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class JSONResponseData extends ResponseData {
	/** used by response serializer */
	@SerializedName(value=Definitions.JSON_METHOD)
	protected String _method = null;
	/** used by response serializer */
	@SerializedName(value=Definitions.JSON_MESSAGE)
	protected String _message = null;
	/** used by response serializer */
	@SerializedName(value=Definitions.JSON_SERVICE)
	protected String _service = null;
	/** used by response serializer */
	@SerializedName(value=Definitions.JSON_STATUS)
	protected Status _stat = Status.OK;
}
