package org.tudelft.graphalytics.giraph.conversion;

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
		
		if (directed)
			job.setMapperClass(DirectedEdgeMapper.class);
		else
			job.setMapperClass(UndirectedEdgeMapper.class);
		job.setMapOutputKeyClass(LongWritable.class);
		job.setMapOutputValueClass(LongWritable.class);
		
		job.setReducerClass(VertexOutReducer.class);
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
