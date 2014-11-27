package org.tudelft.graphalytics.giraph.cd;

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
