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
package core.tut.pori.datatypes;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * class for storing primitive integers
 *
 */
public final class IntPrimitiveList {
	private static final int ARRAY_SIZE = 50;
	private ArrayList<int[]> _arrays = null;
	private int[] _currentArray = null;
	private int _currentIndex = ARRAY_SIZE;	// set to maximum so that new array is created on first call
	private int _valueCount = 0;
	
	/**
	 * 
	 * @param value
	 */
	public void add(int value){
		if(_currentIndex >= ARRAY_SIZE){
			_currentArray = new int[ARRAY_SIZE];
			_currentIndex = 0;
			_arrays.add(_currentArray);
		}
		_currentArray[_currentIndex++] = value;
		++_valueCount;
	}
	
	/**
	 * 
	 */
	public IntPrimitiveList(){
		_arrays = new ArrayList<>();
	}
	
	/**
	 * 
	 * @return the list as an array
	 */
	public int[] toArray(){
		if(_arrays.isEmpty()){
			return null;
		}
		int[] array = new int[_valueCount];
		int index = 0;
		for(Iterator<int[]> iter = _arrays.iterator();iter.hasNext();){
			int[] temp = iter.next();
			for(int i=0;i<temp.length && index < _valueCount;++i){
				array[index++] = temp[i];
			}
		}
		return array;
	}
}
