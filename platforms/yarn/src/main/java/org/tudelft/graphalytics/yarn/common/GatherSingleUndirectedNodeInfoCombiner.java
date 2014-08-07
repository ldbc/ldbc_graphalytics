package org.tudelft.graphalytics.yarn.common;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.Iterator;

public class GatherSingleUndirectedNodeInfoCombiner extends MapReduceBase
        implements Reducer<Text, Text, Text, Text> {
    private Text combinedValues = new Text();

    public void reduce(Text key, Iterator<Text> values,
                       OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
        String combinerResult = new String();
        while (values.hasNext()) {
            combinerResult += (values.next()).toString() + "\t";
        }

        combinedValues.set(combinerResult);

        output.collect(key, combinedValues);
    }
}