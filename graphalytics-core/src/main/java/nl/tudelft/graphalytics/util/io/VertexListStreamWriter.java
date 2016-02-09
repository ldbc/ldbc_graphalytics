package nl.tudelft.graphalytics.util.io;

import java.io.*;

/**
 * Writes a VertexListStream to an OutputStream in the EVLP vertex-list format.
 *
 * @author Tim Hegeman
 */
public class VertexListStreamWriter implements AutoCloseable {

	private final VertexListStream inputStream;
	private final Writer outputWriter;

	public VertexListStreamWriter(VertexListStream inputStream, OutputStream outputStream) {
		this.inputStream = inputStream;
		this.outputWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
	}

	public void writeAll() throws IOException {
		while (inputStream.hasNextVertex()) {
			writeNextVertex();
		}
	}

	private void writeNextVertex() throws IOException {
		VertexListStream.VertexData vertexData = inputStream.getNextVertex();
		outputWriter.write(String.valueOf(vertexData.getId()));
		for (String value : vertexData.getValues()) {
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
