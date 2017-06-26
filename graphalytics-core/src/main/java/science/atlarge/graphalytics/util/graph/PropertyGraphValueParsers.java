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
package science.atlarge.graphalytics.util.graph;

import science.atlarge.graphalytics.util.graph.PropertyGraphParser.ValueParser;

import java.io.IOException;
import java.util.Arrays;

/**
 * Collections of commonly used PropertyGraph.ValueParser implementations.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class PropertyGraphValueParsers {

	/**
	 * Creates a parser that returns the value null with type Void, regardless of the input.
	 *
	 * @return a ValueParser for type Void
	 */
	public static ValueParser<Void> voidParser() {
		return new ValueParser<Void>() {
			@Override
			public Void parse(String[] valueTokens) throws IOException {
				return null;
			}
		};
	}

	/**
	 * Creates a parser that expects exactly one property and interprets it as a Long value.
	 * 
	 * @return a ValueParser for type Long
	 */
	public static ValueParser<Long> longParser() {
		return new ValueParser<Long>() {
			@Override
			public Long parse(String[] valueTokens) throws IOException {
				if (valueTokens.length != 1) {
					throw new IOException("Expected single property with value of type Long.");
				}

				try {
					return Long.parseLong(valueTokens[0]);
				} catch (NumberFormatException ex) {
					throw new IOException("Expected single property with value of type Long.", ex);
				}
			}
		};
	}

	/**
	 * Creates a parser that expects exactly one property and interprets it as a Double value.
	 *
	 * @return a ValueParser for type Double
	 */
	public static ValueParser<Double> doubleParser() {
		return new ValueParser<Double>() {
			@Override
			public Double parse(String[] valueTokens) throws IOException {
				if (valueTokens.length != 1) {
					throw new IOException("Expected single property with value of type Double.");
				}

				try {
					return Double.parseDouble(valueTokens[0]);
				} catch (NumberFormatException ex) {
					throw new IOException("Expected single property with value of type Double.", ex);
				}
			}
		};
	}

	/**
	 * Creates a parser that returns a copy of the input properties without any interpretation of the values.
	 *
	 * @return a ValueParser that returns a copy of the input
	 */
	public static ValueParser<String[]> identityParser() {
		return new ValueParser<String[]>() {
			@Override
			public String[] parse(String[] valueTokens) throws IOException {
				return Arrays.copyOf(valueTokens, valueTokens.length);
			}
		};
	}

}
