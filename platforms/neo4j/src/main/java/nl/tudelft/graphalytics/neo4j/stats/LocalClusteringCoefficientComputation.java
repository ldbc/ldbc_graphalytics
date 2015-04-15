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
package nl.tudelft.graphalytics.neo4j.stats;

import nl.tudelft.graphalytics.neo4j.Neo4jConfiguration;
import org.neo4j.graphdb.*;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of the local clustering coefficient algorithm in Neo4j. This class is responsible for the computation,
 * given a functional Neo4j database instance.
 *
 * @author Tim Hegeman
 */
public class LocalClusteringCoefficientComputation {

	public static final String LCC = "LCC";
	private final GraphDatabaseService graphDatabase;

	/**
	 * @param graphDatabase graph database representing the input graph
	 */
	public LocalClusteringCoefficientComputation(GraphDatabaseService graphDatabase) {
		this.graphDatabase = graphDatabase;
	}

	/**
	 * Executes the local clustering coefficient algorithm by setting the LCC property on all nodes, and returning
	 * the mean value as a result.
	 */
	public LocalClusteringCoefficientResult run() {
		double sumLcc = 0.0;
		int numNodes = 0;

		try (Transaction transaction = graphDatabase.beginTx()) {
			for (Node node : GlobalGraphOperations.at(graphDatabase).getAllNodes()) {
				double lcc = computeLcc(node);
				node.setProperty(LCC, lcc);
				sumLcc += lcc;
				numNodes++;
			}
			transaction.success();
		}

		return new LocalClusteringCoefficientResult(sumLcc / numNodes);
	}

	private double computeLcc(Node node) {
		Set<Node> neighbours = new HashSet<>();
		for (Relationship edge : node.getRelationships(Neo4jConfiguration.EDGE, Direction.BOTH)) {
			neighbours.add(edge.getOtherNode(node));
		}
		if (neighbours.size() <= 1)
			return 0.0;

		long numEdges = 0;
		for (Node neighbour : neighbours) {
			for (Relationship edge : neighbour.getRelationships(Neo4jConfiguration.EDGE, Direction.OUTGOING)) {
				if (neighbours.contains(edge.getOtherNode(neighbour))) {
					numEdges++;
				}
			}
		}

		long possibleEdges = (long)neighbours.size() * (neighbours.size() - 1);

		return (double)numEdges / possibleEdges;
	}

	/**
	 * Data container for the result of the local clustering coefficient algorithm.
	 */
	public static class LocalClusteringCoefficientResult {

		private final double meanLcc;

		/**
		 * @param meanLcc the mean local clustering coefficient of the graph
		 */
		public LocalClusteringCoefficientResult(double meanLcc) {
			this.meanLcc = meanLcc;
		}

		/**
		 * @return the mean local clustering coefficient of the graph
		 */
		public double getMeanLcc() {
			return meanLcc;
		}

	}

}
