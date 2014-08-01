package org.hadoop.test.jobs.tasks.ffm;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.jobcontrol.Job;
import org.apache.hadoop.util.Tool;
import org.hadoop.test.map.undirected.UndirectedFFMMap;
import org.hadoop.test.reduce.undirected.UndirectedFFMReducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UndirectedFFMJob extends Configured implements Tool {
    public int run(String[] args) throws IOException {
        int iteration = 0;
        JobConf ffmConf = new JobConf(new Configuration());
        int maxHoops = Integer.parseInt(args[10]);
        // used to update vertices edges and send next wave of ambassadors (neighbours IDs)
        Map<VIntWritable, List<VIntWritable>> burnedEdges = new HashMap<VIntWritable, List<VIntWritable>>();


        long t0 = System.currentTimeMillis();

        while (iteration < maxHoops) {
            Configuration config = new Configuration();
            ffmConf = new JobConf(config);
            Job job = new Job(ffmConf);
            RunningJob runningJob;

            ffmConf.setJarByClass(UndirectedFFMJob.class);

            ffmConf.setMapOutputKeyClass(VIntWritable.class);
            ffmConf.setMapOutputValueClass(Text.class);

            ffmConf.setMapperClass(UndirectedFFMMap.class);
            ffmConf.setReducerClass(UndirectedFFMReducer.class);

            ffmConf.setOutputKeyClass(NullWritable.class);
            ffmConf.setOutputValueClass(Text.class);

            ffmConf.setInputFormat(TextInputFormat.class);
            ffmConf.setOutputFormat(TextOutputFormat.class);

            ffmConf.setNumMapTasks(Integer.parseInt(args[2]));
            ffmConf.setNumReduceTasks(Integer.parseInt(args[3]));

            if(iteration == 0) {
                int slots = Integer.parseInt(args[2]);
                int newVertices = Integer.parseInt(args[7]);
                int verticesPerSlot = newVertices / slots;
                ffmConf.setInt(FFMUtils.NEW_VERTICES_NR, verticesPerSlot);
                ffmConf.set(FFMUtils.ID_SHIFT, args[2]);
                ffmConf.setBoolean(FFMUtils.IS_INIT, true);
            }
            ffmConf.setInt(FFMUtils.MAX_ID, Integer.parseInt(args[6]));
            ffmConf.setFloat(FFMUtils.P_RATIO, Float.parseFloat(args[8]));
            ffmConf.setFloat(FFMUtils.R_RATIO, Float.parseFloat(args[9]));
            ffmConf.set(FFMUtils.CURRENT_AMBASSADORS, FFMUtils.verticesIDsMap2String(burnedEdges));

            //platform config
            /* pseudo */
            if(args[11].equals("pseudo")) {
                ffmConf.set("io.sort.mb", "768");
                ffmConf.set("fs.inmemory.size.mb", "768");
                ffmConf.set("io.sort.factor", "50");
            }
            /* DAS4 conf Hadoop ver 0.20.203 */
            else if(args[11].equals("das")) {
                ffmConf.set("io.sort.mb", "1536");
                ffmConf.set("io.sort.factor", "80");
                ffmConf.set("fs.inmemory.size.mb", "1536");
            }

            FileSystem dfs;

            if(iteration % 2 == 0) {
                dfs = FileSystem.get(config);
                dfs.delete(new Path(args[5]+"/FFM"), true);

                FileInputFormat.addInputPath(ffmConf, new Path(args[5] + "/nodeGraph"));
                FileOutputFormat.setOutputPath(ffmConf, new Path(args[5]+"/FFM"));
                runningJob = JobClient.runJob(ffmConf);
            } else {
                dfs = FileSystem.get(config);
                dfs.delete(new Path(args[5]+"/nodeGraph"), true);

                FileInputFormat.addInputPath(ffmConf, new Path(args[5]+"/FFM"));
                FileOutputFormat.setOutputPath(ffmConf, new Path(args[5]+"/nodeGraph"));
                runningJob = JobClient.runJob(ffmConf);
            }

            Counters counters = runningJob.getCounters();
            Counters.Group burned = counters.getGroup(FFMUtils.NEW_VERTICES);
            burnedEdges = new HashMap<VIntWritable, List<VIntWritable>>(); // clean previous iteration data
            for(Counters.Counter counter : burned) {
                String data[] = counter.getName().split(",");
                String newVertex = data[0];
                String ambassador = data[1];

                if(burnedEdges.containsKey(new VIntWritable(Integer.parseInt(newVertex)))) {
                    List<VIntWritable> ambassadors = burnedEdges.get(new VIntWritable(Integer.parseInt(newVertex)));
                    ambassadors.add(new VIntWritable(Integer.parseInt(ambassador)));
                    burnedEdges.put(new VIntWritable(Integer.parseInt(newVertex)), ambassadors);
                } else {
                    List<VIntWritable> ambassadors = new ArrayList<VIntWritable>();
                    ambassadors.add(new VIntWritable(Integer.parseInt(ambassador)));
                    burnedEdges.put(new VIntWritable(Integer.parseInt(newVertex)), ambassadors);
                }
            }

            System.out.println("\n************************************");
            System.out.println("* FFM Hoops "+(iteration+1)+" FINISHED *");
            System.out.println("************************************\n");

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
            FileSystem fs = FileSystem.get(ffmConf);
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