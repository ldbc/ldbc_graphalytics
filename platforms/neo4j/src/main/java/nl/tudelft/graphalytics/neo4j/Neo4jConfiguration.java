package nl.tudelft.graphalytics.neo4j;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.RelationshipType;

/**
 * Helper class containing constants used throughout the Neo4j implementation.
 *
 * @author Tim Hegeman
 */
public final class Neo4jConfiguration {

	public static final RelationshipType EDGE = DynamicRelationshipType.withName("EDGE");

	/** Class contains only static values, so no instance required. */
	private Neo4jConfiguration() { }

}
