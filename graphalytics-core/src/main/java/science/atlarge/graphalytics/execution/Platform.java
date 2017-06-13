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
package science.atlarge.graphalytics.execution;

import science.atlarge.graphalytics.domain.graph.FormattedGraph;
import science.atlarge.graphalytics.report.result.BenchmarkMetrics;
import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;

/**
 * The common interface for any platform that implements the Graphalytics benchmark suite. It
 * defines the API that must be provided by a platform to be compatible with the Graphalytics
 * benchmark driver. The driver uses the {@link #loadGraph(FormattedGraph) loadGraph} and
 * {@link #deleteGraph(FormattedGraph) deleteGraph} functions to ensure the right graphs are loaded,
 * and uses {@link #run(BenchmarkRun) executeAlgorithmOnGraph}
 * to trigger the executing of various algorithms on each graph.
 * <p>
 * Note: it is highly recommended for platform implementations to extend {@link AbstractPlatform}
 * instead of implementing the Platform interface. As Graphalytics evolves, this interface may
 * be extended with additional (optional) methods.
 *
 * @author Tim Hegeman
 */
public interface Platform {


    void verifySetup();

    /**
     * Called before executing algorithms on a graph to allow the platform driver to import a graph.
     * This may include uploading to a distributed filesystem, importing in a graph database, etc.
     * The platform driver must ensure that this dataset remains available for multiple calls to
     * {@link #run(BenchmarkRun) executeAlgorithmOnGraph}, until
     * the removal of the graph is triggered using {@link #deleteGraph(FormattedGraph) deleteGraph}.
     *
     * @param formattedGraph information on the graph to be uploaded
     * @throws Exception if any exception occurred during the upload
     */
    void loadGraph(FormattedGraph formattedGraph) throws Exception;


    /**
     * @param benchmarkRun
     */
    void prepare(BenchmarkRun benchmarkRun);


    /**
     * @param benchmarkRun
     */
    void startup(BenchmarkRun benchmarkRun);

    /**
     * Called to trigger the executing of an algorithm on a specific graph. The execution of this
     * method is timed as part of the benchmarking process. The benchmark driver guarantees that the
     * graph has been uploaded using the {@link #loadGraph(FormattedGraph) loadGraph} method, and
     * that it has not been removed by a corresponding call to {@link #deleteGraph(FormattedGraph)
     * deleteGraph}.
     *
     * @param benchmarkRun the algorithm to execute, the graph to run the algorithm on,
     *                     and the algorithm- and graph-specific parameters
     * @return a PlatformBenchmarkResult object detailing the execution of the algorithm
     * @throws PlatformExecutionException if any exception occurred during the execution of the algorithm, or if
     *                                    the platform otherwise failed to complete the algorithm successfully
     */
    boolean run(BenchmarkRun benchmarkRun) throws PlatformExecutionException;


    /**
     * @param benchmarkRun
     */
    BenchmarkMetrics finalize(BenchmarkRun benchmarkRun);

    /**
     * @param benchmarkRun
     */
    void terminate(BenchmarkRun benchmarkRun);

    /**
     * Called by the benchmark driver to signal when a graph may be removed from the system. The
     * driver guarantees that every graph that is uploaded using {@link #loadGraph(FormattedGraph)
     * loadGraph} is removed using exactly one corresponding call to this method.
     *
     * @param formattedGraph information on the graph to be uploaded
     */
    void deleteGraph(FormattedGraph formattedGraph);


    /**
     * A unique identifier for the platform, used to name benchmark results, etc.
     * This should be the same as the platform name used to compile and run the benchmark
     * for this platform, for consistency.
     *
     * @return the unique name of the platform
     */
    String getPlatformName();

}
