package nl.tudelft.graphalytics.util.io;

import java.io.*;

/**
 * Writes an EdgeListStream to an OutputStream in the EVLP edge-list format.
 *
 * @author Tim Hegeman
 */
public class EdgeListStreamWriter implements AutoCloseable {

	private final EdgeListStream inputStream;
	private final Writer outputWriter;

	public EdgeListStreamWriter(EdgeListStream inputStream, OutputStream outputStream) {
		this.inputStream = inputStream;
		this.outputWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
	}

	public void writeAll() throws IOException {
		while (inputStream.hasNextEdge()) {
			writeNextEdge();
		}
	}

	private void writeNextEdge() throws IOException {
		EdgeListStream.EdgeData edgeData = inputStream.getNextEdge();
		outputWriter.write(String.valueOf(edgeData.getSourceId()));
		outputWriter.append(' ');
		outputWriter.write(String.valueOf(edgeData.getSourceId()));
		for (String value : edgeData.getValues()) {
			outputWriter.append(' ');
			outputWriter.write(value);
		}
		outputWriter.append('\n');
	}

	@Override
	public void close() throws IOException {
		outputWriter.close();
	}

}
