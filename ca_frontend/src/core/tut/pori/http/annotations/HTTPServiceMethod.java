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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import core.tut.pori.http.Definitions;


/**
 * The objects returned by the method MUST BE of type or inherited type of core.tut.pori.http.Response or void if default Response is to be used
 *
 * Note: Your web services MUST be thread-safe and re-entrant
 * 
 * The methods are mapped based on the method name (name()) and the accepted methods (acceptedMethods()).
 * The name-method pairs must be unique:
 * <ul>
 *  <li>for the a single method with a name NAME there can be methods GET and POST, or</li>
 * 	<li>two separate methods can both have the name NAME if and only if their methods do not contain same method names (i.e. method 1 can have method name GET, and method 2 can have method name POST, but not GET)</li>
 *  <li>the method name does not have to be one specified in RFC2616 (or, as defined by the document) can be any valid String</li>
 * </ul>
 * 
 * Example 1:
 * 
 * \@HTTPServiceMethod(name="TEST", acceptedMethods={"GET"})<br>
 * public Response function1( ... ){ ... }
 * 
 * \@HTTPServiceMethod(name="TEST",acceptedMethods={"POST"})<br>
 * public Response function2( ... ){ ... }
 * 
 * <ul>
 *  <li>In this example, the calls for method TEST will be directed to function1 when HTTP GET is performed, and to function2 when HTTP POST is performed</li>
 *  <li>Note that function1 and function2 are separate functions, and will be treated as such, and can have different signatures (e.g. parameters)</li>
 * </ul> 
 *  
 *  
 *  Example 2:
 *  
 *  \@HTTPServiceMethod(name="TEST", acceptedMethods={"GET","POST"})<br>
 *  public Response function1( ... ){ ... }
 *  
 *  <ul><li>In this example, the calls for method TEST with HTTP GET or HTTP POST will both be directed to function1</li></ul>
 *  
 *  The method will be reachable in the path designated by the \@HTTPService annotation's name and this annotation's name, generally in the format /WebApplicationName/rest/SERVICE_NAME/METHOD_NAME
 */
@Retention(RUNTIME)
@Target({METHOD})
@Inherited
@Documented
public @interface HTTPServiceMethod {
	/**
	 * Name for the method
	 * 
	 * @return method name
	 */
	String name();
	
	/**
	 * 
	 * @return accepted HTTP Methods for this service method
	 */
	String[] acceptedMethods() default {Definitions.METHOD_GET,Definitions.METHOD_POST};
}
