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

/**
 * Validation rule which checks whether the output value equal to the reference value within
 * an error margin of x%.
 */
public class EpsilonValidationRule implements ValidationRule<Double> {

	private final static double COMPARISON_THRESHOLD = 0.0001;

	@Override
	public Double parse(String val) {
		try {
			return Double.parseDouble(val);
		} catch(NumberFormatException e) {
			return null;
		}
	}

	@Override
	public boolean match(Double outputValue, Double correctValue) {
		try {
			double a = outputValue;
			double b = correctValue;

			if (Double.isNaN(a) && Double.isNaN(b)) {
				return true;
			} else if (Math.abs(a - b) < COMPARISON_THRESHOLD * b) {
				return true;
			}

			return false;
		} catch(NumberFormatException e) {
			return false;
		}
	}
}
