package nl.tudelft.graphalytics.graphlab;

import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.domain.GraphFormat;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Base class for all jobs in the GraphLab benchmark suite. Configures a GraphLab job
 * using the computation and vertex format specified by subclasses of GraphLabJob.
 * An instance of this class can then be fed into the {@link nl.tudelft.graphalytics.graphlab.GraphLabPlatform#executePythonJob} method to be executed.
 * @author Jorai Rijsdijk
 */
public abstract class GraphLabJob {
    protected String pythonFile;
    protected Algorithm algorithm;
    protected String graphPath;
    protected GraphFormat graphFormat;
    protected Object parameters;

    /**
     * Construct a new GraphLab Job with the given parameters.
     * @param pythonFile   The location of the GraphLab python script to execute
     * @param algorithm    The algorithm to execute
     * @param parameters   The algorithm parameters
     * @param graphPath    The path to the uploaded graph on the HDFS filesystem
     * @param graphFormat  The format of the graph
     */
    protected GraphLabJob(String pythonFile, Algorithm algorithm, Object parameters,
                       String graphPath, GraphFormat graphFormat) {
        this.pythonFile = pythonFile;
        this.algorithm = algorithm;
        this.parameters = parameters;
        this.graphPath = graphPath;
        this.graphFormat = graphFormat;
    }

    /**
     * Format the parameters in a way that the relevant python script knows how to parse them.
     * @return The parameters for this job in a useful format for the python script.
     */
    public abstract String[] formatParametersAsStrings();

    /**
     * Build a parameter String array with default parameters and algorithm specific options.
     * @param algorithmSpecificOptions An arraylist of algorithm specific options
     * @return The constructed parameter array
     */
    public String[] formatParametersHelper(String... algorithmSpecificOptions) {
        ArrayList<String> parameters = new ArrayList<>(6 + algorithmSpecificOptions.length);
        parameters.add("-f");
        parameters.add(graphPath);

        parameters.add("--directed");
        parameters.add(String.valueOf(graphFormat.isDirected()));

        parameters.add("--edge-based");
        parameters.add(String.valueOf(graphFormat.isEdgeBased()));

        Collections.addAll(parameters, algorithmSpecificOptions);
        return parameters.toArray(new String[parameters.size()]);
    }

    /**
     * Get the script file to execute.
     * @return The python file to execute for this job
     */
    public String getPythonFile() {
        return pythonFile;
    }

    /**
     * Get the algorithm to be executed.
     * @return The algorithm this job is going to run
     */
    public Algorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * Get the path to the graph on the HDFS filesystem
     * @return The path to the graph used for this job
     */
    public String getGraphPath() {
        return graphPath;
    }

    /**
     * Get the graph format
     * @return The format of the graph for this job
     */
    public GraphFormat getGraphFormat() {
        return graphFormat;
    }

    /**
     * Get the algorithm parameters.
     * @return The parameters for the algorithm
     */
    public Object getParameters() {
        return parameters;
    }
}
