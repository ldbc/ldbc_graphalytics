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

import java.io.IOException;
import java.util.Arrays;

/**
 * Decorator for a VertexListStream that filters and rearranges vertex properties.
 *
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class VertexListPropertyFilter implements VertexListStream {

	private final VertexListStream inputStream;
	private final int[] propertyIndicesToKeep;
	private final VertexData cache = new VertexData();

	/**
	 * Construct a VertexListPropertyFilter that reads vertices from a VertexListStream, and filter and rearranges
	 * vertex properties before outputting the vertices. Properties are identified by their index (0 represents the first
	 * property in the underlying VertexListStream). Property indices may occur multiple times to duplicate a property.
	 *
	 * @param inputStream           the underlying VertexListStream to filter
	 * @param propertyIndicesToKeep a list of property indices to copy to the output
	 */
	public VertexListPropertyFilter(VertexListStream inputStream, int[] propertyIndicesToKeep) {
		this.inputStream = inputStream;
		this.propertyIndicesToKeep = Arrays.copyOf(propertyIndicesToKeep, propertyIndicesToKeep.length);
		this.cache.setValues(new String[propertyIndicesToKeep.length]);
	}

	@Override
	public boolean hasNextVertex() throws IOException {
		return inputStream.hasNextVertex();
	}

	@Override
	public VertexData getNextVertex() throws IOException {
		VertexData inputData = inputStream.getNextVertex();
		cache.setId(inputData.getId());

		String[] inputValues = inputData.getValues();
		String[] outputValues = cache.getValues();
		for (int i = 0; i < propertyIndicesToKeep.length; i++) {
			outputValues[i] = inputValues[propertyIndicesToKeep[i]];
		}

		return cache;
	}

	@Override
	public void close() throws IOException {
		inputStream.close();
	}
}
