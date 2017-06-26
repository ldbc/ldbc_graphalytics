/*
 * Copyright 2015 - 2017 Atlarge Research Team,
 * operating at Technische Universiteit Delft
 * and Vrije Universiteit Amsterdam, the Netherlands.
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
package science.atlarge.graphalytics.configuration;

import science.atlarge.graphalytics.domain.graph.FormattedGraph;
import science.atlarge.graphalytics.domain.graph.Property;
import science.atlarge.graphalytics.domain.graph.PropertyList;
import science.atlarge.graphalytics.domain.graph.PropertyType;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Test cases for FormattedGraphParser.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class FormattedGraphParserTest {

	@Test
	public void testParseGraphOnBasicConfiguration() throws InvalidConfigurationException {
		final String ROOT_DIR = "graph-root-dir";
		final Fixture FIXTURE = constructBasicGraph(ROOT_DIR);

		FormattedGraphParser parser = new FormattedGraphParser(FIXTURE.getConfiguration(),
				FIXTURE.getGraphName(), ROOT_DIR);
		assertGraphEqual(FIXTURE.getExpectedGraph(), parser.parseFormattedGraph());
	}

	@Test
	public void testParseGraphOnVertexPropertiesConfiguration() throws InvalidConfigurationException {
		final String ROOT_DIR = "graph-root-dir";
		final Fixture FIXTURE = constructVertexPropertyGraph(ROOT_DIR);

		FormattedGraphParser parser = new FormattedGraphParser(FIXTURE.getConfiguration(),
				FIXTURE.getGraphName(), ROOT_DIR);
		assertGraphEqual(FIXTURE.getExpectedGraph(), parser.parseFormattedGraph());
	}

	@Test
	public void testParseGraphOnEdgePropertiesConfiguration() throws InvalidConfigurationException {
		final String ROOT_DIR = "graph-root-dir";
		final Fixture FIXTURE = constructEdgePropertyGraph(ROOT_DIR);

		FormattedGraphParser parser = new FormattedGraphParser(FIXTURE.getConfiguration(),
				FIXTURE.getGraphName(), ROOT_DIR);
		assertGraphEqual(FIXTURE.getExpectedGraph(), parser.parseFormattedGraph());
	}

	private static void assertGraphEqual(FormattedGraph expected, FormattedGraph actual) {
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getNumberOfVertices(), actual.getNumberOfVertices());
		assertEquals(expected.getNumberOfEdges(), actual.getNumberOfEdges());
		assertEquals(expected.isDirected(), actual.isDirected());
		assertEquals(expected.getVertexFilePath(), actual.getVertexFilePath());
		assertEquals(expected.getEdgeFilePath(), actual.getEdgeFilePath());
		assertEquals(expected.getVertexProperties(), actual.getVertexProperties());
		assertEquals(expected.getEdgeProperties(), actual.getEdgeProperties());
	}

	private static Fixture constructBasicGraph(String rootDir) {
		final String NAME = "Graph name";
		final long NUM_VERTICES = 123;
		final long NUM_EDGES = 765;
		final boolean IS_DIRECTED = true;
		final String VERTEX_FILE_PATH = "example.graph.v";
		final String EDGE_FILE_PATH = "other.example.graph.edges";

		FormattedGraph formattedGraph = new FormattedGraph(NAME, NUM_VERTICES, NUM_EDGES, IS_DIRECTED,
				Paths.get(rootDir, VERTEX_FILE_PATH).toString(), Paths.get(rootDir, EDGE_FILE_PATH).toString(),
				new PropertyList(), new PropertyList());

		PropertiesConfiguration configuration = new PropertiesConfiguration();
		configuration.setProperty("meta.vertices", NUM_VERTICES);
		configuration.setProperty("meta.edges", NUM_EDGES);
		configuration.setProperty("directed", IS_DIRECTED);
		configuration.setProperty("vertex-file", VERTEX_FILE_PATH);
		configuration.setProperty("edge-file", EDGE_FILE_PATH);

		return new Fixture(NAME, formattedGraph, configuration);
	}

	private static Fixture constructVertexPropertyGraph(String rootDir) {
		final String NAME = "Graph name";
		final long NUM_VERTICES = 123;
		final long NUM_EDGES = 765;
		final boolean IS_DIRECTED = true;
		final String VERTEX_FILE_PATH = "example.graph.v";
		final String EDGE_FILE_PATH = "other.example.graph.edges";
		final String VERTEX_PROPERTY_NAME_1 = "prop-1";
		final String VERTEX_PROPERTY_NAME_2 = "prop-2";
		final String VERTEX_PROPERTY_NAME_3 = "prop-3";
		final PropertyType VERTEX_PROPERTY_TYPE_1 = PropertyType.INTEGER;
		final PropertyType VERTEX_PROPERTY_TYPE_2 = PropertyType.INTEGER;
		final PropertyType VERTEX_PROPERTY_TYPE_3 = PropertyType.REAL;

		FormattedGraph formattedGraph = new FormattedGraph(NAME, NUM_VERTICES, NUM_EDGES, IS_DIRECTED,
				Paths.get(rootDir, VERTEX_FILE_PATH).toString(), Paths.get(rootDir, EDGE_FILE_PATH).toString(),
				new PropertyList(new Property(VERTEX_PROPERTY_NAME_1, VERTEX_PROPERTY_TYPE_1),
						new Property(VERTEX_PROPERTY_NAME_2, VERTEX_PROPERTY_TYPE_2),
						new Property(VERTEX_PROPERTY_NAME_3, VERTEX_PROPERTY_TYPE_3)),
				new PropertyList());

		PropertiesConfiguration configuration = new PropertiesConfiguration();
		configuration.setProperty("meta.vertices", NUM_VERTICES);
		configuration.setProperty("meta.edges", NUM_EDGES);
		configuration.setProperty("directed", IS_DIRECTED);
		configuration.setProperty("vertex-file", VERTEX_FILE_PATH);
		configuration.setProperty("edge-file", EDGE_FILE_PATH);
		configuration.setProperty("vertex-properties.names", VERTEX_PROPERTY_NAME_1 + "," + VERTEX_PROPERTY_NAME_2 +
				"," + VERTEX_PROPERTY_NAME_3);
		configuration.setProperty("vertex-properties.types", VERTEX_PROPERTY_TYPE_1 + "," + VERTEX_PROPERTY_TYPE_2 +
				"," + VERTEX_PROPERTY_TYPE_3);

		return new Fixture(NAME, formattedGraph, configuration);
	}

	private static Fixture constructEdgePropertyGraph(String rootDir) {
		final String NAME = "Graph name";
		final long NUM_VERTICES = 123;
		final long NUM_EDGES = 765;
		final boolean IS_DIRECTED = true;
		final String VERTEX_FILE_PATH = "example.graph.v";
		final String EDGE_FILE_PATH = "other.example.graph.edges";
		final String EDGE_PROPERTY_NAME_1 = "prop-1";
		final String EDGE_PROPERTY_NAME_2 = "prop-2";
		final String EDGE_PROPERTY_NAME_3 = "prop-3";
		final PropertyType EDGE_PROPERTY_TYPE_1 = PropertyType.INTEGER;
		final PropertyType EDGE_PROPERTY_TYPE_2 = PropertyType.INTEGER;
		final PropertyType EDGE_PROPERTY_TYPE_3 = PropertyType.REAL;

		FormattedGraph formattedGraph = new FormattedGraph(NAME, NUM_VERTICES, NUM_EDGES, IS_DIRECTED,
				Paths.get(rootDir, VERTEX_FILE_PATH).toString(), Paths.get(rootDir, EDGE_FILE_PATH).toString(),
				new PropertyList(),
				new PropertyList(new Property(EDGE_PROPERTY_NAME_1, EDGE_PROPERTY_TYPE_1),
						new Property(EDGE_PROPERTY_NAME_2, EDGE_PROPERTY_TYPE_2),
						new Property(EDGE_PROPERTY_NAME_3, EDGE_PROPERTY_TYPE_3)));

		PropertiesConfiguration configuration = new PropertiesConfiguration();
		configuration.setProperty("meta.vertices", NUM_VERTICES);
		configuration.setProperty("meta.edges", NUM_EDGES);
		configuration.setProperty("directed", IS_DIRECTED);
		configuration.setProperty("vertex-file", VERTEX_FILE_PATH);
		configuration.setProperty("edge-file", EDGE_FILE_PATH);
		configuration.setProperty("edge-properties.names", EDGE_PROPERTY_NAME_1 + "," + EDGE_PROPERTY_NAME_2 +
				"," + EDGE_PROPERTY_NAME_3);
		configuration.setProperty("edge-properties.types", EDGE_PROPERTY_TYPE_1 + "," + EDGE_PROPERTY_TYPE_2 +
				"," + EDGE_PROPERTY_TYPE_3);

		return new Fixture(NAME, formattedGraph, configuration);
	}

	private static class Fixture {

		private final String graphName;
		private final FormattedGraph formattedGraph;
		private final Configuration configuration;

		private Fixture(String graphName, FormattedGraph formattedGraph, Configuration configuration) {
			this.graphName = graphName;
			this.formattedGraph = formattedGraph;
			this.configuration = configuration;
		}

		public String getGraphName() {
			return graphName;
		}

		public FormattedGraph getExpectedGraph() {
			return formattedGraph;
		}

		public Configuration getConfiguration() {
			return configuration;
		}

	}

}
