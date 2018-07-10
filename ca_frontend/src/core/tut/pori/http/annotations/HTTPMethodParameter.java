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
 * Annotation for defining a single parameter within a HTTPServiceMethod.
 * 
 */
@Retention(RUNTIME)
@Target({PARAMETER})
@Inherited
@Documented
public @interface HTTPMethodParameter {
	/**
	 * 
	 * @return name of the parameter
	 */
	String name();
	
	/**
	 * The parameter value should be defined as it would appear in a method request with values separated by , and URL encoded if needed.
	 * 
	 * @return default value for this parameter
	 */
	String defaultValue() default "";
	
	/**
	 * 
	 * @return whether this parameter is required or not
	 */
	boolean required() default true;
	
	/**
	 * If set to true, the value of body shall be passed on to this parameter, and the default URL parameter lookup will be skipped,
	 * in this case the name() of the parameter is ignored.
	 * 
	 * Note: there can be ONLY ONE body parameter per method
	 * 
	 * @return whether this is a body parameter
	 */
	boolean bodyParameter() default false;
}
