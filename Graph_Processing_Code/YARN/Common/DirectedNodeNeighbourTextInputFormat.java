package org.hadoop.test.utils.directed;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.*;
import org.hadoop.test.data.directed.DirectedNodeNeighbourhood;

import java.io.IOException;

public class DirectedNodeNeighbourTextInputFormat extends FileInputFormat<LongWritable, DirectedNodeNeighbourhood> {
    public RecordReader<LongWritable, DirectedNodeNeighbourhood> getRecordReader(InputSplit input,
                                                                 JobConf job, Reporter reporter)
            throws IOException {
        return new DirectedNodeNeighbourRecordReader(job, (FileSplit)input);
    }
}

