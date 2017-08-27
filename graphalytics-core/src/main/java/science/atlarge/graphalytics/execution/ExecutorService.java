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
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import org.apache.commons.configuration.Configuration;
import science.atlarge.graphalytics.configuration.ConfigurationUtil;
import science.atlarge.graphalytics.report.result.BenchmarkRunResult;
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

    public static Map<String, BenchmarkRunStatus> runnerStatuses = new HashMap<>();


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

            Notification notification = (Notification) message;
            BenchmarkRunStatus runnerStatus = runnerStatuses.get(notification.getBenchmarkId());
            runnerStatus.setActor(this.sender());

            if(!runnerStatus.isTerminated) {
                if(notification.getLabel() == Notification.Label.REGISTRATION) {
                    runnerStatus.setInitialized(true);;
                } else if(notification.getLabel() == Notification.Label.EXECUTION) {
                    runnerStatus.setRunned(true);
                } else if(notification.getLabel() == Notification.Label.VALIDATION) {
                    runnerStatus.setValidated(true);
                } else if(notification.getLabel() == Notification.Label.FAILURE) {
                    runnerStatus.addFailure((BenchmarkFailure) ((Notification) message).getPayload());
                    LOG.error("A benchmark failure (" + ((Notification) message).getPayload() + ") is caught by the runner.");
                }
            }

        } else if(message instanceof BenchmarkRunResult) {
            BenchmarkRunResult result = (BenchmarkRunResult) message;

            BenchmarkRunStatus runnerStatus = runnerStatuses.get(result.getBenchmarkRun().getId());

            if(!runnerStatus.isTerminated) {
                runnerStatus.setFinalized(true);
                runnerStatus.setBenchmarkRunResult(result);
            }
        }
    }

    public void sendTask(RunSpecification runSpecification) {
        LOG.debug("Sending benchmark specification to runner.");
        BenchmarkRunStatus benchmarkRunStatus = runnerStatuses.get(runSpecification.getBenchmarkRun().getId());
        ActorRef executorActor = benchmarkRunStatus.getActor();
        executorActor.tell(runSpecification, getSelf());
    }


    public static Integer getExecutorPort() {
        Configuration configuration = ConfigurationUtil.loadConfiguration(BENCHMARK_PROPERTIES_FILE);
        return ConfigurationUtil.getInteger(configuration, BENCHMARK_EXECUTOR_PORT);
    }
}
