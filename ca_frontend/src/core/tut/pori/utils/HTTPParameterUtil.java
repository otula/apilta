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
package core.tut.pori.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import core.tut.pori.http.Definitions;

/**
 * common HTTP parameter utility methods
 *
 */
public final class HTTPParameterUtil {
	private static final String[] DECODED_CONTENT_TYPES = new String[]{"x-www-form-urlencoded"}; // list of content types already decoded by the servlet container
	private static final Logger LOGGER = Logger.getLogger(HTTPParameterUtil.class);

	/**
	 * 
	 */
	private HTTPParameterUtil(){
		// nothing needed
	}

	/**
	 * 
	 * <p>separates parameter values based on the defined parameter separator (",")</p>
	 * 
	 * <p>This is a helper method for parsing parameter values. By default the map returned by HttpServlerRequest (e.g. getParameterMap())
	 * will not handle parameter values separated by ",". Also, it will do automatic URL de-coding causing "," and URL encoded comma "%2C" to appear as the same character, making further value separation impossible.
	 * Note that POST body parameters will NOT be separated by "," character because it is impossible to retrieve the raw body parameter list without implementing the entire HTTP request parsing manually through InputStreams.</p>
	 * 
	 * <p>e.g. the query string .../method?parameter=value1,value2 will generate a map in which "parameter" is associated with String[] = {"value1,value2"}, and not with String[] = {"value1","value2"}.</p>
	 * 
	 * <p>Similarly, the query string .../method?parameter=value1,value2&amp;parameter=value3 will generate a map in which "parameter" is associated with String[] = {"value1,value2","value3"}, and not with String[] = {"value1","value2","value3"}.</p>
	 * 
	 * <p>The returned map will contain parameters associated with String[] = {"value1", "value2"} in URL decoded form, preserving the encoded comma (e.g. String[] = {"value1,value2"} if the query string contained ?parameter=value1%2Cvalue2</p>
	 * 
	 * <p>This method is uses the HttpServletRequest's parameter map as its basis, and thus, the returned list may contain both URL parameters and url-encoded-form parameters from HTTP body.</p>
	 *
	 * 
	 * @param httpServletRequest
	 * @param decode if false, the strings inside the map will NOT be URL decoded
	 * @return the map of parameter or null if none or req was null, note if the parameter has no values, the associated List will be null (NOT empty list)
	 * @throws IllegalArgumentException on bad query string
	 */
	public static Map<String, List<String>> getParameterMap(HttpServletRequest httpServletRequest, boolean decode) throws IllegalArgumentException{
		if(httpServletRequest == null){
			return null;
		}
		Map<String, String[]> params = httpServletRequest.getParameterMap();	// the map may contain a mix of body (e.g. form) and URL params
		if(params == null || params.isEmpty()){
			return null;
		}
		
		boolean encodeBodyParams = !decode && isDecodedContent(httpServletRequest.getContentType()); // if decode was not requested, but the content was decoded by the container, re-encode it, this is because on some content-types the HttpServletRequest will decode the params even though encoded ones are requested
		String[] queryStringParams = StringUtils.split(httpServletRequest.getQueryString(), Definitions.SEPARATOR_URI_QUERY_PARAMS);	// get parameters strictly appearing in the uri {"param=value","param2=value2"}
		if(ArrayUtils.isEmpty(queryStringParams)){
			Map<String,List<String>> map = new HashMap<>(params.size());
			for(Map.Entry<String, String[]> e : params.entrySet()){	// convert arrays to lists, remove unnecessary empty arrays
				putConverted(map, e.getKey(), e.getValue(), encodeBodyParams);
			}
			return map;
		}

		Map<String, List<String>> queryStringParamMap = new HashMap<>(queryStringParams.length);
		try {
			for(int i=0;i<queryStringParams.length;++i){
				String[] parts = queryStringParams[i].split(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUE);	// split the parameters and their values
				if(parts.length == 2){
					List<String> previousValues = queryStringParamMap.get(parts[0]);
					String[] values = parts[1].split(Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES);	// split the values
					if(previousValues == null){	// create new list if one does not already exist
						previousValues = new ArrayList<>(values.length);
						queryStringParamMap.put(parts[0], previousValues);
					}
					for(int j=0;j<values.length;++j){
						if(decode){
							previousValues.add(URLDecoder.decode(values[j], Definitions.ENCODING_UTF8));
						}else{
							previousValues.add(values[j]);
						}				
					}
				}else if(parts.length > 2){	// this is most likely parameter=value=value which is if not entirely wrong, at least slightly ambiguous
					throw new IllegalArgumentException("Invalid query parameter: "+parts[0]);
				}	// else length == 1, we can ignore this, as in the later loop, this will automatically be replaced with null list
			}
		} catch (UnsupportedEncodingException ex) {	// this should not happen
			LOGGER.error(ex, ex);
			return null;
		}

		Map<String, List<String>> map = new HashMap<>(params.size());
		for(Map.Entry<String, String[]> e : params.entrySet()){
			String parameter = e.getKey();

			List<String> values = queryStringParamMap.get(parameter);
			if(values == null){	// not an uri parameter, assume it was parsed properly
				putConverted(map, parameter, e.getValue(), encodeBodyParams);
			}else{	// it was a query param, in this case discard whatever was in the original map
				map.put(parameter, values);
			}
		}

		return map;
	}

	/**
	 * 
	 * @param contentType
	 * @return true if the given content type denoted decoded content
	 */
	private static boolean isDecodedContent(String contentType){
		if(StringUtils.isBlank(contentType)){
			LOGGER.debug("Content type was null.");
			return false;
		}
		for(int i=0;i<DECODED_CONTENT_TYPES.length;++i){
			if(contentType.contains(DECODED_CONTENT_TYPES[i])){
				return true;
			}
		}
		return false;
	}

	/**
	 * helper method for converting the values array to List or setting null if empty
	 * 
	 * @param map
	 * @param key
	 * @param values
	 * @param encode if true the values will be URL encoded before applying to the list in the list
	 * @throws IllegalArgumentException
	 */
	private static final void putConverted(Map<String, List<String>> map, String key, String[] values, boolean encode) throws IllegalArgumentException{
		if(ArrayUtils.isEmpty(values) || (values.length == 1 && StringUtils.isBlank(values[0]))){
			map.put(key, null);
		}else if(encode){
			try {
				ArrayList<String> valueList = new ArrayList<>(values.length);
				for(int i=0;i<values.length;++i){	
					valueList.add(URLEncoder.encode(values[i], Definitions.ENCODING_UTF8));
				}
				map.put(key, valueList);
			} catch (UnsupportedEncodingException ex) { // should never happen
				LOGGER.error(ex, ex);
				throw new IllegalArgumentException("Invalid query parameter: "+key);
			}
		}else{
			map.put(key, Arrays.asList(values));
		}
	}
}
