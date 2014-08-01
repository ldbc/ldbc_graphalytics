package org.hadoop.test.utils.undirected;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.*;
import org.hadoop.test.data.undirected.NodeNeighbourhood;

import java.io.IOException;

public class UndirectedNodeNeighbourTextInputFormat extends FileInputFormat<LongWritable, NodeNeighbourhood> {
    public RecordReader<LongWritable, NodeNeighbourhood> getRecordReader(InputSplit input,
                                                                 JobConf job, Reporter reporter)
            throws IOException {
        return new UndirectedNodeNeighbourRecordReader(job, (FileSplit)input);
    }
}
