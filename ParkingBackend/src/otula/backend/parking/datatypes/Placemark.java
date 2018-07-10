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
package otula.backend.parking.datatypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import otula.backend.parking.datatypes.MultiGeometry.LinearRing;
import otula.backend.parking.datatypes.MultiGeometry.OuterBoundaryIs;
import otula.backend.parking.datatypes.MultiGeometry.Polygon;

/**
 * Placemark details.
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_PLACEMARK, namespace = Definitions.NAMESPACE_KML)
@XmlAccessorType(XmlAccessType.NONE)
public class Placemark {
	@XmlElement(name = Definitions.ELEMENT_EXTENDED_DATA, namespace = Definitions.NAMESPACE_KML)
	private ExtendedData _extendedData = null;
	@XmlElement(name = Definitions.ELEMENT_MULTIGEOMETRY, namespace = Definitions.NAMESPACE_KML)
	private MultiGeometry _multiGeometry = null;
	@XmlElement(name = Definitions.ELEMENT_POLYGON, namespace = Definitions.NAMESPACE_KML)
	private Polygon _polygon = null;

	/**
	 * @return the extendedData
	 */
	public ExtendedData getExtendedData() {
		return _extendedData;
	}

	/**
	 * @param extendedData the extendedData to set
	 */
	public void setExtendedData(ExtendedData extendedData) {
		_extendedData = extendedData;
	}

	/**
	 * @return the multiGeometry
	 */
	public MultiGeometry getMultiGeometry() {
		return _multiGeometry;
	}
	
	/**
	 * This is a helper method to go around the issue with the source kml of mixing up Polygon/MultiGeometry in the kml tree
	 * 
	 * @return coordinates contained in this placemark or null if no coordinate data
	 */
	public Coordinates getCoordinates() {
		Polygon polygon = null;
		if(_multiGeometry != null){
			polygon = _multiGeometry.getPolygon();
		}else{
			polygon = _polygon;
		}
		
		if(polygon != null){
			OuterBoundaryIs boundary = polygon.getOuterBoundaryIs();
			if(boundary != null){
				LinearRing lr = boundary.getLinearRing();
				if(lr != null){
					return lr.getCoordinates();
				}
			}
		}
		return null;
	}

	/**
	 * @param multiGeometry the multiGeometry to set
	 */
	public void setMultiGeometry(MultiGeometry multiGeometry) {
		_multiGeometry = multiGeometry;
	}
}
