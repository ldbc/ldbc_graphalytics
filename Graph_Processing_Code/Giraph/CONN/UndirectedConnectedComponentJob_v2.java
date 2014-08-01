package org.test.giraph.jobs.undirected;

import com.google.common.base.Preconditions;
import org.apache.giraph.graph.EdgeListVertex;
import org.apache.giraph.graph.GiraphJob;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.log4j.Logger;
import org.test.giraph.utils.readers.undirected.UndirectedFilteredVertexInputFormat;
import org.test.giraph.utils.writers.undirected.UndirectedFilteredLabeledVertexOutputFormat;

import java.io.IOException;
import java.util.Iterator;

/*
    OUTPUTS filtered vertex format with label attached "$label"
*/
public class UndirectedConnectedComponentJob_v2 extends EdgeListVertex<VIntWritable, Text, VIntWritable, Text> implements Tool {
    /** Class logger */
    private static final Logger LOG = Logger.getLogger(UndirectedConnectedComponentJob_v2.class);
    /** Configuration */
    private Configuration conf;

    /* Per Vertex vars */
    protected Text label = new Text();

    public Configuration getConf() { return conf; }
    public void setConf(Configuration conf) { this.conf = conf; }

    public void compute(Iterator<Text> msgIterator) throws IOException {
        if(this.getSuperstep() == 0) { // init vertex label AND init algorithm first iteration
            this.label.set(this.getVertexId().toString());
            this.map(); // send alg init label MSG (vertexID)
        }

        // vertex label stable
        boolean isStable = this.reducer(msgIterator); // receive msgs and checks if label is stable

        if(isStable) {
            this.prepareVertexOutputValue();
            voteToHalt();
        }
        else
            this.map();     // send my label to neighbours
    }

    private void map() {
        //send label to ALL neighbours
        for(VIntWritable outNeighbour : this)
            this.sendMsg(outNeighbour, label);
    }

    private boolean reducer(Iterator<Text> msgIterator) {
        String theTag = this.label.toString();  // fakes receiving own label from MR Algorithm

        // compare with neighbours
        while (msgIterator.hasNext()) {
            Text potentialLabel = msgIterator.next();
            if(potentialLabel.toString().compareTo(theTag) < 0)
                theTag = potentialLabel.toString();
        }

        if(theTag.equals(label.toString()))
            return true;
        else
            label.set(theTag);

        return false;
    }

    // vertex-based format and "$label"
    private void prepareVertexOutputValue() {
        // store vertex data as Value to store vertex in output in (Vertex - Based - Format)
        String vertexAsString = new String(this.getVertexId()+"\t");


        boolean isFirst = true;
        for(VIntWritable out : this) {
            if(isFirst) {
                vertexAsString += out.toString();
                isFirst = false;
            } else
                vertexAsString += ","+out.toString();
        }

        // label
        vertexAsString += "$"+this.label;

        this.setVertexValue(new Text(vertexAsString));
    }

    public int run(String[] args) throws Exception {
        int result;

        Preconditions.checkArgument(args.length == 4,
                "Job run: Must have 4 arguments <type> <input path> <output path> <# of workers>");

        GiraphJob job = new GiraphJob(getConf(), getClass().getName());
        job.setVertexClass(getClass());

        job.setVertexInputFormatClass(UndirectedFilteredVertexInputFormat.class);
        job.setVertexOutputFormatClass(UndirectedFilteredLabeledVertexOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2] + "/labeledConnComp"));

        job.setWorkerConfiguration(Integer.parseInt(args[3]), Integer.parseInt(args[3]), 100.0f);

        // record execution time
        long t0 = System.currentTimeMillis();

        result = job.run(true) ? 0 : -1;

        long t1 = System.currentTimeMillis();
        double elapsedTimeSeconds = (t1 - t0)/1000.0;
        System.out.println("ConnComp_Texe = "+elapsedTimeSeconds);

        // record steps
        long steps = job.getCounters().getGroup("Giraph Stats").findCounter("Superstep").getValue(); // ZooKeeper counter

        try{
            FileSystem fs = FileSystem.get(getConf());
            Path path = new Path(args[2]+"/benchmark.txt");
            FSDataOutputStream os = fs.create(path);
            String benchmarkData = "Only for labeling algorithm (NOT count and filter)\nelapsed time: "+elapsedTimeSeconds+"\nsteps: "+steps;
            os.write(benchmarkData.getBytes());
            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }
}

