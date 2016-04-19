/*
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
package nl.tudelft.graphalytics.validation.rule;

import java.util.HashMap;
import java.util.Map;

/**
 * Validation rule which checks if vertex values are identical under equivalence.
 */
public class EquivalenceValidationRule implements ValidationRule<Long> {
	private Map<Long, Long> left2right;
	private Map<Long, Long> right2left;

	public EquivalenceValidationRule() {
		left2right = new HashMap<Long, Long>();
		right2left = new HashMap<Long, Long>();
	}

	@Override
	public Long parse(String val) {
		try {
			return Long.parseLong(val);
		} catch(NumberFormatException e) {
			return null;
		}
	}

	@Override
	public boolean match(Long left, Long right) {
		Long a = left2right.get(left);
		Long b = right2left.get(right);

		// If a and b are both null then we have not seen these labels
		// before. Add the labels to the equivalence maps.
		if (a == null && b == null) {
			left2right.put(left, right);
			right2left.put(right, left);
			return true;
		}

		return a != null && b != null & a.equals(b);
	}
}
