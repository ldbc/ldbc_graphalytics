package org.tudelft.graphalytics.yarn.cd;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.Tool;
import org.tudelft.graphalytics.algorithms.CDParameters;

import java.io.IOException;


public class UndirectedCambridgeLPAJob extends Configured implements Tool {
	
	private String inputPath;
    private String intermediatePath;
    private String outputPath;
    private CDParameters parameters;
    
    public UndirectedCambridgeLPAJob(String inputPath, String intermediatePath, String outputPath, CDParameters parameters) {
    	this.inputPath = inputPath;
    	this.intermediatePath = intermediatePath;
    	this.outputPath = outputPath;
    	this.parameters = parameters;
    }
    
    public int run(String[] args) throws IOException {
        boolean isFinished = false;
        int iteration = 0;

        FileSystem dfs = FileSystem.get(getConf());
        String inPath = inputPath;

        while (!isFinished && (iteration < parameters.getMaxIterations())) {
        	iteration++;
        	
        	// Prepare job configuration
        	JobConf jobConfiguration = new JobConf(this.getConf());
        	jobConfiguration.setJarByClass(UndirectedCambridgeLPAJob.class);

        	jobConfiguration.setMapOutputKeyClass(Text.class);
        	jobConfiguration.setMapOutputValueClass(Text.class);

        	jobConfiguration.setMapperClass(UndirectedCambridgeLPAMap.class);
        	jobConfiguration.setReducerClass(UndirectedCambridgeLPAReducer.class);

        	jobConfiguration.setOutputKeyClass(NullWritable.class);
        	jobConfiguration.setOutputValueClass(Text.class);

        	jobConfiguration.setInputFormat(TextInputFormat.class);
        	jobConfiguration.setOutputFormat(TextOutputFormat.class);
        	
        	jobConfiguration.set(CDJobLauncher.HOP_ATTENUATION, Float.toString(parameters.getHopAttenuation()));
        	jobConfiguration.set(CDJobLauncher.NODE_PREFERENCE, Float.toString(parameters.getNodePreference()));
        	
        	// Set the input and output paths
        	String outPath = intermediatePath + "/iteration-" + iteration;
        	FileInputFormat.addInputPath(jobConfiguration, new Path(inPath));
        	FileOutputFormat.setOutputPath(jobConfiguration, new Path(outPath));
        	
        	// Execute the current iteration
        	RunningJob jobExecution = JobClient.runJob(jobConfiguration);
        	jobExecution.waitForCompletion();
        	Counters jobCounters = jobExecution.getCounters();
        	long nodesVisisted = jobCounters.getCounter(CDJobLauncher.Label.CHANGED);
        	if (nodesVisisted == 0)
        		isFinished = true;
        	
        	// Remove the output of the previous job
        	dfs.delete(new Path(inPath), true);
        	inPath = outPath;

            System.out.println("\n************************************");
            System.out.println("* CD Iteration "+(iteration)+" FINISHED *");
            System.out.println("* Nodes visited: " + nodesVisisted + " *");
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
