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
package core.tut.pori.context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import core.tut.pori.http.Definitions;
import core.tut.pori.http.Response;
import core.tut.pori.http.ServiceRequest;
import core.tut.pori.http.Response.Status;
import core.tut.pori.http.annotations.HTTPAuthenticationParameter;
import core.tut.pori.http.annotations.HTTPHeaderParameter;
import core.tut.pori.http.annotations.HTTPMethodParameter;
import core.tut.pori.http.annotations.HTTPService;
import core.tut.pori.http.annotations.HTTPServiceMethod;
import core.tut.pori.http.headers.HTTPHeader;
import core.tut.pori.http.parameters.AuthenticationParameter;
import core.tut.pori.http.parameters.HTTPParameter;
import core.tut.pori.users.UserIdentity;
import core.tut.pori.utils.StringUtils;

/**
 * This class initializes and handles the Service registry. 
 * 
 * Instances of the Service objects can be retrieved using this class, though generally the instances do not have to be called directly by any class or object.
 * 
 * The service initialization happens automatically based on the rest servlet configuration and annotated classes, 
 * the services are automatically invoked when called through the associated web application URI paths.
 *
 * One should not initialize this handler directly, as an instantiated version is available from ServiceInitializer.
 */
public class ServiceHandler {
	private static final Logger LOGGER = Logger.getLogger(ServiceHandler.class);
	private static final String SERVLET_CONFIGURATION_FILE = "rest-servlet.xml";
	private ClassPathXmlApplicationContext _context = null;
	private Map<String, Service> _services = null;	// service name-service map

	/**
	 * 
	 * @throws BeansException on failure
	 */
	public ServiceHandler() throws BeansException{
		initialize();
	}

	/**
	 * 
	 * @throws BeansException on failure
	 */
	private void initialize() throws BeansException{
		LOGGER.debug("Initializing handler...");
		Date started = new Date();
		_context = new ClassPathXmlApplicationContext(ServiceInitializer.getConfigHandler().getConfigFilePath()+SERVLET_CONFIGURATION_FILE);

		LOGGER.debug("Class Path XML Context initialized in "+StringUtils.getDurationString(started, new Date()));

		Map<String, Object> services = _context.getBeansWithAnnotation(HTTPService.class);
		int count = services.size();
		LOGGER.info("Found "+count+" service(s).");
		_services = new HashMap<>(count);

		for(Iterator<Object> iter = services.values().iterator();iter.hasNext();){
			addService(iter.next());
		}

		LOGGER.debug("Service Handler initialized in "+StringUtils.getDurationString(started, new Date()));
	}

	/**
	 * close this Service handler and release are resources associated with it
	 */
	public void close(){
		_context.close();
		_context = null;
		_services = null;
	}

	/**
	 * remove leading and trailing separator / from the name if any are present
	 * 
	 * @param name
	 * @return the name or null if name was invalid
	 */
	private String clearSeparators(String name) {
		if(org.apache.commons.lang3.StringUtils.isBlank(name)){
			return null;
		}

		boolean startsWith = name.startsWith(Definitions.SEPARATOR_URI_PATH);
		boolean endsWith = name.endsWith(Definitions.SEPARATOR_URI_PATH);
		if(startsWith || endsWith){
			name = name.substring((startsWith ? 1 : 0), (endsWith ? name.length()-1 : name.length()));	// chop from beginning and end if required
			if(org.apache.commons.lang3.StringUtils.isBlank(name)){
				name = null;
			}
		}
		return name;
	}

	/**
	 * Invoke a service described by the given request.
	 * 
	 * @param serviceRequest
	 * @return a Response object with status notifying about the result of the invocation.
	 */
	public Response invoke(ServiceRequest serviceRequest){
		if(!ServiceRequest.isValid(serviceRequest)){
			return new Response(Status.BAD_REQUEST);
		}
		String serviceName = serviceRequest.getServiceName();
		Service service = _services.get(serviceName);
		if(service == null){
			return new Response(Status.NOT_FOUND, "No such service: "+serviceName);
		}

		String methodName = serviceRequest.getMethodName();
		String httpMethod = serviceRequest.getHttpMethod();
		ServiceMethod method = service.getMethod(httpMethod, methodName);
		if(method == null){
			return new Response(Status.NOT_FOUND, "No such method: "+httpMethod+" "+methodName);
		}

		int parameterCount = method.getParameterCount();
		if(parameterCount < 1){	// no required arguments
			return invoke(method.getMethod(), null, method.getReturnType(), service.getServiceObject());
		}

		Object[] args = new Object[parameterCount];

		try { // catch instantation exceptions, which should never really happen
			AuthParameter authParam = method.getAuthParam();
			if(!setAuthParam(authParam, args, serviceRequest)){ // check if authentication is required
				Response response = method.getReturnType().newInstance();
				if(authParam.isShowLoginPrompt()){
					response.setStatus(Status.UNAUTHORIZED);
				}else{
					response.setStatus(Status.FORBIDDEN);
				}
				return response;
			}

			try{ // check if the given parameters are valid
				setHeaderParams(args, method.getHeaderParams(), serviceRequest);
				setMethodParams(args, method.getMethodParams(), serviceRequest);
			}catch(IllegalArgumentException ex){
				LOGGER.debug(ex, ex);
				Response response = method.getReturnType().newInstance();
				response.setStatus(Status.BAD_REQUEST);
				response.setMessage(ex.getMessage());
				return response;
			}
		} catch (InstantiationException | IllegalAccessException ex) { // should not happen
			LOGGER.error(ExceptionUtils.getStackTrace(ex));
			return new Response(Status.INTERNAL_SERVER_ERROR);
		}

		return invoke(method.getMethod(), args, method.getReturnType(), service.getServiceObject());
	}

	/**
	 * 
	 * @param method
	 * @param methodArgs
	 * @param returnType
	 * @param serviceObject
	 * @return response
	 */
	private Response invoke(Method method, Object[] methodArgs, Class<? extends Response> returnType, Object serviceObject){
		try{
			try {
				Object retval = method.invoke(serviceObject, methodArgs);
				if(retval == null){
					return returnType.newInstance();	// return default OK for void
				}else{
					return (Response) retval;	// this is checked by the initializer to be the only possible return type
				}
			} catch (InvocationTargetException ex) {
				Throwable cause = ex.getCause();
				LOGGER.error(ex, cause);	// print exception and the actual cause
				Response response = returnType.newInstance();
				if(cause instanceof IllegalArgumentException){	// accept as bad request
					response.setStatus(Status.BAD_REQUEST);
					response.setMessage(cause.getMessage());
				}else{	// this should have been caught by the implementation
					response.setStatus(Status.INTERNAL_SERVER_ERROR);
				}
				return response;
			} 
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException ex) {	// should not happen
			LOGGER.error(ExceptionUtils.getStackTrace(ex));
			return new Response(Status.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 
	 * @param methodArgs methodArgs argument list for the method to be called, the parameter will be set to this array to its correct index if needed
	 * @param params can be null
	 * @param serviceRequest
	 * @throws IllegalArgumentException on bad request argument
	 */
	private void setMethodParams(Object[] methodArgs, Map<String,MethodParameter> params, ServiceRequest serviceRequest) throws IllegalArgumentException{
		if(params == null){
			return;
		}

		Map<String,List<String>> paramMap = serviceRequest.getRawParameters();

		try {
			for(Map.Entry<String, MethodParameter> e : params.entrySet()){
				String paramName = e.getKey();
				MethodParameter param = e.getValue();
				HTTPParameter p = param.getParameter().newInstance();
				p.setParameterName(paramName);
				if(param.isBodyParameter()){
					p.initialize(serviceRequest.getBody());
				}else{
					List<String> values = (paramMap == null ? null : paramMap.get(paramName));
					if(param.isRequired() && values == null){
						throw new IllegalArgumentException("Requested parameter "+paramName+" was not found.");
					}

					if(values == null){
						List<String> defaultValues = param.getDefaultValues();
						if(defaultValues != null){	// if default value has been given
							if(defaultValues.size() == 1){
								p.initializeRaw(defaultValues.get(0));
							}else{
								p.initializeRaw(defaultValues);
							}
						}	
					}else if(values.size() == 1){
						p.initializeRaw(values.get(0));
					}else{
						p.initializeRaw(values);
					}
				}
				methodArgs[param.getParameterIndex()] = p;
			}
		} catch (InstantiationException | IllegalAccessException ex) {	// this should not happen
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to parse parameters.");
		}
	}

	/**
	 * 
	 * @param methodArgs argument list for the method to be called, the parameter will be set to this array to its correct index if needed
	 * @param params can be null
	 * @param serviceRequest
	 * @throws IllegalArgumentException on bad request argument
	 */
	private void setHeaderParams(Object[] methodArgs, Map<String, HeaderParameter> params, ServiceRequest serviceRequest) throws IllegalArgumentException{
		if(params == null){
			return;
		}

		try {
			for(Map.Entry<String, HeaderParameter> e : params.entrySet()){
				String headerName = e.getKey();
				HeaderParameter param = e.getValue();
				String value = serviceRequest.getHeaderValue(headerName);
				if(param.isRequired() && value == null){
					throw new IllegalArgumentException("Requested Header field "+headerName+" was not found.");
				}

				HTTPHeader header = param.getParameter().newInstance();
				header.setName(headerName);
				if(value == null){
					value = param.getDefaultValue();
				}
				header.setValue(value);
				methodArgs[param.getParameterIndex()] = header;
			}
		} catch (InstantiationException | IllegalAccessException ex) {	// should not happen
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to parse HTTP headers.");
		}
	}

	/**
	 * helper method for setting the authentication parameter to method arguments
	 * 
	 * @param authParam can be null
	 * @param methodArgs argument list for the method to be called, the parameter will be set to this array to its correct index if needed
	 * @param serviceRequest
	 * @return true on success (=user was authenticated, and authentication was required)
	 */
	private boolean setAuthParam(AuthParameter authParam, Object[] methodArgs, ServiceRequest serviceRequest){
		if(authParam != null){	// authentication is at least requested
			UserIdentity authenticatedUser = serviceRequest.getAuthenticatedUser();
			if(authParam.isRequired() && authenticatedUser == null){	// authentication is required, but not provided
				return false;
			}else{
				try {
					AuthenticationParameter p = authParam.getParameter().newInstance();
					if(!p.initialize(serviceRequest)){
						LOGGER.warn("Failed to initialize authentication parameter.");
						return false;
					}
					methodArgs[authParam.getParameterIndex()] = p;
				} catch (InstantiationException | IllegalAccessException ex) {	// this should not happen...
					LOGGER.error(ex, ex);
					return false;	// ...but if it does, do not allow further user access
				}
			}
		}
		return true;
	}

	/**
	 * 
	 * @param object
	 * @throws IllegalArgumentException
	 */
	private void addService(Object object) throws IllegalArgumentException {
		Class<?> cls = object.getClass();
		HTTPService serviceAnnotation = cls.getAnnotation(HTTPService.class);

		Service service = new Service(object);
		Method[] methods = cls.getMethods();
		for(int i=0;i<methods.length;++i){
			HTTPServiceMethod methodAnnotation = methods[i].getAnnotation(HTTPServiceMethod.class);
			if(methodAnnotation != null){
				String[] am = methodAnnotation.acceptedMethods();
				if(am == null || am.length < 1){
					throw new IllegalArgumentException("The method "+methods[i].toString()+" does not allow any of HTTP Methods.");
				}
				for(int j=0;j<am.length;++j){	// separate to different pairs based on HTTP method
					service.addMethod(methods[i],clearSeparators(methodAnnotation.name()), am[j]);
				}
			}	// if
		}	// for
		if(methods.length < 1){
			LOGGER.warn("Ignored service "+cls.toString()+": no valid methods defined.");
			return;
		}

		String name = clearSeparators(serviceAnnotation.name());
		if(name == null){
			throw new IllegalArgumentException("Invalid service name for "+cls.toString());
		}else if(_services.containsKey(name)){
			throw new IllegalArgumentException("Duplicate service name "+name+" for "+cls.toString());
		}else{
			_services.put(name, service);
		}
	}

	/**
	 * Defines a single service invokable by the handler.
	 *
	 */
	private static class Service{
		private Map<Pair<String, String>, ServiceMethod> _methods = null;	// method name/httpMethod-method map, where httpMethod is the HTTP verb, e.g. POST or GET
		private Object _service = null; // service object represented as spring bean

		/**
		 * 
		 * @param service
		 */
		public Service(Object service){
			_service = service;
			_methods = new HashMap<>();
		}

		/**
		 * 
		 * @param method
		 * @param methodName
		 * @param httpMethod e.g. POST or GET
		 * @throws IllegalArgumentException on bad methodName
		 */
		public void addMethod(Method method, String methodName, String httpMethod) throws IllegalArgumentException{
			if(methodName == null){
				throw new IllegalArgumentException("Invalid methodName for "+method.toString());
			}else{
				Pair<String, String> methodPair = Pair.of(methodName, httpMethod);
				if(_methods.containsKey(methodPair)){			
					throw new IllegalArgumentException("Duplicate methodName: "+methodName+" and/or method type "+httpMethod+" for "+method.toString());
				}else{
					_methods.put(methodPair, new ServiceMethod(method));
				}
			}

		}

		/**
		 * 
		 * @param httpMethod
		 * @param methodName
		 * @return the method or null if none found
		 */
		public ServiceMethod getMethod(String httpMethod, String methodName){
			return _methods.get(Pair.of(methodName, httpMethod));
		}

		/**
		 * 
		 * @return the service object
		 */
		public Object getServiceObject(){
			return _service;
		}
	} // class Service

	/**
	 * Defines a single method invokable through a Service.
	 *
	 */
	private static class ServiceMethod{
		private AuthParameter _authParam = null;
		private Map<String,HeaderParameter> _headerParams = null;	// parameter name-parameter_type map
		private Method _method = null;
		private Map<String,MethodParameter> _methodParams = null;	// parameter name-parameter_type map
		private int _parameterCount = 0;
		private Class<? extends Response> _returnType = null;

		/**
		 * 
		 * @param method
		 * @throws IllegalArgumentException on bad Method
		 */
		public ServiceMethod(Method method) throws IllegalArgumentException{
			_method = method;
			initialize();
		}

		/**
		 * 
		 * @throws IllegalArgumentException on bad parameter
		 */
		@SuppressWarnings("unchecked")
		private void initialize() throws IllegalArgumentException{
			Class<?> responseClass= _method.getReturnType();
			if(responseClass.equals(Void.TYPE)){
				_returnType = Response.class;
			}else if(!Response.class.isAssignableFrom(responseClass)){	// check for valid return type
				throw new IllegalArgumentException("Return type not "+Void.TYPE.toString()+" or "+Response.class.toString()+" for "+_method.toString());
			}else{
				_returnType = (Class<? extends Response>) responseClass;
			}

			Annotation[][] annotations = _method.getParameterAnnotations();	// get annotation "map"
			if(annotations.length < 1){	// no parameters
				return;
			}
			_methodParams = new HashMap<>();
			_headerParams = new HashMap<>();

			Class<?>[] paramTypes = _method.getParameterTypes();	// get types of the parameters
			boolean bodyParameterGiven = false;
			for(int i=0;i<annotations.length;++i){	// go through types
				if(AuthenticationParameter.class.isAssignableFrom(paramTypes[i])){	// check for authentication parameter
					if(_authParam != null){
						throw new IllegalArgumentException("Duplicate "+AuthenticationParameter.class.toString()+" in "+_method.toString());
					}

					HTTPAuthenticationParameter parameterAnnotation = null;
					for(Annotation annotation : annotations[i]){	// check that the required annotation is present
						if(annotation.annotationType() == HTTPAuthenticationParameter.class){
							parameterAnnotation = (HTTPAuthenticationParameter)annotation;
							break;
						}
					}
					if(parameterAnnotation == null){	// the annotation is missing
						throw new IllegalArgumentException("Annotation "+HTTPAuthenticationParameter.class.toString()+" is missing from: "+paramTypes[i].toString()+" in "+_method.toString());
					}

					_authParam = new AuthParameter((Class<? extends AuthenticationParameter>) paramTypes[i], i, parameterAnnotation.required(), parameterAnnotation.showLoginPrompt());	// not really "unchecked" cast
				}else if(HTTPParameter.class.isAssignableFrom(paramTypes[i])){	// if this is a http parameter
					HTTPMethodParameter parameterAnnotation = null;
					for(Annotation annotation : annotations[i]){	// check that the required annotation is present
						if(annotation.annotationType() == HTTPMethodParameter.class){
							parameterAnnotation = (HTTPMethodParameter)annotation;
							break;
						}
					}
					if(parameterAnnotation == null){	// the annotation is missing
						throw new IllegalArgumentException("Annotation "+HTTPMethodParameter.class.toString()+" is missing from: "+paramTypes[i].toString()+" in "+_method.toString());
					}

					boolean isBody = parameterAnnotation.bodyParameter();
					if(isBody){
						if(bodyParameterGiven){
							throw new IllegalArgumentException("Duplicate HTTP body paramater in "+_method.toString());
						}else{
							bodyParameterGiven = true;
						}
					}

					String name = parameterAnnotation.name();
					if(org.apache.commons.lang3.StringUtils.isBlank(name)){
						throw new IllegalArgumentException("Invalid parameter name "+name+" in "+_method.toString());
					}else if(_methodParams.containsKey(name)){
						throw new IllegalArgumentException("Duplicate parameter name "+name+" in "+_method.toString());
					}

					_methodParams.put(name, new MethodParameter(parameterAnnotation.defaultValue(), isBody, (Class<? extends HTTPParameter>) paramTypes[i], i,parameterAnnotation.required()));	// not really "unchecked" cast
				}else if(HTTPHeader.class.isAssignableFrom(paramTypes[i])){ // this is a header
					HTTPHeaderParameter parameterAnnotation = null;
					for(Annotation annotation : annotations[i]){	// check that the required annotation is present
						if(annotation.annotationType() == HTTPHeaderParameter.class){
							parameterAnnotation = (HTTPHeaderParameter)annotation;
							break;
						}
					}
					if(parameterAnnotation == null){	// the annotation is missing
						throw new IllegalArgumentException("Annotation "+HTTPHeaderParameter.class.toString()+" is missing from: "+paramTypes[i].toString()+" in "+_method.toString());
					}

					String name = parameterAnnotation.name();
					if(org.apache.commons.lang3.StringUtils.isBlank(name)){
						throw new IllegalArgumentException("Invalid header name "+name+" in "+_method.toString());
					}else if(_headerParams.containsKey(name)){
						throw new IllegalArgumentException("Duplicate header name "+name+" in "+_method.toString());
					}

					_headerParams.put(name, new HeaderParameter(parameterAnnotation.defaultValue(), (Class<? extends HTTPHeader>) paramTypes[i], i, parameterAnnotation.required()));	// not really "unchecked" cast
				}else{	// unknown type
					throw new IllegalArgumentException(paramTypes[i].toString()+" is not subclass of "+HTTPParameter.class.toString()+" in "+_method.toString());
				}

				Constructor<?>[] constructors = paramTypes[i].getConstructors();
				for(int j=0;j<constructors.length;++j){	// check that no-args constructor is present for the argument
					if(constructors[j].getParameterTypes().length < 1){
						constructors = null;
						break;
					}
				}
				if(constructors != null){
					throw new IllegalArgumentException("No no-args constructor available for the type: "+paramTypes[i].toString()+" in "+_method.toString());
				}
			}	// for types
			if(_headerParams.isEmpty()){
				_headerParams = null;	// empty list not needed
			}
			if(_methodParams.isEmpty()){
				_methodParams = null;	// empty list not needed
			}
			_parameterCount = annotations.length;	// get the argument count
		}

		/**
		 * @return the method
		 */
		public Method getMethod() {
			return _method;
		}

		/**
		 * @return the methodParams
		 */
		public Map<String, MethodParameter> getMethodParams() {
			return _methodParams;
		}

		/**
		 * @return the headerParams
		 */
		public Map<String, HeaderParameter> getHeaderParams() {
			return _headerParams;
		}

		/**
		 * @return the authParam
		 */
		public AuthParameter getAuthParam() {
			return _authParam;
		}

		/**
		 * @return the parameter count
		 */
		public int getParameterCount() {
			return _parameterCount;
		}

		/**
		 * @return the returnType
		 */
		public Class<? extends Response> getReturnType() {
			return _returnType;
		}
	} // class ServiceMethod

	/**
	 * Method parameter base class 
	 *
	 */
	private static abstract class Parameter{
		private int _parameterIndex = -1;

		/**
		 * 
		 * @param parameterIndex the index of this parameter in the method declaration
		 */
		public Parameter(int parameterIndex){
			_parameterIndex = parameterIndex;
		}

		/**
		 * @return the parameterIndex
		 */
		public int getParameterIndex() {
			return _parameterIndex;
		}
	} // class Parameter

	/**
	 * Defines a single method parameter assigned to service method.
	 * 
	 */
	private static class MethodParameter extends Parameter{
		private List<String> _defaultValues = null;
		private boolean _isBodyParameter = false;
		private Class<? extends HTTPParameter> _parameter = null;	// for instantiating new classes for parsing process, if null, null will be returned as value
		private boolean _required = false;

		/**
		 * 
		 * @param defaultValue
		 * @param isBodyParameter
		 * @param parameterClass
		 * @param parameterIndex
		 * @param required
		 */
		public MethodParameter(String defaultValue, boolean isBodyParameter, Class<? extends HTTPParameter> parameterClass, int parameterIndex, boolean required){
			super(parameterIndex);
			_parameter = parameterClass;
			if(org.apache.commons.lang3.StringUtils.isBlank(defaultValue)){
				_defaultValues = null;
			}else{
				_defaultValues = Arrays.asList(org.apache.commons.lang3.StringUtils.split(defaultValue, Definitions.SEPARATOR_URI_QUERY_PARAM_VALUES));
			}
			_required = required;
			_isBodyParameter = isBodyParameter;
		}

		/**
		 * @return the parameter
		 */
		public Class<? extends HTTPParameter> getParameter() {
			return _parameter;
		}

		/**
		 * @return the defaultValue or null if none available
		 */
		public List<String> getDefaultValues() {
			return _defaultValues;
		}

		/**
		 * @return the required
		 */
		public boolean isRequired() {
			return _required;
		}

		/**
		 * @return the isBodyParameter
		 */
		public boolean isBodyParameter() {
			return _isBodyParameter;
		}
	} // class MethodParamater

	/**
	 * A special authentication parameter, which defines whether user authentication should be required or not upon method invocation.
	 *
	 */
	private static class AuthParameter extends Parameter{
		private Class<? extends AuthenticationParameter> _parameter = null;
		private boolean _required = false;
		private boolean _showLoginPrompt = false;

		/**
		 * 
		 * @param parameter
		 * @param parameterIndex
		 * @param required
		 * @param showLoginPrompt
		 */
		public AuthParameter(Class<? extends AuthenticationParameter> parameter, int parameterIndex, boolean required, boolean showLoginPrompt){
			super(parameterIndex);
			_parameter = parameter;
			_required = required;
			_showLoginPrompt = showLoginPrompt;
		}

		/**
		 * @return the parameter
		 */
		public Class<? extends AuthenticationParameter> getParameter() {
			return _parameter;
		}

		/**
		 * @return the required
		 */
		public boolean isRequired() {
			return _required;
		}

		/**
		 * @return the showLoginPrompt
		 */
		public boolean isShowLoginPrompt() {
			return _showLoginPrompt;
		}
	} // class AuthParameter

	/**
	 * Defines a header parameter inside a Service method.
	 *
	 */
	private static class HeaderParameter extends Parameter{
		private String _defaultValue = null;
		private Class<? extends HTTPHeader> _parameter = null;
		private boolean _required = false;

		/**
		 * 
		 * @param defaultValue
		 * @param parameter
		 * @param parameterIndex
		 * @param required
		 */
		public HeaderParameter(String defaultValue, Class<? extends HTTPHeader> parameter, int parameterIndex, boolean required){
			super(parameterIndex);
			_parameter = parameter;
			_defaultValue = defaultValue;
			if(org.apache.commons.lang3.StringUtils.isBlank(_defaultValue)){
				_defaultValue = null;
			}
			_required = required;
		}

		/**
		 * @return the parameter
		 */
		public Class<? extends HTTPHeader> getParameter() {
			return _parameter;
		}

		/**
		 * @return the defaultValue
		 */
		public String getDefaultValue() {
			return _defaultValue;
		}

		/**
		 * @return the required
		 */
		public boolean isRequired() {
			return _required;
		}
	} // class HeaderParameter
}
