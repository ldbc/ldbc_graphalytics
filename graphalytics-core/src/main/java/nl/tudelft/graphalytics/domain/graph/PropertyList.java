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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Represents an ordered list of properties (i.e. all vertex properties or all edge properties of a graph).
 *
 * @author Tim Hegeman
 */
public final class PropertyList implements Serializable, Iterable<Property> {

	private final Property[] properties;

	public PropertyList(Property... properties) {
		this.properties = new Property[properties.length];
		System.arraycopy(properties, 0, this.properties, 0, properties.length);
	}

	public PropertyList(Collection<Property> properties) {
		this.properties = properties.toArray(new Property[properties.size()]);
	}

	/**
	 * @return the number of properties in the list
	 */
	public int size() {
		return properties.length;
	}

	/**
	 * @param index the index of a property to retrieve from the list, starts at 0
	 * @return the property at the given index
	 */
	public Property get(int index) {
		return properties[index];
	}

	/**
	 * @param property a property to look up
	 * @return true iff the given property exists in the list
	 */
	public boolean contains(Property property) {
		for (Property propertyInList : properties) {
			if (propertyInList.equals(property)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param superset the list of properties for which to verify if this list is a subset
	 * @return true iff all items in this list also occur in the given list
	 */
	public boolean isSubsetOf(PropertyList superset) {
		for (Property propertyInList : properties) {
			if (!superset.contains(propertyInList)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Iterator<Property> iterator() {
		return new Iterator<Property>() {

			private int index = -1;

			@Override
			public boolean hasNext() {
				return index + 1 < size();
			}

			@Override
			public Property next() {
				index++;
				return get(index);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removing a Property from a PropertyList is not supported.");
			}

		};
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PropertyList that = (PropertyList)o;

		return Arrays.equals(properties, that.properties);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(properties);
	}

	@Override
	public String toString() {
		return "PropertyList{" +
				"properties=" + Arrays.toString(properties) +
				'}';
	}

}
