/*
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.tudelft.graphalytics.domain.graph;

/**
 * Represents the type of a property in a property graph.
 *
 * @author Tim Hegeman
 */
public enum PropertyType {

	/**
	 * Property is an integral number in the range [-2^63, 2^63 - 1].
	 */
	INTEGER,
	/**
	 * Property is a real number.
	 */
	REAL;

	/**
	 * @param propertyString a string representation of a PropertyType
	 * @return the corresponding PropertyType value, or null if the propertyString does not match any value
	 */
	public static PropertyType fromString(String propertyString) {
		switch (propertyString.toLowerCase()) {
			case "int":
			case "integer":
				return INTEGER;
			case "real":
				return REAL;
		}
		return null;
	}

}
