package nl.tudelft.graphalytics.mapreducev2;

import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

import java.io.File;

/**
 * @author Tim Hegeman
 */
public class HadoopTestFolders extends ExternalResource {

	private TemporaryFolder temporaryFolder = new TemporaryFolder();
	private File rawInputDirectory;
	private File inputDirectory;
	private File intermediateDirectory;
	private File outputDirectory;

	@Override
	protected void before() throws Throwable {
		temporaryFolder.create();

		rawInputDirectory = new File(temporaryFolder.getRoot(), "raw-input");
		inputDirectory = new File(temporaryFolder.getRoot(), "input");
		intermediateDirectory = new File(temporaryFolder.getRoot(), "intermediate");
		outputDirectory = new File(temporaryFolder.getRoot(), "output");
	}

	@Override
	protected void after() {
		temporaryFolder.delete();
	}

	public File getRawInputDirectory() {
		return rawInputDirectory;
	}

	public File getInputDirectory() {
		return inputDirectory;
	}

	public File getIntermediateDirectory() {
		return intermediateDirectory;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

}
