package org.tudelft.graphalytics.yarn.stats;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.jobcontrol.Job;
import org.apache.hadoop.util.Tool;
import org.tudelft.graphalytics.yarn.common.Node;
import org.tudelft.graphalytics.yarn.common.NodeNeighbourhood;

public class GatherUndirectedNodeNeighboursInfoJob extends Configured implements Tool {
    public int  run(String[] args) throws IOException {

        long t0 = System.currentTimeMillis();

        JobConf gatherNodeNeighboursInfoConf = new JobConf(new Configuration());
        Job job2 = new Job(gatherNodeNeighboursInfoConf);
        gatherNodeNeighboursInfoConf.setJarByClass(GatherUndirectedNodeNeighboursInfoJob.class);

        gatherNodeNeighboursInfoConf.setMapOutputKeyClass(Text.class);
        gatherNodeNeighboursInfoConf.setMapOutputValueClass(Node.class);

        gatherNodeNeighboursInfoConf.setMapperClass(GatherUndirectedNodeNeighboursInfoMap.class);
        gatherNodeNeighboursInfoConf.setReducerClass(GatherUndirectedNodeNeighboursInfoReducer.class);

        gatherNodeNeighboursInfoConf.setOutputKeyClass(NullWritable.class);
        gatherNodeNeighboursInfoConf.setOutputValueClass(NodeNeighbourhood.class);

        gatherNodeNeighboursInfoConf.setInputFormat(TextInputFormat.class);
        gatherNodeNeighboursInfoConf.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.addInputPath(gatherNodeNeighboursInfoConf, new Path(args[5] + "/nodeGraph"));
        FileOutputFormat.setOutputPath(gatherNodeNeighboursInfoConf, new Path(args[5] + "/nodeNeighbourhood"));

        gatherNodeNeighboursInfoConf.setNumMapTasks(Integer.parseInt(args[2]));
        gatherNodeNeighboursInfoConf.setNumReduceTasks(Integer.parseInt(args[3]));

        //platform config
        /* Comment to to perform test in local-mode

        /* pseudo */
        /*gatherNodeNeighboursInfoConf.set("io.sort.mb", "768"); //640
        gatherNodeNeighboursInfoConf.set("io.sort.factor", "50");
        gatherNodeNeighboursInfoConf.set("fs.inmemory.size.mb", "768");*/ //640

        /* DAS4 conf Hadoop ver 0.20.203 */
        gatherNodeNeighboursInfoConf.set("io.sort.mb", "1536");
        gatherNodeNeighboursInfoConf.set("io.sort.factor", "80");
        gatherNodeNeighboursInfoConf.set("fs.inmemory.size.mb", "1536");

        JobClient.runJob(gatherNodeNeighboursInfoConf);

        long t1 = System.currentTimeMillis();
        double elapsedTimeSeconds = (t1 - t0)/1000.0;
        System.out.println("NodeNeigh_Texe = "+elapsedTimeSeconds);

        if(Boolean.parseBoolean(args[1])) {
            System.out.println("\n@@@ Deleting intermediate results");
            FileSystem dfs = FileSystem.get(gatherNodeNeighboursInfoConf);
            dfs.delete(new Path(args[5]+"/nodeGraph"), true);
        }

        System.out.println("\n*****************************************");
        System.out.println("* node neighbourhood retrieved FINISHED *");
        System.out.println("*****************************************\n");

        return 0;
    }
}
