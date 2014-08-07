package org.tudelft.graphalytics.yarn.common;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.*;

import java.io.IOException;

public class UndirectedNodeNeighbourTextInputFormat extends FileInputFormat<LongWritable, NodeNeighbourhood> {
    public RecordReader<LongWritable, NodeNeighbourhood> getRecordReader(InputSplit input,
                                                                 JobConf job, Reporter reporter)
            throws IOException {
        return new UndirectedNodeNeighbourRecordReader(job, (FileSplit)input);
    }
}
