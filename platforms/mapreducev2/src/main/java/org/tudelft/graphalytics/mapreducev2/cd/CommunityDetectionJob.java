package org.tudelft.graphalytics.mapreducev2.cd;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.Counters;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.tudelft.graphalytics.algorithms.CDParameters;
import org.tudelft.graphalytics.mapreducev2.MapReduceJob;

public class CommunityDetectionJob extends MapReduceJob<CDParameters> {

	private boolean directed;
	private boolean finished = false;
	
	public CommunityDetectionJob(String inputPath, String intermediatePath, String outputPath,
			CDParameters parameters, boolean directed) {
		super(inputPath, intermediatePath, outputPath, parameters);
		this.directed = directed;
	}

	@Override
	protected Class<?> getMapOutputKeyClass() {
		return Text.class;
	}

	@Override
	protected Class<?> getMapOutputValueClass() {
		return Text.class;
	}

	@Override
	protected Class<?> getOutputKeyClass() {
		return NullWritable.class;
	}

	@Override
	protected Class<?> getOutputValueClass() {
		return Text.class;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends InputFormat> getInputFormatClass() {
		return TextInputFormat.class;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends OutputFormat> getOutputFormatClass() {
		return TextOutputFormat.class;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends Mapper> getMapperClass() {
		return (directed ?
				DirectedCambridgeLPAMap.class :
				UndirectedCambridgeLPAMap.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends Reducer> getReducerClass() {
		return (directed ?
				DirectedCambridgeLPAReducer.class :
				UndirectedCambridgeLPAReducer.class);
	}

	@Override
	protected boolean isFinished() {
		return finished || getIteration() >= getParameters().getMaxIterations();
	}
	
	@Override
	protected void setConfigurationParameters(JobConf jobConfiguration) {
		super.setConfigurationParameters(jobConfiguration);
		jobConfiguration.set(CDJobLauncher.HOP_ATTENUATION, Float.toString(getParameters().getHopAttenuation()));
    	jobConfiguration.set(CDJobLauncher.NODE_PREFERENCE, Float.toString(getParameters().getNodePreference()));
	}

	@Override
	protected void processJobOutput(RunningJob jobExecution) throws IOException {
		Counters jobCounters = jobExecution.getCounters();
    	long nodesVisisted = jobCounters.getCounter(CDJobLauncher.Label.CHANGED);
    	if (nodesVisisted == 0)
    		finished = true;
    	
    	System.out.println("\n************************************");
        System.out.println("* CD Iteration "+ getIteration() +" FINISHED *");
        System.out.println("* Nodes visited: " + nodesVisisted + " *");
        System.out.println("************************************\n");
	}
	
	
	
}
