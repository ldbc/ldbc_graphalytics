import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tudelft.graphalytics.Graph;
import org.tudelft.graphalytics.Platform;
import org.tudelft.graphalytics.algorithms.AlgorithmType;

/**
 * Entry point of the Graphalytics benchmark for Giraph. Provides the platform
 * API required by the Graphalytics core to perform operations such as uploading
 * graphs and executing specific algorithms on specific graphs.
 *
 * @author Jorai Rijsdijk
 */
public class GraphlabPlatform implements Platform {
    private static final Logger LOG = LogManager.getLogger();
    private Configuration graphlabConfig;

    /**
     * Constructor that opens the Giraph-specific properties file for the public
     * API implementation to use.
     */
    public GraphlabPlatform() {
        loadConfiguration();
    }

    private void loadConfiguration() {
        // Load Giraph-specific configuration
        try {
            graphlabConfig = new PropertiesConfiguration("giraph.properties");
        } catch (ConfigurationException e) {
            // Fall-back to an empty properties file
            LOG.info("Could not find or load giraph.properties.");
            graphlabConfig = new PropertiesConfiguration();
        }
    }

    @Override
    public void uploadGraph(Graph graph, String graphFilePath) throws Exception {
        //TODO
    }

    @Override
    public boolean executeAlgorithmOnGraph(AlgorithmType algorithmType, Graph graph, Object parameters) {
        //TODO
        return false;
    }

    @Override
    public void deleteGraph(String graphName) {
        //TODO
    }

    @Override
    public String getName() {
        return "graphlab";
    }
}
