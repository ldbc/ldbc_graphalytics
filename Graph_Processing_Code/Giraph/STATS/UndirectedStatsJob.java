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
import org.test.giraph.data.Neighbourhood;
import org.test.giraph.utils.readers.undirected.UndirectedFilteredVertexInputFormat;
import org.test.giraph.utils.writers.undirected.UndirectedStatsOutputFormat;

import java.io.IOException;
import java.util.*;

public class UndirectedStatsJob extends EdgeListVertex<VIntWritable, Text, VIntWritable, Text> implements Tool {
    /** Class logger */
    private static final Logger LOG = Logger.getLogger(UndirectedStatsJob.class);
    /** Configuration */
    private Configuration conf;
    /*
        Execution fields
     */
    private Text msg;
    private Vector<Neighbourhood> neighbourhood;  // stores neighbours id && out edges dst id
    private float nodeCC;
    private int totalDegree = 0;
    public static final String MAIN_ID = "Stats.mainId"; /** Main vertex which gathers all CC and produce final output */

    private boolean isMain() {
        return getVertexId().get() == getContext().getConfiguration().getInt(MAIN_ID, -1);
    }

    private Integer getMain() {
        return getContext().getConfiguration().getInt(MAIN_ID, -1);
    }

    @Override
    /*
        1. Send my edgesInfo to my Neighbours
        3. compute cc
        4. pick single vertex sendAll(node, cc)
        5. avg cc + getNumEdges + getNumVertex
     */
    public void compute(Iterator<Text> msgIterator) throws IOException {
        if(this.getSuperstep() ==  0) {
            // prepare "myID | [edgesDstIds]" msg
            boolean hadOutEdges = false;
            this.msg = new Text(this.getVertexId()+"|");
            for(VIntWritable targetVertexId : this) {
                byte[] outNeighbour = (String.valueOf(targetVertexId.get())).getBytes();
                this.msg.append(outNeighbour, 0 ,outNeighbour.length);
                this.msg.append(",".getBytes(), 0, 1);
                hadOutEdges = true;
            }

            // check if any out edges exists, avoid tokenizer crash in step 2
            if(!hadOutEdges)
                this.msg.append(",".getBytes(), 0, 1);

            // send my OUT edges to my neighbours
            for (VIntWritable targetVertexId : this) {
                this.sendMsg(targetVertexId, this.msg);
                this.totalDegree++;
            }

        }
        else if(this.getSuperstep() ==  1) {
            // gather my neighbours
            this.neighbourhood = new Vector<Neighbourhood>();
            while (msgIterator.hasNext()) {
                String incomingMsg = msgIterator.next().toString();

                // neighbour ID
                StringTokenizer inNeighbourTokenizer = new StringTokenizer(incomingMsg, "|");
                if(inNeighbourTokenizer.countTokens() == 2) {
                    VIntWritable neighourID = new VIntWritable(Integer.parseInt(inNeighbourTokenizer.nextToken()));

                    // neighbour Edges dst
                    Vector<VIntWritable> inNeighbourEdgeDst = new Vector<VIntWritable>();
                    StringTokenizer inNeighbourEdgeTokenizer = new StringTokenizer(inNeighbourTokenizer.nextToken(), ",");
                    while (inNeighbourEdgeTokenizer.hasMoreTokens())
                        inNeighbourEdgeDst.add(new VIntWritable(Integer.parseInt(inNeighbourEdgeTokenizer.nextToken())));
                    this.neighbourhood.add(new Neighbourhood(neighourID, inNeighbourEdgeDst));
                } else
                    throw new IOException("Message format not suported, required \"neighbourID|[outEdgeDstId]\", got "+incomingMsg);
            }

            // calculate cc && send to "main" vertex && voteHalt() next step is executed only by "main" vertex
            this.nodeCC = this.nodeCC();

            this.msg = new Text(String.valueOf(nodeCC)+","+this.getNumOutEdges());

            // send cc to main vertex and stop, except main vertex
            VIntWritable mainId = new VIntWritable(this.getMain());
            if(mainId.equals(-1))
                throw new IOException("Main Vertex ID is -1. Main vertex ID is REQUIRED");
            this.sendMsg(mainId, this.msg);
            if(!this.getVertexId().equals(mainId)) {
                this.voteToHalt();
            }
        }
        else if(this.getSuperstep() ==  2 && this.getVertexId().equals(new VIntWritable(this.getMain()))) {
            // executed only by "main" vertex
            // calculate avg cc and #edges n #nodes
            long verticesNr = this.getNumVertices();
            long edgesNr = this.getNumEdges() / 2;
            Map<Integer, Integer> outDegreeDistributionMap = new TreeMap<Integer, Integer>();

            // calculate avg cc
            int ccCounter = 0;
            float ccSum = 0;
            while (msgIterator.hasNext()) {
                String[] data = msgIterator.next().toString().split(",");
                ccSum += Float.parseFloat(data[0]);
                ccCounter++;

                if(outDegreeDistributionMap.containsKey(Integer.parseInt(data[1]))) {
                    int val = outDegreeDistributionMap.get(Integer.parseInt(data[1]));
                    outDegreeDistributionMap.put(Integer.parseInt(data[1]), ++val);
                } else
                    outDegreeDistributionMap.put(Integer.parseInt(data[1]), 1);
            }

            float avgCC = ccSum / (float)ccCounter;
            this.voteToHalt();

            //store stats in main vertex value
            Text stats = new Text("number of nodes: "+verticesNr+"\nnumber of edges: "+edgesNr+"\navgCC: "+avgCC+"\navgDegree: "+((float)edgesNr/(float)verticesNr)+
                                  "\nout degree distribution = "+outDegreeDistributionMap.toString());
            this.setVertexValue(stats);
        }
        else
            this.voteToHalt();
    }

    private float nodeCC() {
        Map<VIntWritable, Boolean> centralNeighboursIds = this.buildNeighboursMap();
        int counter = 0;

        for(Neighbourhood outNode : this.neighbourhood) {
            for(VIntWritable edgeDst : outNode.getOutEdgesDst()) {
                if(centralNeighboursIds.get(edgeDst) != null)  //comparing only dst; src is known to be a neighbour
                    counter++;
            }
        }

        float bottom = (this.totalDegree * (this.totalDegree - 1));

        if(bottom <= 0)
            return 0;

        return (float)counter/bottom;
    }

    private Map<VIntWritable, Boolean> buildNeighboursMap() {
        Map<VIntWritable, Boolean> centralNeighboursIds = new HashMap<VIntWritable, Boolean>();
        for(Neighbourhood neighbour : this.neighbourhood) {
            centralNeighboursIds.put(neighbour.getId(), true);
        }

        return centralNeighboursIds;
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
                "Job run: Must have 5 arguments <type> <input path> <output path> <# of workers> <\"main\" vertex>");

        GiraphJob job = new GiraphJob(getConf(), getClass().getName());
        job.setVertexClass(getClass());

        job.setVertexInputFormatClass(UndirectedFilteredVertexInputFormat.class);
        job.setVertexOutputFormatClass(UndirectedStatsOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2]+"/cc_stats"));

        job.setWorkerConfiguration(Integer.parseInt(args[3]), Integer.parseInt(args[3]), 100.0f);
        job.getConfiguration().setLong(MAIN_ID, Integer.parseInt(args[4]));

        // record execution time
        long t0 = System.currentTimeMillis();

        result = job.run(true) ? 0 : -1;

        long t1 = System.currentTimeMillis();
        double elapsedTimeSeconds = (t1 - t0)/1000.0;
        System.out.println("Stats_Texe = "+elapsedTimeSeconds);
        try{
            FileSystem fs = FileSystem.get(getConf());
            Path path = new Path(args[2]+"/benchmark.txt");
            FSDataOutputStream os = fs.create(path);
            String benchmarkData = "elapsed time: "+elapsedTimeSeconds;
            os.write(benchmarkData.getBytes());
            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }
}
