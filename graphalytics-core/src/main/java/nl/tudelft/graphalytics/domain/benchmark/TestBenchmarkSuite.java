package nl.tudelft.graphalytics.domain.benchmark;

import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.domain.GraphSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class TestBenchmarkSuite extends BasicBenchmarkSuite {

    private static final Logger LOG = LogManager.getLogger();

    Map<String, GraphSet> availableGraphs;

    public TestBenchmarkSuite(Map<String, GraphSet> availableGraphs) {
        super();
        this.availableGraphs = availableGraphs;
    }

    public void setup() {
        experiments.addAll(setupExperiments());
        for (BenchmarkExperiment experiment : experiments) {
            for (BenchmarkJob benchmarkJob : experiment.getJobs()) {
                jobs.add(benchmarkJob);
            }
        }
    }

    public List<BenchmarkExperiment> setupExperiments() {
        List<BenchmarkExperiment> experiments = new ArrayList<>();

        List<Algorithm> algorithms = Arrays.asList(
                Algorithm.BFS, Algorithm.WCC, Algorithm.PR, Algorithm.CDLP, Algorithm.LCC, Algorithm.SSSP);

        for (Algorithm algorithm : algorithms) {
            experiments.add(setupBaselineExperiment(algorithm));
        }

        return experiments;
    }


    public BenchmarkExperiment setupBaselineExperiment(Algorithm algorithm) {
        String expType = String.format("std:%s", algorithm.getAcronym());
        BenchmarkExperiment experiment = new BenchmarkExperiment(expType);
        List<StandardGraph> addedGraphs = new ArrayList<>();

        for (StandardGraph standardGraph : StandardGraph.values()) {

            if (standardGraph != StandardGraph.XDIR && standardGraph != StandardGraph.XUNDIR) {
                continue;
            }
            if (algorithm == Algorithm.SSSP && !standardGraph.hasProperty) {
                continue;
            }

            GraphSet graphSet = availableGraphs.get(standardGraph.fileName);
            if (graphSet == null) {
                LOG.error(String.format("Required graphset not %s available.", standardGraph.fileName));
                throw new IllegalStateException("Standard Benchmark: Baseline cannot be constructed due to missing graphs.");
            }

            int repetition = 1;
            int res = 1;
            BenchmarkJob job = new BenchmarkJob(algorithm, graphSet, res, repetition);
            addedGraphs.add(standardGraph);
            experiment.addJob(job);

        }

        LOG.info(String.format(" Experiment %s runs algorithm %s on graph %s", expType, algorithm.getAcronym(), addedGraphs));

        return experiment;
    }


}
