package nl.tudelft.graphalytics.domain.benchmark;

import nl.tudelft.graphalytics.domain.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BaselineBenchmarkSuite extends StandardBenchmarkSuite {

    private static final Logger LOG = LogManager.getLogger();

    public static boolean DebugMode;

    Scale targeScale;
    Map<String, GraphSet> availableGraphs;

    public BaselineBenchmarkSuite(String targeScale, Map<String, GraphSet> availableGraphs) {
        super();
        this.targeScale = Scale.valueOf(targeScale);
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
        String expType = String.format("std:baseline:%s", algorithm.getAcronym());
        BenchmarkExperiment experiment = new BenchmarkExperiment(expType);

        List<StandardGraph> addedGraphs = new ArrayList<>();

        double maxSize = Math.pow(10, targeScale.maxScale);
        for (StandardGraph standardGraph : StandardGraph.values()) {
            if(standardGraph.graphSize < maxSize) {

                if(DebugMode) {
                    if(algorithm != Algorithm.BFS && algorithm != Algorithm.WCC) {
                        continue;
                    }
                }

//                if(DebugMode) {
//                    if(standardGraph != StandardGraph.XUNDIR) {
//                        continue;
//                    }
//                }

                if(!DebugMode) {
                    if(standardGraph == StandardGraph.XDIR || standardGraph == StandardGraph.XUNDIR) {
                        continue;
                    }
                }

                if(algorithm == Algorithm.SSSP && !standardGraph.hasProperty) {
                    continue;
                }

                GraphSet graphSet = availableGraphs.get(standardGraph.fileName);

                if(graphSet == null) {
                    if (DebugMode) {
                        continue;
                    } else {
                        LOG.error(String.format("Required graphset not %s available.", graphSet.getName()));
                        throw new IllegalStateException("Standard Benchmark: Baseline cannot be constructed due to missing graphs.");
                    }
                }

                int repetition = 3;

                if(DebugMode) {
                    repetition = 2;
                }
                int res = 1;
                BenchmarkJob job = new BenchmarkJob(algorithm, graphSet, res, repetition);
                addedGraphs.add(standardGraph);
                experiment.addJob(job);
            }
        }

        LOG.info(String.format(" Experiment %s runs algorithm %s on graph %s", expType, algorithm.getAcronym(), addedGraphs));

        return experiment;
    }


}
