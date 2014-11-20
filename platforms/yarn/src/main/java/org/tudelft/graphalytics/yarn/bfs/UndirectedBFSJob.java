package org.tudelft.graphalytics.yarn.bfs;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.Tool;

import java.io.IOException;

public class UndirectedBFSJob extends Configured implements Tool {
    // Stopping condition
    public enum Node {
        VISITED
    }
    
    private String inputPath;
    private String intermediatePath;
    private String outputPath;
    private String sourceVertex;
    
    public UndirectedBFSJob(String inputPath, String intermediatePath, String outputPath, String sourceVertex) {
    	this.inputPath = inputPath;
    	this.intermediatePath = intermediatePath;
    	this.outputPath = outputPath;
    	this.sourceVertex = sourceVertex;
    }

    public int run(String[] args) throws IOException {
        boolean isFinished = false;
        int iteration = 0;

        FileSystem dfs = FileSystem.get(getConf());
        String inPath = inputPath;

        while (!isFinished) {
        	iteration++;
        	
        	// Prepare job configuration
        	JobConf jobConfiguration = new JobConf(this.getConf());
        	jobConfiguration.setJarByClass(UndirectedBFSJob.class);

        	jobConfiguration.setMapOutputKeyClass(IntWritable.class);
        	jobConfiguration.setMapOutputValueClass(Text.class);

        	jobConfiguration.setMapperClass(UndirectedBFSMap.class);
        	jobConfiguration.setReducerClass(GenericBFSReducer.class);

        	jobConfiguration.setOutputKeyClass(NullWritable.class);
        	jobConfiguration.setOutputValueClass(Text.class);

        	jobConfiguration.setInputFormat(TextInputFormat.class);
        	jobConfiguration.setOutputFormat(TextOutputFormat.class);
        	
        	jobConfiguration.set(BFSJobLauncher.SOURCE_VERTEX_KEY, sourceVertex);
        	
        	// Set the input and output paths
        	String outPath = intermediatePath + "/iteration-" + iteration;
        	FileInputFormat.addInputPath(jobConfiguration, new Path(inPath));
        	FileOutputFormat.setOutputPath(jobConfiguration, new Path(outPath));
        	
        	// Execute the current iteration
        	RunningJob jobExecution = JobClient.runJob(jobConfiguration);
        	jobExecution.waitForCompletion();
        	Counters jobCounters = jobExecution.getCounters();
        	long nodesVisisted = jobCounters.getCounter(Node.VISITED);
        	if (nodesVisisted == 0)
        		isFinished = true;
        	
        	// Remove the output of the previous job
        	dfs.delete(new Path(inPath), true);
        	inPath = outPath;

            System.out.println("\n************************************");
            System.out.println("* BFS Iteration "+(iteration)+" FINISHED *");
            System.out.println("************************************\n");
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
}

