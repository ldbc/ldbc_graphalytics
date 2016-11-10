package nl.tudelft.graphalytics.domain.benchmark;

import nl.tudelft.graphalytics.domain.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

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

    private StandardGraph selectLargest(Collection<StandardGraph> graphs) {
        double largestSize = Double.MIN_VALUE;
        StandardGraph largestGraph = null;
        for (StandardGraph graph : graphs) {
            if(graph.graphSize > largestSize) {
                largestSize = graph.graphSize;
                largestGraph = graph;
            }
        }
        return largestGraph;
    }


    private List<StandardGraph> filterByPropertiesGraph(Collection<StandardGraph> graphs) {
        List<StandardGraph> selectedGraphs = new ArrayList<>();
        for (StandardGraph standardGraph : graphs) {
            if(standardGraph.hasProperty) {
                selectedGraphs.add(standardGraph);
            }
        }
        return selectedGraphs;
    }

    private List<StandardGraph> filterByInitial(Collection<StandardGraph> graphs, String initial) {
        List<StandardGraph> selectedGraphs = new ArrayList<>();
        for (StandardGraph standardGraph : graphs) {
            if(standardGraph.scale.startsWith(initial)) {
                selectedGraphs.add(standardGraph);
            }
        }
        return selectedGraphs;
    }

    private List<StandardGraph> filterByMaxSize(Collection<StandardGraph> graphs, double maxSize) {
        List<StandardGraph> selectedGraphs = new ArrayList<>();
        for (StandardGraph standardGraph : graphs) {
            if(standardGraph.graphSize < maxSize) {
                selectedGraphs.add(standardGraph);
            }
        }
        return selectedGraphs;
    }

    public BenchmarkExperiment baselineExperiment(Algorithm algorithm) {
        String expType = String.format("std:baseline:%s", algorithm.getAcronym());
        BenchmarkExperiment experiment = new BenchmarkExperiment(expType);

        List<StandardGraph> addedGraphs = new ArrayList<>();

        double maxSize = Math.pow(10, targeScale.maxScale);
        List<StandardGraph> scaledGraphs = filterByMaxSize(Arrays.asList(StandardGraph.values()), maxSize);
        List<StandardGraph> realGraphs = filterByInitial(scaledGraphs, "R");
        List<StandardGraph> dgGraphs = filterByInitial(scaledGraphs, "D");
        List<StandardGraph> g500Graphs = filterByInitial(scaledGraphs, "G");

        if(algorithm == Algorithm.BFS || algorithm == Algorithm.PR) {

            for (StandardGraph realGraph : realGraphs) {
                addedGraphs.add(realGraph);
            }
            if(dgGraphs.size() > 0) {
                addedGraphs.add(selectLargest(dgGraphs));
            }
            if(g500Graphs.size() > 0) {
                addedGraphs.add(selectLargest(g500Graphs));
            }


        } else {
            if(filterByPropertiesGraph(realGraphs).size() > 0) {
                addedGraphs.add(selectLargest(filterByPropertiesGraph(realGraphs)));
            }
            if(dgGraphs.size() > 0) {
                addedGraphs.add(selectLargest(dgGraphs));
            }
        }

        for (StandardGraph addedGraph : addedGraphs) {

            GraphSet graphSet = availableGraphs.get(addedGraph.fileName);

            if (graphSet == null) {
                LOG.error(String.format("Required graphset not %s available.", graphSet.getName()));
                throw new IllegalStateException("Standard Benchmark: Baseline cannot be constructed due to missing graphs.");
            }

            int repetition = 3;
            int res = 1;
            BenchmarkJob job = new BenchmarkJob(algorithm, graphSet, res, repetition);
            experiment.addJob(job);

        }

        LOG.info(String.format(" Experiment %s runs algorithm %s on graph %s", expType, algorithm.getAcronym(), addedGraphs));

        return experiment;
    }

    public BenchmarkExperiment debugExperiment(Algorithm algorithm) {
        String expType = String.format("std:baseline:%s", algorithm.getAcronym());
        BenchmarkExperiment experiment = new BenchmarkExperiment(expType);
        List<StandardGraph> addedGraphs = new ArrayList<>();

        for (StandardGraph standardGraph : StandardGraph.values()) {

            if (algorithm != Algorithm.BFS && algorithm != Algorithm.WCC) {
                continue;
            }
            if (standardGraph != StandardGraph.XDIR) {
                continue;
            }
            if (algorithm == Algorithm.SSSP && !standardGraph.hasProperty) {
                continue;
            }

            GraphSet graphSet = availableGraphs.get(standardGraph.fileName);
            int repetition = 2;
            int res = 1;
            BenchmarkJob job = new BenchmarkJob(algorithm, graphSet, res, repetition);
            addedGraphs.add(standardGraph);
            experiment.addJob(job);

        }

        LOG.info(String.format(" Experiment %s runs algorithm %s on graph %s", expType, algorithm.getAcronym(), addedGraphs));

        return experiment;
    }

    public BenchmarkExperiment setupBaselineExperiment(Algorithm algorithm) {
        if(DebugMode) {
            return debugExperiment(algorithm);
        } else {
            return baselineExperiment(algorithm);
        }
    }


}
