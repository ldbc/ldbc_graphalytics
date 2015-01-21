package nl.tudelft.graphalytics.neo4j;

import nl.tudelft.graphalytics.Platform;
import nl.tudelft.graphalytics.PlatformExecutionException;
import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.domain.Graph;
import nl.tudelft.graphalytics.domain.PlatformBenchmarkResult;
import nl.tudelft.graphalytics.domain.PlatformConfiguration;

/**
 * Entry point of the Graphalytics benchmark for Neo4j. Provides the platform
 * API required by the Graphalytics core to perform operations such as uploading
 * graphs and executing specific algorithms on specific graphs.
 *
 * @author Tim Hegeman
 */
public class Neo4jPlatform implements Platform {
	@Override
	public void uploadGraph(Graph graph, String graphFilePath) throws Exception {
		// TODO
	}

	@Override
	public PlatformBenchmarkResult executeAlgorithmOnGraph(Algorithm algorithm, Graph graph, Object parameters) throws PlatformExecutionException {
		return null;
	}

	@Override
	public void deleteGraph(String graphName) {
		// TODO
	}

	@Override
	public String getName() {
		return "neo4j";
	}

	@Override
	public PlatformConfiguration getPlatformConfiguration() {
		return null;
	}
}
