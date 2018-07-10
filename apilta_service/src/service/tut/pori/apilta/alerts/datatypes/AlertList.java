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
package service.tut.pori.apilta.alerts.datatypes;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import core.tut.pori.http.ResponseData;

/**
 * list of alerts
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_ALERT_LIST)
@XmlAccessorType(value=XmlAccessType.NONE)
public class AlertList extends ResponseData {
	@XmlElement(name = Definitions.ELEMENT_ALERT)
	private List<Alert> _alerts = null;

	/**
	 * @return the alerts
	 * @see #setAlerts(List)
	 */
	public List<Alert> getAlerts() {
		return _alerts;
	}

	/**
	 * @param alerts the alerts to set
	 * @see #getAlerts()
	 */
	public void setAlerts(List<Alert> alerts) {
		_alerts = alerts;
	}
	
	/**
	 * for sub-classing, use the static
	 * 
	 * @return true if empty
	 * @see #isEmpty(AlertList)
	 */
	protected boolean isEmpty() {
		return (_alerts == null || _alerts.isEmpty());
	}
	
	/**
	 * 
	 * @param list
	 * @return true if the list is null or empty
	 */
	public static boolean isEmpty(AlertList list) {
		if(list == null){
			return true;
		}else{
			return list.isEmpty();
		}
	}
}
