package org.hadoop.test.jobs.tasks.utils.undirected;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.jobcontrol.Job;
import org.apache.hadoop.util.Tool;
import org.hadoop.test.jobs.tasks.utils.directed.LabelDirectedConnectedComponentsJob;
import org.hadoop.test.map.undirected.LabelUndirectedConnectedComponentsMap;
import org.hadoop.test.reduce.undirected.LabelUndirectedConnectedComponentsReducer;

public class LabelUndirectedConnectedComponentsJob extends Configured implements Tool {
    public enum Label {
        UPDATED
    }

    public int run(String[] args) throws Exception {
        JobConf connCompConf = new JobConf(new Configuration());

        int iteration = 0;
        boolean stop = false;
        boolean realStop = false;

        long t0 = System.currentTimeMillis();

        while(!stop || !realStop) {
            Configuration config = new Configuration();
            connCompConf= new JobConf(config);
            Job job = new Job(connCompConf);
            RunningJob runningJob;

            connCompConf.setJarByClass(LabelDirectedConnectedComponentsJob.class);

            connCompConf.setMapOutputKeyClass(Text.class);
            connCompConf.setMapOutputValueClass(Text.class);

            connCompConf.setMapperClass(LabelUndirectedConnectedComponentsMap.class);
            connCompConf.setReducerClass(LabelUndirectedConnectedComponentsReducer.class);

            connCompConf.setOutputKeyClass(Text.class);
            connCompConf.setOutputValueClass(Text.class);

            connCompConf.setInputFormat(TextInputFormat.class);
            connCompConf.setOutputFormat(TextOutputFormat.class);

            connCompConf.setNumMapTasks(Integer.parseInt(args[2]));
            connCompConf.setNumReduceTasks(Integer.parseInt(args[3]));

            //platform config
            connCompConf.set("io.sort.mb", "1536");
            connCompConf.set("io.sort.factor", "80");
            connCompConf.set("fs.inmemory.size.mb", "1536");

            FileSystem dfs = FileSystem.get(config);

            if(iteration % 2 == 0) {
                dfs = FileSystem.get(config);
                dfs.delete(new Path(args[5]+"/componentsLabels"), true);

                FileInputFormat.addInputPath(connCompConf, new Path(args[5] + "/nodeGraphConCompFormat"));
                FileOutputFormat.setOutputPath(connCompConf, new Path(args[5]+"/componentsLabels"));
                runningJob = JobClient.runJob(connCompConf);
            } else {
                dfs = FileSystem.get(config);
                dfs.delete(new Path(args[5]+"/nodeGraphConCompFormat"), true);

                FileInputFormat.addInputPath(connCompConf, new Path(args[5]+"/componentsLabels"));
                FileOutputFormat.setOutputPath(connCompConf, new Path(args[5]+"/nodeGraphConCompFormat"));
                runningJob = JobClient.runJob(connCompConf);
            }

            System.out.println("\n************************************");
            System.out.println("* Connected Components Iteration "+(iteration+1)+" FINISHED *");
            System.out.println("************************************\n");

            Counters counters = runningJob.getCounters();
            long counter = counters.getCounter(LabelUndirectedConnectedComponentsJob.Label.UPDATED);

            if(stop == true) {
                realStop = true;
                if(Boolean.parseBoolean(args[1])) {
                    System.out.println("\n@@@ Deleting intermediate results");
                    dfs.delete(new Path(args[5]+"/nodeGraphConCompFormat"), true);
                }
            }

            if(!(counter > 0))
                stop = true;

            iteration++;
        }

        // Record benchmark
        long t1 = System.currentTimeMillis();

        double elapsedTimeSeconds = (t1 - t0)/1000.0;
        System.out.println("ConComp_Texe = "+elapsedTimeSeconds);

        // creating benchmark data
        try{
            FileSystem fs = FileSystem.get(connCompConf);
            Path path = new Path(args[5]+"/benchmark.txt");
            FSDataOutputStream os = fs.create(path);
            String benchmarkData = "elapsed time: "+elapsedTimeSeconds+"\nsteps: "+(iteration - 1);
            os.write(benchmarkData.getBytes());
            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }
}

