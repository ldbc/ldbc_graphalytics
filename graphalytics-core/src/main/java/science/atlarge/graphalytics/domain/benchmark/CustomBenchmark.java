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

import science.atlarge.graphalytics.configuration.ConfigurationUtil;
import science.atlarge.graphalytics.configuration.InvalidConfigurationException;
import science.atlarge.graphalytics.domain.algorithms.Algorithm;
import science.atlarge.graphalytics.domain.algorithms.AlgorithmParameters;
import science.atlarge.graphalytics.domain.graph.Graph;
import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.*;

/**
 * @author Wing Lung Ngai
 */
public class CustomBenchmark extends Benchmark {

    private static final Logger LOG = LogManager.getLogger();

    private static final String BENCHMARK_PROPERTIES_FILE = "benchmark.properties";
    private static final String BENCHMARK_RUN_GRAPHS_KEY = "benchmark.custom.graphs";
    private static final String BENCHMARK_RUN_ALGORITHMS_KEY = "benchmark.custom.algorithms";
    private static final String BENCHMARK_RUN_TIMEOUT_KEY = "benchmark.custom.timeout";
    private static final String BENCHMARK_RUN_OUTPUT_REQUIRED_KEY = "benchmark.custom.output-required";
    private static final String BENCHMARK_RUN_VALIDATION_REQUIRED_KEY = "benchmark.custom.validation-required";
    private static final String BENCHMARK_RUN_REPETITIONS = "benchmark.custom.repetitions";
    private static final String BENCHMARK_WRITE_RESULT_AFTER_EACH_JOB = "benchmark.custom.write-result-after-each-job";

    public CustomBenchmark(String type, String platformName,
                           Path baseReportDir, Path baseOutputDir, Path baseValidationDir,
                           Map<String, Graph> foundGraphs, Map<String, Map<Algorithm, AlgorithmParameters>> algorithmParameters) {

        super(platformName, true, true,
                baseReportDir, baseOutputDir, baseValidationDir,
                foundGraphs, algorithmParameters);
        this.baseReportDir = formatReportDirectory(baseReportDir, platformName, type);
        this.type = type;

        Configuration benchmarkConfiguration = ConfigurationUtil.loadConfiguration(BENCHMARK_PROPERTIES_FILE);
        this.timeout = ConfigurationUtil.getInteger(benchmarkConfiguration, BENCHMARK_RUN_TIMEOUT_KEY);

        this.validationRequired = ConfigurationUtil.getBoolean(benchmarkConfiguration, BENCHMARK_RUN_VALIDATION_REQUIRED_KEY);
        this.outputRequired = ConfigurationUtil.getBoolean(benchmarkConfiguration, BENCHMARK_RUN_OUTPUT_REQUIRED_KEY);
        this.isWriteResultsDirectlyEnabled = ConfigurationUtil.getBooleanIfExists(benchmarkConfiguration, BENCHMARK_WRITE_RESULT_AFTER_EACH_JOB);

        if (this.validationRequired && !this.outputRequired) {
            LOG.warn("Validation can only be enabled if output is generated. "
                    + "Please enable the key " + BENCHMARK_RUN_OUTPUT_REQUIRED_KEY + " in your configuration.");
            LOG.info("Validation will be disabled for all benchmarks.");
            this.validationRequired = false;
        }

    }


    public void setup() {

        Configuration benchmarkConfiguration = ConfigurationUtil.loadConfiguration(BENCHMARK_PROPERTIES_FILE);

        String[] algorithmSelectionNames = benchmarkConfiguration.getStringArray(BENCHMARK_RUN_ALGORITHMS_KEY);
        String[] graphSelectionNames = benchmarkConfiguration.getStringArray(BENCHMARK_RUN_GRAPHS_KEY);


        Set<Algorithm> algorithmSelection = null;
        Set<Graph> graphSelection = null;

        try {
            algorithmSelection = parseAlgorithmSelection(algorithmSelectionNames);

            graphSelection = parseGraphSetSelection(graphSelectionNames);
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        experiments = new ArrayList<>();
        jobs = new ArrayList<>();

        BenchmarkExp experiment = new BenchmarkExp("custom:exp");
        experiments.add(experiment);

       benchmarkRuns = new HashSet<>();
        for (Algorithm algorithm : algorithmSelection) {
            for (Graph graph : graphSelection) {

                // if graph does not support algorithm, skip.
                if(!graph.getAlgorithmParameters().containsKey(algorithm)) {
                    LOG.error(String.format("Skipping benchmark %s on %s: " +
                                    "algorithm %s cannot run on dataset %s.",
                            algorithm.getAcronym(), graph.getName(),
                            algorithm.getAcronym(), graph.getName()));
                    continue;
                }

                BenchmarkJob job = new BenchmarkJob(algorithm, graph, 1, benchmarkConfiguration.getInt(BENCHMARK_RUN_REPETITIONS));

                for (int i = 0; i < job.getRepetition(); i++) {
                    BenchmarkRun benchmarkRun = contructBenchmarkRun(job.algorithm, job.graph);
                    job.addBenchmark(benchmarkRun);
                    benchmarkRuns.add(benchmarkRun);
                }

                jobs.add(job);
                experiment.addJob(job);
            }
        }

        for (BenchmarkRun benchmarkRun : benchmarkRuns) {
            algorithms.add(benchmarkRun.getAlgorithm());
            graphs.add(benchmarkRun.getFormattedGraph().getGraph());
        }

    }


    private Set<Graph> parseGraphSetSelection(String[] graphSelectionNames) throws InvalidConfigurationException {
        Set<Graph> graphSelection;

        // Parse the graph names
        graphSelection = new HashSet<>();
        for (String graphSelectionName : graphSelectionNames) {
            if (foundGraphs.containsKey(graphSelectionName)) {
                graphSelection.add(foundGraphs.get(graphSelectionName));
            } else if (!graphSelectionName.isEmpty()) {
                LOG.warn("Found unknown graph name \"" + graphSelectionName + "\" in property \"" +
                        BENCHMARK_RUN_GRAPHS_KEY + "\". " + " This graph may not be imported correctly due to misconfiguration.");
            } else {
                throw new InvalidConfigurationException("Incorrectly formatted selection of graph names in property \"" +
                        BENCHMARK_RUN_GRAPHS_KEY + "\".");
            }
        }
        return graphSelection;
    }

    private Set<Algorithm> parseAlgorithmSelection(String[] algorithmSelectionNames) throws InvalidConfigurationException {

        Set<Algorithm> algorithmSelection;

        // Parse the algorithm acronyms
        algorithmSelection = new HashSet<>();
        for (String algorithmSelectionName : algorithmSelectionNames) {
            Algorithm algorithm = Algorithm.fromAcronym(algorithmSelectionName);
            if (algorithm != null) {
                algorithmSelection.add(algorithm);
            } else if (!algorithmSelectionName.isEmpty()) {
                LOG.warn("Found unknown algorithm name \"" + algorithmSelectionName + "\" in property \"" +
                        BENCHMARK_RUN_ALGORITHMS_KEY + "\".");
            } else {
                throw new InvalidConfigurationException("Incorrectly formatted selection of algorithm names in property \"" +
                        BENCHMARK_RUN_ALGORITHMS_KEY + "\".");
            }
        }
        return algorithmSelection;
    }
}
