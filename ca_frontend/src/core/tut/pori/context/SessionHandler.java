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

import org.apache.log4j.Logger;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;

import core.tut.pori.users.UserIdentity;

/**
 * Accessible interface to active Sessions within the web application (service) context.
 * 
 * This class can be used to register new sessions and retrieve information for a particular session. 
 * The session information is provided for all services, though the session management is generally handled internally and modifying the active sessions may cause undefined behavior.
 * 
 * One should not initialize this handler directly, as an instantiated version is available from ServiceInitializer.
 */
public class SessionHandler {
	private static final Logger LOGGER = Logger.getLogger(SessionHandler.class);
	private static SessionHandler _handler = new SessionHandler();
	private SessionHandlerPrivate _handlerPrivate = null;

	/**
	 * 
	 * @param handler
	 */
	private static synchronized void setHandler(SessionHandlerPrivate handler){
		if(handler == null){
			LOGGER.debug("Removing handler...");
		}else if(_handler._handlerPrivate != null){
			LOGGER.warn("Replacing previous handler...");
		}
		_handler.setHandlerPrivate(handler);
	}

	/**
	 * 
	 * @return the session handler
	 */
	public static SessionHandler getSessionHandler(){
		return _handler;
	}

	/**
	 * 
	 */
	private SessionHandler(){
		// nothing needed
	}

	/**
	 * @param sessionId
	 * @return session information
	 * @see org.springframework.security.core.session.SessionRegistry#getSessionInformation(java.lang.String)
	 */
	public SessionInformation getSessionInformation(String sessionId) {
		if(_handlerPrivate == null){
			LOGGER.debug("Session registry not available.");
			return null;
		}else{
			LOGGER.debug("Retrieving session information for sessionId: "+sessionId);
			return _handlerPrivate.getSessionRegistry().getSessionInformation(sessionId); // in principle we should synchronize and check if registry is available, but in practice it will never NOT be available
		}
	}

	/**
	 * @param sessionId
	 * @param userId
	 * @see org.springframework.security.core.session.SessionRegistry#registerNewSession(java.lang.String, java.lang.Object)
	 * @throws IllegalStateException if registry is not available
	 */
	public void registerNewSession(String sessionId, UserIdentity userId) throws IllegalStateException{
		if(_handlerPrivate == null){
			throw new IllegalStateException("Session registry not available.");
		}
		LOGGER.debug("Registering new session for sessionId: "+sessionId+", userId: "+userId.getUserId());
		_handlerPrivate.getSessionRegistry().registerNewSession(sessionId, userId); // in principle we should synchronize and check if registry is available, but in practice it will never NOT be available
	}

	/**
	 * This is essentially the same as calling LoginHandler.authentice() and registerNewSession()
	 * 
	 * @param sessionId
	 * @param userId
	 * @throws IllegalStateException
	 */
	public void registerAndAuthenticate(String sessionId, UserIdentity userId) throws IllegalStateException{
		LoginHandler.authenticate(userId);
		registerNewSession(sessionId, userId);
	}

	/**
	 * @param sessionId
	 * @see org.springframework.security.core.session.SessionRegistry#removeSessionInformation(java.lang.String)
	 */
	public void removeSessionInformation(String sessionId) {
		if(_handlerPrivate == null){
			LOGGER.debug("Session registry not available.");
		}else{
			LOGGER.debug("Removing session information for sessionId: "+sessionId);
			_handlerPrivate.getSessionRegistry().removeSessionInformation(sessionId); // in principle we should synchronize and check if registry is available, but in practice it will never NOT be available
		}
	}

	/**
	 * Note: this is NOT synchronized, which means that it may be possible for the user to re-login whilst the operation is in progress, if this is called for user account removal,
	 * remember to FIRST remove the account to make sure user cannot re-login
	 * 
	 * @param userId
	 */
	public void removeSessionInformation(UserIdentity userId){
		if(_handlerPrivate == null){ // in principle we should synchronize and check if registry is available, but in practice it will never NOT be available
			LOGGER.debug("Session registry not available.");
		}else{
			LOGGER.debug("Removing session information for userId: "+userId.getUserId());
			SessionRegistry registry = _handlerPrivate.getSessionRegistry();
			for(Object principal : registry.getAllPrincipals()){ // check all principals, note that simply asking for all sessions for the given userId object (principal) may not work as there might be slight differences between the passed object and the one known by the system, which may fool the equals check
				if(principal.getClass() != UserIdentity.class){
					continue;
				}
				UserIdentity pIdentity = (UserIdentity) principal;
				if(UserIdentity.equals(userId, (UserIdentity) principal)){
					for(SessionInformation sessionInformation : registry.getAllSessions(pIdentity, true)){
						_handlerPrivate.getSessionRegistry().removeSessionInformation(sessionInformation.getSessionId());
					} // for session information
				} // if equals
			} // for
		} // else
	}

	/**
	 * @param handlerPrivate the handlerPrivate to set
	 */
	private void setHandlerPrivate(SessionHandlerPrivate handlerPrivate) {
		_handlerPrivate = handlerPrivate;
	}

	/**
	 * Private instance of session handler.
	 * 
	 * Created as a bean.
	 */
	private static class SessionHandlerPrivate{
		private SessionRegistry _sessionRegistry = null;

		/**
		 * 
		 * @param sessionRegistry
		 */
		@SuppressWarnings("unused")
		public SessionHandlerPrivate(SessionRegistry sessionRegistry){
			_sessionRegistry = sessionRegistry;
		}

		/**
		 * Called by bean initialization
		 */
		@SuppressWarnings("unused")
		public void initialized(){
			LOGGER.debug("Initialized.");
			setHandler(this);
		}

		/**
		 * Called when bean is destroyed
		 */
		@SuppressWarnings("unused")
		public void destroyed(){
			LOGGER.debug("Destroyed.");
			setHandler(null);
		}

		/**
		 * @return the sessionRegistry
		 */
		public SessionRegistry getSessionRegistry() {
			return _sessionRegistry;
		}
	} // class SessionHandlerPrivate
}
