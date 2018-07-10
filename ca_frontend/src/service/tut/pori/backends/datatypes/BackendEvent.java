/**
 * Copyright 2016 Tampere University of Technology, Pori Department
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
package service.tut.pori.backends.datatypes;

import org.springframework.context.ApplicationEvent;

/**
 * Event class used for notifying changes in the back end details. By default the event is of type {@link EventType#BACKEND_REMOVED}.
 *
 */
public class BackendEvent extends ApplicationEvent {
	private static final long serialVersionUID = -9103811512895890183L;
	private Long _backendId = null;
	private EventType _type = EventType.BACKEND_REMOVED;
	
	/**
	 * Type of the event.
	 * 
	 */
	public enum EventType{
		/** A back end was removed, the details of the back end are in this event. */
		BACKEND_REMOVED
	} // enum EventType
	
	/**
	 * @param source class of the event creator
	 * @param backendId
	 */
	public BackendEvent(Class<?> source, Long backendId) {
		super(source);
		_backendId = backendId;
	}
	
	/**
	 * @param source class of the event creator
	 * @param backendId
	 * @param type 
	 */
	public BackendEvent(Class<?> source, Long backendId, EventType type) {
		super(source);
		_backendId = backendId;
		_type = type;
	}

	@Override
	public Class<?> getSource() {
		return (Class<?>) super.getSource();
	}

	/**
	 * @return the backendId
	 */
	public Long getBackendId() {
		return _backendId;
	}

	/**
	 * @return the type
	 */
	public EventType getType() {
		return _type;
	}
}
