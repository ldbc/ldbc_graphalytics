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
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.util.UuidUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Wing Lung Ngai
 */
public class BenchmarkJob {
    String id;
    Algorithm algorithm;
    Graph graph;
    int resourceSize;
    int repetition;
    List<BenchmarkRun> benchmarkRuns;

    public BenchmarkJob(Algorithm algorithm, Graph graph, int resourceSize, int repetition) {
        this.id = UuidUtil.getRandomUUID("j", 6);
        this.algorithm = algorithm;
        this.graph = graph;
        this.resourceSize = resourceSize;
        this.repetition = repetition;
        this.benchmarkRuns = new ArrayList<>();
    }

    public BenchmarkJob(String id, Algorithm algorithm, Graph graph, int resourceSize, int repetition) {
        this.id = id;
        this.algorithm = algorithm;
        this.graph = graph;
        this.resourceSize = resourceSize;
        this.repetition = repetition;
        this.benchmarkRuns = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public Graph getGraph() {
        return graph;
    }

    public int getResourceSize() {
        return resourceSize;
    }

    public int getRepetition() {
        return repetition;
    }

    public List<BenchmarkRun> getBenchmarkRuns() {
        return benchmarkRuns;
    }

    public void addBenchmark(BenchmarkRun benchmarkRun) {
        benchmarkRuns.add(benchmarkRun);
    }

}
