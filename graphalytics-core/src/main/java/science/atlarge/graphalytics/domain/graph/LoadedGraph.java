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
    private final String inputFilePath;
    private final String vertexFilePath;
    private final String edgeFilePath;

    public LoadedGraph(FormattedGraph formattedGraph, String vertexFilePath, String edgeFilePath) {
        this.formattedGraph = formattedGraph;
        this.inputFilePath = null;
        this.vertexFilePath = vertexFilePath;
        this.edgeFilePath = edgeFilePath;
    }

    public LoadedGraph(FormattedGraph formattedGraph, String inputFilePath) {
        this.formattedGraph = formattedGraph;
        this.inputFilePath = inputFilePath;
        this.vertexFilePath = null;
        this.edgeFilePath = null;
    }

    public FormattedGraph getFormattedGraph() {
        return formattedGraph;
    }

    public String getInputFilePath() {
        return inputFilePath;
    }

    public String getVertexFilePath() {
        return vertexFilePath;
    }

    public String getEdgeFilePath() {
        return edgeFilePath;
    }
}
