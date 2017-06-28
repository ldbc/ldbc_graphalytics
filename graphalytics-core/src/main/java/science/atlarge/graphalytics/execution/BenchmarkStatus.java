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

import science.atlarge.graphalytics.report.result.BenchmarkMetrics;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author Wing Lung Ngai
 */
public class BenchmarkStatus implements Serializable {

    private Date startOfBenchmark;
    private Date endOfBenchmark;
    private boolean validated;

    public BenchmarkStatus() {
        startOfBenchmark = new Date();
        endOfBenchmark = new Date();
        validated = false;
    }


    /**
     * @return the start time of the benchmark execution
     */
    public Date getStartOfBenchmark() {
        return new Date(startOfBenchmark.getTime());
    }

    public void setStartOfBenchmark() {
        this.startOfBenchmark = new Date();
    }

    /**
     * @return the completion time of the benchmark execution
     */
    public Date getEndOfBenchmark() {
        return new Date(endOfBenchmark.getTime());
    }

    public void setEndOfBenchmark() {
        this.endOfBenchmark = new Date();
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public boolean isValidated() {
        return validated;
    }


}
