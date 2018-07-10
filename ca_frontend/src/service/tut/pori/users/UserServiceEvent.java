package service.tut.pori.users;

import core.tut.pori.users.ExternalAccountConnection.UserServiceType;
import core.tut.pori.users.UserEvent;
import core.tut.pori.users.UserIdentity;

/**
 * User Event with extensions for user service events.
 * 
 */
public class UserServiceEvent extends UserEvent{
	private static final long serialVersionUID = 6070897020729593055L;
	private UserServiceType _serviceType = null;
	
	/**
	 * 
	 * 
	 * @param eventType
	 * @param serviceType optional service type
	 * @param source
	 * @param userId
	 */
	public UserServiceEvent(EventType eventType, UserServiceType serviceType, Class<?> source, UserIdentity userId) {
		super(source, userId, eventType);
		_serviceType = serviceType;
	}

	/**
	 * @return the serviceType or null if none specified
	 */
	public UserServiceType getUserServiceType() {
		return _serviceType;
	}
}
