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

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import org.apache.commons.configuration.Configuration;
import science.atlarge.graphalytics.configuration.ConfigurationUtil;
import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;
import science.atlarge.graphalytics.report.result.BenchmarkResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Wing Lung Ngai
 */
public class ExecutorService extends MircoService {

    private static final Logger LOG = LogManager.getLogger();

    private static final String BENCHMARK_PROPERTIES_FILE = "benchmark.properties";
    private static final String BENCHMARK_EXECUTOR_PORT = "benchmark.executor.port";
    public static final String SERVICE_NAME = "executor-service";
    public static final String SERVICE_IP = "localhost";

    BenchmarkExecutor executor;

    public ExecutorService(BenchmarkExecutor executor) {

        this.executor = executor;
        executor.setService(this);

    }

    public static Map<String, BenchmarkRunnerInfo> runnerInfos = new HashMap<>();


    public static void InitService(BenchmarkExecutor executor) {
        Config config = defaultConfiguration();
        config = config.withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(getExecutorPort()));
        config = config.withValue("akka.remote.netty.tcp.hostname", ConfigValueFactory.fromAnyRef(SERVICE_IP));
        final ActorSystem system = ActorSystem.create(SERVICE_NAME, config);
        system.actorOf(Props.create(ExecutorService.class, executor), SERVICE_NAME);
    }


    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof Notification) {
//            LOG.info("Received notification");
            Notification notification = (Notification) message;
//            LOG.info(String.format("Received notification: %s", notification.getBenchmarkId()));
            BenchmarkRunnerInfo benchmarkRunnerStatus = runnerInfos.get(notification.getBenchmarkId());
            benchmarkRunnerStatus.setActor(this.sender());

            if(notification.getLabel() == Notification.Label.REGISTRATION) {
                benchmarkRunnerStatus.setRegistered(true);;
                sendTask(benchmarkRunnerStatus.getBenchmarkRun());
            } else if(notification.getLabel() == Notification.Label.EXECUTION) {
                benchmarkRunnerStatus.setExecuted(true);
            } else if(notification.getLabel() == Notification.Label.VALIDATION) {
                benchmarkRunnerStatus.setValidated(true);
            }

        } else if(message instanceof BenchmarkResult) {
            BenchmarkResult result = (BenchmarkResult) message;

            BenchmarkRunnerInfo benchmarkRunnerStatus = runnerInfos.get(result.getBenchmarkRun().getId());
            benchmarkRunnerStatus.setCompleted(true);
            benchmarkRunnerStatus.setBenchmarkResult(result);
        }
    }

    public void sendTask(BenchmarkRun benchmarkRun) {
        LOG.debug("Sending benchmark specification to runner.");
        BenchmarkRunnerInfo benchmarkRunnerStatus = runnerInfos.get(benchmarkRun.getId());
        ActorRef executorActor = benchmarkRunnerStatus.getActor();
        executorActor.tell(benchmarkRun, getSelf());
    }


    public static Integer getExecutorPort() {
        Configuration configuration = ConfigurationUtil.loadConfiguration(BENCHMARK_PROPERTIES_FILE);
        return ConfigurationUtil.getInteger(configuration, BENCHMARK_EXECUTOR_PORT);
    }
}
