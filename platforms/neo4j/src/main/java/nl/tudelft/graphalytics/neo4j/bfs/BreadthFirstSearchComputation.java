package nl.tudelft.graphalytics.neo4j.bfs;

import nl.tudelft.graphalytics.neo4j.Neo4jConfiguration;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;

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
		try (Transaction transaction = graphDatabase.beginTx()) {
			Node startNode = graphDatabase.getNodeById(startVertexId);
			TraversalDescription traversalDescription = graphDatabase.traversalDescription()
					.breadthFirst()
					.relationships(Neo4jConfiguration.EDGE, Direction.OUTGOING)
					.evaluator(Evaluators.all());

			Traverser traverser = traversalDescription.traverse(startNode);
			for (Path path : traverser) {
				path.endNode().setProperty(DISTANCE, (long)path.length());
			}
			transaction.success();
		}
	}

}
