/*
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
package nl.tudelft.graphalytics.domain.benchmark;

import nl.tudelft.graphalytics.domain.*;
import nl.tudelft.graphalytics.domain.graph.GraphSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class StandardBenchmarkSuite extends BasicBenchmarkSuite {

    private static final Logger LOG = LogManager.getLogger();


    Scale targeScale;
    Map<String, GraphSet> availableGraphs;

    public StandardBenchmarkSuite(String targeScale, Map<String, GraphSet> availableGraphs) {
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
            experiments.add(setupStandardExperiment(algorithm));
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

    private List<StandardGraph> filterByTargetScale(Collection<StandardGraph> graphs, double minScale, double maxScale) {

        List<StandardGraph> selectedGraphs = new ArrayList<>();
        for (StandardGraph standardGraph : graphs) {
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.FLOOR);
            double graphScale = Math.log(standardGraph.graphSize) / Math.log(10);
            double roundedGraphScale = Double.parseDouble(df.format(graphScale));

            double epsilon = 0.01;
            if(roundedGraphScale <= maxScale || epsilon > Math.abs(maxScale - roundedGraphScale)) {
                if(roundedGraphScale >= minScale || epsilon > Math.abs(roundedGraphScale - minScale)) {
                    selectedGraphs.add(standardGraph);
                }
            }
        }
        return selectedGraphs;
    }

    public BenchmarkExperiment setupStandardExperiment(Algorithm algorithm) {
        String expType = String.format("std:%s", algorithm.getAcronym());
        BenchmarkExperiment experiment = new BenchmarkExperiment(expType);

        List<StandardGraph> addedGraphs = new ArrayList<>();

        double minScale = targeScale.minScale;
        double maxScale = targeScale.maxScale;

        List<StandardGraph> scaledGraphs = filterByTargetScale(Arrays.asList(StandardGraph.values()), minScale, maxScale);
        List<StandardGraph> realGraphs = filterByInitial(scaledGraphs, "R");

        if(!(algorithm == Algorithm.SSSP)) {

            for (StandardGraph scaledGraph : scaledGraphs) {
                addedGraphs.add(scaledGraph);
            }
        } else {
            for (StandardGraph propertiesGraph : filterByPropertiesGraph(scaledGraphs)) {
                addedGraphs.add(propertiesGraph);
            }
        }


        LOG.info(String.format(" Experiment %s runs algorithm %s on graph %s", expType, algorithm.getAcronym(), addedGraphs));

        for (StandardGraph addedGraph : addedGraphs) {

            GraphSet graphSet = availableGraphs.get(addedGraph.fileName);

            if (graphSet == null) {
//                LOG.error(String.format("Required graphset [%s] not available.", addedGraph.fileName));
//                throw new IllegalStateException("Standard Benchmark: Baseline cannot be constructed due to missing graphs.");
            }

            int repetition = 3;
            int res = 1;
            BenchmarkJob job = new BenchmarkJob(algorithm, graphSet, res, repetition);
            experiment.addJob(job);

        }

        return experiment;
    }


    public BenchmarkExperiment setupStandardExperimentOld(Algorithm algorithm) {
        String expType = String.format("std:%s", algorithm.getAcronym());
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
                LOG.error(String.format("Required graphset not %s available.", addedGraph.fileName));
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


}
