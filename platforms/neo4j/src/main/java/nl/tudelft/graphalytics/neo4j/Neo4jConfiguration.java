/**
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
package nl.tudelft.graphalytics.neo4j;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

/**
 * Helper class containing constants used throughout the Neo4j implementation.
 *
 * @author Tim Hegeman
 */
public final class Neo4jConfiguration {

	public static final RelationshipType EDGE = DynamicRelationshipType.withName("EDGE");
	public static final String ID_PROPERTY = "VID";

	public enum VertexLabelEnum implements Label {
		Vertex
	}

	/** Class contains only static values, so no instance required. */
	private Neo4jConfiguration() { }

}
