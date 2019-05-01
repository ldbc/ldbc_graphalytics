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
package science.atlarge.graphalytics.domain.graph;

import java.io.Serializable;

/**
 * LoadedGraph represents graph data loaded into the system-dependent environment.
 * @author Wing Lung Ngai
 */
public class LoadedGraph implements Serializable {

    private final FormattedGraph formattedGraph;
    private final String loadedPath;
    private final String vertexPath;
    private final String edgePath;

    public LoadedGraph(FormattedGraph formattedGraph, String vertexPath, String edgePath) {
        this.formattedGraph = formattedGraph;
        this.loadedPath = null;
        this.vertexPath = vertexPath;
        this.edgePath = edgePath;
    }

    public LoadedGraph(FormattedGraph formattedGraph, String loadedPath) {
        this.formattedGraph = formattedGraph;
        this.loadedPath = loadedPath;
        this.vertexPath = null;
        this.edgePath = null;
    }

    public FormattedGraph getFormattedGraph() {
        return formattedGraph;
    }

    public String getLoadedPath() {
        return loadedPath;
    }

    public String getVertexPath() {
        return vertexPath;
    }

    public String getEdgePath() {
        return edgePath;
    }
}
