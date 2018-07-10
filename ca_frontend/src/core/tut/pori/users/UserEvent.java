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
package core.tut.pori.users;

import org.springframework.context.ApplicationEvent;

/**
 * Event class used for notifying changes in the user details. By default the event is of type {@link EventType#USER_MODIFIED}.
 *
 */
public class UserEvent extends ApplicationEvent{
	private static final long serialVersionUID = -7592873142973183830L;
	private EventType _type = EventType.USER_MODIFIED;
	private UserIdentity _userId = null;
		
	/**
	 * Type of the event.
	 *
	 */
	public enum EventType{
		/** New user has been created, the details of the new user are contained in this event. */
		USER_CREATED,
		/** user was modified and the event contains the new, updated details. */
		USER_MODIFIED,
		/** User has been removed and does not exist in the system anymore. The last known details of the user are contained in this event. */
		USER_REMOVED,
		/** User has given authorization for a specified service type(s) */
		USER_AUTHORIZATION_GIVEN,
		/** User has revoked authorization for a specified service type(s)*/
		USER_AUTHORIZATION_REVOKED
	} // enum EventType

	/**
	 * 
	 * @param source class of the event creator
	 * @param userId
	 */
	public UserEvent(Class<?> source, UserIdentity userId) {
		super(source);
		_userId = userId;
	}
	
	/**
	 * 
	 * @param source class of the event creator
	 * @param userId
	 * @param type
	 */
	public UserEvent(Class<?> source, UserIdentity userId, EventType type) {
		super(source);
		_userId = userId;
		_type = type;
	}

	/**
	 * @return the userId
	 */
	public UserIdentity getUserId() {
		return _userId;
	}

	/**
	 * @return the type
	 */
	public EventType getType() {
		return _type;
	}

	@Override
	public Class<?> getSource() {
		return (Class<?>) super.getSource();
	}
}
