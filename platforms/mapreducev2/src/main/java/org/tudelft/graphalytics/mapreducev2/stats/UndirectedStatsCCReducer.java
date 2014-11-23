package org.tudelft.graphalytics.mapreducev2.stats;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.tudelft.graphalytics.mapreducev2.common.StatsCCContainer;

public class UndirectedStatsCCReducer extends MapReduceBase implements Reducer<IntWritable, StatsCCContainer, NullWritable, StatsCCContainer> {
    private StatsCCContainer container = new StatsCCContainer();

    public void reduce(IntWritable key, Iterator<StatsCCContainer> values,
                       OutputCollector<NullWritable, StatsCCContainer> output, Reporter reporter) throws IOException {
        double tmpCC = 0;
        double cc = 0;
        int degree = 0;
        int counter = 0;
        int nodes = 0;
        while (values.hasNext()) {
            StatsCCContainer tmpContainer = values.next();
            cc += tmpContainer.getCc();
            degree += tmpContainer.getEdgesNr();
            nodes += tmpContainer.getNodesNr();
            counter++;
        }

        container.setCc(cc/ (double)nodes);
        container.setEdgesNr(degree/2);
        container.setNodesNr(nodes);

        output.collect(null, container);
    }
}


