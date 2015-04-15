/**
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
package nl.tudelft.graphalytics.graphlab.conn;

import nl.tudelft.graphalytics.domain.Algorithm;
import nl.tudelft.graphalytics.domain.GraphFormat;
import nl.tudelft.graphalytics.graphlab.GraphLabJob;

/**
 * The job configuration of the connected-components implementation for GraphLab.
 * @author Jorai Rijsdijk
 */
public class ConnectedComponentsJob extends GraphLabJob {

    /**
     * Construct a new BreadthFirstSearchJob.
     * @param graphPath   The path to the graph on the HDFS file system
     * @param graphFormat The format of hte graph
     */
    public ConnectedComponentsJob(String graphPath, GraphFormat graphFormat) {
        super(
                "conn/ConnectedComponents.py",
                Algorithm.CONN,
                null,
                graphPath,
                graphFormat
        );
    }

    @Override
    public String[] formatParametersAsStrings() {
        return formatParametersHelper();
    }
}
