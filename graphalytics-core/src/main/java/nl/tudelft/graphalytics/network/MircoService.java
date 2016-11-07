package nl.tudelft.graphalytics.network;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

public abstract class MircoService extends UntypedActor {

    public MircoService() {

    }


    protected static Config defaultConfiguration() {
        Config config = ConfigFactory.empty();
        config = config.withValue("akka.actor.java", ConfigValueFactory.fromAnyRef("akka.serialization.JavaSerializer"));
        config = config.withValue("akka.actor.provider", ConfigValueFactory.fromAnyRef("akka.remote.RemoteActorRefProvider"));
        config = config.withValue("akka.actor.warn-about-java-serializer-usage", ConfigValueFactory.fromAnyRef("off"));

//        config = config.withValue("akka.loglevel", ConfigValueFactory.fromAnyRef("ERROR"));
//        config = config.withValue("akka.remote.log-remote-lifecycle-events", ConfigValueFactory.fromAnyRef("off"));
//        config = config.withValue("akka.log-dead-letters-during-shutdown", ConfigValueFactory.fromAnyRef("off"));
//        config = config.withValue("akka.log-dead-letters", ConfigValueFactory.fromAnyRef("off"));

        return config;
    }


    public void terminate() {
//        getContext().stop(getSelf());
        getContext().system().shutdown();
//        System.exit(0);
    }


}
