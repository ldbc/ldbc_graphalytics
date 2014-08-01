package org.hadoop.test.jobs.tasks.community;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.jobcontrol.Job;
import org.apache.hadoop.util.Tool;
import org.hadoop.test.jobs.tasks.community.utils.LPAUtils;
import org.hadoop.test.map.community.UndirectedCambridgeLPAMap;
import org.hadoop.test.reduce.community.UndirectedCambridgeLPAReducer;

import java.io.IOException;

/**
 Towards Real-Time Community Detection in Large Networks
                        by
 Ian X.Y. Leung,Pan Hui,Pietro Li,and Jon Crowcroft
 */
public class UndirectedCambridgeLPAJob extends Configured implements Tool {
    public int run(String[] args) throws IOException {
        boolean isFinished = false;
        int iteration = 0;
        long counter = 0;
        JobConf lpaConf = new JobConf(new Configuration());
        int maxIterThreshold = 20; // read as param (default.value > 0)
        int step = 0;

        long t0 = System.currentTimeMillis();

        while (!isFinished && (step < maxIterThreshold)) {
            step++;

            Configuration config = new Configuration();
            lpaConf = new JobConf(config);
            Job job = new Job(lpaConf);
            RunningJob runningJob;

            lpaConf.setJarByClass(UndirectedCambridgeLPAJob.class);

            lpaConf.setMapOutputKeyClass(VIntWritable.class);
            lpaConf.setMapOutputValueClass(Text.class);

            lpaConf.setMapperClass(UndirectedCambridgeLPAMap.class);
            lpaConf.setReducerClass(UndirectedCambridgeLPAReducer.class);

            lpaConf.setOutputKeyClass(NullWritable.class);
            lpaConf.setOutputValueClass(Text.class);

            lpaConf.setInputFormat(TextInputFormat.class);
            lpaConf.setOutputFormat(TextOutputFormat.class);

            lpaConf.setNumMapTasks(Integer.parseInt(args[2]));
            lpaConf.setNumReduceTasks(Integer.parseInt(args[3]));

            lpaConf.set(LPAUtils.DELTA_PARAM, args[6]);
            lpaConf.set(LPAUtils.M_PARAM, args[7]);
            maxIterThreshold = Integer.parseInt(args[8]);

            //platform config
            /* pseudo */
            if(args[9].equals("pseudo")) {
                lpaConf.set("io.sort.mb", "768");
                lpaConf.set("fs.inmemory.size.mb", "768");
                lpaConf.set("io.sort.factor", "50");
            }
            /* DAS4 conf Hadoop ver 0.20.203 */
            else if(args[9].equals("das")) {
                lpaConf.set("io.sort.mb", "1536");
                lpaConf.set("io.sort.factor", "80");
                lpaConf.set("fs.inmemory.size.mb", "1536");
            }

            FileSystem dfs;

            if(iteration % 2 == 0) {
                dfs = FileSystem.get(config);
                dfs.delete(new Path(args[5]+"/Communities"), true);

                FileInputFormat.addInputPath(lpaConf, new Path(args[5] + "/nodeGraph"));
                FileOutputFormat.setOutputPath(lpaConf, new Path(args[5]+"/Communities"));
                runningJob = JobClient.runJob(lpaConf);
            } else {
                dfs = FileSystem.get(config);
                dfs.delete(new Path(args[5]+"/nodeGraph"), true);

                FileInputFormat.addInputPath(lpaConf, new Path(args[5]+"/Communities"));
                FileOutputFormat.setOutputPath(lpaConf, new Path(args[5]+"/nodeGraph"));
                runningJob = JobClient.runJob(lpaConf);
            }

            Counters counters = runningJob.getCounters();
            counter = counters.getCounter(LPAUtils.Label.CHANGED);

            System.out.println("\n************************************");
            System.out.println("* Cambridge_LPA Iteration "+(iteration+1)+" FINISHED *");
            System.out.println("* Labels changed "+counter+" *");
            System.out.println("************************************\n");

            if(counter == 0)
                isFinished = true;

            iteration++;
        }

        // recording end time of stats
        long t1 = System.currentTimeMillis();

        double elapsedTimeSeconds = (t1 - t0)/1000.0;
        System.out.println("Stats_Texe = "+elapsedTimeSeconds);

        /*
            Clean UP

         */
        Configuration config = new Configuration();
        FileSystem dfs;

        // by the end of iteration iterationCounter is increased, thus del opposite directories then within WHILE()
        if(iteration % 2 == 0) {
            dfs = FileSystem.get(config);
            dfs.delete(new Path(args[5]+"/Communities"), true);
        } else {
            dfs = FileSystem.get(config);
            dfs.delete(new Path(args[5]+"/nodeGraph"), true);
        }

        // creating benchmark data
        try{
            FileSystem fs = FileSystem.get(lpaConf);
            Path path = new Path(args[5]+"/benchmark.txt");
            FSDataOutputStream os = fs.create(path);
            String benchmarkData = "elapsed time: "+elapsedTimeSeconds+"\nsteps: "+iteration+" (counting from 1)";
            os.write(benchmarkData.getBytes());
            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }
}
