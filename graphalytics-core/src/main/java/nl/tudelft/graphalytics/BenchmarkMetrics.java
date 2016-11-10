package nl.tudelft.graphalytics;

import java.io.Serializable;

public class BenchmarkMetrics implements Serializable{
    private long processingTime;

    public BenchmarkMetrics() {
        this.processingTime = -1;
    }

    public BenchmarkMetrics(long processingTime) {
        this.processingTime = processingTime;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }
}
