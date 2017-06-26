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
 * VertexListStream that reads vertex data for an InputStream.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class VertexListInputStreamReader implements VertexListStream {

	private final BufferedReader reader;
	private final VertexData cache = new VertexData();
	private boolean cacheValid;

	public VertexListInputStreamReader(InputStream inputStream) {
		 reader = new BufferedReader(new InputStreamReader(inputStream));
	}

	@Override
	public boolean hasNextVertex() throws IOException {
		if (cacheValid) {
			return true;
		} else {
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}

				String[] tokens = line.split(" ");

				try {
					cache.setId(Long.parseLong(tokens[0]));
				} catch (NumberFormatException ex) {
					throw new IOException("Failed to parse vertex identifier from stream.", ex);
				}

				if (cache.getValues().length == tokens.length - 1) {
					System.arraycopy(tokens, 1, cache.getValues(), 0, tokens.length - 1);
				} else {
					cache.setValues(Arrays.copyOfRange(tokens, 1, tokens.length));
				}

				cacheValid = true;
				return true;
			}
			return false;
		}
	}

	@Override
	public VertexData getNextVertex() throws IOException {
		if (!hasNextVertex()) {
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
