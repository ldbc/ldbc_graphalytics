package org.tudelft.graphalytics.mapreducev2.common;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.*;

import java.io.IOException;

public class DirectedNodeNeighbourTextInputFormat extends FileInputFormat<LongWritable, DirectedNodeNeighbourhood> {
    public RecordReader<LongWritable, DirectedNodeNeighbourhood> getRecordReader(InputSplit input,
                                                                 JobConf job, Reporter reporter)
            throws IOException {
        return new DirectedNodeNeighbourRecordReader(job, (FileSplit)input);
    }
}

