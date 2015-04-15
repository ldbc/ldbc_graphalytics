/**
 * Copyright 2015 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.tudelft.graphalytics.giraph.cd;

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