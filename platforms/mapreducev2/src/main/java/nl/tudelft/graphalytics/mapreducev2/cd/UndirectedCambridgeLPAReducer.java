package nl.tudelft.graphalytics.mapreducev2.cd;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import nl.tudelft.graphalytics.mapreducev2.cd.CommunityDetectionConfiguration.LABEL_STATUS;
import nl.tudelft.graphalytics.mapreducev2.common.UndirectedNode;

import java.io.IOException;
import java.util.*;

/**
Towards Real-Time Community Detection in Large Networks
                       by
Ian X.Y. Leung,Pan Hui,Pietro Li,and Jon Crowcroft

 Executes EQ2 n EQ3
*/

/**
 * @author Marcin Biczak
 */
public class UndirectedCambridgeLPAReducer extends MapReduceBase implements Reducer<Text, Text, NullWritable, Text> {
    private Text oVal = new Text();
    private final Random rnd = new Random();
    private float deltaParam = 0;
    private float mParam = 0;

    public void configure(JobConf job) {
    	this.deltaParam = Float.parseFloat(job.get(CommunityDetectionConfiguration.HOP_ATTENUATION));
        this.mParam = Float.parseFloat(job.get(CommunityDetectionConfiguration.NODE_PREFERENCE));
    }

    public void reduce(Text key, Iterator<Text> values,
                       OutputCollector<NullWritable, Text> output, Reporter reporter) throws IOException {
        UndirectedNode node = new UndirectedNode();
        String label = new String();
        List<String> labelMsgs = new ArrayList<String>();

        float originalDelta = this.deltaParam;

        // group msgs
        while (values.hasNext()) {
            String value = values.next().toString();
            if(value.indexOf("$") != -1) {
                // vertex data with oldLabel
                StringTokenizer tokenizer = new StringTokenizer(value, "$");
                node.readFields(tokenizer.nextToken());
                label = tokenizer.nextToken();
            } else if(value.indexOf("|") != -1) {
                // label MSG
                labelMsgs.add(new String(value.getBytes())); // avoid Iterator shallow copy
            } else
                throw new IOException("Got incorrect msg = "+value+" for key = "+key.toString());
        }

        String[] labelResult = this.determineLabel(labelMsgs.iterator(), label, reporter); // 0 - label, 1 - score

        oVal.set(node.toText()+"$"+labelResult[0]+"|"+labelResult[1]);
        output.collect(null, oVal);

        // revert to original delta
        if(this.deltaParam == 0)
            this.deltaParam = originalDelta;
    }

    /**
        Algorithm based methods
        @return Returns Array which conatins 0 - new label, 1 - new label score.
    */
    private String[] determineLabel(Iterator<String> msgIterator, String oldLabel, Reporter reporter) {
        String[] result = new String[2]; // 0 - new label, 1 - new label score
        float maxLabelScore = -100;
        Map<String, Float> neighboursLabels = new HashMap<String, Float>(); // key - label, value - output of EQ 2
        Map<String, Float> labelsMaxScore = new HashMap<String, Float>();   // helper struct for updating new label score

        // gather labels
        while (msgIterator.hasNext()) {
            String msgLabel = msgIterator.next();
            float eq2 = this.processLabelMsg(msgLabel);
            String neighbourLabel = this.getMsgLabel(msgLabel);

            if(neighboursLabels.containsKey(neighbourLabel)) {
                float labelAggScore = neighboursLabels.get(neighbourLabel);
                labelAggScore = labelAggScore + eq2;    // label aggregated score
                neighboursLabels.put(neighbourLabel, labelAggScore);

                // check if max score for this label
                if(labelsMaxScore.get(neighbourLabel) < this.retrieveLabelScore(msgLabel))
                    labelsMaxScore.put(neighbourLabel, this.retrieveLabelScore(msgLabel));
            } else {
                neighboursLabels.put(neighbourLabel, eq2);
                labelsMaxScore.put(neighbourLabel, this.retrieveLabelScore(msgLabel));
            }
        }

        // chose MAX score label OR random tie break
        List<String> potentialLabels = new ArrayList<String>();
	    for (Map.Entry<String, Float> labelEntry : neighboursLabels.entrySet()) {
            String tmpLabel = labelEntry.getKey();
            float labelAggScore = labelEntry.getValue();

            if(labelAggScore > maxLabelScore) {
                maxLabelScore = labelAggScore;
                result[0] = tmpLabel; // new label
                potentialLabels.clear();
                potentialLabels.add(tmpLabel);
            }
            else if (labelAggScore == maxLabelScore)
                potentialLabels.add(tmpLabel);
        }

        // random tie break
        if(potentialLabels.size() > 1) {
            int labelIndex = this.rnd.nextInt(potentialLabels.size());
            result[0] = potentialLabels.get(labelIndex); // new label
        }

        // set delta param value
        if(result[0].equals(oldLabel))
            this.deltaParam = 0;
        else
            reporter.incrCounter(LABEL_STATUS.CHANGED, 1);

        // update new label score
        result[1] = String.valueOf(this.updateLabelScore(labelsMaxScore.get(result[0]))); // new label score

        return result;
    }

    // perform EQ 2 calculations
    private float processLabelMsg(String msg) {
        String[] data = msg.split("\\|");
        //String label = data[0];                       // L
        float labelScore = Float.parseFloat(data[1]); // s(L)
        int function = Integer.parseInt(data[2]);     // f(i) = Deg(i) NOTE degree is just one of possible solution

        return (labelScore * (float)Math.pow((double)function, (double) this.mParam)); // I know this is nasty,but I don't think that alg requires high precision
    }

    /*
        based on max score of label from T-1, which was chosen as current label based on EQ2
     */
    // perform EQ 3 calculations
    private float updateLabelScore(float score) {
        return score - this.deltaParam;
    }

    private float retrieveLabelScore(String msg) {
        String[] data = msg.toString().split("\\|");

        return Float.parseFloat(data[1]);
    }

    private String getMsgLabel(String msg) {
        String[] data = msg.toString().split("\\|");

        return data[0];
    }
}
