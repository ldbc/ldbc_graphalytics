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

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import org.apache.commons.configuration.Configuration;
import science.atlarge.graphalytics.configuration.ConfigurationUtil;
import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;
import science.atlarge.graphalytics.report.result.BenchmarkMetrics;
import science.atlarge.graphalytics.report.result.BenchmarkResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RunnerService extends MircoService {

    private static final Logger LOG = LogManager.getLogger();

    private static final String BENCHMARK_PROPERTIES_FILE = "benchmark.properties";
    private static final String BENCHMARK_RUNNER_PORT = "benchmark.runner.port";
    public static final String SERVICE_NAME = "runner-service";
    public static final String SERVICE_IP = "localhost";
    BenchmarkRunner runner;

    public RunnerService(BenchmarkRunner runner) {
        LOG.info("Benchmark runner service started.");
        this.runner = runner;
        runner.setService(this);
        LOG.info("Benchmark runner service registration started.");
        register();
        LOG.info("Benchmark runner service registration ended.");
    }

    public static void InitService(BenchmarkRunner benchmarkRunner) {
        Config config = defaultConfiguration();
        config = config.withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(getRunnerPort()));
        config = config.withValue("akka.remote.netty.tcp.hostname", ConfigValueFactory.fromAnyRef(SERVICE_IP));
        final ActorSystem system = ActorSystem.create(SERVICE_NAME, config);
        system.actorOf(Props.create(RunnerService.class, benchmarkRunner), SERVICE_NAME);

        System.out.println("Started Graphalytics Runner Service");
    }

    private void register() {
        String masterAddress = getExecutorAddress();
        LOG.info(String.format("Register %s at %s.", runner.getBenchmarkId(), masterAddress));
        Notification notification = new Notification(
                runner.getBenchmarkId(),
                "Registrating benchmark runner.",
                Notification.Label.REGISTRATION);
        getContext().actorSelection(masterAddress).tell(notification, getSelf());
    }

    private void reportValidation() {
        String masterAddress = getExecutorAddress();
        LOG.info(String.format("Report validation for %s at %s.", runner.getBenchmarkId(), masterAddress));
        Notification notification = new Notification(
                runner.getBenchmarkId(),
                "Validated benchmark result.",
                Notification.Label.VALIDATION);
        getContext().actorSelection(masterAddress).tell(notification, getSelf());
    }

    private void reportExecution() {
        String masterAddress = getExecutorAddress();
        LOG.info(String.format("Report execution %s at %s.", runner.getBenchmarkId(), masterAddress));
        Notification notification = new Notification(
                runner.getBenchmarkId(),
                "Executed benchmark.",
                Notification.Label.EXECUTION);
        getContext().actorSelection(masterAddress).tell(notification, getSelf());
    }



    private void reportRetrievedResult(BenchmarkResult benchmarkResult) {
        String executorAddress = getExecutorAddress();
        LOG.info(String.format("Report benchmark result for %s at %s.", runner.getBenchmarkId(), executorAddress));
        getContext().actorSelection(executorAddress).tell(benchmarkResult, getSelf());
    }




    private String getExecutorAddress() {
//        return String.format("akka.tcp://%s@%s:%s/user/%s",
//                ExecutorService.SERVICE_NAME, SERVICE_IP, 8099, ExecutorService.SERVICE_NAME);
        return String.format("akka.tcp://%s@%s:%s/user/%s",
                ExecutorService.SERVICE_NAME, SERVICE_IP, ExecutorService.getExecutorPort(), ExecutorService.SERVICE_NAME);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof BenchmarkRun) {
            BenchmarkRun benchmarkRun = (BenchmarkRun) message;

            LOG.info(String.format("The runner received specification for benchmark %s.", benchmarkRun.getId()));
            LOG.info(String.format("The runner is executing benchmark %s.", benchmarkRun.getId()));

            runner.preprocess(benchmarkRun);
            runner.execute(benchmarkRun);
            reportExecution();
            runner.validate(benchmarkRun);
            reportValidation();

            BenchmarkResult benchmarkResult = runner.summarize(benchmarkRun);
            BenchmarkMetrics metrics = runner.postprocess(benchmarkRun);
            benchmarkResult.withUpdatedBenchmarkMetrics(metrics);
            reportRetrievedResult(benchmarkResult);

//            terminate();
        }

    }


    public static Integer getRunnerPort() {
        Configuration configuration = ConfigurationUtil.loadConfiguration(BENCHMARK_PROPERTIES_FILE);
        return ConfigurationUtil.getInteger(configuration, BENCHMARK_RUNNER_PORT);
    }

}
