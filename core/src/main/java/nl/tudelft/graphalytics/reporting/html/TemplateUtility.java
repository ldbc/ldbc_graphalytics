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
package nl.tudelft.graphalytics.reporting.html;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.FieldPosition;

/**
 * Utility class for use in the HTML report templates.
 *
 * @author Tim Hegeman
 */
public class TemplateUtility {

	/**
	 * Formats an integer value into a condensed human readable format using 'K' (thousands), 'M' (millions), or 'B'
	 * (billions) postfixes. Values are rounded to two decimal places.
	 *
	 * @param value the value to format
	 * @return a human-readable string representation of the value
	 */
	public String formatIntegerHumanReadable(long value) {
		final long THOUSAND = 1000;
		final long MILLION = THOUSAND * THOUSAND;
		final long BILLION = THOUSAND * MILLION;

		StringBuffer sb = new StringBuffer();

		// Insert minus sign if needed
		if (value < 0) {
			sb.append("-");
			value = -value;
		}

		// Check for billions/millions/thousands
		double scaledValue = value;
		String humanReadableScale = "";
		if (value >= BILLION) {
			scaledValue /= BILLION;
			humanReadableScale = "B";
		} else if (value >= MILLION) {
			scaledValue /= MILLION;
			humanReadableScale = "M";
		} else if (value >= THOUSAND) {
			scaledValue /= THOUSAND;
			humanReadableScale = "K";
		}

		// Round the scaled value
		DecimalFormat format = new DecimalFormat("#.##");
		format.setRoundingMode(RoundingMode.HALF_UP);
		format.format(scaledValue, sb, new FieldPosition(DecimalFormat.INTEGER_FIELD));

		sb.append(humanReadableScale);
		return sb.toString();
	}

}
