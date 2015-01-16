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
 * Job specification for converting edge-based graphs to a vertex-based format.
 *
 * @author Tim Hegeman
 */
public class EdgesToAdjacencyListConversion {

	private String inputPath;
	private String outputPath;
	private boolean directed;
	private int numReducers;
	
	public EdgesToAdjacencyListConversion(String inputPath, String outputPath, boolean directed) {
		this.inputPath = inputPath;
		this.outputPath = outputPath;
		this.directed = directed;
		this.numReducers = 1;
	}
	
	public EdgesToAdjacencyListConversion withNumberOfReducers(int numReducers) {
		this.numReducers = numReducers;
		return this;
	}
	
	public void run() throws IOException, ClassNotFoundException, InterruptedException {
		Job job = Job.getInstance();
		job.setJarByClass(getClass());
		
		job.setMapOutputKeyClass(LongWritable.class);
		if (directed) {
			job.setMapperClass(DirectedEdgeMapper.class);
			job.setMapOutputValueClass(EdgeData.class);
			job.setReducerClass(DirectedVertexReducer.class);
		} else {
			job.setMapperClass(UndirectedEdgeMapper.class);
			job.setMapOutputValueClass(LongWritable.class);
			job.setReducerClass(UndirectedVertexReducer.class);
		}
		
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
