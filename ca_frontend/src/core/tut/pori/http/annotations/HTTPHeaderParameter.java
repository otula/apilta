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

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * A header parameter for HTTPServiceMethod.
 * 
 * Note that the both the method name and value MUST follow the HTTP header standard, as defined in http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
 */
@Retention(RUNTIME)
@Target({PARAMETER})
@Inherited
@Documented
public @interface HTTPHeaderParameter {
	/**
	 * 
	 * @return name of the header
	 */
	String name();
	
	/**
	 * 
	 * @return header value
	 */
	String defaultValue() default "";
	
	/**
	 * 
	 * @return whether this parameter is required or not
	 */
	boolean required() default true;
}
