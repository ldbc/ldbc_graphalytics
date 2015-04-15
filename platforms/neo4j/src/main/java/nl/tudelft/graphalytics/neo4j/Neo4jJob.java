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

import org.neo4j.graphdb.GraphDatabaseService;

import java.net.URL;

/**
 * Generic Neo4j job configuration. This class is responsible for initializing a Neo4j database and executing the
 * algorithm-specific computation provided by the inheriting subclass.
 *
 * @author Tim Hegeman
 */
public abstract class Neo4jJob {

	private final String databasePath;
	private final URL propertiesFile;

	/**
	 * @param databasePath   the path of the pre-loaded graph database
	 * @param propertiesFile a Neo4j properties file
	 */
	public Neo4jJob(String databasePath, URL propertiesFile) {
		this.propertiesFile = propertiesFile;
		this.databasePath = databasePath;
	}

	/**
	 * Opens the Neo4j database, executes the algorithm-specific computation, and shuts down the database.
	 */
	public void run() {
		try (Neo4jDatabase graphDatabase = new Neo4jDatabase(databasePath, propertiesFile)) {
			runComputation(graphDatabase.get());
		}
	}

	/**
	 * Hook for the algorithm-specific computation, to be executed using the provided Neo4j database.
	 *
	 * @param graphDatabase a Neo4j graph database
	 */
	public abstract void runComputation(GraphDatabaseService graphDatabase);

}
