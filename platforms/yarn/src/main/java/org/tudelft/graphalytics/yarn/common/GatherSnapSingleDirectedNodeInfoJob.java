package org.tudelft.graphalytics.yarn.common;

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
	
    public int run(String[] args) throws IOException {
        JobConf gatherSingleNodeInfoConf = new JobConf(getConf());
        log.info(gatherSingleNodeInfoConf.get("mapred.job.tracker"));
        log.info(gatherSingleNodeInfoConf.get("mapreduce.jobtracker.address"));
        gatherSingleNodeInfoConf.set("mapreduce.jobtracker.address", "localhost:9001");
        gatherSingleNodeInfoConf.set("mapreduce.framework.name", "yarn");
        gatherSingleNodeInfoConf.set("fs.defaultFS", "hdfs://localhost:9000");
        //gatherSingleNodeInfoConf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        
        gatherSingleNodeInfoConf.setJarByClass(GatherSnapSingleDirectedNodeInfoJob.class);
        //gatherSingleNodeInfoConf.setJar("/data/tudelft/graphalytics/platforms/yarn/target/graphalytics-platforms-yarn-0.0.1-SNAPSHOT-jar-with-dependencies.jar");

        gatherSingleNodeInfoConf.setMapOutputKeyClass(Text.class);
        gatherSingleNodeInfoConf.setMapOutputValueClass(Text.class);

        gatherSingleNodeInfoConf.setMapperClass(GatherSnapSingleDirectedNodeInfoMap.class);
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
        
        log.info(gatherSingleNodeInfoConf);
        log.info(gatherSingleNodeInfoConf.get("mapred.job.tracker"));
        log.info(gatherSingleNodeInfoConf.get("mapreduce.jobtracker.address"));

        JobClient.runJob(gatherSingleNodeInfoConf);

        if(Boolean.parseBoolean(args[0])) {
            System.out.println("\n@@@ Deleting src input");
            FileSystem dfs = FileSystem.get(gatherSingleNodeInfoConf);
            dfs.delete(new Path(args[4]), true);
        }

        System.out.println("\n********************************");
        System.out.println("* node based data set FINISHED *");
        System.out.println("********************************\n");

        return 0;
    }
}
