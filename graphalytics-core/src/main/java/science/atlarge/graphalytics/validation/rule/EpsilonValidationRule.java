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
 * Validation rule which checks whether the output value equal to the reference value within
 * an error margin of x%.
 *
 * @author Stijn Heldens
 * @author Wing Lung Ngai
 */
public class EpsilonValidationRule implements ValidationRule<Double> {

	// The platform output should be within this margin of the reference output (i.e., |a-b|/b < threshold)
	private final static double COMPARISON_THRESHOLD = 0.0001;

	// Maximum value for when a double is considered to be zero-like (10 ULPs away from 0.0)
	private final static double MAX_NEAR_ZERO = Double.MIN_VALUE * 10;

	// Minimum value for when a double is considered to be infinity-like (within 1% of MAX_VALUE)
	private final static double MIN_NEAR_INFINITY = Double.MAX_VALUE * 0.999;


	@Override
	public Double parse(String val) throws NumberFormatException {

		// According to the specifications for Java SE7, Double.parseDouble(String) will
		// only consider the string "Infinity" (optionally prefixed with "+" or "-") to
		// be a infinity value. Graphalytics will be more liberal in what is accepted.
		String low = val.toLowerCase();

		if (low.equals("inf") || low.equals("+inf") || low.equals("infinity") || low.equals("+infinity")) {
			return Double.POSITIVE_INFINITY;
		} else if (low.equals("-inf") || low.equals("-infinity")) {
			return Double.NEGATIVE_INFINITY;
		}

		return Double.parseDouble(val);
	}

	private static boolean isNearInfinity(double v) {
		return Double.isInfinite(v) || Math.abs(v) > MIN_NEAR_INFINITY;
	}

	private static boolean isNearZero(double v) {
		return Math.abs(v) < MAX_NEAR_ZERO;
	}

	@Override
	public boolean match(Double outputValue, Double correctValue) {

		double a = outputValue;
		double b = correctValue;

		// Check if a and b are identical
		if (a == b) {
			return true;
		}

		// Check if a and b are both NaN
		else if (Double.isNaN(a) && Double.isNaN(b)) {
			return true;
		}

		// Check if a and b are both infinity-like
		else if (isNearInfinity(a) && isNearInfinity(b)) {
			return Math.signum(a) == Math.signum(b); //Signs match?
		}

		// Check if a and b are both zero-like
		else if (isNearZero(a) && isNearZero(b)) {
			return true;
		}

		// Use Graphalytics rule: |a-b|/b < THRESHOLD
		else {
			return Math.abs(a - b) < COMPARISON_THRESHOLD * b;
		}
	}
}
