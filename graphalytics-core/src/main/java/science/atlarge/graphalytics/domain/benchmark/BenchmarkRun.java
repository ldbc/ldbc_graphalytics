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
import science.atlarge.graphalytics.domain.graph.FormattedGraph;
import science.atlarge.graphalytics.domain.graph.LoadedGraph;
import science.atlarge.graphalytics.util.UuidUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * A single benchmark in the Graphalytics benchmark suite. Consists of a single algorithm, a single graph,
 * and parameters for the algorithm.
 *
 * @author Mihai Capotă
 * @author Stijn Heldens
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public final class BenchmarkRun implements Serializable {

    private String id;
    private Algorithm algorithm;
    private Graph graph;
    private int timeout;

    /**
     * @param algorithm the algorithm to run for this benchmark
     */
    public BenchmarkRun(Algorithm algorithm, Graph graph, int timeout) {

        this.id = UuidUtil.getRandomUUID("r", 6);
        this.algorithm = algorithm;
        this.graph = graph;
        this.timeout = timeout;
    }

    public String getId() {
        return id;
    }

    /**
     * @return the algorithm to run for this benchmark
     */
    public Algorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * @return parameters for the algorithm
     */
    public AlgorithmParameters getAlgorithmParameters() {
        return graph.getAlgorithmParameters().get(algorithm);
    }

    public Graph getGraph() {
        return graph;
    }

    /**
     * @return the graph to run this benchmark on
     */
    public FormattedGraph getFormattedGraph() {
        return graph.getGraphPerAlgorithm().get(algorithm);
    }

    /**
     * @return a string uniquely identifying this benchmark to use for e.g. naming files
     */
    public String getName() {
        return String.format("%s-%s-%s", id, algorithm.getAcronym(), graph.getName());
    }

    @Override
    public String toString() {
        return String.format("BenchmarkRun [%s, %s[%s], %s, timeout=%ss]",
                id, algorithm.getAcronym(),
                getAlgorithmParameters().getDescription(),
                graph.getName(), timeout);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(id);
        stream.writeObject(algorithm);
        stream.writeObject(graph);
        stream.writeInt(timeout);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        id = (String) stream.readObject();
        algorithm = (Algorithm) stream.readObject();
        graph = (Graph) stream.readObject();
        timeout = stream.readInt();
    }

}
