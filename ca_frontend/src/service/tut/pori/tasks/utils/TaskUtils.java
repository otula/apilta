/**
 * Copyright 2017 Tampere University of Technology, Pori Department
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
package service.tut.pori.tasks.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import service.tut.pori.tasks.datatypes.Task.State;

/**
 * Common utility methods for processing tasks.
 *
 */
public final class TaskUtils {
	
	/**
	 * 
	 */
	private TaskUtils() {
		// nothing needed
	}
	
	/**
	 * 
	 * @param states
	 * @return set of States or null if null or empty collection was passed
	 * @throws IllegalArgumentException on invalid value
	 */
	public static Set<State> stringsToStates(Collection<String> states) throws IllegalArgumentException {
		if(states == null || states.isEmpty()){
			return null;
		}
		Set<State> stateSet = new HashSet<>(states.size());
		for(String s : states){
			stateSet.add(State.fromString(s));
		}
		return stateSet;
	}
	
	/**
	 * 
	 * @param states
	 * @return the collections of states as integers or null if collection was null or empty
	 */
	public static int[] statesToInts(Collection<State> states) {
		if(states == null || states.isEmpty()){
			return null;
		}
		int[] array = new int[states.size()];
		int index = -1;
		for(State state : states){
			array[++index] = state.toInt();
		}
		return array;
	}
}
