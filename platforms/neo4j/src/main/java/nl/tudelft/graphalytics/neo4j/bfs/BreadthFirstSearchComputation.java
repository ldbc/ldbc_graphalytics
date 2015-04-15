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

	/**
	 * @param graphDatabase graph database representing the input graph
	 * @param startVertexId source vertex for the breadth-first search
	 */
	public BreadthFirstSearchComputation(GraphDatabaseService graphDatabase, long startVertexId) {
		this.graphDatabase = graphDatabase;
		this.startVertexId = startVertexId;
	}

	/**
	 * Executes the breadth-first search algorithm by setting the DISTANCE property of all nodes reachable from the
	 * start vertex.
	 */
	public void run() {
		try (Transaction transaction = graphDatabase.beginTx()) {
			Node startNode = graphDatabase.getNodeById(startVertexId);
			TraversalDescription traversalDescription = graphDatabase.traversalDescription()
					.breadthFirst()
					.relationships(Neo4jConfiguration.EDGE, Direction.OUTGOING)
					.evaluator(Evaluators.all());

			Traverser traverser = traversalDescription.traverse(startNode);
			for (Path path : traverser) {
				path.endNode().setProperty(DISTANCE, (long) path.length());
			}
			transaction.success();
		}
	}

}
