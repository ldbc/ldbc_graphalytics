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

import org.apache.commons.configuration.Configuration;
import science.atlarge.graphalytics.configuration.ConfigurationUtil;
import science.atlarge.graphalytics.domain.algorithms.Algorithm;
import science.atlarge.graphalytics.domain.algorithms.AlgorithmParameters;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.domain.graph.StandardGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Wing Lung Ngai
 */
public class TestBenchmark extends Benchmark {

    private static final Logger LOG = LogManager.getLogger();


    private static final String BENCHMARK_PROPERTIES_FILE = "benchmark.properties";
    private static final String BENCHMARK_RUN_TIMEOUT_KEY = "benchmark.test.timeout";

    public TestBenchmark(String type, String platformName,
                         Path baseReportDir, Path baseOutputDir, Path baseValidationDir,
                         Map<String, Graph> foundGraphs, Map<String, Map<Algorithm, AlgorithmParameters>> algorithmParameters) {

        super(platformName, true, true,
                baseReportDir, baseOutputDir, baseValidationDir,
                foundGraphs, algorithmParameters);
        this.baseReportDir = formatReportDirectory(baseReportDir, platformName, type);
        this.type = type;

        Configuration benchmarkConfiguration = ConfigurationUtil.loadConfiguration(BENCHMARK_PROPERTIES_FILE);
        this.timeout = ConfigurationUtil.getInteger(benchmarkConfiguration, BENCHMARK_RUN_TIMEOUT_KEY);

    }



    public void setup() {
        experiments.addAll(setupExperiments());
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

            String expType = String.format("test:%s", algorithm.getAcronym());
            BenchmarkExp experiment = new BenchmarkExp(expType);
            List<StandardGraph> selectedGraphs = new ArrayList<>();

            for (StandardGraph graph : StandardGraph.values()) {

                if (graph != StandardGraph.XDIR && graph != StandardGraph.XUNDIR) {
                    continue;
                }
                if (algorithm == Algorithm.SSSP && !graph.hasProperty) {
                    continue;
                }

                Graph graphSet = foundGraphs.get(graph.fileName);


                if(!verifyGraphInfo(graph, graphSet)) {
                    throw new IllegalStateException(
                            String.format("Benchmark failed: graph info does not match expectation: ", graph.fileName));
                }


                int repetition = 1;
                int res = 1;
                BenchmarkJob job = new BenchmarkJob(algorithm, graphSet, res, repetition);
                selectedGraphs.add(graph);
                experiment.addJob(job);

            }
            experiments.add(experiment);
        }
        return experiments;
    }


}
