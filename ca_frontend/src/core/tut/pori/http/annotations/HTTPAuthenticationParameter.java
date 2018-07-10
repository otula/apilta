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
 * Define the method parameter to be a special authentication parameter.
 * 
 * Note that this annotation can only be used in combination with classes or subclass of AuthenticationParameter, 
 * an attempt to the contrary will result in an error on web application context initialization.
 */
@Retention(RUNTIME)
@Target({PARAMETER})
@Inherited
@Documented
public @interface HTTPAuthenticationParameter {	
	/**
	 * 
	 * @return whether authenticated user is required
	 */
	boolean required() default true;
	/**
	 * 
	 * @return whether login prompt should be shown (with 401 Unauthorized), if false, this will simply return 403 FORBIDDEN
	 */
	boolean showLoginPrompt() default false;
}
