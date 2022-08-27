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
 * Validation rule which checks if two longs are identical.
 *
 * @author Stijn Heldens
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class MatchLongValidationRule implements ValidationRule<Long> {

	@Override
	public String getQuery() {
		return "SELECT expected.v AS v, expected.x AS expected, actual.x AS actual\n" +
				"FROM expected, actual\n" +
				"WHERE expected.v = actual.v\n" +
				"  AND expected.x != actual.x\n" +
				";";
	}

}
