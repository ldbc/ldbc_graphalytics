package org.test.giraph.jobs.generic;

import com.google.common.base.Preconditions;
import org.apache.giraph.graph.EdgeListVertex;
import org.apache.giraph.graph.GiraphJob;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.CounterGroup;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.log4j.Logger;
import org.test.giraph.utils.readers.undirected.UndirectedFilteredVertexInputFormat;
import org.test.giraph.utils.writers.generic.BFSOutputFormat;
import org.test.giraph.utils.readers.directed.DirectedFilteredVertexInputFormat;

import java.io.IOException;
import java.util.Iterator;

public class BFSJob extends EdgeListVertex<VIntWritable, Text, VIntWritable, Text> implements Tool {
    /** Class logger */
    private static final Logger LOG = Logger.getLogger(BFSJob.class);
    /** Configuration */
    private Configuration conf;
    long t0;
    long t1;

    /*
       Execution fields
    */
    public static final String MAIN_ID = "Stats.mainId"; /** Main vertex for which we calculate SSSP */
    private boolean isVisited = false;

    protected void startTime() { t0 = System.nanoTime(); }
    protected void stopTime() {
        t1 = System.nanoTime();
        double elapsedTimeSeconds = t1 - t0;
        LOG.info("BFS STEP="+this.getSuperstep()+" vertexID = "+this.getVertexId().get()+" T_exe = "+elapsedTimeSeconds+" nanoSeconds");
    }

    protected boolean isSource() {
        return getVertexId().get() == getContext().getConfiguration().getInt(MAIN_ID, -1);
    }

    public void compute(Iterator<Text> msgIterator) throws IOException {
        //this.startTime(); // Step Start TIME

        int counter = 1;
        // avoid killing JOB by ZooKeeper when tasks are not reporting
        this.getContext().progress();

        // initialize BSF from source vertex
        if (getSuperstep() == 0 && this.isSource()) {
            for (VIntWritable targetVertexId : this) {
                sendMsg(targetVertexId, new Text(String.valueOf(1)));
            }

            this.isVisited = true;
            setVertexValue(new Text(String.valueOf(0)));
            voteToHalt();
            //this.stopTime(); // Step Stop TIME
        }

        // continue search only if vertex is not visited already
        if(getSuperstep() > 0 && msgIterator.hasNext() && !this.isVisited) {
            counter = 1;
            // get value of the shortest path
            int shortest = 0;
            boolean isFirstMsg = false;
            while (msgIterator.hasNext()) {
                int distance = Integer.parseInt(msgIterator.next().toString());
                if(!isFirstMsg) {
                    shortest = distance;
                    isFirstMsg = true;
                } else {
                    if(distance < shortest)
                        shortest = distance;
                }

                if(counter % 1000 == 0)
                    this.getContext().progress();
                counter++;
            }

            // send msgs
            counter = 1;
            for (VIntWritable targetVertexId : this) {
                sendMsg(targetVertexId, new Text(String.valueOf(shortest+1)));
                if(counter % 1000 == 0)
                    this.getContext().progress();
                counter++;
            }

            // "terminate" itself
            this.isVisited = true;
            setVertexValue(new Text(String.valueOf(shortest)));
            voteToHalt();
            //this.stopTime(); // Step Stop TIME
        } else {
            voteToHalt();
            //this.stopTime();
        }
    }

    @Override
    public Configuration getConf() {
        return conf;
    }

    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    public int run(String[] args) throws Exception {
        int result;

        Preconditions.checkArgument(args.length == 5,
                "Job run: Must have 5 arguments <type> <input path> <output path> <# of workers> <\"main\" vertex>\n Got "+args.length+" parameters");

        GiraphJob job = new GiraphJob(getConf(), getClass().getName());
        job.setVertexClass(getClass());

        if(args[0].equals("directed"))
            job.setVertexInputFormatClass(DirectedFilteredVertexInputFormat.class);
        else if(args[0].equals("undirected"))
            job.setVertexInputFormatClass(UndirectedFilteredVertexInputFormat.class);
        else
            throw new IOException("Unrecognized graph type: "+args[0]);

        job.setVertexOutputFormatClass(BFSOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2] + "/sssp"));

        job.setWorkerConfiguration(Integer.parseInt(args[3]), Integer.parseInt(args[3]), 100.0f);
        job.getConfiguration().setLong(MAIN_ID, Integer.parseInt(args[4]));

        // record execution time
        long t0 = System.currentTimeMillis();

        result = job.run(true) ? 0 : -1;

        long t1 = System.currentTimeMillis();
        double elapsedTimeSeconds = (t1 - t0)/1000.0;
        System.out.println("BFS_Texe = "+elapsedTimeSeconds);

        // record steps
        long steps = job.getCounters().getGroup("Giraph Stats").findCounter("Superstep").getValue(); // ZooKeeper counter

        try{
            FileSystem fs = FileSystem.get(getConf());
            Path path = new Path(args[2]+"/benchmark.txt");
            FSDataOutputStream os = fs.create(path);
            String benchmarkData = "elapsed time: "+elapsedTimeSeconds+"\nsteps: "+steps+"\nlongest BFS: "+(steps-1);
            os.write(benchmarkData.getBytes());
            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }
}

