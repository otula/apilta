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

import java.nio.charset.Charset;


/**
 * Definitions for the http package.
 * 
 * In addition to the internally used element definitions,
 * this utility class also contains the common HTTP method names and encodings.
 *
 */
public final class Definitions {

	/* elements */
	/** HTTP response element */
	protected static final String ELEMENT_MESSAGE = "message";
	/** HTTP response element */
	protected static final String ELEMENT_RESPONSE = "response";
	/** HTTP response element */
	protected static final String ELEMENT_STATUS = "status";
	
	/* attributes */
	/** HTTP response element response attribute */
	protected static final String ATTRIBUTE_METHOD = "method";
	/** HTTP response element response attribute */
	protected static final String ATTRIBUTE_SERVICE = "service";
	
	/* json objects */
	/** HTTP response object */
	protected static final String JSON_METHOD = "method";
	/** HTTP response object */
	protected static final String JSON_MESSAGE = "message";
	/** HTTP response object */
	protected static final String JSON_SERVICE = "service";
	/** HTTP response object */
	protected static final String JSON_STATUS = "status";
	
	/* HTTP Methods */
	/** HTTP method GET */
	public static final String METHOD_GET = "GET";
	/** HTTP method DELETE */
	public static final String METHOD_DELETE = "DELETE";
	/** HTTP method PATCH */
	public static final String METHOD_PATCH = "PATCH";
	/** HTTP method POST */
	public static final String METHOD_POST = "POST";
	/** HTTP method PUT */
	public static final String METHOD_PUT = "PUT";
	
	/* headers */
	/** HTTP WWW-authenticate header (<a href="http://tools.ietf.org/html/rfc2617#section-3.2.1">WWW-Authenticate header</a>)*/
	public static final String HEADER_AUTHENTICATE = "WWW-Authenticate";
	/** 
	 * Default HTTP authenticate realm for basic auth 
	 * 
	 * @see core.tut.pori.http.Definitions#HEADER_AUTHENTICATE
	 * */
	public static final String HEADER_AUTHENTICATE_VALUE = "Basic realm=\"CAFrontend\"";
	
	/* uri separators */
	/** separator used in the service uri path to separate methods from parameters i.e. www.domain.fi/somethingSEPARATOR_URI_METHOD_PARAMSparam=value */
	public static final String SEPARATOR_URI_METHOD_PARAMS = "?";
	/** separator used in the service uri path i.e. www.domain.fiSEPARATOR_URI_PATHsomething */
	public static final String SEPARATOR_URI_PATH = "/";
	/** separator used in query string to separate parameter values ({@value core.tut.pori.http.Definitions#SEPARATOR_URI_QUERY_PARAM_VALUES}) */
	public static final String SEPARATOR_URI_QUERY_PARAM_VALUES = ",";
	/** separator used in query string to separate query parameters ({@value core.tut.pori.http.Definitions#SEPARATOR_URI_QUERY_PARAMS}) */
	public static final String SEPARATOR_URI_QUERY_PARAMS = "&";
	/** separator used in query string to separate parameters from values ({@value core.tut.pori.http.Definitions#SEPARATOR_URI_QUERY_PARAM_VALUE}) */
	public static final String SEPARATOR_URI_QUERY_PARAM_VALUE = "=";
	/** separates types from values. e.g. ?param=type[SEPARATOR_URI_QUERY_TYPE_VALUE]value */
	public static final String SEPARATOR_URI_QUERY_TYPE_VALUE = ";";
	
	/* common */
	/** the default encoding for HTTP traffic */
	public static final String ENCODING_UTF8 = "UTF-8";
	/** the default charset for HTTP traffic */
	public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
	/** HTTP content type for JSON */
	public static final String CONTENT_TYPE_JSON = "application/json";
	/** HTTP content type for XML */
	public static final String CONTENT_TYPE_XML = "text/xml";
	/** HTTP content type for plain text */
	public static final String CONTENT_TYPE_TEXT = "text/plain";
	
	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
