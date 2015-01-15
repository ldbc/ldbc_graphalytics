package nl.tudelft.graphalytics.mapreducev2.cd;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import nl.tudelft.graphalytics.mapreducev2.common.DirectedNode;
import nl.tudelft.graphalytics.mapreducev2.common.Edge;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
Towards Real-Time Community Detection in Large Networks
                       by
Ian X.Y. Leung,Pan Hui,Pietro Li,and Jon Crowcroft
*/

/**
 * @author Marcin Biczak
 */
public class DirectedCambridgeLPAMap extends MapReduceBase
        implements Mapper<LongWritable, Text, Text, Text> {
    private Text oKey = new Text();
    private Text oVal = new Text();

    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter)
            throws IOException {
        String record = value.toString();
        DirectedNode node = new DirectedNode();
        String label;
        String labelScore; // init label score

        /* read vertex data */
        // init iteration
        if(record.indexOf("$") == -1) {
            node.readFields(record);
            label = new String(node.getId());
            labelScore = new String("1");
        }
        // N iteration
        else {
            StringTokenizer tokenizer = new StringTokenizer(record, "$");
            String nodeData = tokenizer.nextToken();
            node.readFields(nodeData);

            StringTokenizer labelTokenizer = new StringTokenizer(tokenizer.nextToken(), "|");
            label = labelTokenizer.nextToken();
            labelScore = labelTokenizer.nextToken();
        }

        oVal.set(label+"|"+labelScore+"|"+(node.getInEdges().size() + node.getOutEdges().size()));

        Set<String> uniqueNeighbours = new HashSet<String>();
        for(Edge edge : node.getOutEdges())
            uniqueNeighbours.add(edge.getDest());

        // send to IN neighbours
        for(Edge edge : node.getInEdges())
            uniqueNeighbours.add(edge.getSrc());

        for(String dst : uniqueNeighbours) {
            oKey.set(dst);
            output.collect(oKey, oVal);
        }

        // propagate vertex data
        oKey.set(node.getId());
        oVal.set(node.toText()+"$"+label);
        output.collect(oKey, oVal);
    }
}
