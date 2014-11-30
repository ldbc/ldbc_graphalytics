package org.tudelft.graphalytics.mapreducev2.conn;

import java.io.IOException;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.Tool;

public class LabelDirectedConnectedComponentsJob extends Configured implements Tool {

	private String inputPath;
    private String intermediatePath;
    private String outputPath;
    
    public LabelDirectedConnectedComponentsJob(String inputPath, String intermediatePath, String outputPath) {
    	this.inputPath = inputPath;
    	this.intermediatePath = intermediatePath;
    	this.outputPath = outputPath;
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
        	jobConfiguration.setJarByClass(LabelDirectedConnectedComponentsJob.class);

        	jobConfiguration.setMapOutputKeyClass(Text.class);
        	jobConfiguration.setMapOutputValueClass(Text.class);

        	jobConfiguration.setMapperClass(LabelDirectedConnectedComponentsMap.class);
        	jobConfiguration.setReducerClass(LabelDirectedConnectedComponentsReducer.class);

        	jobConfiguration.setOutputKeyClass(Text.class);
        	jobConfiguration.setOutputValueClass(Text.class);

        	jobConfiguration.setInputFormat(TextInputFormat.class);
        	jobConfiguration.setOutputFormat(TextOutputFormat.class);
        	
        	jobConfiguration.setNumReduceTasks(10);
        	
        	// Set the input and output paths
        	String outPath = intermediatePath + "/iteration-" + iteration;
        	FileInputFormat.addInputPath(jobConfiguration, new Path(inPath));
        	FileOutputFormat.setOutputPath(jobConfiguration, new Path(outPath));
        	
        	// Execute the current iteration
        	RunningJob jobExecution = JobClient.runJob(jobConfiguration);
        	jobExecution.waitForCompletion();
        	Counters jobCounters = jobExecution.getCounters();
        	long nodesUpdated = jobCounters.getCounter(CONNJobLauncher.Label.UPDATED);
        	if (nodesUpdated== 0)
        		isFinished = true;
        	
        	// Remove the output of the previous job
        	if (iteration != 1)
        		dfs.delete(new Path(inPath), true);
        	inPath = outPath;

            System.out.println("\n************************************");
            System.out.println("* CONN Iteration "+(iteration)+" FINISHED *");
            System.out.println("* Nodes updated: "+ nodesUpdated + " *");
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
