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
package science.atlarge.graphalytics.domain.benchmark;

import science.atlarge.graphalytics.domain.algorithms.Algorithm;
import science.atlarge.graphalytics.domain.algorithms.AlgorithmParameters;
import science.atlarge.graphalytics.domain.graph.GraphSet;
import science.atlarge.graphalytics.domain.graph.GraphScale;
import science.atlarge.graphalytics.domain.graph.StandardGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

public class StandardBenchmark extends Benchmark {

    private static final Logger LOG = LogManager.getLogger();

    GraphScale targetGraphScale;

    public StandardBenchmark(String type, String targetScale, String platformName,
                             int timeout, boolean outputRequired, boolean validationRequired,
                             Path baseLogDir, Path baseOutputDir, Path baseValidationDir,
                             Map<String, GraphSet> foundGraphs, Map<String, Map<Algorithm, AlgorithmParameters>> algorithmParameters) {

        super(platformName, timeout, outputRequired, validationRequired,
                baseLogDir, baseOutputDir, baseValidationDir,
                foundGraphs, algorithmParameters);
        this.targetGraphScale = GraphScale.valueOf(targetScale);
        this.baseLogDir = Paths.get(formatReportDirectory(platformName, type + "_" + targetScale));
        this.type = type;
    }


    public void setup() {
        experiments.addAll(setupExperiments());
        benchmarkRuns = new HashSet<>();
        for (BenchmarkExp experiment : experiments) {
            for (BenchmarkJob benchmarkJob : experiment.getJobs()) {
                jobs.add(benchmarkJob);
            }
        }

        for (BenchmarkJob benchmarkJob : getJobs()) {
            for (int i = 0; i < benchmarkJob.getRepetition(); i++) {
                BenchmarkRun benchmarkRun = contructBenchmarkRun(benchmarkJob.getAlgorithm(), benchmarkJob.getGraphSet());
                benchmarkJob.addBenchmark(benchmarkRun);
                benchmarkRuns.add(benchmarkRun);
            }
        }

        for (BenchmarkRun benchmarkRun : benchmarkRuns) {
            algorithms.add(benchmarkRun.getAlgorithm());
            graphSets.add(benchmarkRun.getGraph().getGraphSet());
        }
    }




    public List<BenchmarkExp> setupExperiments() {
        List<BenchmarkExp> experiments = new ArrayList<>();

        List<Algorithm> algorithms = Arrays.asList(
                Algorithm.BFS, Algorithm.WCC, Algorithm.PR, Algorithm.CDLP, Algorithm.LCC, Algorithm.SSSP);

        for (Algorithm algorithm : algorithms) {
            String expType = String.format("standard:%s", algorithm.getAcronym());
            BenchmarkExp experiment = new BenchmarkExp(expType);

            List<StandardGraph> selectedGraphs = new ArrayList<>();

            double minScale = targetGraphScale.minSize;
            double maxScale = targetGraphScale.maxSize;

            List<StandardGraph> scaledGraphs = filterByTargetScale(Arrays.asList(StandardGraph.values()), minScale, maxScale);
            List<StandardGraph> realGraphs = filterByInitial(scaledGraphs, "R");

            if (!(algorithm == Algorithm.SSSP)) {

                for (StandardGraph scaledGraph : scaledGraphs) {
                    selectedGraphs.add(scaledGraph);
                }
            } else {
                for (StandardGraph propertiesGraph : filterByPropertiesGraph(scaledGraphs)) {
                    selectedGraphs.add(propertiesGraph);
                }
            }

            for (StandardGraph selectedGraph : selectedGraphs) {

                GraphSet graphSet = foundGraphs.get(selectedGraph.fileName);


                if(!verifyGraphInfo(selectedGraph, graphSet)) {
                    throw new IllegalStateException(
                            String.format("Benchmark failed: graph info does not match expectation: ", selectedGraph.fileName));
                }


                int repetition = 3;
                int res = 1;
                BenchmarkJob job = new BenchmarkJob(algorithm, graphSet, res, repetition);
                experiment.addJob(job);
            }
            experiments.add(experiment);
        }
        return experiments;
    }

    private StandardGraph selectLargest(Collection<StandardGraph> graphs) {
        double largestSize = Double.MIN_VALUE;
        StandardGraph largestGraph = null;
        for (StandardGraph graph : graphs) {
            if (graph.graphSize > largestSize) {
                largestSize = graph.graphSize;
                largestGraph = graph;
            }
        }
        return largestGraph;
    }


    private List<StandardGraph> filterByPropertiesGraph(Collection<StandardGraph> graphs) {
        List<StandardGraph> selectedGraphs = new ArrayList<>();
        for (StandardGraph standardGraph : graphs) {
            if (standardGraph.hasProperty) {
                selectedGraphs.add(standardGraph);
            }
        }
        return selectedGraphs;
    }

    private List<StandardGraph> filterByInitial(Collection<StandardGraph> graphs, String initial) {
        List<StandardGraph> selectedGraphs = new ArrayList<>();
        for (StandardGraph standardGraph : graphs) {
            if (standardGraph.scale.startsWith(initial)) {
                selectedGraphs.add(standardGraph);
            }
        }
        return selectedGraphs;
    }

    private List<StandardGraph> filterByMaxSize(Collection<StandardGraph> graphs, double maxSize) {
        List<StandardGraph> selectedGraphs = new ArrayList<>();
        for (StandardGraph standardGraph : graphs) {
            if (standardGraph.graphSize < maxSize) {
                selectedGraphs.add(standardGraph);
            }
        }
        return selectedGraphs;
    }

    private List<StandardGraph> filterByTargetScale(Collection<StandardGraph> graphs, double minScale, double maxScale) {

        List<StandardGraph> selectedGraphs = new ArrayList<>();
        for (StandardGraph graph : graphs) {
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.FLOOR);
            double graphScale = Math.log(graph.graphSize) / Math.log(10);
            double roundedGraphScale = Double.parseDouble(df.format(graphScale));

            double epsilon = 0.01;
            if (roundedGraphScale <= maxScale || epsilon > Math.abs(maxScale - roundedGraphScale)) {
                if (roundedGraphScale >= minScale || epsilon > Math.abs(roundedGraphScale - minScale)) {
                    selectedGraphs.add(graph);
                }
            }
        }
        return selectedGraphs;
    }


}
