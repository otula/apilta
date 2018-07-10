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
package service.tut.pori.users;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import core.tut.pori.http.ResponseData;
import core.tut.pori.users.ExternalAccountConnection;

/**
 * List of external account connections usable with Response.
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_EXTERNAL_ACCOUNT_CONNECTION_LIST)
@XmlAccessorType(XmlAccessType.NONE)
public class ExternalAccountConnectionList extends ResponseData {
	private static final Logger LOGGER = Logger.getLogger(ExternalAccountConnectionList.class);
	@XmlElement(name=core.tut.pori.users.Definitions.ELEMENT_EXTERNAL_ACCOUNT_CONNECTION)
	private List<ExternalAccountConnection> _connections = null;

	/**
	 * @return the connections
	 */
	public List<ExternalAccountConnection> getConnections() {
		return _connections;
	}

	/**
	 * @param connections the connections to set
	 */
	public void setConnections(List<ExternalAccountConnection> connections) {
		_connections = connections;
	}
	
	/**
	 * 
	 * @param connection null connection will be ignored
	 */
	public void addConnection(ExternalAccountConnection connection) {
		if(connection == null){
			LOGGER.debug("Ignored null connection.");
			return;
		}
		
		if(_connections == null){
			_connections = new ArrayList<>();
		}
		_connections.add(connection);
	}
	
	/**
	 * for sub-classing, use the static
	 * @return true if this list has no connections
	 */
	protected boolean isEmpty(){
		return (_connections == null ? true : _connections.isEmpty());
	}
	
	/**
	 * 
	 * @param list
	 * @return true if list is null or empty
	 */
	public static boolean isEmpty(ExternalAccountConnectionList list){
		return (list == null ? true : list.isEmpty());
	}
}
