package nl.tudelft.graphalytics.network;

import java.io.Serializable;

public class Notification implements Serializable {
    String benchmarkId;

    public Notification(String benchmarkId) {
        this.benchmarkId = benchmarkId;
    }

    public String getBenchmarkId() {
        return benchmarkId;
    }
}
