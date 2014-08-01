package org.hadoop.test.reduce.undirected;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VIntWritable;
import org.apache.hadoop.mapred.*;
import org.hadoop.test.data.Edge;
import org.hadoop.test.data.undirected.Node;
import org.hadoop.test.jobs.tasks.ffm.FFMUtils;
import org.hadoop.test.jobs.utils.GeometricalMeanUtil;

import java.io.IOException;
import java.util.*;

public class UndirectedFFMReducer extends MapReduceBase implements Reducer<VIntWritable, Text, NullWritable, Text> {
    private boolean isInit = false;
    private Random rnd = new Random();
    private Node newVertex = new Node();
    private int maxID = 0;
    private List<Integer> potentialAmbassadors = new ArrayList<Integer>();
    private GeometricalMeanUtil gmu = new GeometricalMeanUtil();
    private float pRatio = 0; // todo directed R_RATIO && Y

    private Text oVal = new Text();

    @Override
    public void configure(JobConf conf) {
        this.isInit = conf.getBoolean(FFMUtils.IS_INIT, false);
        this.maxID = conf.getInt(FFMUtils.MAX_ID, -1);
        this.pRatio = conf.getFloat(FFMUtils.P_RATIO, 0);
    }

    public void reduce(VIntWritable key, Iterator<Text> values,
                       OutputCollector<NullWritable, Text> output, Reporter reporter) throws IOException {
        this.reset();

        // new vertex (also processes immediately regular vertices passing by)
        if(this.processMsgs(values, output)) {
            if(this.isInit) {
                int initAmbassador = this.chooseRndInitAmbassador();
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
            if(Integer.parseInt(data[0]) >= this.maxID && data.length > 1) {
                result = true;
                this.newVertex.readFields(value);
            } else if(Integer.parseInt(data[0]) >= this.maxID && this.isInit) {
                result = true;
                this.newVertex.readFields(value);
            } else {
                if(data.length > 1) { // passing vertex
                    Node passingVertex = new Node();
                    passingVertex.readFields(value);
                    oVal.set(passingVertex.toText());
                    output.collect(null, passingVertex.toText());
                } else { // potential ambassador
                    potentialAmbassadors.add(Integer.parseInt(value));
                }
            }
        }

        return result;
    }

    private int chooseRndInitAmbassador() {
        int index = rnd.nextInt(potentialAmbassadors.size());
        return potentialAmbassadors.get(index);
    }

    private void reset() {
        this.newVertex = new Node();
        this.potentialAmbassadors = new ArrayList<Integer>();
    }

    private int calculateOutLinks() {
        return gmu.getGeoDev(1.0 - this.pRatio);
    }

    private void burn(int x, Reporter reporter) {
        Vector<Edge> edges =  this.newVertex.getEdges();

        // filter visited
        for(Edge edge : edges)
            if(this.potentialAmbassadors.contains(Integer.valueOf(edge.getDest())))
                this.potentialAmbassadors.remove(Integer.valueOf(edge.getDest()));

        // filter out itself todo dla directed both in n out
        if(this.potentialAmbassadors.contains(Integer.valueOf(this.newVertex.getId())))
            this.potentialAmbassadors.remove(Integer.valueOf(this.newVertex.getId()));

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
            for(Integer id : potentialAmbassadors) {
                edges.add(new Edge(this.newVertex.getId(), String.valueOf(id)));
                // update global view
                reporter.incrCounter(FFMUtils.NEW_VERTICES, this.newVertex.getId()+","+id, 1);
            }
        }

        this.newVertex.setEdges(edges);
    }
}
