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

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import core.tut.pori.users.UserIdentity;

/**
 * Acquire a lock per-thread, per-user identity, multiple instance of this class will be entirely independent, and will NOT block each-other
 * UserIdentityLock userIdentityLock = UserIdentityLock();
 * 
 * Usage:
 * userIdentityLock.acquire(userId);
 * try{
 *  ...
 * }finally{
 *  userIdentityLock.release(userId);
 * }
 */
public class UserIdentityLock {
	private static final Logger LOGGER = Logger.getLogger(UserIdentityLock.class);
	private HashMap<Long, LockCounter> _locks = new HashMap<>();

	/**
	 * Blocks until the lock for the given user identity has been acquired.
	 * 
	 * @param userId
	 * @throws IllegalArgumentException on invalid user identity
	 */
	public void acquire(UserIdentity userId) throws IllegalArgumentException {
		if(!UserIdentity.isValid(userId)){
			throw new IllegalArgumentException("Invalid user identity.");
		}
		
		Long userIdValue = userId.getUserId();
		ReentrantLock lock = null;
		synchronized (_locks) {
			LockCounter counter = _locks.get(userIdValue);
			if(counter == null){
				LOGGER.debug("Creating new lock for user, id: "+userIdValue);
				_locks.put(userIdValue, (counter = new LockCounter()));
			}else if(counter._lock.isHeldByCurrentThread()){
				LOGGER.debug("Already held by current thread.");
				return;
			}else{
				++counter._count;
			}
			lock = counter._lock;
		}
		
		LOGGER.debug("Attempting to acquire lock for user, id: "+userIdValue);
		lock.lock();
		LOGGER.debug("Lock acquired for user, id: "+userIdValue);
	}
	
	/**
	 * Release the lock for the given user identity.
	 * 
	 * @param userId
	 * @throws IllegalArgumentException
	 */
	public void release(UserIdentity userId) throws IllegalArgumentException {
		if(!UserIdentity.isValid(userId)){
			throw new IllegalArgumentException("Invalid user identity.");
		}
		
		Long userIdValue = userId.getUserId();
		synchronized (_locks) {
			LockCounter counter = _locks.get(userIdValue);
			if(counter == null){
				throw new IllegalArgumentException("No lock for user, id: "+userIdValue);
			}
			
			LOGGER.debug("Releasing lock for user, id: "+userIdValue);
			counter._lock.unlock();
			
			if(--counter._count < 1){
				LOGGER.debug("The last was lock released, removing lock for user, id: "+userIdValue);
				_locks.remove(userIdValue);
			}
		}
	}
	
	/**
	 * Used to count the number of locks acquired.
	 */
	private class LockCounter {
		public int _count = 1;
		public ReentrantLock _lock = new ReentrantLock();
	} // class LockCounter
}
