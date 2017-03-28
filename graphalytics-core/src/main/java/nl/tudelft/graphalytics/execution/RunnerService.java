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
package nl.tudelft.graphalytics.execution;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import nl.tudelft.graphalytics.domain.benchmark.BenchmarkRun;
import nl.tudelft.graphalytics.report.result.BenchmarkResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RunnerService extends MircoService {

    private static final Logger LOG = LogManager.getLogger();
    public static final String SERVICE_NAME = "runner-service";
    public static final String SERVICE_IP = "localhost";
    public static final int SERVICE_PORT = 8012;

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

        config = config.withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(SERVICE_PORT));
        config = config.withValue("akka.remote.netty.tcp.hostname", ConfigValueFactory.fromAnyRef(SERVICE_IP));
        final ActorSystem system = ActorSystem.create(SERVICE_NAME, config);
        system.actorOf(Props.create(RunnerService.class, benchmarkRunner), SERVICE_NAME);

        System.out.println("Started Graphalytics Runner Service");
    }

    private void register() {
        String masterAddress = getExecutorAddress();
        LOG.info(String.format("Register %s at %s.", runner.getBenchmarkId(), masterAddress));
        getContext().actorSelection(masterAddress).tell(new Notification(runner.getBenchmarkId()), getSelf());
    }


    private void report(BenchmarkResult benchmarkResult) {
        String executorAddress = getExecutorAddress();
        LOG.info(String.format("Report benchmark result for %s at %s.", runner.getBenchmarkId(), executorAddress));
        getContext().actorSelection(executorAddress).tell(benchmarkResult, getSelf());
    }




    private String getExecutorAddress() {
        return String.format("akka.tcp://%s@%s:%s/user/%s",
                ExecutorService.SERVICE_NAME, SERVICE_IP, ExecutorService.SERVICE_PORT, ExecutorService.SERVICE_NAME);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof BenchmarkRun) {
            BenchmarkRun benchmarkRun = (BenchmarkRun) message;

            LOG.info(String.format("Runner receives benchmark %s.", benchmarkRun.getId()));

            Platform platform = runner.getPlatform();
            platform.prepare(benchmarkRun);
            BenchmarkResult benchmarkResult = runner.execute(benchmarkRun);
            platform.cleanup(benchmarkRun);

            report(benchmarkResult);
//            terminate();
        }

    }

}
