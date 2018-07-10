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

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * JSON formatter.
 * 
 * This class can be used to create formatter/parser for marshaling objects to JSON output, and unmarshaling objects from JSON input.
 * 
 * Creates a default GSON parser with extended support for ISODATE (ISO 8601) using java date objects.
 */
public final class JSONFormatter {
	/**
	 * 
	 */
	private JSONFormatter(){
		// nothing needed
	}
	
	/**
	 * Initialize Gson serializer with an ISO Date parser
	 * @return new serializer instance
	 */
	public static Gson createGsonSerializer(){
		GsonBuilder builder = new GsonBuilder();
		registerDateAdapters(builder);
		return builder.create();
	}

	/**
	 * Register the ISODate serializer/deserializr for the given builder
	 * 
	 * @param builder
	 */
	public static void registerDateAdapters(GsonBuilder builder) {
		builder.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {

			@Override
			public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
				if(src == null){
					return null;
				}else{
					return new JsonPrimitive(StringUtils.dateToISOString(src));
				}
			}
		});
		
		builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {

			@Override
			public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				return (json == null ? null : StringUtils.ISOStringToDate(json.getAsString()));
			}
		});
	}
}
