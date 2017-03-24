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

import nl.tudelft.graphalytics.domain.algorithms.Algorithm;
import nl.tudelft.graphalytics.domain.graph.GraphSet;
import nl.tudelft.graphalytics.domain.graph.StandardGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class TestBenchmark extends Benchmark {

    private static final Logger LOG = LogManager.getLogger();

    Map<String, GraphSet> foundGraphs;

    public TestBenchmark(Map<String, GraphSet> foundGraphs) {
        super();
        this.foundGraphs = foundGraphs;
    }

    public void setup() {
        experiments.addAll(setupExperiments());
        for (BenchmarkExp experiment : experiments) {
            for (BenchmarkJob benchmarkJob : experiment.getJobs()) {
                jobs.add(benchmarkJob);
            }
        }
    }

    public List<BenchmarkExp> setupExperiments() {
        List<BenchmarkExp> experiments = new ArrayList<>();

        List<Algorithm> algorithms = Arrays.asList(
                Algorithm.BFS, Algorithm.WCC, Algorithm.PR, Algorithm.CDLP, Algorithm.LCC, Algorithm.SSSP);

        for (Algorithm algorithm : algorithms) {

            String expType = String.format("std:%s", algorithm.getAcronym());
            BenchmarkExp experiment = new BenchmarkExp(expType);
            List<StandardGraph> selectedGraphs = new ArrayList<>();

            for (StandardGraph graph : StandardGraph.values()) {

                if (graph != StandardGraph.XDIR && graph != StandardGraph.XUNDIR) {
                    continue;
                }
                if (algorithm == Algorithm.SSSP && !graph.hasProperty) {
                    continue;
                }

                GraphSet graphSet = foundGraphs.get(graph.fileName);


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
            LOG.info(String.format(" Experiment %s runs algorithm %s on graph %s", expType, algorithm.getAcronym(), selectedGraphs));
            experiments.add(experiment);
        }
        return experiments;
    }


}
