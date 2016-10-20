package nl.tudelft.graphalytics.domain.benchmark;

import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.domain.Benchmark;
import nl.tudelft.graphalytics.domain.GraphSet;
import nl.tudelft.graphalytics.util.UuidGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wlngai on 10/14/16.
 */
public class BenchmarkJob {
    String id;
    Algorithm algorithm;
    GraphSet graphSet;
    int resourceSize;
    int repetition;
    List<Benchmark> benchmarks;

    public BenchmarkJob(Algorithm algorithm, GraphSet graphSet, int resourceSize, int repetition) {
        this.id = UuidGenerator.getRandomUUID("j", 6);
        this.algorithm = algorithm;
        this.graphSet = graphSet;
        this.resourceSize = resourceSize;
        this.repetition = repetition;
        this.benchmarks = new ArrayList<>();
    }

    public BenchmarkJob(String id, Algorithm algorithm, GraphSet graphSet, int resourceSize, int repetition) {
        this.id = id;
        this.algorithm = algorithm;
        this.graphSet = graphSet;
        this.resourceSize = resourceSize;
        this.repetition = repetition;
        this.benchmarks = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public GraphSet getGraphSet() {
        return graphSet;
    }

    public int getResourceSize() {
        return resourceSize;
    }

    public int getRepetition() {
        return repetition;
    }

    public List<Benchmark> getBenchmarks() {
        return benchmarks;
    }

    public void addBenchmark(Benchmark benchmark) {
        benchmarks.add(benchmark);
    }

}
