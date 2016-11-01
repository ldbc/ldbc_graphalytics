package nl.tudelft.graphalytics;

import akka.actor.ActorRef;
import nl.tudelft.graphalytics.domain.Benchmark;
import nl.tudelft.graphalytics.domain.BenchmarkResult;

/**
 * Created by wlngai on 10/31/16.
 */
public class BenchmarkRunnerInfo {

    public BenchmarkRunnerInfo(Benchmark benchmark, Process process) {
        this.benchmark = benchmark;
        this.process = process;
    }

    boolean isRegistered;
    boolean isCompleted;

    BenchmarkResult benchmarkResult;
    Benchmark benchmark;
    Process process;
    ActorRef actor;

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public Benchmark getBenchmark() {
        return benchmark;
    }

    public void setBenchmark(Benchmark benchmark) {
        this.benchmark = benchmark;
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

    public BenchmarkResult getBenchmarkResult() {
        return benchmarkResult;
    }

    public void setBenchmarkResult(BenchmarkResult benchmarkResult) {
        this.benchmarkResult = benchmarkResult;
    }
}
