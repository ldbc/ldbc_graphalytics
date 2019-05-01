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
package science.atlarge.graphalytics.execution;

import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;

import java.io.Serializable;

/**
 * The specification of the benchmark run.
 * @author Wing Lung Ngai
 */
public class RunSpecification implements Serializable {

    final BenchmarkRun benchmarkRun;
    final BenchmarkRunSetup benchmarkRunSetup;
    final RuntimeSetup runtimeSetup;

    public RunSpecification(BenchmarkRun benchmarkRun, BenchmarkRunSetup benchmarkRunSetup, RuntimeSetup runtimeSetup) {
        this.benchmarkRun = benchmarkRun;
        this.benchmarkRunSetup = benchmarkRunSetup;
        this.runtimeSetup = runtimeSetup;
    }

    public BenchmarkRun getBenchmarkRun() {
        return benchmarkRun;
    }

    public BenchmarkRunSetup getBenchmarkRunSetup() {
        return benchmarkRunSetup;
    }

    public RuntimeSetup getRuntimeSetup() {
        return runtimeSetup;
    }
}
