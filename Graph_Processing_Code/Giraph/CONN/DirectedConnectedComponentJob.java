package org.test.giraph.jobs.directed;

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
import org.test.giraph.utils.readers.directed.DirectedFilteredVertexInputFormat;
import org.test.giraph.utils.writers.directed.DirectedFilteredLabeledVertexOutputFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/*
    OUTPUTS filtered vertex format with label attached "$label"
*/
public class DirectedConnectedComponentJob extends EdgeListVertex<VIntWritable, Text, VIntWritable, Text> implements Tool {
    /** Class logger */
    private static final Logger LOG = Logger.getLogger(DirectedConnectedComponentJob.class);
    /** Configuration */
    private Configuration conf;

    /* Per Vertex vars */
    // holds IN neighbours (prevents algorithm from crashing if node with only IN is current)
    protected List<VIntWritable> inNeighbours = new ArrayList<VIntWritable>();
    public Configuration getConf() { return conf; }
    public void setConf(Configuration conf) { this.conf = conf; }
    protected Text label = new Text();

    public void compute(Iterator<Text> msgIterator) throws IOException {
        if(this.getSuperstep() < 3) { // INIT data steps -> PULL/PUSH neighbours degree AND init worker global var
            this.initAlgorithm(msgIterator);
            return;
        } else if(this.getSuperstep() == 3) { // init vertex label AND init algorithm first iteration
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
        for(VIntWritable inNeighbour : this.inNeighbours)
            this.sendMsg(inNeighbour, label);

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

    /*
       Prepares algorithm required data
       0 - request OUT degree
       1 - respond with requested data
       2 - init worker inEdges

       ALSO FOR THIS PARTICULAR ALGORITHM sets INIT_TAG
    */
    protected void initAlgorithm(Iterator<Text> msgIterator) {
        // process init msgs
        while (msgIterator.hasNext()) {
            String msg = msgIterator.next().toString();
            StringTokenizer msgTokenizer = new StringTokenizer(msg, " ");
            String msgType = msgTokenizer.nextToken();
            if(msgType.equals("request")) { // get degree request and respond
                VIntWritable requesterId = new VIntWritable(Integer.parseInt(msgTokenizer.nextToken()));
                //Text myDegree = new Text("response_OUT "+this.getVertexId()+" "+this.getVertexValue());
                Text myDegree = new Text("response_OUT "+this.getVertexId()+" 0");
                // send requested OUT neighbours
                this.sendMsg(requesterId, myDegree);
            } else if(msgType.equals("response_OUT")) { // receive degree request response
                VIntWritable neighbourId = new VIntWritable(Integer.parseInt(msgTokenizer.nextToken())); //todo DEL redundant data
                VIntWritable neighbourDegree = new VIntWritable(Integer.parseInt(msgTokenizer.nextToken())); //todo DEL redundant data
            } else if(msgType.equals("response_IN")) { // receive degree send by IN vertex
                VIntWritable neighbourId = new VIntWritable(Integer.parseInt(msgTokenizer.nextToken()));
                VIntWritable neighbourDegree = new VIntWritable(Integer.parseInt(msgTokenizer.nextToken()));
                this.inNeighbours.add(neighbourId);
            }
        }

        // request neighbours degree
        if(this.getSuperstep() == 0) {
            Text msg = new Text("request "+this.getVertexId());
            for(VIntWritable neighbourID : this) {
                this.sendMsg(neighbourID, msg);
            }

            this.setVertexValue(new Text(this.getVertexId().toString()));
        }

        // synchronize with sending response to requests
        if(this.getSuperstep() == 1) {
            /*
                MY CHANGE TO ALGORITHM (extends neighbours on IN as well)
                send IN neighbours (prevents from crashing program if node with only IN is current)
            */
            Text myDegree = new Text("response_IN "+this.getVertexId()+" "+this.getVertexValue());
            for(VIntWritable vertexID : this)
                this.sendMsg(vertexID, myDegree);
        }
    }

    // vertex-based format and "$label"
    private void prepareVertexOutputValue() {
        // store vertex data as Value to store vertex in output in (Vertex - Based - Format)
            String vertexAsString = new String(this.getVertexId()+"\t");
            vertexAsString += "# ";

            // IN
            boolean isFirst = true;
            for(VIntWritable in : this.inNeighbours) {
                if(isFirst) {
                    vertexAsString += in.toString();
                    isFirst = false;
                } else
                    vertexAsString += ","+in.toString();
            }

            vertexAsString += "\t";

            // OUT
            vertexAsString += "@ ";
            isFirst = true;
            for(VIntWritable out : this) {
                if(isFirst) {
                    vertexAsString += out.toString();
                    isFirst = false;
                } else
                    vertexAsString += ","+out.toString();
            }

            vertexAsString += "\t";

            // label
            vertexAsString += "$"+this.label;

            this.setVertexValue(new Text(vertexAsString));
    }

    public int run(String[] args) throws Exception {
        System.out.println("D_CON_COMP_v2");

        int result;

        Preconditions.checkArgument(args.length == 4,
                "Job run: Must have 4 arguments <type> <input path> <output path> <# of workers>");

        GiraphJob job = new GiraphJob(getConf(), getClass().getName());
        job.setVertexClass(getClass());

        job.setVertexInputFormatClass(DirectedFilteredVertexInputFormat.class);
        job.setVertexOutputFormatClass(DirectedFilteredLabeledVertexOutputFormat.class);

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
