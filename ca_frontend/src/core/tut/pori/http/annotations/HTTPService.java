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
package core.tut.pori.http.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation for making a class a Service.
 * 
 * The method will be reachable in the path designated by the \@HTTPService annotation's name, generally in the format /WebApplicationName/rest/SERVICE_NAME
 *
 * The service names MUST be unique, an attempt to declare multiple methods with identical (case sensitive) names will result in an error on application context (service) initialization.
 */
@Retention(RUNTIME)
@Target({TYPE})
@Inherited
@Documented
public @interface HTTPService {
	/**
	 * 
	 * @return name of the service
	 */
	String name();
}
