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

/**
 * Definitions for parking datatypes
 * 
 */
public final class Definitions {
	/* xml attributes */
	/** xml attribute declaration */
	public static final String ATTRIBUTE_NAME = "name";
	
	/* xml elements */
	/** xml element declaration */
	public static final String ELEMENT_COORDINATES = "coordinates";
	/** xml element declaration */
	public static final String ELEMENT_DOCUMENT = "Document";
	/** xml element declaration */
	public static final String ELEMENT_EXTENDED_DATA = "ExtendedData";
	/** xml element declaration */
	public static final String ELEMENT_FOLDER = "Folder";
	/** xml element declaration */
	public static final String ELEMENT_KML = "kml";
	/** xml element declaration */
	public static final String ELEMENT_LINEAR_RING = "LinearRing";
	/** xml element declaration */
	public static final String ELEMENT_MULTIGEOMETRY = "MultiGeometry";
	/** xml element declaration */
	public static final String ELEMENT_OUTER_BOUNDARY_IS = "outerBoundaryIs";
	/** xml element declaration */
	public static final String ELEMENT_PLACEMARK = "Placemark";
	/** xml element declaration */
	public static final String ELEMENT_POLYGON = "Polygon";
	/** xml element declaration */
	public static final String ELEMENT_SCHEMA_DATA = "SchemaData";
	/** xml element declaration */
	public static final String ELEMENT_SIMPLE_DATA = "SimpleData";
	
	/* namespaces */
	/** xml namespace declaration for standard kml */
	public static final String NAMESPACE_KML = "http://www.opengis.net/kml/2.2";
	
	/* data keys for simple data / kml */
	/** simple data key */
	public static final String SIMPLE_DATA_KEY_NAME = "Nimi";
	/** simple data key */
	public static final String SIMPLE_DATA_KEY_PLACE_COUNT = "Autopaikat";
	/** simple data key */
	public static final String SIMPLE_DATA_KEY_NOTICE = "Huom";
	/** simple data key */
	public static final String SIMPLE_DATA_KEY_OWNER = "Omistusmuoto";
	/** simple data key */
	public static final String SIMPLE_DATA_KEY_PAYMENT_REQUIRED = "Maksullisuus";
	/** simple data key */
	public static final String SIMPLE_DATA_KEY_TIME_LIMIT = "Aikarajoitus";
	/** simple data key */
	public static final String SIMPLE_DATA_KEY_ZONE = "vyohyke";
	
	/**
	 * 
	 */
	private Definitions(){
		// nothing needed
	}
}
