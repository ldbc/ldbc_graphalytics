package org.tudelft.graphalytics.mapreducev2.conversion;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 * Job specification for mapping a directed vertex-based graph to the correct output format.
 *
 * @author Tim Hegeman
 */
public class DirectedVertexToAdjacencyListConversion  {

	private String inputPath;
	private String outputPath;
	private int numReducers;
	
	public DirectedVertexToAdjacencyListConversion(String inputPath, String outputPath) {
		this.inputPath = inputPath;
		this.outputPath = outputPath;
		this.numReducers = 1;
	}
	
	public DirectedVertexToAdjacencyListConversion withNumberOfReducers(int numReducers) {
		this.numReducers = numReducers;
		return this;
	}
	
	public void run() throws IOException, ClassNotFoundException, InterruptedException {
		Job job = Job.getInstance();
		job.setJarByClass(getClass());
		
		job.setMapperClass(DirectedVertexMapper.class);
		job.setMapOutputKeyClass(LongWritable.class);
		job.setMapOutputValueClass(EdgeData.class);
		
		job.setReducerClass(DirectedVertexReducer.class);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Text.class);
		
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		
		job.setNumReduceTasks(numReducers);
		
		FileInputFormat.addInputPath(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, new Path(outputPath));
		
		job.waitForCompletion(true);
	}
	
}
