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

public class GatherDelftSingleDirectedNodeInfoJob extends Configured implements Tool {
	private static final Logger log = LogManager.getLogger();
	
    public int run(String[] args) throws IOException {
    	log.entry((Object[])args);
    	
        JobConf gatherSingleNodeInfoConf = new JobConf(getConf(), GatherSnapSingleDirectedNodeInfoJob.class);
        gatherSingleNodeInfoConf.set("mapred.job.tracker", "localhost:9001");
        gatherSingleNodeInfoConf.set("fs.default.name", "hdfs://localhost:9000");
        
        gatherSingleNodeInfoConf.setMapOutputKeyClass(Text.class);
        gatherSingleNodeInfoConf.setMapOutputValueClass(Text.class);

        gatherSingleNodeInfoConf.setMapperClass(GatherDelftSingleDirectedNodeInfoMap.class);
        gatherSingleNodeInfoConf.setCombinerClass(GatherSingleDirectedNodeInfoCombiner.class);
        gatherSingleNodeInfoConf.setReducerClass(GatherSingleDirectedNodeInfoReducer.class);

        gatherSingleNodeInfoConf.setOutputKeyClass(NullWritable.class);
        gatherSingleNodeInfoConf.setOutputValueClass(Text.class);

        gatherSingleNodeInfoConf.setInputFormat(TextInputFormat.class);
        gatherSingleNodeInfoConf.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.addInputPath(gatherSingleNodeInfoConf, new Path(args[4]));
        FileOutputFormat.setOutputPath(gatherSingleNodeInfoConf, new Path(args[5] + "/nodeGraph"));

        gatherSingleNodeInfoConf.setNumMapTasks(Integer.parseInt(args[2]));
        gatherSingleNodeInfoConf.setNumReduceTasks(Integer.parseInt(args[3]));

        JobClient.runJob(gatherSingleNodeInfoConf).waitForCompletion();

        if(Boolean.parseBoolean(args[0])) {
            System.out.println("\n@@@ Deleting src input");
            FileSystem dfs = FileSystem.get(gatherSingleNodeInfoConf);
            dfs.delete(new Path(args[4]), true);
        }

        System.out.println("\n********************************");
        System.out.println("* node based data set FINISHED *");
        System.out.println("********************************\n");

        return log.exit(0);
    }
}
