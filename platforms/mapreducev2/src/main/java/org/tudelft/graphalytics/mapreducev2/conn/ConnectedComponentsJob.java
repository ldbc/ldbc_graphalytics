package org.tudelft.graphalytics.mapreducev2.conn;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.Counters;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.tudelft.graphalytics.mapreducev2.ToolRunnerJob;

public class ConnectedComponentsJob extends ToolRunnerJob<Object> {

	private boolean directed;
	private boolean finished = false;
	
	public ConnectedComponentsJob(String inputPath, String intermediatePath,
			String outputPath, Object parameters, boolean directed) {
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
		return Text.class;
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
				LabelDirectedConnectedComponentsMap.class :
				LabelUndirectedConnectedComponentsMap.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends Reducer> getReducerClass() {
		return (directed ?
				LabelDirectedConnectedComponentsReducer.class :
				LabelUndirectedConnectedComponentsReducer.class);
	}

	@Override
	protected boolean isFinished() {
		return finished;
	}

	@Override
	protected void processJobOutput(RunningJob jobExecution) throws IOException {
		Counters jobCounters = jobExecution.getCounters();
    	long nodesUpdated = jobCounters.getCounter(CONNJobLauncher.Label.UPDATED);
    	if (nodesUpdated== 0)
    		finished = true;
    	
    	System.out.println("\n************************************");
        System.out.println("* CONN Iteration "+ getIteration() +" FINISHED *");
        System.out.println("* Nodes updated: "+ nodesUpdated + " *");
        System.out.println("************************************\n");
	}
	
}
