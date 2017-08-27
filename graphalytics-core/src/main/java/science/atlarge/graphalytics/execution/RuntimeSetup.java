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

import science.atlarge.graphalytics.domain.graph.LoadedGraph;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * The setup of the runtime configuraiton.
 * @author Wing Lung Ngai
 */
public class RuntimeSetup implements Serializable {

    private LoadedGraph loadedGraph;

    public RuntimeSetup(LoadedGraph loadedGraph) {
        this.loadedGraph = loadedGraph;
    }

    public LoadedGraph getLoadedGraph() {
        return loadedGraph;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(loadedGraph);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        loadedGraph = (LoadedGraph) stream.readObject();
    }
}
