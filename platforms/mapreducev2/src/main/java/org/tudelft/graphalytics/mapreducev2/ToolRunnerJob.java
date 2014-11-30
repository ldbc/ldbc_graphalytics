package org.tudelft.graphalytics.mapreducev2;

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

public abstract class ToolRunnerJob<ParamType> extends Configured implements Tool {

	private String inputPath;
	private String intermediatePath;
	private String outputPath;
	private ParamType parameters;
	private int numMappers;
	private int numReducers;
	
	private int iteration;
	
	public ToolRunnerJob(String inputPath, String intermediatePath, String outputPath, ParamType parameters) {
    	this.inputPath = inputPath;
    	this.intermediatePath = intermediatePath;
    	this.outputPath = outputPath;
    	this.parameters = parameters;
    	numMappers = -1;
    	numReducers = -1;
    	iteration = 0;
    }
	
	public void setNumMappers(int numMappers) {
		if (numMappers <= 0)
			this.numMappers = -1;
		else
			this.numMappers = numMappers;
	}
	public void setNumReducers(int numReducers) {
		if (numReducers <= 0)
			this.numReducers = -1;
		else
			this.numReducers = numReducers;
	}
	
	public int getIteration() {
		return iteration;
	}
	public int getNumMappers() {
		return numMappers;
	}
	public int getNumReducers() {
		return numReducers;
	}
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
        	e.printStackTrace();
        }

        return 0;
	}
	
	protected abstract Class<?> getMapOutputKeyClass();
	protected abstract Class<?> getMapOutputValueClass();
	protected abstract Class<?> getOutputKeyClass();
	protected abstract Class<?> getOutputValueClass();
	
	@SuppressWarnings("rawtypes")
	protected abstract Class<? extends InputFormat> getInputFormatClass();
	@SuppressWarnings("rawtypes")
	protected abstract Class<? extends OutputFormat> getOutputFormatClass();
	
	@SuppressWarnings("rawtypes")
	protected abstract Class<? extends Mapper> getMapperClass();
	@SuppressWarnings("rawtypes")
	protected Class<? extends Reducer> getCombinerClass() { return null; }
	@SuppressWarnings("rawtypes")
	protected abstract Class<? extends Reducer> getReducerClass();
	
	protected abstract boolean isFinished();
	protected void setConfigurationParameters(JobConf jobConfiguration) { }
	protected abstract void processJobOutput(RunningJob jobExecution) throws IOException;
	
}
