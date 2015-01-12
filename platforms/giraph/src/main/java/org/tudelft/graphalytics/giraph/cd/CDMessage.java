package org.tudelft.graphalytics.giraph.cd;

import org.apache.hadoop.io.Text;

/**
 * @author Wing Ngai
 */
public class CDMessage {

    // label name
    private String labelName;
    // label score, or the hop score of this label at the incoming vertex
    private float labelScore;
    // arbitrary comparablecharacteristic
    private int f;

    public CDMessage(String labelName,float labelScore, int f) {
        this.labelName = labelName;
        this.labelScore = labelScore;
        this.f = f;
    }

    public static CDMessage FromText(Text text) {
        String[] msgData = text.toString().split(",");
        String labelName = msgData[0];
        float labelScore = Float.parseFloat(msgData[1]);
        int f = Integer.parseInt(msgData[2]);
        return new CDMessage(labelName, labelScore, f);
    }

    public String getLabelName() {
        return labelName;
    }

    public float getLabelScore() {
        return labelScore;
    }

    public int getF() {
        return f;
    }
}
