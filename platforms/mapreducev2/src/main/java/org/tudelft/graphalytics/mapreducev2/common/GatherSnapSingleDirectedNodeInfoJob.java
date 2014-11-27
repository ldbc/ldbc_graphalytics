package org.tudelft.graphalytics.mapreducev2.common;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.Tool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class GatherSnapSingleDirectedNodeInfoJob extends Configured implements Tool {
	private static final Logger log = LogManager.getLogger(GatherSnapSingleDirectedNodeInfoJob.class);
	
	private String inputPath;
	private String outputPath;
	
	public GatherSnapSingleDirectedNodeInfoJob(String inputPath, String outputPath) {
		this.inputPath = inputPath;
		this.outputPath = outputPath;
	}
	
    public int run(String[] args) throws IOException {
        JobConf gatherSingleNodeInfoConf = new JobConf(getConf());
        gatherSingleNodeInfoConf.setJarByClass(GatherSnapSingleDirectedNodeInfoJob.class);

        gatherSingleNodeInfoConf.setMapOutputKeyClass(Text.class);
        gatherSingleNodeInfoConf.setMapOutputValueClass(Text.class);

        gatherSingleNodeInfoConf.setMapperClass(GatherSnapSingleDirectedNodeInfoMap.class);
        gatherSingleNodeInfoConf.setCombinerClass(GatherSingleDirectedNodeInfoCombiner.class);
        gatherSingleNodeInfoConf.setReducerClass(GatherSingleDirectedNodeInfoReducer.class);

        gatherSingleNodeInfoConf.setOutputKeyClass(NullWritable.class);
        gatherSingleNodeInfoConf.setOutputValueClass(Text.class);

        gatherSingleNodeInfoConf.setInputFormat(TextInputFormat.class);
        gatherSingleNodeInfoConf.setOutputFormat(TextOutputFormat.class);
        
        gatherSingleNodeInfoConf.setNumReduceTasks(10);

        FileInputFormat.addInputPath(gatherSingleNodeInfoConf, new Path(inputPath));
        FileOutputFormat.setOutputPath(gatherSingleNodeInfoConf, new Path(outputPath + "/prepared-graph"));

        JobClient.runJob(gatherSingleNodeInfoConf);

        System.out.println("\n********************************");
        System.out.println("* node based data set FINISHED *");
        System.out.println("********************************\n");

        return 0;
    }
}
