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
import org.test.giraph.jobs.undirected.UndirectedLPACambridgeJob_DevDebug;
import org.test.giraph.utils.readers.directed.DirectedFilteredVertexInputFormat;
import org.test.giraph.utils.writers.undirected.LPAOutputFormat;

import java.io.IOException;
import java.util.*;

/**
 Towards Real-Time Community Detection in Large Networks
 by
 Ian X.Y. Leung,Pan Hui,Pietro Li,and Jon Crowcroft
 */
public class DirectedLPACambridgeJob extends EdgeListVertex<VIntWritable, Text, VIntWritable, Text> implements Tool {
    /** Class logger */
    private static final Logger LOG = Logger.getLogger(DirectedLPACambridgeJob.class);
    /** Configuration */
    private Configuration conf;

    private static final String M_PARAM = "M_PARAM";
    private static final String DELTA_PARAM = "DELTA_PARAM";
    private static final String ITERATIONS_NR = "ITERATIONS_NR";

    /** Execution fields **/
    // worker global
    private static final Random rnd = new Random();
    private static float mParam = 0;
    private static float deltaParam = 0; // global value (specified by user)
    // vertex local
    private Text label = new Text();
    private float labelScore = 1.0f;
    private float vertexDeltaParam = 0;  // local (== 0, if label does not change)
    private Set<VIntWritable> inNeighbours = new HashSet<VIntWritable>();
    /** Exe Helpers **/
    private boolean isParamInit = false;
    private static int iterationThreshold = 11; // passed as a parameter
    private static final int maxIterationsThreshold = 20;
    private Set<VIntWritable> uniqueNeighbours = new HashSet<VIntWritable>(); // used for label propagation

    /** DEBUG **/
    private static final String MAIN_ID = "MAIN_ID"; // used for debug only (Pretty Print)
    private Text msg = new Text();
    private static int step = -1;

    private float getMParam() { return getContext().getConfiguration().getFloat(M_PARAM, -1); }
    private float getDeltaParam() { return getContext().getConfiguration().getFloat(DELTA_PARAM, -1); }
    private int getIterNrParam() { return getContext().getConfiguration().getInt(ITERATIONS_NR, -1); }

    private void createUniqueNeighbours() {
        // OUT
        for(VIntWritable id : this)
            this.uniqueNeighbours.add(id);

        // IN
        for(VIntWritable id : this.inNeighbours)
            this.uniqueNeighbours.add(id);
    }

    @Override
    public void compute(Iterator<Text> msgIterator) throws IOException {
        // MAX iter stopping condition for datasets which do not converge (FORCE STOP)
        if(this.getSuperstep() >= DirectedLPACambridgeJob.maxIterationsThreshold) {
            this.voteToHalt();
            return;
        }

        // INIT Alg n 1st label exchange
        if(this.getSuperstep() == 0) {
            this.label.set(this.getVertexId().toString());
            this.labelScore = 1.0f;
            if(!this.isParamInit) {
                DirectedLPACambridgeJob.mParam = this.getMParam();
                DirectedLPACambridgeJob.deltaParam = this.getDeltaParam();
                DirectedLPACambridgeJob.iterationThreshold = (this.getIterNrParam() + 1);
                this.isParamInit = false;
            }
            this.vertexDeltaParam = DirectedLPACambridgeJob.deltaParam; // init vertex local delta param
        }

        /* Initialize IN edges and create neighbours list  */
        if(this.getSuperstep() == 0 || this.getSuperstep() == 1) {
            this.initInEdges(msgIterator);

            if(this.getSuperstep() == 0)
                return;
            else // create unique neighbours
                this.createUniqueNeighbours();
        }

        // label assign
        if(this.getSuperstep() < DirectedLPACambridgeJob.iterationThreshold) {
            if(msgIterator.hasNext())
                this.determineLabel(msgIterator);

            for(VIntWritable id : this.uniqueNeighbours)
                this.sendMsg(id, new Text(this.label+","+this.labelScore+","+(this.getNumOutEdges()+this.inNeighbours.size())));

            this.setVertexValue(new Text(this.getVertexId()+"\t"+this.label));
        }

        if(this.getSuperstep() >= DirectedLPACambridgeJob.iterationThreshold)
            this.voteToHalt();
    }

    private void determineLabel(Iterator<Text> msgIterator) {
        float maxLabelScore = -100; // nasty workaround for label with score smaller than 0
        Map<Text, Float> neighboursLabels = new HashMap<Text, Float>(); // key - label, value - output of EQ 2
        Map<Text, Float> labelsMaxScore = new HashMap<Text, Float>();   // helper struct for updating new label score

        Text oldLabel = new Text(this.label); // for delta value

        // gather labels
        while (msgIterator.hasNext()) {
            Text msgLabel = msgIterator.next();
            float eq2 = this.processLabelMsg(msgLabel);
            Text neighbourLabel = this.getMsgLabel(msgLabel);

            if(neighboursLabels.containsKey(neighbourLabel)) {
                float labelAggScore = neighboursLabels.get(neighbourLabel);
                labelAggScore = labelAggScore + eq2;    // label aggregated score
                neighboursLabels.put(neighbourLabel, labelAggScore);

                // check if max score for this label
                if(labelsMaxScore.get(neighbourLabel) < this.retrieveLabelScore(msgLabel))
                    labelsMaxScore.put(neighbourLabel, this.retrieveLabelScore(msgLabel));
            } else {
                neighboursLabels.put(neighbourLabel, eq2);
                labelsMaxScore.put(neighbourLabel, this.retrieveLabelScore(msgLabel));
            }
        }

        // chose MAX score label OR random tie break
        Iterator<Text> labelIter = neighboursLabels.keySet().iterator();
        List<Text> potentialLabels = new ArrayList<Text>();
        while (labelIter.hasNext()) {
            Text tmpLabel = labelIter.next();
            float labelAggScore = neighboursLabels.get(tmpLabel);

            if(labelAggScore > maxLabelScore) {
                maxLabelScore = labelAggScore;
                this.label.set(tmpLabel);
                potentialLabels.clear();
                potentialLabels.add(tmpLabel);
            }
            else if (labelAggScore == maxLabelScore)
                potentialLabels.add(tmpLabel);
        }

        // random tie break
        if(potentialLabels.size() > 1) {
            int labelIndex = DirectedLPACambridgeJob.rnd.nextInt(potentialLabels.size());
            this.label.set(potentialLabels.get(labelIndex));
        }

        // set delta param value
        if(this.label.equals(oldLabel))
            this.vertexDeltaParam = 0;
        else
            this.vertexDeltaParam = DirectedLPACambridgeJob.deltaParam;

        // update new label score
        this.labelScore = this.updateLabelScore(labelsMaxScore.get(this.label));
    }

    // perform EQ 2 calculations
    private float processLabelMsg(Text msg) {
        String[] data = msg.toString().split(",");
        String label = data[0];                       // L
        float labelScore = Float.parseFloat(data[1]); // s(L)
        int function = Integer.parseInt(data[2]);     // f(i) = Deg(i) NOTE degree is just one of possible solution

        return (labelScore * (float)Math.pow((double)function, (double) DirectedLPACambridgeJob.mParam));
    }

    // perform EQ 3 calculations
    private float updateLabelScore(float score) {
        return score - this.vertexDeltaParam;
    }

    private float retrieveLabelScore(Text msg) {
        String[] data = msg.toString().split(",");

        return Float.parseFloat(data[1]);
    }

    private Text getMsgLabel(Text msg) {
        String[] data = msg.toString().split(",");

        return new Text(data[0]);
    }

    public Configuration getConf() { return conf; }
    public void setConf(Configuration conf) { this.conf = conf; }

    private void initInEdges(Iterator<Text> msgIterator) {
        if(this.getSuperstep() == 0) {
            // propagate IN edges
            for(VIntWritable id : this)
                this.sendMsg(id, new Text(this.getVertexId().toString()));
        }
        else if(this.getSuperstep() == 1) {
            // read group edges n init neighbours list
            while (msgIterator.hasNext()) {
                int inNeighbour = Integer.parseInt(msgIterator.next().toString());
                this.inNeighbours.add(new VIntWritable(inNeighbour));
            }
        }
    }

    /* Debug, Utils */
    private boolean isMain() { return getVertexId().get() == getContext().getConfiguration().getInt(MAIN_ID, -1); }
    private Integer getMain() { return getContext().getConfiguration().getInt(MAIN_ID, -1); }

    // Pretty Print
    private void prettyPrint(Iterator<Text> msgIterator) {
        Map<String, SortedSet<String>> dupa = new HashMap<String, SortedSet<String>>();

        while (msgIterator.hasNext()) {
            String msg = msgIterator.next().toString();

            String[] data = msg.split(",");
            String id = data[0];
            String community = data[1];

            if(dupa.containsKey(community)) {
                SortedSet x = dupa.get(community);
                x.add(id);
                dupa.put(community, x);
            } else {
                SortedSet<String> x = new TreeSet<String>();
                x.add(id);
                dupa.put(community, x);
            }
        }

        String outputCrap = new String();
        Iterator<String> keyIter = dupa.keySet().iterator();
        while (keyIter.hasNext()) {
            String key = keyIter.next();
            outputCrap += key+" | "+dupa.get(key).toString()+" # "+dupa.get(key).size()+"\n";
        }

        this.setVertexValue(new Text(outputCrap+"id = "+this.getVertexId()+" comm = "+this.label+"\niterThreshold = "+ (DirectedLPACambridgeJob.iterationThreshold - 1)));
    }
    /** END Debug, Utils **/

    public int run(String[] args) throws Exception {
        int result;

        Preconditions.checkArgument(args.length == 8,
                "Job run: Must have 8 arguments <type> <input path> <output path> <# of workers> <mainID [debug, prettyPrint]> <mParam -> paper 0.1> <delta param> <#iterations>");

        GiraphJob job = new GiraphJob(getConf(), getClass().getName());
        job.setVertexClass(getClass());

        job.setVertexInputFormatClass(DirectedFilteredVertexInputFormat.class);
        job.setVertexOutputFormatClass(LPAOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2] + "/communities"));

        job.setWorkerConfiguration(Integer.parseInt(args[3]), Integer.parseInt(args[3]), 100.0f);

        job.getConfiguration().setInt(MAIN_ID, Integer.parseInt(args[4]));
        job.getConfiguration().setFloat(M_PARAM, Float.parseFloat(args[5]));
        job.getConfiguration().setFloat(DELTA_PARAM, Float.parseFloat(args[6]));
        job.getConfiguration().setInt(ITERATIONS_NR, Integer.parseInt(args[7]));

        // record execution time
        long t0 = System.currentTimeMillis();

        result = job.run(true) ? 0 : -1;

        long t1 = System.currentTimeMillis();
        double elapsedTimeSeconds = (t1 - t0)/1000.0;
        System.out.println("Community_Texe = "+elapsedTimeSeconds);

        // record steps
        long steps = job.getCounters().getGroup("Giraph Stats").findCounter("Superstep").getValue(); // ZooKeeper counter

        try{
            FileSystem fs = FileSystem.get(getConf());
            Path path = new Path(args[2]+"/benchmark.txt");
            FSDataOutputStream os = fs.create(path);
            String benchmarkData = "elapsed time: "+elapsedTimeSeconds+"\nsteps: "+steps;
            os.write(benchmarkData.getBytes());
            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }
}