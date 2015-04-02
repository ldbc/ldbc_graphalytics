package nl.tudelft.graphalytics.mapreducev2;

import java.io.IOException;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class for MapReduce jobs with hooks for algorithm-specific configuration.
 * Processes all the necessary MapReduce configuration and performs the main job
 * execution loop.
 *
 * @param <ParamType> the algorithm-specification parameter type
 */
public abstract class MapReduceJob<ParamType> extends Configured implements Tool {
	private static final Logger LOG = LogManager.getLogger();

	private String inputPath;
	private String intermediatePath;
	private String outputPath;
	private ParamType parameters;
	private int numMappers;
	private int numReducers;
	
	private int iteration;

	/**
	 * @param inputPath the HDFS path of the input graph
	 * @param intermediatePath the HDFS path for intermediary output
	 * @param outputPath the HDFS path for the job output
	 * @param parameters algorithm-specific parameters
	 */
	public MapReduceJob(String inputPath, String intermediatePath, String outputPath, ParamType parameters) {
    	this.inputPath = inputPath;
    	this.intermediatePath = intermediatePath;
    	this.outputPath = outputPath;
    	this.parameters = parameters;
    	numMappers = -1;
    	numReducers = -1;
    	iteration = 0;
    }

	/**
	 * @param numMappers the number of mappers to use
	 */
	public void setNumMappers(int numMappers) {
		if (numMappers <= 0)
			this.numMappers = -1;
		else
			this.numMappers = numMappers;
	}

	/**
	 * @param numReducers the number of reducers to use
	 */
	public void setNumReducers(int numReducers) {
		if (numReducers <= 0)
			this.numReducers = -1;
		else
			this.numReducers = numReducers;
	}

	/**
	 * @return the current algorithm iteration
	 */
	public int getIteration() {
		return iteration;
	}

	/**
	 * @return the number of mappers set for this job
	 */
	public int getNumMappers() {
		return numMappers;
	}

	/**
	 * @return the number of reducers set for this job
	 */
	public int getNumReducers() {
		return numReducers;
	}

	/**
	 * @return the algorithm-specific parameters
	 */
	protected ParamType getParameters() {
		return parameters;
	}
	
	@Override
	public int run(String[] args) throws Exception {
        FileSystem dfs = FileSystem.get(getConf());
        String inPath = inputPath;

        while (!isFinished()) {
        	iteration++;
        	
        	// Prepare job configuration
        	JobConf jobConfiguration = new JobConf(this.getConf());
        	jobConfiguration.setJarByClass(this.getClass());

        	jobConfiguration.setMapOutputKeyClass(getMapOutputKeyClass());
        	jobConfiguration.setMapOutputValueClass(getMapOutputValueClass());

        	jobConfiguration.setMapperClass(getMapperClass());
        	if (getCombinerClass() != null)
        		jobConfiguration.setCombinerClass(getCombinerClass());
        	jobConfiguration.setReducerClass(getReducerClass());

        	jobConfiguration.setOutputKeyClass(getOutputKeyClass());
        	jobConfiguration.setOutputValueClass(getOutputValueClass());

        	jobConfiguration.setInputFormat(getInputFormatClass());
        	jobConfiguration.setOutputFormat(getOutputFormatClass());
        	
        	if (getNumMappers() != -1)
        		jobConfiguration.setNumMapTasks(getNumMappers());
        	if (getNumReducers() != -1)
        		jobConfiguration.setNumReduceTasks(getNumReducers());
        	
        	setConfigurationParameters(jobConfiguration);
        	
        	// Set the input and output paths
        	String outPath = intermediatePath + "/iteration-" + iteration;
        	FileInputFormat.addInputPath(jobConfiguration, new Path(inPath));
        	FileOutputFormat.setOutputPath(jobConfiguration, new Path(outPath));
        	
        	// Execute the current iteration
        	RunningJob jobExecution = JobClient.runJob(jobConfiguration);
        	jobExecution.waitForCompletion();
        	        	
        	// Remove the output of the previous job (unless it is the input graph)
        	if (iteration != 1) {
        		dfs.delete(new Path(inPath), true);
        	}
        	inPath = outPath;

            processJobOutput(jobExecution);
        }

        // Rename the last job output to the specified output path
        try {
        	dfs.mkdirs(new Path(outputPath).getParent());
        	dfs.rename(new Path(inPath), new Path(outputPath));
        } catch (Exception e) {
        	LOG.warn("Failed to rename MapReduce job output.", e);
        }

        return 0;
	}

	/**
	 * @return the type of the map-phase output keys
	 */
	protected abstract Class<?> getMapOutputKeyClass();

	/**
	 * @return the type of the map-phase output values
	 */
	protected abstract Class<?> getMapOutputValueClass();

	/**
	 * @return the type of the reduce-phase output keys
	 */
	protected abstract Class<?> getOutputKeyClass();

	/**
	 * @return the type of the reduce-phase output values
	 */
	protected abstract Class<?> getOutputValueClass();

	/**
	 * @return the input format type
	 */
	@SuppressWarnings("rawtypes")
	protected abstract Class<? extends InputFormat> getInputFormatClass();

	/**
	 * @return the output format type
	 */
	@SuppressWarnings("rawtypes")
	protected abstract Class<? extends OutputFormat> getOutputFormatClass();

	/**
	 * @return the job-specific mapper class
	 */
	@SuppressWarnings("rawtypes")
	protected abstract Class<? extends Mapper> getMapperClass();

	/**
	 * @return the job-specific combiner class, or null if unused
	 */
	@SuppressWarnings("rawtypes")
	protected Class<? extends Reducer> getCombinerClass() { return null; }

	/**
	 * @return the job-specific reducer class
	 */
	@SuppressWarnings("rawtypes")
	protected abstract Class<? extends Reducer> getReducerClass();

	/**
	 * @return true if no more iterations of the algorithm are needed
	 */
	protected abstract boolean isFinished();

	/**
	 * Called before executing a job to allow for job-specific configuration.
	 *
	 * @param jobConfiguration the job configuration that may be updated
	 */
	protected void setConfigurationParameters(JobConf jobConfiguration) { }

	/**
	 * Called after job completion to allow for parsing of job output such as counters.
	 *
	 * @param jobExecution the job execution result
	 * @throws IOException if an exception occurs while reading job output
	 */
	protected abstract void processJobOutput(RunningJob jobExecution) throws IOException;
	
}
