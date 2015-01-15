package nl.tudelft.graphalytics.mapreducev2.evo;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.*;
import nl.tudelft.graphalytics.mapreducev2.common.DirectedNode;
import nl.tudelft.graphalytics.mapreducev2.common.Edge;

import java.io.IOException;
import java.util.*;

/**
 * @author Marcin Biczak
 */
public class DirectedForestFireModelReducer extends MapReduceBase implements Reducer<LongWritable, Text, NullWritable, Text> {
    private boolean isInit = false;
    private Random rnd = new Random();
    private DirectedNode newVertex = new DirectedNode();
    private long maxID = 0;
    private List<Long> potentialAmbassadors = new ArrayList<Long>();
    private GeometricalMeanUtil gmu = new GeometricalMeanUtil();
    private float pRatio = 0;
    private float rRatio = 0;

    private Text oVal = new Text();

    @Override
    public void configure(JobConf conf) {
        this.isInit = conf.getBoolean(ForestFireModelUtils.IS_INIT, false);
        this.maxID = conf.getLong(ForestFireModelUtils.MAX_ID, -1);
        this.pRatio = conf.getFloat(ForestFireModelUtils.P_RATIO, 0);
        this.rRatio = conf.getFloat(ForestFireModelUtils.R_RATIO, 0);
    }

    public void reduce(LongWritable key, Iterator<Text> values,
                       OutputCollector<NullWritable, Text> output, Reporter reporter) throws IOException {
        this.reset();
        
        // new vertex (also processes immediately regular vertices passing by)
        if(this.processMsgs(values, output)) {
            if(this.isInit) {
                long initAmbassador = this.chooseRndInitAmbassador();
                Vector<Edge> newEdges = new Vector<Edge>();
                newEdges.add(new Edge(this.newVertex.getId(), String.valueOf(initAmbassador)));
                this.newVertex.setOutEdges(newEdges);

                output.collect(null, newVertex.toText());
                reporter.incrCounter(ForestFireModelUtils.NEW_VERTICES, this.newVertex.getId()+","+initAmbassador, 1);
            } else { // continue burning
                int x = this.calculateOutLinks();
                int y = this.calculateInLinks();
                this.burn(x, y, reporter);
                output.collect(null, this.newVertex.toText());
            }
        }
    }

    /*
        Returns true if newVertex MSGs
     */
    private boolean processMsgs(Iterator<Text> iterator, OutputCollector<NullWritable, Text> output) throws IOException {
        boolean result = false;

        while (iterator.hasNext()) {
            String value = iterator.next().toString();
            String[] data = value.split("\t");

            // new Vertex
            if(Long.parseLong(data[0]) >= this.maxID && data.length > 1) {
                result = true;
                this.newVertex.readFields(value);
            } else if(Long.parseLong(data[0]) >= this.maxID && this.isInit) {
                result = true;
                this.newVertex.readFields(value);
            } else {
                if(data.length > 1) { // passing vertex
                    DirectedNode passingVertex = new DirectedNode();
                    passingVertex.readFields(value);
                    oVal.set(passingVertex.toText());
                    output.collect(null, passingVertex.toText());
                } else { // potential ambassador
                    potentialAmbassadors.add(Long.parseLong(value));
                }
            }
        }

        return result;
    }

    private long chooseRndInitAmbassador() {
        int index = rnd.nextInt(potentialAmbassadors.size());
        return potentialAmbassadors.get(index);
    }

    private void reset() {
        this.newVertex = new DirectedNode();
        this.potentialAmbassadors = new ArrayList<Long>();
    }

    private int calculateOutLinks() {
        return gmu.getGeoDev(1.0 - this.pRatio);
    }

    private int calculateInLinks() {
        return gmu.getGeoDev(1.0 - this.rRatio);
    }

    private void burn(int x, int y, Reporter reporter) {
        Vector<Edge> edges =  this.newVertex.getOutEdges();

        // filter visited
        for(Edge edge : edges)
            if(this.potentialAmbassadors.contains(Long.valueOf(edge.getDest())))
                this.potentialAmbassadors.remove(Long.valueOf(edge.getDest()));

        // filter out itself
        if(this.potentialAmbassadors.contains(Long.valueOf(this.newVertex.getId())))
            this.potentialAmbassadors.remove(Long.valueOf(this.newVertex.getId()));
        
        int maxIndex = 0;
        if(x < this.potentialAmbassadors.size()) {
            for(int i=0; i<x; i++) {
                maxIndex = this.potentialAmbassadors.size();
                int index = this.rnd.nextInt(maxIndex);
                edges.add(new Edge(this.newVertex.getId(), String.valueOf(this.potentialAmbassadors.get(index))));

                // update global view
                reporter.incrCounter(ForestFireModelUtils.NEW_VERTICES, this.newVertex.getId()+","+this.potentialAmbassadors.get(index), 1);
                this.potentialAmbassadors.remove(index); // filter out just added
            }

            if(y < this.potentialAmbassadors.size()) {
                for(int i=0; i<y; i++) {
                    maxIndex = this.potentialAmbassadors.size();
                    int index = this.rnd.nextInt(maxIndex);
                    edges.add(new Edge(this.newVertex.getId(), String.valueOf(this.potentialAmbassadors.get(index))));

                    // update global view
                    reporter.incrCounter(ForestFireModelUtils.NEW_VERTICES, this.newVertex.getId()+","+this.potentialAmbassadors.get(index), 1);
                    this.potentialAmbassadors.remove(index); // filter out just added
                }
            } else {
                for(Long id : potentialAmbassadors) {
                    edges.add(new Edge(this.newVertex.getId(), String.valueOf(id)));
                    // update global view
                    reporter.incrCounter(ForestFireModelUtils.NEW_VERTICES, this.newVertex.getId()+","+id, 1);
                }
            }
        } else {
            for(Long id : potentialAmbassadors) {
                edges.add(new Edge(this.newVertex.getId(), String.valueOf(id)));
                // update global view
                reporter.incrCounter(ForestFireModelUtils.NEW_VERTICES, this.newVertex.getId()+","+id, 1);
            }
        }

        this.newVertex.setOutEdges(edges);
    }
}

