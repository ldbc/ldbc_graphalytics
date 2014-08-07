package org.tudelft.graphalytics.yarn.common;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;

public class UndirectedNodeNeighbourRecordReader implements RecordReader<LongWritable, NodeNeighbourhood> {
    private LineRecordReader lineReader;
    private LongWritable lineKey;
    private Text lineValue;

    public UndirectedNodeNeighbourRecordReader(JobConf job, FileSplit split) throws IOException {
        lineReader = new LineRecordReader(job, split);

        lineKey = lineReader.createKey();
        lineValue = lineReader.createValue();
    }

    public boolean next(LongWritable key, NodeNeighbourhood value) throws IOException {
        if (!lineReader.next(lineKey, lineValue)) {
           return false;
        }

        key.set(lineKey.get());
        NodeNeighbourhood tmp = new NodeNeighbourhood(this.textValueToObj(lineValue));
        value.setCentralNode(tmp.getCentralNode());
        value.setNodeNeighbourhood(tmp.getNodeNeighbourhood());

        return true;
    }

    public LongWritable createKey() {
        return new LongWritable();
    }

    public NodeNeighbourhood createValue() {
        return new NodeNeighbourhood();
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

    private NodeNeighbourhood textValueToObj(Text line) throws IOException{
        String dataLine = line.toString();
        NodeNeighbourhood nodeNeighbourhood = new NodeNeighbourhood();
        nodeNeighbourhood.readFields(dataLine);

        return nodeNeighbourhood;
    }
}

