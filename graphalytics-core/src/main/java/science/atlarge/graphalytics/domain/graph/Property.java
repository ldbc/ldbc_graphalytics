/*
 * Copyright 2015 - 2017 Atlarge Research Team,
 * operating at Technische Universiteit Delft
 * and Vrije Universiteit Amsterdam, the Netherlands.
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
package science.atlarge.graphalytics.domain.graph;

import java.io.Serializable;

/**
 * Represents a single vertex or edge property in a graph, i.e. a (name, type) pair.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public final class Property implements Serializable {

	private final String name;
	private final PropertyType type;

	/**
	 * @param name the name of the property
	 * @param type the type of the property
	 */
	public Property(String name, PropertyType type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * @return the name of the property
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the type of the property
	 */
	public PropertyType getType() {
		return type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Property property = (Property)o;

		if (!name.equals(property.name)) return false;
		return type == property.type;

	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + type.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "Property{" +
				"name='" + name + '\'' +
				", type=" + type +
				'}';
	}

}
