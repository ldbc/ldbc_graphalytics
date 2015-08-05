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
package nl.tudelft.graphalytics.domain.algorithms;

import nl.tudelft.graphalytics.configuration.InvalidConfigurationException;
import org.apache.commons.configuration.Configuration;

/**
 * Factory interface that defines an API to parse algorithm-specific parameters from a Configuration.
 *
 * @author Tim Hegeman
 */
public interface ParameterFactory<T> {

	/**
	 * Parses an object of type T from the properties of a Configuration object.
	 *
	 * @param configuration the Configuration describing the object of type T
	 * @param baseProperty  the name of the property whose sub-properties describe the object
	 * @return the parsed object
	 * @throws InvalidConfigurationException iff the configuration does not contain the required properties
	 */
	public T fromConfiguration(Configuration configuration, String baseProperty) throws InvalidConfigurationException;

}
