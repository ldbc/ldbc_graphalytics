package nl.tudelft.graphalytics.neo4j.bfs;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Implementation of the breadth-first search algorithm in Neo4j. This class is responsible for the computation of the
 * distance to each node from the start node, given a functional Neo4j database instance.
 *
 * @author Tim Hegeman
 */
public class BreadthFirstSearchComputation {

	public static final String DISTANCE = "DISTANCE";

	private final GraphDatabaseService graphDatabase;
	private final long startVertexId;

	public BreadthFirstSearchComputation(GraphDatabaseService graphDatabase, long startVertexId) {
		this.graphDatabase = graphDatabase;
		this.startVertexId = startVertexId;
	}

	public void run() {

	}

}
