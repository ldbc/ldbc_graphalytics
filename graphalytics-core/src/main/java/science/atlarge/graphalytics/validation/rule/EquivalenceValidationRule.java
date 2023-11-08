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
package science.atlarge.graphalytics.validation.rule;

/**
 * Validation rule which checks if vertex values are identical under equivalence.
 *
 * @author Stijn Heldens
 * @author Wing Lung Ngai
 */
public class EquivalenceValidationRule implements ValidationRule<Long> {

	@Override
	public String getQuery() {
		return "SELECT e1.v AS v, e1.x AS x, a1.x AS x\n" +
				"FROM expected e1, actual a1\n" +
				"WHERE e1.v = a1.v -- select a node in the expected-actual tables\n" +
				"  AND (\n" +
				"    EXISTS (\n" +
				"      FROM expected e2, actual a2\n" +
				"      WHERE e2.v  = a2.v -- another node which occurs in both the 'expected' and the 'actual' tables,\n" +
				"        AND e1.x  = e2.x -- where the node is in the same equivalence class in the 'expected' table\n" +
				"        AND a1.x != a2.x -- but in a different one in the 'actual' table\n" +
				"      LIMIT 1            -- finding a single counterexample is sufficient\n" +
				"    ) OR\n" +
				"    EXISTS (\n" +
				"      FROM expected e2, actual a2\n" +
				"      WHERE e2.v  = a2.v -- another node which occurs in both the 'expected' and the 'actual' tables,\n" +
				"        AND a1.x  = a2.x -- where the node is in the same equivalence class in the 'actual' table\n" +
				"        AND e1.x != e2.x -- but in a different one in the 'expected' table\n" +
				"      LIMIT 1            -- finding a single counterexample is sufficient\n" +
				"    )\n" +
				"  )\n" +
				"LIMIT 100;";
	}

}
