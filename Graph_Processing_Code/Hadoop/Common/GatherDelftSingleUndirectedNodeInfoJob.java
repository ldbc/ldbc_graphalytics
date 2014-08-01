package org.hadoop.test.jobs.tasks.utils.undirected;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.jobcontrol.Job;
import org.apache.hadoop.util.Tool;
import org.hadoop.test.combine.undirected.GatherSingleUndirectedNodeInfoCombiner;
import org.hadoop.test.map.undirected.GatherDelftSingleUndirectedNodeInfoMap;
import org.hadoop.test.reduce.undirected.GatherSingleUndirectedNodeInfoReducer;

import java.io.IOException;

public class GatherDelftSingleUndirectedNodeInfoJob extends Configured implements Tool {
    public int run(String[] args) throws IOException {
        JobConf gatherSingleNodeInfoConf = new JobConf(new Configuration());
        Job job = new Job(gatherSingleNodeInfoConf);
        gatherSingleNodeInfoConf.setJarByClass(GatherDelftSingleUndirectedNodeInfoJob.class);

        gatherSingleNodeInfoConf.setMapOutputKeyClass(Text.class);
        gatherSingleNodeInfoConf.setMapOutputValueClass(Text.class);

        gatherSingleNodeInfoConf.setMapperClass(GatherDelftSingleUndirectedNodeInfoMap.class);
        gatherSingleNodeInfoConf.setCombinerClass(GatherSingleUndirectedNodeInfoCombiner.class);
        gatherSingleNodeInfoConf.setReducerClass(GatherSingleUndirectedNodeInfoReducer.class);

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

