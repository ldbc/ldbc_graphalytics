package nl.tudelft.graphalytics.domain.benchmark;

import nl.tudelft.graphalytics.util.UuidGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wlngai on 10/14/16.
 */
public class BenchmarkExperiment {
    String id;
    String type;
    List<BenchmarkJob> jobs;

    public BenchmarkExperiment( String type) {
        this.id = UuidGenerator.getRandomUUID("e", 6);
        this.type = type;
        this.jobs = new ArrayList<>();
    }

    public BenchmarkExperiment(String id, String type) {
        this.id = id;
        this.type = type;
        this.jobs = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<BenchmarkJob> getJobs() {
        return jobs;
    }

    public void addJob(BenchmarkJob job) {
        jobs.add(job);
    }
}
