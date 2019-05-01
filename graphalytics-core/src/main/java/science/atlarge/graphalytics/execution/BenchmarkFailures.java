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

import java.io.Serializable;
import java.util.*;

/**
 * @author Wing Lung Ngai
 */
public class BenchmarkFailures implements Serializable {

    Set<BenchmarkFailure> benchmarkFailures;

    public BenchmarkFailures() {
        benchmarkFailures = new LinkedHashSet<>();
    }

    public void add(BenchmarkFailure failure) {
        this.benchmarkFailures.add(failure);
    }

    public void addAll(BenchmarkFailures failures) {
        for (BenchmarkFailure failure : failures.list()) {
            benchmarkFailures.add(failure);
        }
    }

    public Set<BenchmarkFailure> list() {
        return benchmarkFailures;
    }

    public boolean hasNone() {
        return benchmarkFailures.size() == 0;
    }

    @Override
    public String toString() {
        return benchmarkFailures.toString();
    }
}
