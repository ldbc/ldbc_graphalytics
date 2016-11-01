package nl.tudelft.graphalytics.network;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import nl.tudelft.graphalytics.BenchmarkRunner;
import nl.tudelft.graphalytics.domain.Benchmark;
import nl.tudelft.graphalytics.domain.BenchmarkResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RunnerService extends MircoService {

    private static final Logger LOG = LogManager.getLogger();
    public static final String SERVICE_NAME = "runner-service";
    public static final String SERVICE_IP = "localhost";
    public static final int SERVICE_PORT = 8012;

    BenchmarkRunner runner;

    public RunnerService(BenchmarkRunner runner) {
        this.runner = runner;
        runner.setService(this);
        register();
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
        getContext().actorSelection(masterAddress).tell(new Notification(runner.getBenchmarkId()), getSelf());
    }


    private void report(BenchmarkResult benchmarkResult) {
        String executorAddress = getExecutorAddress();
        getContext().actorSelection(executorAddress).tell(benchmarkResult, getSelf());
    }




    private String getExecutorAddress() {
        return String.format("akka.tcp://%s@%s:%s/user/%s",
                ExecutorService.SERVICE_NAME, SERVICE_IP, ExecutorService.SERVICE_PORT, ExecutorService.SERVICE_NAME);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Benchmark) {
            Benchmark benchmark = (Benchmark) message;
            BenchmarkResult benchmarkResult = runner.execute(benchmark);
            report(benchmarkResult);
//            terminate();
        }

    }

}
