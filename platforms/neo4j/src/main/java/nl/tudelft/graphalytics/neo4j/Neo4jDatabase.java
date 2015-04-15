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
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.net.URL;

/**
 * Wrapper class for the initialization and safe shutdown of a Neo4j database.
 *
 * @author Tim Hegeman
 */
public class Neo4jDatabase implements AutoCloseable {

	private final GraphDatabaseService graphDatabase;

	/**
	 * Initializes an embedded Neo4j database using data stored in the specified path, and using configuration specified
	 * in the provided properties file.
	 *
	 * @param databasePath   the path of the pre-loaded graph database
	 * @param propertiesFile a Neo4j properties file
	 */
	public Neo4jDatabase(String databasePath, URL propertiesFile) {
		this.graphDatabase = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder(databasePath)
				.loadPropertiesFromURL(propertiesFile)
				.newGraphDatabase();
	}

	/**
	 * @return a handle to the Neo4j database
	 */
	public GraphDatabaseService get() {
		return graphDatabase;
	}

	@Override
	public void close() {
		graphDatabase.shutdown();
	}

}
