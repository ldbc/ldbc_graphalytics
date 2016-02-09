package nl.tudelft.graphalytics.util.io;

import java.io.IOException;
import java.util.Arrays;

/**
 * Decorator for a EdgeListStream that filters and rearranges edge properties.
 *
 * @author Tim Hegeman
 */
public class EdgeListPropertyFilter implements EdgeListStream {

	private final EdgeListStream inputStream;
	private final int[] propertyIndicesToKeep;
	private final EdgeData cache = new EdgeData();

	/**
	 * Construct a EdgeListPropertyFilter that reads edges from a EdgeListStream, and filter and rearranges
	 * edge properties before outputting the edges. Properties are identified by their index (0 represents the first
	 * property in the underlying EdgeListStream). Property indices may occur multiple times to duplicate a property.
	 *
	 * @param inputStream           the underlying EdgeListStream to filter
	 * @param propertyIndicesToKeep a list of property indices to copy to the output
	 */
	public EdgeListPropertyFilter(EdgeListStream inputStream, int[] propertyIndicesToKeep) {
		this.inputStream = inputStream;
		this.propertyIndicesToKeep = Arrays.copyOf(propertyIndicesToKeep, propertyIndicesToKeep.length);
		this.cache.setValues(new String[propertyIndicesToKeep.length]);
	}

	@Override
	public boolean hasNextEdge() throws IOException {
		return inputStream.hasNextEdge();
	}

	@Override
	public EdgeData getNextEdge() throws IOException {
		EdgeData inputData = inputStream.getNextEdge();
		cache.setSourceId(inputData.getSourceId());
		cache.setDestinationId(inputData.getDestinationId());

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
