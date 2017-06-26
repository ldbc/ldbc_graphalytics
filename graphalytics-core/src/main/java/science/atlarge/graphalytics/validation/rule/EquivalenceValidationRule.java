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

import java.util.HashMap;
import java.util.Map;

/**
 * Validation rule which checks if vertex values are identical under equivalence.
 *
 * @author Stijn Heldens
 * @author Wing Lung Ngai
 */
public class EquivalenceValidationRule implements ValidationRule<Long> {
	private Map<Long, Long> leftMap;
	private Map<Long, Long> rightMap;
	private long counter;

	public EquivalenceValidationRule() {
		leftMap = new HashMap<Long, Long>();
		rightMap = new HashMap<Long, Long>();
		counter = 0;
	}

	@Override
	public Long parse(String val) throws NumberFormatException {
		return Long.parseLong(val);
	}

	@Override
	public boolean match(Long left, Long right) {
		Long a = leftMap.get(left);
		Long b = rightMap.get(right);

		// If a and b are both null then we have not seen these labels
		// before. Unify the labels by mapping them to the same value.
		if (a == null && b == null) {
			leftMap.put(left, counter);
			rightMap.put(right, counter);
			counter++;
			return true;
		}

		return a != null && b != null & a.equals(b);
	}
}
