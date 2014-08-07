package org.tudelft.graphalytics.yarn.common;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.LineRecordReader;
import org.apache.hadoop.mapred.RecordReader;

import java.io.IOException;

public class DirectedNodeNeighbourRecordReader implements RecordReader<LongWritable, DirectedNodeNeighbourhood> {
    private LineRecordReader lineReader;
    private LongWritable lineKey;
    private Text lineValue;

    public DirectedNodeNeighbourRecordReader(JobConf job, FileSplit split) throws IOException {
        lineReader = new LineRecordReader(job, split);

        lineKey = lineReader.createKey();
        lineValue = lineReader.createValue();
    }

    public boolean next(LongWritable key, DirectedNodeNeighbourhood value) throws IOException {
        if (!lineReader.next(lineKey, lineValue)) {
           return false;
        }

        key.set(lineKey.get());
        DirectedNodeNeighbourhood tmp = new DirectedNodeNeighbourhood(this.textValueToObj(lineValue));
        value.setCentralNode(tmp.getCentralNode());
        value.setDirectedNodeNeighbourhood(tmp.getDirectedNodeNeighbourhood());

        return true;
    }

    public LongWritable createKey() {
        return new LongWritable();
    }

    public DirectedNodeNeighbourhood createValue() {
        return new DirectedNodeNeighbourhood();
    }

    public long getPos() throws IOException {
        return lineReader.getPos();
    }

    public float getProgress() throws IOException {
        return lineReader.getProgress();
    }

    public void close() throws IOException {
        lineReader.close();
    }

    private DirectedNodeNeighbourhood textValueToObj(Text line) throws IOException{
        String dataLine = line.toString();
        DirectedNodeNeighbourhood nodeNeighbourhood = new DirectedNodeNeighbourhood();
        nodeNeighbourhood.readFields(dataLine);

        return nodeNeighbourhood;
    }
}


