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

/**
 * @author Wing Ngai
 */
public class CDLabelStatistics {
    String labelName;
    float aggScore;
    float maxScore;

    public CDLabelStatistics(String labelName, float aggScore, float maxScore) {
        this.labelName = labelName;
        this.aggScore = aggScore;
        this.maxScore = maxScore;
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }

    public float getAggScore() {
        return aggScore;
    }

    public void setAggScore(float aggScore) {
        this.aggScore = aggScore;
    }

    public float getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(float maxScore) {
        this.maxScore = maxScore;
    }

    @Override
    public String toString() {
        return "CDLabelStatistics{" +
                "labelName='" + labelName + '\'' +
                ", aggScore=" + aggScore +
                ", maxScore=" + maxScore +
                '}';
    }
}
