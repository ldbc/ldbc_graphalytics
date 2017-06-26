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
package science.atlarge.graphalytics.report.result;

import java.io.Serializable;

/**
 * @author Wing Lung Ngai
 */
public class BenchmarkMetrics implements Serializable{
    private BenchmarkMetric loadTime;
    private BenchmarkMetric makespan;
    private BenchmarkMetric processingTime;

    public BenchmarkMetrics() {
        loadTime = new BenchmarkMetric();
        makespan = new BenchmarkMetric();
        processingTime = new BenchmarkMetric();
    }

    public BenchmarkMetric getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(BenchmarkMetric loadTime) {
        this.loadTime = loadTime;
    }

    public BenchmarkMetric getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(BenchmarkMetric processingTime) {
        this.processingTime = processingTime;
    }

    public BenchmarkMetric getMakespan() {
        return makespan;
    }

    public void setMakespan(BenchmarkMetric makespan) {
        this.makespan = makespan;
    }
}
