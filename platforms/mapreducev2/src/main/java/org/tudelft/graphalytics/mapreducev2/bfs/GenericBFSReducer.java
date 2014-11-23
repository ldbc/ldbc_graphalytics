package org.tudelft.graphalytics.mapreducev2.bfs;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.Iterator;

/*
    GETS:
    - normal filtered node record pattern
    - distance (int wrapped in Text)
    - normal filtered node record pattern + "\t$distance"
 */
public class GenericBFSReducer extends MapReduceBase
        implements Reducer<Text, Text, NullWritable, Text> {
    private Text outputVal = new Text();
    private String distance;
    private int counter = 0;

    public void reduce(Text key, Iterator<Text> values,
                       OutputCollector<NullWritable, Text> output, Reporter reporter) throws IOException {
        counter++;
        if(counter % 10000 == 0)
            reporter.progress();

        boolean isVisited = false;
        boolean isFirst = true;
        int dst = 0;
        distance = new String();
        while (values.hasNext()) {
            String value = values.next().toString();
            if(value.indexOf("\t") == -1) { // distance
                //distance += "\t$T " + value;
                distance += "GOT_MSG";
                int potentialMinDst = Integer.parseInt(value);
                if(isFirst) {
                    dst = potentialMinDst;
                    isFirst = false;
                } else if(potentialMinDst < dst)
                    dst = potentialMinDst;

            } else if(value.indexOf("$") != -1) { // already visited node
                outputVal.set(value);
                isVisited = true;
                //output.collect(null, outputVal);
            } else { // not visited node
                outputVal.set(value);
            }
        }

        if(!distance.isEmpty() && !isVisited) {
            distance = new String("\t$T " + dst);
            outputVal.append(distance.getBytes(), 0, distance.length());
        }

        output.collect(null, outputVal);
    }
}
