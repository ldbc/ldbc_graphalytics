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
package science.atlarge.graphalytics.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * EdgeListStream that reads edge data for an InputStream.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class EdgeListInputStreamReader implements EdgeListStream {

	private final BufferedReader reader;
	private final EdgeData cache = new EdgeData();
	private boolean cacheValid;

	public EdgeListInputStreamReader(InputStream inputStream) {
		 reader = new BufferedReader(new InputStreamReader(inputStream));
	}

	@Override
	public boolean hasNextEdge() throws IOException {
		if (cacheValid) {
			return true;
		} else {
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}

				String[] tokens = line.split(" ");

				if (tokens.length < 2) {
					throw new IOException("Malformed edge data in stream: \"" + line + "\".");
				}

				try {
					cache.setSourceId(Long.parseLong(tokens[0]));
					cache.setDestinationId(Long.parseLong(tokens[1]));
				} catch (NumberFormatException ex) {
					throw new IOException("Failed to parse vertex identifier from stream.", ex);
				}

				if (cache.getValues().length == tokens.length - 2) {
					System.arraycopy(tokens, 2, cache.getValues(), 0, tokens.length - 2);
				} else {
					cache.setValues(Arrays.copyOfRange(tokens, 2, tokens.length));
				}

				cacheValid = true;
				return true;
			}
			return false;
		}
	}

	@Override
	public EdgeData getNextEdge() throws IOException {
		if (!hasNextEdge()) {
			throw new NoSuchElementException();
		}

		cacheValid = false;
		return cache;
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

}
