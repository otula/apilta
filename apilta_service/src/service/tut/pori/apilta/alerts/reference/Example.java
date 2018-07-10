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
package service.tut.pori.apilta.alerts.reference;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import core.tut.pori.http.ResponseData;
import service.tut.pori.apilta.alerts.datatypes.Alert;
import service.tut.pori.apilta.alerts.datatypes.AlertList;

/**
 * Response example
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_EXAMPLE)
@XmlAccessorType(XmlAccessType.NONE)
public class Example extends ResponseData {
	@XmlElementRef
	private Alert _alert = null;
	@XmlElementRef
	private AlertList _alertList = null;

	/**
	 * overridden to gather the real classes required for serialization
	 */
	@Override
	public Class<?>[] getDataClasses() {
		List<Class<?>> classes = new ArrayList<>();
		classes.add(getClass());
		if(_alert != null){
			classes.add(_alert.getClass());
		}
		if(_alertList != null){
			classes.add(_alertList.getClass());
		}
		return classes.toArray(new Class<?>[classes.size()]);
	}

	/**
	 * @return the alert
	 */
	public Alert getAlert() {
		return _alert;
	}

	/**
	 * @param alert the alert to set
	 */
	public void setAlert(Alert alert) {
		_alert = alert;
	}

	/**
	 * @return the alertList
	 */
	public AlertList getAlertList() {
		return _alertList;
	}

	/**
	 * @param alertList the alertList to set
	 */
	public void setAlertList(AlertList alertList) {
		_alertList = alertList;
	}
}
