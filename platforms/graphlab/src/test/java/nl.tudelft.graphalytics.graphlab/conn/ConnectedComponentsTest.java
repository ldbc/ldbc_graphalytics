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
import nl.tudelft.graphalytics.graphlab.AlgorithmTest;
import org.junit.Test;

/**
 * Test class for ConnectedComponents. Executes the CONN on a small graph,
 * and verifies that the output of the computation matches the expected results.
 *
 * @author Jorai Rijsdijk
 */
public class ConnectedComponentsTest extends AlgorithmTest {
    @Test
    public void testDirectedConnectedComponents() {
        performTest(Algorithm.CONN, "conn-dir", "conn/ConnectedComponentsTest.py", null, true, true);
    }

    @Test
    public void testUndirectedConnectedComponents() {
        performTest(Algorithm.CONN, "conn-undir", "conn/ConnectedComponentsTest.py", null, false, true);
    }
}
