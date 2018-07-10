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

/**
 * Contains details for a multigeometry
 * 
 */
@XmlRootElement(name=Definitions.ELEMENT_MULTIGEOMETRY, namespace = Definitions.NAMESPACE_KML)
@XmlAccessorType(XmlAccessType.NONE)
public class MultiGeometry {
	@XmlElement(name = Definitions.ELEMENT_POLYGON, namespace = Definitions.NAMESPACE_KML)
	private Polygon _polygon = null;
	
	/**
	 * @return the polygon
	 */
	public Polygon getPolygon() {
		return _polygon;
	}

	/**
	 * @param polygon the polygon to set
	 */
	public void setPolygon(Polygon polygon) {
		_polygon = polygon;
	}

	/**
	 * Polygon container
	 * 
	 */
	@XmlRootElement(name=Definitions.ELEMENT_POLYGON, namespace = Definitions.NAMESPACE_KML)
	@XmlAccessorType(XmlAccessType.NONE)
	public static class Polygon {
		@XmlElement(name = Definitions.ELEMENT_OUTER_BOUNDARY_IS, namespace = Definitions.NAMESPACE_KML)
		private OuterBoundaryIs _outerBoundaryIs = null;

		/**
		 * @return the outerBoundaryIs
		 */
		public OuterBoundaryIs getOuterBoundaryIs() {
			return _outerBoundaryIs;
		}

		/**
		 * @param outerBoundaryIs the outerBoundaryIs to set
		 */
		public void setOuterBoundaryIs(OuterBoundaryIs outerBoundaryIs) {
			_outerBoundaryIs = outerBoundaryIs;
		}
	} // class Polygon
	
	/**
	 * outer boundary container
	 * 
	 */
	@XmlRootElement(name=Definitions.ELEMENT_OUTER_BOUNDARY_IS, namespace = Definitions.NAMESPACE_KML)
	@XmlAccessorType(XmlAccessType.NONE)
	public static class OuterBoundaryIs {
		@XmlElement(name = Definitions.ELEMENT_LINEAR_RING, namespace = Definitions.NAMESPACE_KML)
		private LinearRing _linearRing = null;

		/**
		 * @return the linearRing
		 */
		public LinearRing getLinearRing() {
			return _linearRing;
		}

		/**
		 * @param linearRing the linearRing to set
		 */
		public void setLinearRing(LinearRing linearRing) {
			_linearRing = linearRing;
		}
	} // class OuterBoundaryIs
	
	/**
	 * linear ring container
	 *
	 */
	@XmlRootElement(name=Definitions.ELEMENT_LINEAR_RING, namespace = Definitions.NAMESPACE_KML)
	@XmlAccessorType(XmlAccessType.NONE)
	public static class LinearRing {
		@XmlElement(name = Definitions.ELEMENT_COORDINATES, namespace = Definitions.NAMESPACE_KML)
		private Coordinates _coordinates = null;

		/**
		 * @return the coordinates
		 */
		public Coordinates getCoordinates() {
			return _coordinates;
		}

		/**
		 * @param coordinates the coordinates to set
		 */
		public void setCoordinates(Coordinates coordinates) {
			_coordinates = coordinates;
		}
	} // class LinearRing
}
