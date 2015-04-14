package nl.tudelft.graphalytics.graphlab;

import nl.tudelft.graphalytics.Platform;
import nl.tudelft.graphalytics.PlatformExecutionException;
import nl.tudelft.graphalytics.configuration.ConfigurationUtil;
import nl.tudelft.graphalytics.configuration.InvalidConfigurationException;
import nl.tudelft.graphalytics.domain.*;
import nl.tudelft.graphalytics.graphlab.bfs.BreadthFirstSearchJob;
import nl.tudelft.graphalytics.graphlab.cd.CommunityDetectionJob;
import nl.tudelft.graphalytics.graphlab.conn.ConnectedComponentsJob;
import nl.tudelft.graphalytics.graphlab.stats.LocalClusteringCoefficientJob;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Paths;
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

    /**
     * Property key for setting whether to use hadoop or local execution/file storage.
     */
    private static final String TARGET_KEY = "graphlab.target";

    /**
     * Property key for setting the amount of virtual cores to use in the Hadoop environment.
     */
    private static final String JOB_VIRTUALCORES_KEY = "graphlab.job.virtual-cores";
    /**
     * Property key for setting the heap size for the Hadoop environment.
     */
    private static final String JOB_HEAPSIZE_KEY = "graphlab.job.heap-size";
    /**
     * Property key for the directory on HDFS in which to store all input and output.
     */
    private static final String HDFS_DIRECTORY_KEY = "hadoop.hdfs.directory";
    /**
     * Default value for the directory on HDFS in which to store all input and output.
     */
    private static final String HDFS_DIRECTORY = "graphalytics";

    private final String RELATIVE_PATH_TO_TARGET;
    private final String VIRTUAL_CORES;
    private final String HEAP_SIZE;
	private final String TARGET;
    private final boolean USE_HADOOP;
    private boolean saveGraphResult = false;

    private Map<String, String> pathsOfGraphs = new HashMap<>();
    private org.apache.commons.configuration.Configuration graphlabConfig;
    private String hdfsDirectory;

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

        // Read the GraphLab specific configuration options that are the same for all algorithms

        TARGET = this.<String, String>getOption(TARGET_KEY, "local").toLowerCase();
        USE_HADOOP = TARGET.equals("hadoop");
        if (USE_HADOOP) {
            VIRTUAL_CORES = String.valueOf(getOption(JOB_VIRTUALCORES_KEY, 2));
            HEAP_SIZE = String.valueOf(getOption(JOB_HEAPSIZE_KEY, 4096));
        } else {
            VIRTUAL_CORES = "";
            HEAP_SIZE = "";
        }
    }

    private void loadConfiguration() {
        // Load GraphLab-specific configuration
        try {
            graphlabConfig = new PropertiesConfiguration("graphlab.properties");
        } catch (ConfigurationException e) {
            // Fall-back to an empty properties file
            LOG.info("Could not find or load graphlab.properties.");
            graphlabConfig = new PropertiesConfiguration();
        }
        hdfsDirectory = graphlabConfig.getString(HDFS_DIRECTORY_KEY, HDFS_DIRECTORY);
    }

    @Override
    public void uploadGraph(Graph graph, String graphFilePath) throws Exception {
        LOG.entry(graph, graphFilePath);

        if (USE_HADOOP) {
        	String uploadPath = Paths.get(hdfsDirectory, getName(), "input", graph.getName()).toString();

        	// Upload the graph to HDFS
        	FileSystem fs = FileSystem.get(new Configuration());
        	fs.copyFromLocalFile(new Path(graphFilePath), new Path(uploadPath));
        	fs.close();

            // Track available datasets in a map
            pathsOfGraphs.put(graph.getName(), fs.getHomeDirectory().toUri() + "/" + uploadPath);
        } else {
            // Use local files, so just put the local file path in the map
            pathsOfGraphs.put(graph.getName(), graphFilePath);
        }

        LOG.exit();
    }

    @Override
    public PlatformBenchmarkResult executeAlgorithmOnGraph(Algorithm algorithmType, Graph graph, Object parameters)
            throws PlatformExecutionException {
        LOG.entry(algorithmType, graph, parameters);

        int result;
        try {
            GraphLabJob job;
            String graphPath = pathsOfGraphs.get(graph.getName());
            GraphFormat graphFormat = graph.getGraphFormat();

            // Execute the GraphLab job
            switch (algorithmType) {
                case BFS:
                    job = new BreadthFirstSearchJob(parameters, graphPath, graphFormat);
                    break;
                case CD:
                    job = new CommunityDetectionJob(parameters, graphPath, graphFormat);
                    break;
                case CONN:
                    job = new ConnectedComponentsJob(graphPath, graphFormat);
                    break;
                // TODO: Implement ForestFireModel
                //case EVO:
                //    job = new ForestFireModelJob(parameters, graphPath, graphFormat);
                //    break;
                case STATS:
                    job = new LocalClusteringCoefficientJob(graphPath, graphFormat);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported algorithm: " + algorithmType);
            }

            result = executePythonJob(job);

            // TODO: Clean up intermediate and output data, depending on some configuration.
        } catch (Exception e) {
            throw new PlatformExecutionException("GraphLab job failed with exception:", e);
        }

        if (result != 0) {
            throw new PlatformExecutionException("GraphLab job completed with exit code = " + result);
        }
        return LOG.exit(new PlatformBenchmarkResult(NestedConfiguration.empty()));
    }

    /**
     * Get a property from the GraphLab config.
     * The required type of the option is decided based on the type of the defaultValue.
     * The type of the property should be assignable from the type of the defaultValue,
     * as defined by: {@link Class#isAssignableFrom}
     * @param sourceProperty The key of the property
     * @param defaultValue The default value if the property is not set or of the wrong type
     * @param <T> The type of the property
     * @param <S> The type of the default value
     * @return The value of the property, if valid, or the default value
     */
    private <T, S extends T> T getOption(String sourceProperty, S defaultValue) {
        try {
            ConfigurationUtil.ensureConfigurationKeyExists(graphlabConfig, sourceProperty);
            Object value = graphlabConfig.getProperty(sourceProperty);

            // Check if the value object is the same class or a superclass of the default value
            if (value.getClass().isAssignableFrom(defaultValue.getClass())) {
                return (T) value;
            } else {
                // If not, throw an exception
                throw new InvalidConfigurationException("Invalid property type. Expected (superclass/instance of): \""
                        + defaultValue.getClass() + "\", but got: \"" + value.getClass() + "\".");
            }
        } catch (InvalidConfigurationException e) {
            LOG.warn(e.getMessage() + " Defaulting to " + defaultValue + ".");
        }
        return defaultValue;
    }

    /**
     * Execute the python script belonging to a given AlgorithmType with the given graph location and extra arguments
     * and return the Process created by the Java Runtime.
     * @param job The GraphLab job to execute
     * @return The exit code of the python subprocess
     * @throws IOException When an I/O error occurs
     */
    private int executePythonJob(GraphLabJob job) throws IOException {
        LOG.entry(job);

        if (job == null) {
            LOG.warn("GraphLab job set to execute is null, skipping execution.");
            return LOG.exit(-1);
        }
        // Extract the script resource file
        File scriptFile = new File(RELATIVE_PATH_TO_TARGET, job.getPythonFile());
        if (scriptFile.exists() && !scriptFile.canWrite()) {
            LOG.error("Cannot extract GraphLab " + job.getPythonFile() + " script to " + System.getProperty("user.dir")
                    + RELATIVE_PATH_TO_TARGET + ", no write access on an already existing file.");
            return LOG.exit(-1);
        } else if (!scriptFile.exists() && !scriptFile.getParentFile().mkdirs() && !scriptFile.createNewFile()) {
            LOG.error("Cannot extract GraphLab " + job.getPythonFile() + " script to " + System.getProperty("user.dir")
                    + RELATIVE_PATH_TO_TARGET + ", failed to create the appropriate files/directories.");
            return LOG.exit(-1);
        }

        // Actually extract the algorithm script
        try (InputStream pythonFileInputStream = GraphLabPlatform.class.getResourceAsStream(job.getPythonFile())) {
            extractPythonAlgorithm(pythonFileInputStream, scriptFile);
        }

        // Construct the commandline execution pattern starting with the python executable
        CommandLine commandLine = new CommandLine("python2");

        // Add the arguments that are the same for all jobs
        commandLine.addArgument(scriptFile.getAbsolutePath());
        commandLine.addArgument("--target " + TARGET);
        if(USE_HADOOP) {
            commandLine.addArgument("--virtual-cores " + VIRTUAL_CORES);
            commandLine.addArgument("--heap-size " + HEAP_SIZE);
        }

        // Let the job format it's arguments and add it to the commandline
        commandLine.addArguments(job.formatParametersAsStrings());

        // Add the save_graph_result parameter is true (default false, but can be set to true for automated testing)
        if (saveGraphResult) {
            commandLine.addArgument("--save-result");
        }

        // Set the executor of the command, if desired this can be changed to a custom implementation
        DefaultExecutor executor = new DefaultExecutor();

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
            return LOG.exit(e.getExitValue());
        }
        return LOG.exit(result);
    }

    /**
     * Extract a given resourceInputStream to the given outputFile, overwriting any preexisting file.
     * @param resourceInputStream The InputStream to copy from
     * @param outputFile          The File to copy to
     * @throws IOException When an I/O error occurs
     */
    private void extractPythonAlgorithm(InputStream resourceInputStream, File outputFile) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            int read;
            byte[] bytes = new byte[1024];

            while ((read = resourceInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
    }

    /**
     * Set the parameter to enable/disable the algorithm argument to save the
     * processed graphs so they can be retrieved later.
     * @param saveGraphResult Whether or not to save the resulting graphs
     */
    public void setSaveGraphResult(boolean saveGraphResult) {
        this.saveGraphResult = saveGraphResult;
    }

    @Override
    public void deleteGraph(String graphName) {
        if (USE_HADOOP) {
            // TODO: Clean up uploaded graph
        }
    }

    @Override
    public String getName() {
        return "graphlab";
    }

    @Override
    public NestedConfiguration getPlatformConfiguration() {
	    try {
		    org.apache.commons.configuration.Configuration configuration =
				    new PropertiesConfiguration("graphlab.properties");
		    return NestedConfiguration.fromExternalConfiguration(configuration, "graphlab.properties");
	    } catch (ConfigurationException ex) {
		    return NestedConfiguration.empty();
	    }
    }
}
