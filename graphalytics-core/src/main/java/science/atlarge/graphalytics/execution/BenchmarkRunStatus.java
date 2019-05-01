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

import akka.actor.ActorRef;
import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;
import science.atlarge.graphalytics.report.result.BenchmarkRunResult;

/**
 * @author Wing Lung Ngai
 */
public class BenchmarkRunStatus {

    boolean isPrepared;
    boolean isInitialized;
    boolean isRunned;
    boolean isValidated;
    boolean isFinalized;
    boolean isTerminated;

    RunSpecification runSpecification;
    BenchmarkRun benchmarkRun;
    BenchmarkFailures runFailures;
    BenchmarkRunResult benchmarkRunResult;

    Process process;
    ActorRef actor;


    public BenchmarkRunStatus(RunSpecification runSpecification) {
        this.runSpecification = runSpecification;
        this.benchmarkRun = runSpecification.getBenchmarkRun();
        this.runFailures = new BenchmarkFailures();
    }

    public boolean isPrepared() {
        return isPrepared;
    }

    public void setPrepared(boolean prepared) {
        isPrepared = prepared;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }

    public boolean isRunned() {
        return isRunned;
    }

    public void setRunned(boolean runned) {
        isRunned = runned;
    }

    public boolean isValidated() {
        return isValidated;
    }

    public void setValidated(boolean validated) {
        isValidated = validated;
    }

    public boolean isFinalized() {
        return isFinalized;
    }

    public void setFinalized(boolean finalized) {
        isFinalized = finalized;
    }

    public boolean isTerminated() {
        return isTerminated;
    }

    public void setTerminated(boolean terminated) {
        isTerminated = terminated;
    }

    public RunSpecification getRunSpecification() {
        return runSpecification;
    }

    public BenchmarkRun getBenchmarkRun() {
        return benchmarkRun;
    }

    public void setBenchmarkRun(BenchmarkRun benchmarkRun) {
        this.benchmarkRun = benchmarkRun;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public ActorRef getActor() {
        return actor;
    }

    public void setActor(ActorRef actor) {
        this.actor = actor;
    }

    public BenchmarkRunResult getBenchmarkRunResult() {
        return benchmarkRunResult;
    }

    public void setBenchmarkRunResult(BenchmarkRunResult benchmarkRunResult) {
        this.benchmarkRunResult = benchmarkRunResult;
    }

    public BenchmarkFailures getRunFailures() {
        return runFailures;
    }

    public void addFailure(BenchmarkFailure failure) {
        this.runFailures.add(failure);
    }
}
