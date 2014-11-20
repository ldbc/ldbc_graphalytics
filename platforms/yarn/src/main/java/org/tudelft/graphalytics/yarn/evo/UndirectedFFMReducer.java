package org.tudelft.graphalytics.yarn.evo;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.*;
import org.tudelft.graphalytics.yarn.common.Edge;
import org.tudelft.graphalytics.yarn.common.GeometricalMeanUtil;
import org.tudelft.graphalytics.yarn.common.Node;

import java.io.IOException;
import java.util.*;

public class UndirectedFFMReducer extends MapReduceBase implements Reducer<LongWritable, Text, NullWritable, Text> {
    private boolean isInit = false;
    private Random rnd = new Random();
    private Node newVertex = new Node();
    private long maxID = 0;
    private List<Long> potentialAmbassadors = new ArrayList<Long>();
    private GeometricalMeanUtil gmu = new GeometricalMeanUtil();
    private float pRatio = 0; // todo directed R_RATIO && Y

    private Text oVal = new Text();

    @Override
    public void configure(JobConf conf) {
        this.isInit = conf.getBoolean(FFMUtils.IS_INIT, false);
        this.maxID = conf.getLong(FFMUtils.MAX_ID, -1);
        this.pRatio = conf.getFloat(FFMUtils.P_RATIO, 0);
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
                this.newVertex.setEdges(newEdges);

                output.collect(null, newVertex.toText());
                reporter.incrCounter(FFMUtils.NEW_VERTICES, this.newVertex.getId()+","+initAmbassador, 1);
            } else { // continue burning
                int x = this.calculateOutLinks();
                this.burn(x, reporter);
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
                    Node passingVertex = new Node();
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
        this.newVertex = new Node();
        this.potentialAmbassadors = new ArrayList<Long>();
    }

    private int calculateOutLinks() {
        return gmu.getGeoDev(1.0 - this.pRatio);
    }

    private void burn(int x, Reporter reporter) {
        Vector<Edge> edges =  this.newVertex.getEdges();

        // filter visited
        for(Edge edge : edges)
            if(this.potentialAmbassadors.contains(Long.valueOf(edge.getDest())))
                this.potentialAmbassadors.remove(Long.valueOf(edge.getDest()));

        // filter out itself todo dla directed both in n out
        if(this.potentialAmbassadors.contains(Long.valueOf(this.newVertex.getId())))
            this.potentialAmbassadors.remove(Long.valueOf(this.newVertex.getId()));

        if(x < this.potentialAmbassadors.size()) {
            int maxIndex = 0;
            for(int i=0; i<x; i++) {
                maxIndex = this.potentialAmbassadors.size();
                int index = this.rnd.nextInt(maxIndex);
                edges.add(new Edge(this.newVertex.getId(), String.valueOf(this.potentialAmbassadors.get(index))));

                // update global view
                reporter.incrCounter(FFMUtils.NEW_VERTICES, this.newVertex.getId()+","+this.potentialAmbassadors.get(index), 1);
                this.potentialAmbassadors.remove(index); // filter out just added
            }
        } else {
            for(Long id : potentialAmbassadors) {
                edges.add(new Edge(this.newVertex.getId(), String.valueOf(id)));
                // update global view
                reporter.incrCounter(FFMUtils.NEW_VERTICES, this.newVertex.getId()+","+id, 1);
            }
        }

        this.newVertex.setEdges(edges);
    }
}
