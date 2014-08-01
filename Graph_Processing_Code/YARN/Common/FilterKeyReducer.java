package org.hadoop.test.reduce.utils;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.Iterator;

/*
   Reducer filters out key and maps values input directly to output
*/
public class FilterKeyReducer extends MapReduceBase
        implements Reducer<LongWritable, Text, NullWritable, Text> {
    public void reduce(LongWritable key, Iterator<Text> values,
                       OutputCollector<NullWritable, Text> output, Reporter reporter) throws IOException {
        while (values.hasNext())
            output.collect(null, values.next());
    }
}
