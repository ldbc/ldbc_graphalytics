package org.tudelft.graphalytics.yarn.stats;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.tudelft.graphalytics.yarn.common.StatsCCContainer;

public class StatsCCCombiner extends MapReduceBase implements Reducer<IntWritable, StatsCCContainer, IntWritable, StatsCCContainer> {
    private StatsCCContainer container = new StatsCCContainer();
    private final IntWritable fakeKey = new IntWritable(0);

    public void reduce(IntWritable key, Iterator<StatsCCContainer> values,
                       OutputCollector<IntWritable, StatsCCContainer> output, Reporter reporter) throws IOException {
        double combinedCC = 0;
        int combinedDegree = 0;
        int counter = 0;
        while (values.hasNext()) {
            StatsCCContainer tmp = values.next();
            double tmpCC = tmp.getCc();
            int tmpDegree = tmp.getEdgesNr();
            if(Double.isNaN(tmpCC)) tmpCC = 0;
            combinedDegree += tmpDegree;
            combinedCC += tmpCC;
            counter++;
        }

        container.setCc(combinedCC);
        container.setEdgesNr(combinedDegree);
        container.setNodesNr(counter);

        output.collect(fakeKey, container);
    }
}

