package org.tudelft.graphalytics.graphlab;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.exec.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tudelft.graphalytics.Graph;
import org.tudelft.graphalytics.Platform;
import org.tudelft.graphalytics.algorithms.AlgorithmType;
import org.tudelft.graphalytics.algorithms.BFSParameters;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Entry point of the Graphalytics benchmark for Giraph. Provides the platform
 * API required by the Graphalytics core to perform operations such as uploading
 * graphs and executing specific algorithms on specific graphs.
 * @author Jorai Rijsdijk
 */
public class GraphLabPlatform implements Platform {
    private static final Logger LOG = LogManager.getLogger();

    // TODO: Make configurable
    private static final String BASE_ADDRESS = "graphalytics-graphlab";

    private final String RELATIVE_PATH_TO_TARGET;

    private Map<String, String> pathsOfGraphs = new HashMap<>();
    private org.apache.commons.configuration.Configuration graphlabConfig;


    /**
     * Constructor that opens the Giraph-specific properties file for the public
     * API implementation to use.
     */
    public GraphLabPlatform() {
        // Fill in the relative path from the working directory to the target directory containing the graphlab build output files,
        // or in the case of a distributed jar file, the location of the jar file itself.
        String absolutePath = GraphLabPlatform.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf('/')).replace("%20", " ");
        // The relative path is the path from the current working directory to the graphlab build output directory
        RELATIVE_PATH_TO_TARGET = new File(System.getProperty("user.dir")).toURI().relativize(new File(absolutePath).toURI()).getPath();

        loadConfiguration();
    }

    private void loadConfiguration() {
        // Load GraphLab-specific configuration
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
        LOG.entry(graph, graphFilePath);

        String uploadPath = BASE_ADDRESS + "/input/" + graph.getName();

        // Upload the graph to HDFS
        FileSystem fs = FileSystem.get(new Configuration());
        fs.copyFromLocalFile(new Path(graphFilePath), new Path(uploadPath));
        fs.close();

        // Track available datasets in a map
        pathsOfGraphs.put(graph.getName(), fs.getHomeDirectory().toUri() + "/" + uploadPath);

        LOG.exit();
    }

    @Override
    public boolean executeAlgorithmOnGraph(AlgorithmType algorithmType, Graph graph, Object parameters) {
        LOG.entry(algorithmType, graph, parameters);

        try {
            // Execute the GraphLab job
            int result;

            switch (algorithmType) {
                case BFS:
                    result = executePythonAlgorithm(
                            "bfs/BreadthFirstSearch.py",
                            pathsOfGraphs.get(graph.getName()),
                            graph.getGraphFormat().isDirected() ? "true" : "false",
                            graph.getGraphFormat().isEdgeBased() ? "true" : "false",
                            ((BFSParameters) parameters).getSourceVertex()
                    );
                    break;
                case CD:
                    //TODO convert CDParameters to commandline arguments
                    result = executePythonAlgorithm(
                            "cd/CommunityDetection.py",
                            pathsOfGraphs.get(graph.getName()),
                            parameters
                    );
                    break;
                case CONN:
                    result = executePythonAlgorithm(
                            "conn/ConnectedComponents.py",
                            pathsOfGraphs.get(graph.getName()),
                            parameters
                    );
                    break;
                case EVO:
                    //TODO convert EVOParameters to commandline arguments
                    result = executePythonAlgorithm(
                            "evo/ForestFireModel.py",
                            pathsOfGraphs.get(graph.getName()),
                            parameters
                    );
                    break;
                case STATS:
                    //TODO convert STATSParameters to commandline arguments
                    result = executePythonAlgorithm(
                            "stats/LocalClusteringCoefficient.py",
                            pathsOfGraphs.get(graph.getName()),
                            parameters
                    );
                    break;
                default:
                    LOG.warn("Unsupported algorithm: " + algorithmType);
                    return LOG.exit(false);
            }

            // TODO: Clean up intermediate and output data, depending on some configuration.
            return LOG.exit(result == 0);
        } catch (Exception e) {
            LOG.catching(Level.ERROR, e);
            return LOG.exit(false);
        }
    }

    /**
     * Execute the python script belonging to a given AlgorithmType with the given graph location and extra arguments
     * and return the Process created by the Java Runtime.
     * @param algorithmScriptFile The relative location to the algorithm scriptfile to execute
     * @param graphPath           The path to the graph file to execute the algorithm on
     * @param args                The extra arguments to pass to the algorithm
     * @return The exit code of the python subprocess
     * @throws IOException When an I/O error occurs
     */
    private int executePythonAlgorithm(String algorithmScriptFile, String graphPath, Object... args) throws IOException {
        LOG.entry(algorithmScriptFile, graphPath, args);

        // Extract the script resource file
        File scriptFile = new File(RELATIVE_PATH_TO_TARGET,  algorithmScriptFile);
        if (scriptFile.exists() && !scriptFile.canWrite()) {
            LOG.error("Cannot extract GraphLab " + algorithmScriptFile + " script to "+ System.getProperty("user.dir")
                    + RELATIVE_PATH_TO_TARGET + ", no write access on an already existing file.");
            return LOG.exit(-1);
        } else if (!scriptFile.exists() && !scriptFile.getParentFile().mkdirs() && !scriptFile.createNewFile()) {
            LOG.error("Cannot extract GraphLab " + algorithmScriptFile + " script to "+ System.getProperty("user.dir")
                    + RELATIVE_PATH_TO_TARGET + ", failed to create the appropriate files/directories.");
            return LOG.exit(-1);
        }

        // Actually extract the algorithm script
        extractPythonAlgorithm(GraphLabPlatform.class.getResourceAsStream(algorithmScriptFile), scriptFile);

        // Construct the commandline execution pattern starting with the python executable
        CommandLine commandLine = new CommandLine("python");
        // Add the absolute location to the script file
        commandLine.addArgument(scriptFile.getAbsolutePath());
        // Add the path to the graph
        commandLine.addArgument(graphPath);
        for (Object arg : args) {
            commandLine.addArgument(arg.toString());
        }

        // Set the executor of the command, if desired this can be changed to a custom implementation
        DefaultExecutor executor = new DefaultExecutor();

        // Uncomment this to set a watchdog to terminate the subprocess after x milliseconds
        //ExecuteWatchdog watchdog = new ExecuteWatchdog(5 * 1000);
        //executor.setWatchdog(watchdog);

        // Set the OutputStream to enable printing the output of the algorithm
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(outputStream));

        int result;
        try {
            // Execute the actual command and store the return code
            result = executor.execute(commandLine);
            // Print the command output
            System.out.println(outputStream.toString());
        } catch (ExecuteException e) {
            // Catch the exception thrown when the process exits with result != 0
            System.out.println(outputStream.toString());
            LOG.catching(Level.ERROR, e);
            return LOG.exit(-1);
        }
        return LOG.exit(result);
    }

    /**
     * Extract a given resourceInputStream to the given outputFile, overwriting any preexisting file.
     * @param resourceInputStream The InputStream to copy from
     * @param outputFile The File to copy to
     * @throws IOException When an I/O error occurs
     */
    private void extractPythonAlgorithm(InputStream resourceInputStream, File outputFile) throws IOException {
        OutputStream outputStream = new FileOutputStream(outputFile);
        int read;
        byte[] bytes = new byte[1024];

        while ((read = resourceInputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, read);
        }

        resourceInputStream.close();
        outputStream.close();
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
