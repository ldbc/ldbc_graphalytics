/*
 * Copyright 2015 - 2017 Atlarge Research Team,
 * operating at Technische Universiteit Delft
 * and Vrije Universiteit Amsterdam, the Netherlands.
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
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.domain.graph.GraphScale;
import science.atlarge.graphalytics.domain.graph.StandardGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author Mihai Capotă
 * @author Wing Lung Ngai
 */
public class StandardBenchmark extends Benchmark {

    private static final Logger LOG = LogManager.getLogger();

    GraphScale targetGraphScale;

    public StandardBenchmark(String type, String targetScale, String platformName,
                             Path baseReportDir, Path baseOutputDir, Path baseValidationDir,
                             Map<String, Graph> foundGraphs, Map<String, Map<Algorithm, AlgorithmParameters>> algorithmParameters) {
        super(platformName, true, true,
                baseReportDir, baseOutputDir, baseValidationDir,
                foundGraphs, algorithmParameters);
        this.targetGraphScale = GraphScale.valueOf(targetScale);
        this.baseReportDir = formatReportDirectory(baseReportDir, platformName, type + "_" + targetScale);
        this.type = type;

        Map<GraphScale, Integer> timeoutPerScale = new HashMap<>();
        timeoutPerScale.put(GraphScale.S, 15 * 60);
        timeoutPerScale.put(GraphScale.M, 30 * 60);
        timeoutPerScale.put(GraphScale.L, 60 * 60);
        timeoutPerScale.put(GraphScale.XL, 120 * 60);
        this.timeout = timeoutPerScale.get(targetGraphScale);
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
                BenchmarkRun benchmarkRun = contructBenchmarkRun(benchmarkJob.getAlgorithm(), benchmarkJob.getGraph());
                benchmarkJob.addBenchmark(benchmarkRun);
                benchmarkRuns.add(benchmarkRun);
            }
        }

        for (BenchmarkRun benchmarkRun : benchmarkRuns) {
            algorithms.add(benchmarkRun.getAlgorithm());
            graphs.add(benchmarkRun.getFormattedGraph().getGraph());
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

            selectedGraphs = selectGraph(targetGraphScale, algorithm);

            for (StandardGraph selectedGraph : selectedGraphs) {

                Graph graph = foundGraphs.get(selectedGraph.fileName);


                if(graph == null || !verifyGraphInfo(selectedGraph, graph)) {
                    throw new IllegalStateException(
                            String.format("Benchmark failed: graph info does not match expectation: %s", selectedGraph.fileName));
                }


                int repetition = 5;
                int res = 1;
                BenchmarkJob job = new BenchmarkJob(algorithm, graph, res, repetition);
                experiment.addJob(job);
            }
            experiments.add(experiment);
        }
        return experiments;
    }

    public List<StandardGraph> selectGraph(GraphScale scale, Algorithm algorithm) {
        List<StandardGraph> selected = new ArrayList<>();

        if(scale==GraphScale.S) {
            if(algorithm != Algorithm.SSSP) {
                selected.add(StandardGraph.DOTA);
                selected.add(StandardGraph.DG79FB);
                selected.add(StandardGraph.DG77ZF);
                selected.add(StandardGraph.GR22);
                selected.add(StandardGraph.DG78ZF);
            } else {
                selected.add(StandardGraph.DOTA);
                selected.add(StandardGraph.DG79FB);
                selected.add(StandardGraph.DG77ZF);
                selected.add(StandardGraph.DG78ZF);
                selected.add(StandardGraph.DG76FB);
            }
        } else if (scale == GraphScale.M) {
            if (algorithm != Algorithm.SSSP) {
                selected.add(StandardGraph.DG84FB);
                selected.add(StandardGraph.DG82ZF);
                selected.add(StandardGraph.GR24);
                selected.add(StandardGraph.DG83ZF);
                selected.add(StandardGraph.DG81FB);
            } else {
                selected.add(StandardGraph.DG84FB);
                selected.add(StandardGraph.DG82ZF);
                selected.add(StandardGraph.DG83ZF);
                selected.add(StandardGraph.DG81FB);
                selected.add(StandardGraph.DG80FB);
            }
        } else if (scale == GraphScale.L) {
            if (algorithm != Algorithm.SSSP) {
                selected.add(StandardGraph.DG89FB);
                selected.add(StandardGraph.DG87ZF);
                selected.add(StandardGraph.GR25);
                selected.add(StandardGraph.DG88ZF);
                selected.add(StandardGraph.DG86FB);
            } else {
                selected.add(StandardGraph.DG89FB);
                selected.add(StandardGraph.DG87ZF);
                selected.add(StandardGraph.DG88ZF);
                selected.add(StandardGraph.DG86FB);
                selected.add(StandardGraph.DG85FB);
            }
        } else if (scale == GraphScale.XL) {
            if (algorithm != Algorithm.SSSP) {
                selected.add(StandardGraph.TWIT);
                selected.add(StandardGraph.FSTER);
                selected.add(StandardGraph.DG94FB);
                selected.add(StandardGraph.DG92ZF);
                selected.add(StandardGraph.GR26);
            } else {
                selected.add(StandardGraph.DG94FB);
                selected.add(StandardGraph.DG92ZF);
                selected.add(StandardGraph.DG93ZF);
                selected.add(StandardGraph.DG91FB);
                selected.add(StandardGraph.DG90FB);
            }
        }


        return selected;
    }


    public List<BenchmarkExp> setupExperimentsDeprecated() {
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

                Graph graph = foundGraphs.get(selectedGraph.fileName);


                if(!verifyGraphInfo(selectedGraph, graph)) {
                    throw new IllegalStateException(
                            String.format("Benchmark failed: graph info does not match expectation: ", selectedGraph.fileName));
                }


                int repetition = 3;
                int res = 1;
                BenchmarkJob job = new BenchmarkJob(algorithm, graph, res, repetition);
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
