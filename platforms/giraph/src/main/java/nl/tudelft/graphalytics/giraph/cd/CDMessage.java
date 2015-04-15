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

/**
 * @author Wing Ngai
 */
public class CDMessage {

	// source vertex
	private long sourceId;
    // label name
    private String labelName;
    // label score, or the hop score of this label at the incoming vertex
    private float labelScore;
    // arbitrary comparablecharacteristic
    private int f;

    public CDMessage(long sourceId, String labelName,float labelScore, int f) {
	    this.sourceId = sourceId;
        this.labelName = labelName;
        this.labelScore = labelScore;
        this.f = f;
    }

    public static CDMessage FromText(Text text) {
        String[] msgData = text.toString().split(",");
	    long sourceId = Long.parseLong(msgData[0]);
        String labelName = msgData[1];
        float labelScore = Float.parseFloat(msgData[2]);
        int f = Integer.parseInt(msgData[3]);
        return new CDMessage(sourceId, labelName, labelScore, f);
    }

	public long getSourceId() {
		return sourceId;
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
