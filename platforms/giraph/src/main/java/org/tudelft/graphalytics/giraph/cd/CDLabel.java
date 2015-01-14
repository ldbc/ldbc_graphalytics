package org.tudelft.graphalytics.giraph.cd;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author Wing Ngai
 */
public class CDLabel implements WritableComparable<CDLabel> {

    private Text labelName;
    private float labelScore = 1.0f;

    public CDLabel() {
        setLabelName(new Text(""));
        setLabelScore(0.0f);
    }


    public CDLabel(String labelName, float labelScore) {
        setLabelName(new Text(labelName));
        setLabelScore(labelScore);
    }

    public void write(DataOutput out) throws IOException {
        labelName.write(out);
        out.writeFloat(labelScore);
    }

    public void readFields(DataInput in) throws IOException {
        labelName.readFields(in);
        labelScore = in.readFloat();
    }

    public int compareTo(CDLabel other) {
        float thisValue = this.labelScore;
        float thatValue = other.labelScore;
        return (thisValue < thatValue ? -1 : (thisValue == thatValue ? 0 : 1));
    }

    @Override
    public String toString() {
        return "CommunityDetectionLabelWritable [labelName=" + labelName + ", labelScore=" + labelScore + "]";
    }


    public Text getLabelName() {
        return labelName;
    }

    public void setLabelName(Text labelName) {
        this.labelName = labelName;
    }

    public float getLabelScore() {
        return labelScore;
    }

    public void setLabelScore(float labelScore) {
        this.labelScore = labelScore;
    }
}