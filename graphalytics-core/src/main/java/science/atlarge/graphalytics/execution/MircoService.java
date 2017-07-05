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

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

/**
 * @author Wing Lung Ngai
 */
public abstract class MircoService extends UntypedActor {

    public MircoService() {

    }


    protected static Config defaultConfiguration() {
        Config config = ConfigFactory.empty();
        config = config.withValue("akka.actor.java", ConfigValueFactory.fromAnyRef("akka.serialization.JavaSerializer"));
        config = config.withValue("akka.actor.provider", ConfigValueFactory.fromAnyRef("akka.remote.RemoteActorRefProvider"));
        config = config.withValue("akka.actor.warn-about-java-serializer-usage", ConfigValueFactory.fromAnyRef("off"));

        config = config.withValue("akka.loglevel", ConfigValueFactory.fromAnyRef("ERROR"));
//        config = config.withValue("akka.remote.log-remote-lifecycle-events", ConfigValueFactory.fromAnyRef("off"));
//        config = config.withValue("akka.log-dead-letters-during-shutdown", ConfigValueFactory.fromAnyRef("off"));
//        config = config.withValue("akka.log-dead-letters", ConfigValueFactory.fromAnyRef("off"));

        return config;
    }


    public void terminate() {
        getContext().system().shutdown();
    }


}
