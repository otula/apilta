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
package core.tut.pori.properties;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections4.map.UnmodifiableEntrySet;

/**
 * Implementation of an unmodifiable/immutable Properties class.
 * 
 * Attempting to call any setter methods of this class will results in UnsupportedOperationException
 */
public class UnmodifiableProperties extends Properties{
	/** for serialization */
	private static final long serialVersionUID = -449888750397691453L;
	private Properties _properties = null;
	
	/**
	 * 
	 */
	private UnmodifiableProperties() {
		super();
	}
	
	/**
	 * wrap the properties object to unmodifiable properties
	 * 
	 * @param properties
	 * @return new UnmodifiableProperties or null if null was passed
	 */
	public static UnmodifiableProperties unmodifiableProperties(Properties properties){
		if(properties == null){
			return null;
		}
		UnmodifiableProperties up = new UnmodifiableProperties();
		up._properties = properties;
		return up;
	}

	@Override
	public synchronized Object setProperty(String key, String value) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public synchronized void load(Reader reader) throws IOException, UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public synchronized void load(InputStream inStream) throws IOException, UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public void save(OutputStream out, String comments) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException, UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public synchronized Object put(Object key, Object value) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public synchronized Object remove(Object key) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public synchronized void putAll(Map<? extends Object, ? extends Object> t) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public synchronized void clear() throws UnsupportedOperationException{
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public Set<Object> keySet() {
		return Collections.unmodifiableSet(super.keySet());
	}

	@Override
	public Set<java.util.Map.Entry<Object, Object>> entrySet() {
		return UnmodifiableEntrySet.unmodifiableEntrySet(super.entrySet());
	}

	@Override
	public Collection<Object> values() {
		return Collections.unmodifiableCollection(super.values());
	}

	@Override
	public void store(Writer writer, String comments) throws IOException {
		_properties.store(writer, comments);
	}

	@Override
	public void store(OutputStream out, String comments) throws IOException {
		_properties.store(out, comments);
	}

	@Override
	public void storeToXML(OutputStream os, String comment) throws IOException {
		_properties.storeToXML(os, comment);
	}

	@Override
	public void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
		_properties.storeToXML(os, comment, encoding);
	}

	@Override
	public String getProperty(String key) {
		return _properties.getProperty(key);
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		return _properties.getProperty(key, defaultValue);
	}

	@Override
	public Enumeration<?> propertyNames() {
		return _properties.propertyNames();
	}

	@Override
	public Set<String> stringPropertyNames() {
		return _properties.stringPropertyNames();
	}

	@Override
	public void list(PrintStream out) {
		_properties.list(out);
	}

	@Override
	public void list(PrintWriter out) {
		_properties.list(out);
	}

	@Override
	public synchronized int size() {
		return _properties.size();
	}

	@Override
	public synchronized boolean isEmpty() {
		return _properties.isEmpty();
	}

	@Override
	public synchronized Enumeration<Object> keys() {
		return _properties.keys();
	}

	@Override
	public synchronized Enumeration<Object> elements() {
		return _properties.elements();
	}

	@Override
	public synchronized boolean contains(Object value) {
		return _properties.contains(value);
	}

	@Override
	public boolean containsValue(Object value) {
		return _properties.containsValue(value);
	}

	@Override
	public synchronized boolean containsKey(Object key) {
		return _properties.containsKey(key);
	}

	@Override
	public synchronized Object get(Object key) {
		return _properties.get(key);
	}

	@SuppressWarnings("sync-override")
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((_properties == null) ? 0 : _properties.hashCode());
		return result;
	}

	@SuppressWarnings("sync-override")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		UnmodifiableProperties other = (UnmodifiableProperties) obj;
		if (_properties == null) {
			if (other._properties != null)
				return false;
		} else if (!_properties.equals(other._properties))
			return false;
		return true;
	}
}
