package org.tudelft.graphalytics.yarn.common;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.jobcontrol.Job;
import org.apache.hadoop.mapred.lib.IdentityMapper;
import org.apache.hadoop.util.Tool;

import java.io.IOException;

/*
    Only pass by JOB
    - read input
    - pass it unchanged
    - output in /nodeGraph

    ** Graph type does not play any role **
 */
public class GatherFilteredSingleNodeInfoJob extends Configured implements Tool {
    public int run(String[] args) throws IOException {
        JobConf gatherSingleNodeInfoConf = new JobConf(new Configuration());
        Job job = new Job(gatherSingleNodeInfoConf);
        gatherSingleNodeInfoConf.setJarByClass(GatherSnapSingleDirectedNodeInfoJob.class);

        gatherSingleNodeInfoConf.setMapOutputKeyClass(LongWritable.class);
        gatherSingleNodeInfoConf.setMapOutputValueClass(Text.class);

        gatherSingleNodeInfoConf.setMapperClass(IdentityMapper.class);
        gatherSingleNodeInfoConf.setReducerClass(FilterKeyReducer.class);

        gatherSingleNodeInfoConf.setOutputKeyClass(NullWritable.class);
        gatherSingleNodeInfoConf.setOutputValueClass(Text.class);

        gatherSingleNodeInfoConf.setInputFormat(TextInputFormat.class);
        gatherSingleNodeInfoConf.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.addInputPath(gatherSingleNodeInfoConf, new Path(args[4]));
        FileOutputFormat.setOutputPath(gatherSingleNodeInfoConf, new Path(args[5] + "/nodeGraph"));

        gatherSingleNodeInfoConf.setNumMapTasks(Integer.parseInt(args[2]));
        gatherSingleNodeInfoConf.setNumReduceTasks(Integer.parseInt(args[3]));

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

