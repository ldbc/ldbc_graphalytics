package nl.tudelft.graphalytics.network;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import nl.tudelft.graphalytics.BenchmarkRunnerInfo;
import nl.tudelft.graphalytics.BenchmarkSuiteExecutor;
import nl.tudelft.graphalytics.domain.Benchmark;
import nl.tudelft.graphalytics.domain.BenchmarkResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wlngai on 10/28/16.
 */
public class ExecutorService extends MircoService {


    private static final Logger LOG = LogManager.getLogger();
    public static final String SERVICE_NAME = "executor-service";
    public static final String SERVICE_IP = "localhost";
    public static final int SERVICE_PORT = 8011;

    BenchmarkSuiteExecutor executor;

    public ExecutorService(BenchmarkSuiteExecutor executor) {
        this.executor = executor;
        executor.setService(this);

    }

    public static Map<String, BenchmarkRunnerInfo> runnerInfos = new HashMap<>();


    public static void InitService(BenchmarkSuiteExecutor executor) {
        Config config = defaultConfiguration();
        config = config.withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(SERVICE_PORT));
        config = config.withValue("akka.remote.netty.tcp.hostname", ConfigValueFactory.fromAnyRef(SERVICE_IP));
        final ActorSystem system = ActorSystem.create(SERVICE_NAME, config);
        system.actorOf(Props.create(ExecutorService.class, executor), SERVICE_NAME);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof Notification) {
            LOG.info("Received notification");
            Notification notification = (Notification) message;
            LOG.info(String.format("Received notification: %s", notification.getBenchmarkId()));
            BenchmarkRunnerInfo benchmarkRunnerStatus = runnerInfos.get(notification.getBenchmarkId());
            benchmarkRunnerStatus.setActor(this.sender());
            benchmarkRunnerStatus.setRegistered(true);;
            sendTask(benchmarkRunnerStatus.getBenchmark());
        } else if(message instanceof BenchmarkResult) {
            BenchmarkResult result = (BenchmarkResult) message;

            BenchmarkRunnerInfo benchmarkRunnerStatus = runnerInfos.get(result.getBenchmark().getId());
            benchmarkRunnerStatus.setCompleted(true);
            benchmarkRunnerStatus.setBenchmarkResult(result);
        }
    }

    public void sendTask(Benchmark benchmark) {
        LOG.info("Sending benchmark");
        BenchmarkRunnerInfo benchmarkRunnerStatus = runnerInfos.get(benchmark.getId());
        ActorRef executorActor = benchmarkRunnerStatus.getActor();
        executorActor.tell(benchmark, getSelf());
    }


}
