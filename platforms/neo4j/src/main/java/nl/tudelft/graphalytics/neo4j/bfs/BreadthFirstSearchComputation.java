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

import nl.tudelft.graphalytics.domain.Graph;
import nl.tudelft.graphalytics.neo4j.Neo4jConfiguration;
import org.neo4j.graphdb.*;

import java.util.HashSet;
import java.util.Set;

import static nl.tudelft.graphalytics.neo4j.Neo4jConfiguration.ID_PROPERTY;
import static nl.tudelft.graphalytics.neo4j.Neo4jConfiguration.VertexLabelEnum.Vertex;

/**
 * Implementation of the breadth-first search algorithm in Neo4j. This class is responsible for the computation of the
 * distance to each node from the start node, given a functional Neo4j database instance.
 *
 * @author Tim Hegeman
 */
public class BreadthFirstSearchComputation {

	public static final String DISTANCE = "DISTANCE";

	private static final int MAX_TRANSACTION_SIZE = 4095;

	private final GraphDatabaseService graphDatabase;
	private final long startVertexId;
	private final Graph graph;
	private int operationsInTransaction;
	private Transaction transaction;
	private Set<Node> currentFrontier;
	private Set<Node> nextFrontier;

	/**
	 * @param graphDatabase graph database representing the input graph
	 * @param startVertexId source vertex for the breadth-first search
	 */
	public BreadthFirstSearchComputation(GraphDatabaseService graphDatabase, long startVertexId, Graph graph) {
		this.graphDatabase = graphDatabase;
		this.startVertexId = startVertexId;
		this.graph = graph;
	}

	// TODO use graph

	/**
	 * Executes the breadth-first search algorithm by setting the DISTANCE property of all nodes reachable from the
	 * start vertex.
	 */
	public void run() {
		long distance = 0;
		operationsInTransaction = 0;
		nextFrontier = new HashSet<>();

		transaction = graphDatabase.beginTx();
		try {
			Node startNode = graphDatabase.findNode(Vertex, ID_PROPERTY, startVertexId);
			startNode.setProperty(DISTANCE, distance);
			nextFrontier.add(startNode);

			while (false == nextFrontier.isEmpty()) {
				switchFrontiers();
				distance++;
				for (Node currentFrontierNode : currentFrontier) {
					for (Relationship relationship : currentFrontierNode.getRelationships(Neo4jConfiguration.EDGE, Direction.OUTGOING)) {
						Node nextFrontierNode = relationship.getEndNode();
						if (false == currentFrontier.contains(nextFrontierNode) && false == nextFrontierNode.hasProperty(DISTANCE)) {
							nextFrontierNode.setProperty(DISTANCE, distance);
							nextFrontier.add(nextFrontierNode);
							commitTransactionIfNecessary();
						}
					}
				}
			}
		} finally {
			transaction.success();
			transaction.close();
		}
	}

	private void commitTransactionIfNecessary() {
		operationsInTransaction++;
		if (MAX_TRANSACTION_SIZE == operationsInTransaction) {
			transaction.success();
			transaction.close();
			transaction = graphDatabase.beginTx();
			operationsInTransaction = 0;
		}
	}

	private void switchFrontiers() {
		currentFrontier = nextFrontier;
		nextFrontier = new HashSet<>();
	}
}