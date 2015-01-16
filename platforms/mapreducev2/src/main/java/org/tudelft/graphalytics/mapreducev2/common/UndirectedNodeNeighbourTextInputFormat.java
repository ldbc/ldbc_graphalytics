package org.tudelft.graphalytics.mapreducev2.common;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.*;

import java.io.IOException;

/**
 * @author Marcin Biczak
 */
public class UndirectedNodeNeighbourTextInputFormat extends FileInputFormat<LongWritable, UndirectedNodeNeighbourhood> {
    public RecordReader<LongWritable, UndirectedNodeNeighbourhood> getRecordReader(InputSplit input,
                                                                 JobConf job, Reporter reporter)
            throws IOException {
        return new UndirectedNodeNeighbourRecordReader(job, (FileSplit)input);
    }
}
