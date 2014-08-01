package org.test.giraph.jobs.directed;

import com.google.common.base.Preconditions;
import org.apache.giraph.graph.BasicVertex;
import org.apache.giraph.graph.Edge;
import org.apache.giraph.graph.EdgeListVertex;
import org.apache.giraph.graph.GiraphJob;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.CounterGroup;
import org.apache.hadoop.mapreduce.TaskID;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.log4j.Logger;
import org.test.giraph.utils.GeometricalMeanUtil;
import org.test.giraph.utils.JobName;
import org.test.giraph.utils.readers.directed.DirectedFilteredVertexInputFormat;
import org.test.giraph.utils.writers.generic.OutEdgesVertexOutputFormat;

import java.io.IOException;
import java.util.*;

public class DirectedForestFireModelJob extends EdgeListVertex<VIntWritable, Text, VIntWritable, Text> implements Tool {
    private static final Logger LOG = Logger.getLogger(DirectedForestFireModelJob.class);
    private Configuration conf;

    // JOB PARAMS
    private static final String MAX_ID = "MAX_ID";
    private static final String P_RATIO = "P";
    private static final String R_RATIO = "R";
    private static final String MAX_HOPS = "MAX_HOPS";
    private static final String WORKER_NEW_VERTICES = "WORKER_NEW_VERTICES";
    private static final String NEW_VERTICES_EDGES = "NEW_VERTICES_EDGES";

    /* GLOBAL HELPERS */
    private static List<VIntWritable> allIDs = new ArrayList<VIntWritable>();
    private static int workerNewVerticesSize = 0;
    private static Map<VIntWritable, BasicVertex> workerNewVertices = new HashMap<VIntWritable, BasicVertex>();
    // single exe based data
    private static boolean isNewVerticesInit = false;
    private Random rnd = new Random();
    private GeometricalMeanUtil gmu = new GeometricalMeanUtil();

    /* Vertex local */
    private int hopsCovered = 1;
    // used by new vertex
    private boolean isAmbassadorInit = false;
    private Map<VIntWritable, Boolean> visitedMap = new HashMap<VIntWritable, Boolean>(); // hold already visited vertices
    private Set<VIntWritable> inNeighbours = new HashSet<VIntWritable>();

    /*
       Global
    */
    private int getMaxHops() { return getContext().getConfiguration().getInt(MAX_HOPS, -1); }
    private float getP_Ratio() { return getContext().getConfiguration().getFloat(P_RATIO, -1); }
    private float getR_Ratio() { return getContext().getConfiguration().getFloat(R_RATIO, -1); }
    private int getWorkerNewVertices() { return getContext().getConfiguration().getInt(WORKER_NEW_VERTICES, -1); }
    // biggest id in original graph, starting point for new vertices IDs
    private int getMaxId() { return getContext().getConfiguration().getInt(MAX_ID, -1); }

    @Override
    public void compute(Iterator<Text> msgIterator) throws IOException {
        if(this.getSuperstep() > 5) {
            if(this.getSuperstep() % 2 == 0)
                this.hopsCovered++;
        }

        if(this.hopsCovered >= this.getMaxHops()) {
            this.voteToHalt();
            return;
        }

        /* INIT Process */
        /* Initialize IN edges and create neighbours list  */
        if(this.getSuperstep() == 0 || this.getSuperstep() == 1) {
            this.initInEdges(msgIterator);

            if(this.getSuperstep() == 0) {
                DirectedForestFireModelJob.allIDs.add(this.getVertexId());
                return;
            }
        }

        /*******************************
         Start Evolution Algorithm
         *******************************/
        // create new vertices
        if(this.getSuperstep() == 2 && !DirectedForestFireModelJob.isNewVerticesInit) {
            DirectedForestFireModelJob.isNewVerticesInit = true;
            DirectedForestFireModelJob.workerNewVerticesSize = this.getWorkerNewVertices();
            int workerID = this.getWorkerId(this.getContext().getTaskAttemptID().getTaskID());
            for(int i=0; i<DirectedForestFireModelJob.workerNewVerticesSize; i++) {
                int newID = workerID * DirectedForestFireModelJob.workerNewVerticesSize + i + this.getMaxId();
                newID -= (DirectedForestFireModelJob.workerNewVerticesSize - 1); // THIS IS SO LAME (prevents overwriting MAX_ID vertex)
                this.createNewVertex(newID);
            }

            return;
        } else if(this.getSuperstep() == 2 && !DirectedForestFireModelJob.isNewVerticesInit)
            return;

        // NEW VERTICES
        if(this.getSuperstep() > 2 && this.getVertexId().get() > this.getMaxId()) {
            // initialize burning
            if(!this.isAmbassadorInit) {
                VIntWritable ambassador = this.randomInitAmbassador();
                this.visitedMap.put(ambassador, Boolean.TRUE);
                // create link to INIT ambassador (ONLY OUT)
                this.addEdgeRequest(this.getVertexId(), new Edge<VIntWritable, VIntWritable>(ambassador, new VIntWritable(0)));
                // statistics
                Counter counter = this.getContext().getCounter(NEW_VERTICES_EDGES, this.getVertexId().toString());
                counter.increment(1);

                this.sendMsg(ambassador, new Text("$ "+this.getVertexId()));
                this.isAmbassadorInit = true;
                this.visitedMap.put(this.getVertexId(), Boolean.TRUE); //prevents from creating link to itself
            }
            // process reply
            else if(msgIterator.hasNext()) {
                while (msgIterator.hasNext()) {
                    String msg = msgIterator.next().toString();
                    // Ambssadors neighbours request
                    if(msg.charAt(0) == '$') {
                        VIntWritable srcID = this.processAmbassadorMsgs(msg);
                        List<VIntWritable> neighbours = new ArrayList<VIntWritable>();
                        for(VIntWritable id : this)
                            neighbours.add(id);

                        String outNeighbours = this.verticesIDsList2String(neighbours);
                        this.sendMsg(srcID, new Text(outNeighbours));
                    }
                    // ambassador reply
                    else if(msg.charAt(0) == '%') {
                        // read string2Vertices
                        List<VIntWritable> neighbours = this.processAmbassadorReplyMsg(msg);

                        // BURN BABY BURN
                        double x = gmu.getGeoDev(1.0 - this.getP_Ratio());
                        double y = gmu.getGeoDev(1.0 - this.getR_Ratio());
                        List<VIntWritable> burnOutDst = this.burnEdgesTo((int)x, (int)y, neighbours);

                        // contact new ambassadors
                        for(VIntWritable id : burnOutDst)
                            this.sendMsg(id,new Text("$ "+this.getVertexId()));
                    }
                }
            }
            // waiting for ambassador reply
            else
                return;
        }
        // EXISTING VERTICES (ambassadors)
        else if(this.getSuperstep() > 2 && this.getVertexId().get() <= this.getMaxId()) {
            if(msgIterator.hasNext()) {
                while (msgIterator.hasNext()) {
                    VIntWritable srcID = this.processAmbassadorMsgs(msgIterator.next().toString());
                    Set<VIntWritable> neighbours = new HashSet<VIntWritable>();
                    // OUT
                    for(VIntWritable id : this)
                        neighbours.add(id);

                    neighbours.addAll(this.inNeighbours);

                    String neighboursStr = this.verticesIDsList2String(neighbours);
                    this.sendMsg(srcID, new Text("% "+neighboursStr));
                }
            } else
                this.voteToHalt();
        }
    }

    /*
        If current ambassador has only already visited Neighbours (they will be filtered out)
        this evolution branch dies out (gets killed by implementation, returns empty list no more msgs on this branch).
     */
    private List<VIntWritable> burnEdgesTo(int x, int y, List<VIntWritable> neighbours) throws IOException {
        List<VIntWritable> burned = new ArrayList<VIntWritable>();

        Set<VIntWritable> visitedSet = this.visitedMap.keySet();
        for(VIntWritable id : visitedSet) {
            if(neighbours.contains(id))
                neighbours.remove(id);
        }


        // burn as much as you can
        if(neighbours.size() <= x) {
            for(VIntWritable id : neighbours) {
                burned.add(id);
                this.visitedMap.put(id, Boolean.TRUE);
                this.addEdgeRequest(this.getVertexId(), new Edge<VIntWritable, VIntWritable>(id, new VIntWritable(0)));

                // statistics
                Counter counter = this.getContext().getCounter(NEW_VERTICES_EDGES, this.getVertexId().toString());
                counter.increment(1);
            }
        }
        // burn == X
        else {
            int maxIndex = neighbours.size();
            for(int i=x; i>0; i--) {
                int index = this.rnd.nextInt(maxIndex);
                VIntWritable neighbourID = neighbours.get(index);
                burned.add(neighbourID);
                this.visitedMap.put(neighbourID, Boolean.TRUE);
                neighbours.remove(neighbourID);
                maxIndex = neighbours.size();

                this.addEdgeRequest(this.getVertexId(), new Edge<VIntWritable, VIntWritable>(neighbourID, new VIntWritable(0)));

                // statistics
                Counter counter = this.getContext().getCounter(NEW_VERTICES_EDGES, this.getVertexId().toString());
                counter.increment(1);
            }

            if(neighbours.size() <= y) {
                for(VIntWritable id : neighbours) {
                    burned.add(id);
                    this.visitedMap.put(id, Boolean.TRUE);
                    this.addEdgeRequest(this.getVertexId(), new Edge<VIntWritable, VIntWritable>(id, new VIntWritable(0)));

                    // statistics
                    Counter counter = this.getContext().getCounter(NEW_VERTICES_EDGES, this.getVertexId().toString());
                    counter.increment(1);
                }
            } else {
                for(int i=y; i>0; i--) {
                    int index = this.rnd.nextInt(maxIndex);
                    VIntWritable neighbourID = neighbours.get(index);
                    burned.add(neighbourID);
                    this.visitedMap.put(neighbourID, Boolean.TRUE);
                    neighbours.remove(neighbourID);
                    maxIndex = neighbours.size();

                    this.addEdgeRequest(this.getVertexId(), new Edge<VIntWritable, VIntWritable>(neighbourID, new VIntWritable(0)));

                    // statistics
                    Counter counter = this.getContext().getCounter(NEW_VERTICES_EDGES, this.getVertexId().toString());
                    counter.increment(1);
                }
            }
        }

        return burned;
    }

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

    // "$ srcID"
    private VIntWritable processAmbassadorMsgs(String msg) {
        return new VIntWritable(Integer.parseInt(msg.split(" ")[1])); // pretty sweet n unreadable ;)
    }

    private List<VIntWritable> processAmbassadorReplyMsg(String msg) {
        return this.verticesIdsString2List(msg.split(" ")[1]); // pretty sweet n unreadable ;)
    }

    /*
        HELPERS
     */
    private String verticesIDsList2String(Collection<VIntWritable> list) {
        String result = new String();
        boolean isFirst = true;

        for(VIntWritable elem : list) {
            if(isFirst) {
                result += elem;
                isFirst = false;
            } else
                result += ","+elem;
        }

        return result;
    }

    private List<VIntWritable> verticesIdsString2List(String verticesStr) {
        List<VIntWritable> verticesList = new ArrayList<VIntWritable>();
        String[] vertices = verticesStr.split(",");
        for(String vertex : vertices)
            verticesList.add(new VIntWritable(Integer.parseInt(vertex)));

        return verticesList;
    }

    // get pseudo ID of worker -> task name parsed
    private int getWorkerId(TaskID id) {
        int result = 0;
        String taskID = id.toString();
        int index = taskID.lastIndexOf('_');
        result = Integer.valueOf(taskID.substring(++index, taskID.length()));

        return result;
    }

    private void createNewVertex(int newID) throws IOException {
        VIntWritable newVertexID = new VIntWritable(newID);
        // empty edges MAP
        Map<VIntWritable, VIntWritable> edges = new HashMap<VIntWritable, VIntWritable>();
        BasicVertex basicVertex = this.instantiateVertex(newVertexID, new Text(), edges, new ArrayList<Text>());
        this.addVertexRequest(basicVertex);

        DirectedForestFireModelJob.workerNewVertices.put(newVertexID, basicVertex);
    }

    private VIntWritable randomInitAmbassador() {
        int index =  rnd.nextInt(DirectedForestFireModelJob.allIDs.size());
        return  DirectedForestFireModelJob.allIDs.get(index);
    }

    @Override
    public Configuration getConf() { return conf; }
    @Override
    public void setConf(Configuration conf) { this.conf = conf; }

    public int run(String[] args) throws Exception {
        int result;

        Preconditions.checkArgument(args.length == 9,
                "Job run: Must have 9 arguments <type> <input path> <output path> <# of workers> <maxVertexId> <nrOfNewVertices> <forward burning probability p>" +
                        " <backward burning ratio r> <numberOfHops>");

        GiraphJob job = new GiraphJob(getConf(), getClass().getName());
        job.setVertexClass(getClass());

        job.setVertexInputFormatClass(DirectedFilteredVertexInputFormat.class);
        job.setVertexOutputFormatClass(OutEdgesVertexOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2] + "/ffm"));

        int workersNr = Integer.parseInt(args[3]);
        job.setWorkerConfiguration(workersNr, workersNr, 100.0f);
        job.getConfiguration().setInt(MAX_ID, Integer.parseInt(args[4]));
        job.getConfiguration().setInt(WORKER_NEW_VERTICES, (Integer.parseInt(args[5]) / workersNr));
        job.getConfiguration().setFloat(P_RATIO, Float.parseFloat(args[6]));
        job.getConfiguration().setFloat(R_RATIO, Float.parseFloat(args[7]));
        job.getConfiguration().setInt(MAX_HOPS, Integer.parseInt(args[8]));
        job.setJobName(JobName.createJobName("FFM", args[1]));

        // record execution time
        long t0 = System.currentTimeMillis();

        result = job.run(true) ? 0 : -1;

        long t1 = System.currentTimeMillis();
        double elapsedTimeSeconds = (t1 - t0)/1000.0;
        System.out.println("Evolution_Texe = "+elapsedTimeSeconds);

        // record steps
        long steps = job.getCounters().getGroup("Giraph Stats").findCounter("Superstep").getValue(); // ZooKeeper counter
        String newVerticesStats = new String();
        CounterGroup group = job.getCounters().getGroup(NEW_VERTICES_EDGES);
        for(Counter counter : group) {
            newVerticesStats += "newID = "+counter.getName()+" edges = "+counter.getValue()+"\n";
        }

        try{
            FileSystem fs = FileSystem.get(getConf());
            Path path = new Path(args[2]+"/benchmark.txt");
            FSDataOutputStream os = fs.create(path);
            String benchmarkData = "elapsed time: "+elapsedTimeSeconds+"\nsteps: "+steps+"\n"+newVerticesStats;
            os.write(benchmarkData.getBytes());
            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }
}
