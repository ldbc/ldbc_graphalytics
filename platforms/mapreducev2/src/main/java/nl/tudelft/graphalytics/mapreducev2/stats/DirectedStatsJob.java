package nl.tudelft.graphalytics.mapreducev2.stats;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import nl.tudelft.graphalytics.mapreducev2.MapReduceJob;
import nl.tudelft.graphalytics.mapreducev2.common.DirectedNodeNeighbourTextInputFormat;
import nl.tudelft.graphalytics.mapreducev2.common.DirectedNodeNeighbourhood;

/**
 * @author Tim Hegeman
 */
public class DirectedStatsJob extends MapReduceJob<Object> {

	public DirectedStatsJob(String inputPath, String intermediatePath, String outputPath, Object parameters) {
		super(inputPath, intermediatePath, outputPath, parameters);
	}

	@Override
	protected Class<?> getMapOutputKeyClass() {
		return (getIteration() == 1 ?
				Text.class :
				IntWritable.class);
	}

	@Override
	protected Class<?> getMapOutputValueClass() {
		return (getIteration() == 1 ?
				Text.class :
				StatsCCContainer.class);
	}

	@Override
	protected Class<?> getOutputKeyClass() {
		return (getIteration() == 1 ?
				NullWritable.class :
				NullWritable.class);
	}

	@Override
	protected Class<?> getOutputValueClass() {
		return (getIteration() == 1 ?
				DirectedNodeNeighbourhood.class :
				StatsCCContainer.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends InputFormat> getInputFormatClass() {
		return (getIteration() == 1 ?
				TextInputFormat.class :
				DirectedNodeNeighbourTextInputFormat.class);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends OutputFormat> getOutputFormatClass() {
		return (getIteration() == 1 ?
				TextOutputFormat.class :
				TextOutputFormat.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends Mapper> getMapperClass() {
		return (getIteration() == 1 ?
				GatherDirectedNodeNeighboursInfoMap.class :
				DirectedStatsCCMap.class);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends Reducer> getCombinerClass() {
		return (getIteration() == 1 ?
				null :
				StatsCCCombiner.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Class<? extends Reducer> getReducerClass() {
		return (getIteration() == 1 ?
				GatherDirectedNodeNeighboursInfoReducer.class :
				DirectedStatsCCReducer.class);
	}

	@Override
	protected boolean isFinished() {
		return (getIteration() >= 2);
	}

	@Override
	protected void setConfigurationParameters(JobConf jobConfiguration) {
		
	}

	@Override
	protected void processJobOutput(RunningJob jobExecution) throws IOException {
		if (getIteration() == 1) {
			System.out.println("\n*****************************************");
	        System.out.println("* node neighbourhood retrieved FINISHED *");
	        System.out.println("*****************************************\n");
		} else {
			System.out.println("\n***********************************************************");
	        System.out.println("* basic stats and average clustering coefficient FINISHED *");
	        System.out.println("***********************************************************\n");
		}
	}

}
