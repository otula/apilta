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
package service.tut.pori.tasks.datatypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import service.tut.pori.backends.datatypes.Backend;
import service.tut.pori.tasks.Definitions;

/**
 * back end extended for task specific use
 * 
 */
@XmlRootElement(name=service.tut.pori.backends.Definitions.ELEMENT_BACKEND)
@XmlAccessorType(XmlAccessType.NONE)
public class TaskBackend extends Backend {
	@XmlElement(name = Definitions.ELEMENT_MESSAGE)
	private String _message = null;
	@XmlElement(name = Definitions.ELEMENT_TASK_STATUS)
	private Status _status = null;
	
	/**
	 * The status of the task for this back end.
	 */
	@XmlType(name=Definitions.ELEMENT_TASK_STATUS)
	@XmlEnum
	public enum Status{
		/** unknown or unspecified task status */
		@XmlEnumValue(value = Definitions.TASK_STATUS_UNKNOWN)
		UNKNOWN(0),
		/** task has been created, but back-ends have not yet started to process it, or the the task has not been delivered to back-ends */
		@XmlEnumValue(value = Definitions.TASK_STATUS_NOT_STARTED)
		NOT_STARTED(1),
		/** task has been delivered to back-end, but the analysis has not yet started */
		@XmlEnumValue(value = Definitions.TASK_STATUS_PENDING)
		PENDING(2),
		/** the task is being executed */
		@XmlEnumValue(value = Definitions.TASK_STATUS_EXECUTING)
		EXECUTING(3),
		/** task has completed */
		@XmlEnumValue(value = Definitions.TASK_STATUS_COMPLETED)
		COMPLETED(4),
		/** an error condition has prevented to execution of the task */
		@XmlEnumValue(value = Definitions.TASK_STATUS_ERROR)
		ERROR(5);

		private int _value;


		/**
		 * 
		 * @param value
		 */
		private Status(int value){
			_value = value;
		}


		/**
		 * 
		 * @return TaskStatus as integer
		 */
		public int toInt(){
			return _value;
		}

		/**
		 * 
		 * @param value
		 * @return the value converted to TaskStatus
		 * @throws IllegalArgumentException on bad input
		 */
		public static Status fromInt(int value) throws IllegalArgumentException{
			for(Status s : Status.values()){
				if(s._value == value){
					return s;
				}
			}
			throw new IllegalArgumentException("Bad "+Status.class.toString()+" : "+value);
		}
	}  // enum Status

	/**
	 * @return the status
	 * @see #setStatus(service.tut.pori.tasks.datatypes.TaskBackend.Status)
	 */
	public Status getStatus() {
		return _status;
	}

	/**
	 * @param status the status to set
	 * @see #getStatus()
	 */
	public void setStatus(Status status) {
		_status = status;
	}

	/**
	 * @return the optional message set by the back end describing the status of the analysis or null if no message provided
	 * @see #setMessage(String)
	 */
	public String getMessage() {
		return _message;
	}

	/**
	 * @param message the optional status description set by the back end
	 * @see #getMessage()
	 */
	public void setMessage(String message) {
		_message = message;
	}

	@Override
	protected boolean isValid() {
		if(_status == null || getBackendId() == null){ // only identifier and status needed for a valid task back end
			return false;
		}else{
			return true;
		}
	}
}
