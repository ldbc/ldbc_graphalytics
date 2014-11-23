package org.tudelft.graphalytics.mapreducev2.common;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.jobcontrol.Job;
import org.apache.hadoop.util.Tool;

import java.io.IOException;

public class GatherSnapSingleUndirectedNodeInfoJob extends Configured implements Tool {
    public int run(String[] args) throws IOException {
        JobConf gatherSingleNodeInfoConf = new JobConf(new Configuration());
        Job job = new Job(gatherSingleNodeInfoConf);
        gatherSingleNodeInfoConf.setJarByClass(GatherSnapSingleUndirectedNodeInfoJob.class);

        gatherSingleNodeInfoConf.setMapOutputKeyClass(Text.class);
        gatherSingleNodeInfoConf.setMapOutputValueClass(Text.class);

        gatherSingleNodeInfoConf.setMapperClass(GatherSnapSingleUndirectedNodeInfoMap.class);
        gatherSingleNodeInfoConf.setReducerClass(GatherSingleUndirectedNodeInfoReducer.class);

        gatherSingleNodeInfoConf.setOutputKeyClass(NullWritable.class);
        gatherSingleNodeInfoConf.setOutputValueClass(Text.class);

        gatherSingleNodeInfoConf.setInputFormat(TextInputFormat.class);
        gatherSingleNodeInfoConf.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.addInputPath(gatherSingleNodeInfoConf, new Path(args[4]));
        FileOutputFormat.setOutputPath(gatherSingleNodeInfoConf, new Path(args[5] + "/nodeGraph"));

        gatherSingleNodeInfoConf.setNumMapTasks(Integer.parseInt(args[2]));
        gatherSingleNodeInfoConf.setNumReduceTasks(Integer.parseInt(args[3]));

        /* DAS4 conf Hadoop ver 0.20.203 */
        gatherSingleNodeInfoConf.set("io.sort.mb", "1536");
        gatherSingleNodeInfoConf.set("io.sort.factor", "80");
        gatherSingleNodeInfoConf.set("fs.inmemory.size.mb", "1536");

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

