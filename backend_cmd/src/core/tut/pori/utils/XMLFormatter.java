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

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import core.tut.pori.http.Response;
import core.tut.pori.http.ResponseData;


/**
 * XML formatter.
 * 
 * This class can be used to marshal objects to xml output, and unmarshal objects from xml input.
 */
public class XMLFormatter {
	private static final Logger LOGGER = Logger.getLogger(XMLFormatter.class);
	private boolean _omitXMLDeclaration = false;
	private boolean _throwOnError = true;
	
	/**
	 * 
	 * @param string
	 * @param cls
	 * @return the object or null if null/empty string
	 * @throws IllegalArgumentException on bad xml
	 */
	@SuppressWarnings("unchecked")
	public <T> T toObject(String string, Class<T> cls) throws IllegalArgumentException{
		if(StringUtils.isBlank(string)){
			LOGGER.debug("Input string was null.");
			return null;
		}
		T retval = null;
		try (StringReader reader = new StringReader(string)) {
			JAXBContext context = JAXBContext.newInstance(cls);
			Unmarshaller um = createUnMarshaller(context);
			Object o = um.unmarshal(reader);
			if(o.getClass() != cls){
				throw new IllegalArgumentException("Contents not of expected type.");
			}else{
				retval = (T) o;
			}
		} catch (JAXBException ex) {
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to parse xml.");
		}
		return retval;
	}
	
	/**
	 * 
	 * @param in
	 * @param cls
	 * @return the object or null if null in
	 * @throws IllegalArgumentException on bad xml
	 */
	@SuppressWarnings("unchecked")
	public <T> T toObject(InputStream in, Class<T> cls) throws IllegalArgumentException{
		if(in == null){
			LOGGER.debug("Input was null.");
			return null;
		}
		T retval = null;
		try{
			JAXBContext context = JAXBContext.newInstance(cls);
			Unmarshaller um = createUnMarshaller(context);
			Object o = um.unmarshal(in);
			if(o.getClass() != cls){
				throw new IllegalArgumentException("Contents not of expected type.");
			}else{
				retval = (T) o;
			}
		} catch(JAXBException ex){
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to parse xml.");
		}
		return retval;
	}
	
	/**
	 * 
	 * @param in
	 * @param objectClass class of the object to create
	 * @param requiredClasses additional classes required, note: objectClass is automatically added to requiredClasses
	 * @return the object or null if bad/null input
	 * @throws IllegalArgumentException on bad xml
	 */
	@SuppressWarnings("unchecked")
	public <T> T toObject(InputStream in, Class<T> objectClass, Class<?> ...requiredClasses) throws IllegalArgumentException{
		if(in == null){
			LOGGER.debug("Input was null.");
			return null;
		}
		try{
			JAXBContext context = JAXBContext.newInstance(ArrayUtils.add(requiredClasses, objectClass));
			Unmarshaller um = createUnMarshaller(context);
			Object o = um.unmarshal(in);
			if(o.getClass() != objectClass){
				throw new IllegalArgumentException("Contents not of expected type.");
			}else{
				return (T) o;
			}
		} catch(JAXBException ex){
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to parse xml.");
		}
	}
	
	/**
	 * 
	 * @param node
	 * @param cls
	 * @return the node as an object
	 * @throws IllegalArgumentException on bad xml
	 */
	@SuppressWarnings("unchecked")
	public <T> T toObject(Node node, Class<T> cls) throws IllegalArgumentException{
		if(node == null){
			LOGGER.debug("Input node was null.");
			return null;
		}
		T retval = null;
		try{
			JAXBContext context = JAXBContext.newInstance(cls);
			Unmarshaller um = createUnMarshaller(context);
			Object o = um.unmarshal(node);
			if(o.getClass() != cls){
				throw new IllegalArgumentException("Contents not of expected type.");
			}else{
				retval = (T) o;
			}
		} catch(JAXBException ex){
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to parse xml.");
		}
		return retval;
	}
	
	/**
	 * 
	 * @param r annotated xml object
	 * @return the object as a xml string or null if null object
	 * @throws IllegalArgumentException on invalid object
	 */
	public String toString(Response r) throws IllegalArgumentException{
		if(r == null){
			LOGGER.debug("Input response was null.");
			return null;
		}
		String retval = null;
		try {
			JAXBContext context = null;
			ResponseData t = r.getResponseData();
			if(t == null){
				context = JAXBContext.newInstance(Response.class);
			}else{
				context = JAXBContext.newInstance(ArrayUtils.add(t.getDataClasses(), Response.class));
			}	
			Marshaller marshaller = createMarshaller(context);
			StringWriter w = new StringWriter();
			marshaller.marshal(r, w);
			retval = w.toString();
		} catch (JAXBException ex) {
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to generate xml.");
		}
		
		return retval;
	}
	
	/**
	 * 
	 * @param o annotated xml object
	 * @return the object as a xml string or null if null object
	 * @throws IllegalArgumentException on invalid object
	 */
	public <T> String toString(T o) throws IllegalArgumentException{
		if(o == null){
			LOGGER.debug("Input object was null.");
			return null;
		}
		String retval = null;
		try {
			Marshaller marshaller = createMarshaller(JAXBContext.newInstance(o.getClass()));
			StringWriter w = new StringWriter();
			marshaller.marshal(o, w);
			retval = w.toString();
		} catch (JAXBException ex) {
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to generate xml.");
		}
		
		return retval;
	}
	
	/**
	 * 
	 * @param doc
	 * @return the document as string or null if null document
	 * @throws IllegalArgumentException on invalid document
	 */
	public String toString(Document doc) throws IllegalArgumentException{
		if(doc == null){
			LOGGER.debug("Input document was null.");
			return null;
		}
		String result = null;
		try {
			Transformer tf = TransformerFactory.newInstance().newTransformer();
			if(_omitXMLDeclaration){
				tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			}else{
				tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			}
			StringWriter w = new StringWriter();
			tf.transform(new DOMSource(doc), new StreamResult(w));
			result = w.toString();
		} catch (TransformerFactoryConfigurationError | TransformerException ex) {
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Failed to generate xml.");
		}
		return result;
	}
	
	/**
	 * create and return new marshaller, and set the default values
	 * @param context
	 * @return marshaller for the given context
	 * @throws JAXBException
	 * @throws IllegalArgumentException 
	 */
	protected Marshaller createMarshaller(JAXBContext context) throws JAXBException, IllegalArgumentException{
		Marshaller m = context.createMarshaller();
		if(_throwOnError){
			m.setEventHandler(new ValidationEventHandler() {
				@Override
				public boolean handleEvent(ValidationEvent event ) {
					throw new IllegalArgumentException(event.getMessage(), event.getLinkedException());
				}
			});
		}
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		if(_omitXMLDeclaration){
			m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		}
		return m;
	}
	
	/**
	 * 
	 * @param context
	 * @return unmarshaller for the given context
	 * @throws JAXBException
	 * @throws IllegalArgumentException 
	 */
	protected Unmarshaller createUnMarshaller(JAXBContext context) throws JAXBException, IllegalArgumentException{
		Unmarshaller um = context.createUnmarshaller();
		if(_throwOnError){
			um.setEventHandler(new ValidationEventHandler() {
				@Override
				public boolean handleEvent(ValidationEvent event ) {
					throw new IllegalArgumentException(event.getMessage(), event.getLinkedException());
				}
			});
		}
		return um;
	}
	
	/**
	 * 
	 * @return true if xml declaration is omitted from the output
	 */
	public boolean isOmitXMLDeclaration() {
		return _omitXMLDeclaration;
	}

	/**
	 * 
	 * @param omitXML
	 */
	public void setOmitXMLDeclaration(boolean omitXML) {
		_omitXMLDeclaration = omitXML;
	}

	/**
	 * @return the throwOnError
	 */
	public boolean isThrowOnError() {
		return _throwOnError;
	}

	/**
	 * @param throwOnError the throwOnError to set
	 */
	public void setThrowOnError(boolean throwOnError) {
		_throwOnError = throwOnError;
	}
	
	/**
	 * 
	 * @param in
	 * @param dataClass
	 * @return the object or null if null input
	 * @throws IllegalArgumentException on bad xml
	 */
	public Response toResponse(InputStream in, Class<? extends ResponseData> dataClass) throws IllegalArgumentException{
		if(in == null){
			LOGGER.debug("Input was null.");
			return null;
		}
		Response retval = toObject(in, Response.class, dataClass);
		if(retval == null){
			throw new IllegalArgumentException("Contents not of expected type.");
		}
		
		ResponseData data = retval.getResponseData();
		if(data == null){
			LOGGER.warn("Response contains no data.");
		}else if(!data.getClass().equals(dataClass)){
			throw new IllegalArgumentException("Contents not of expected type: bad data.");
		}
		return retval;
	}
}
