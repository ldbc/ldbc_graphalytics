package nl.tudelft.graphalytics.reporting;

import java.io.IOException;

/**
 * A single page in a benchmark report. Defines a method that allows the page to be written to a file,
 * regardless of the implemented format (e.g., HTML, plain text, RDF).
 *
 * @author Tim Hegeman
 */
public interface BenchmarkReportPage {

	/**
	 * Writes this single benchmark report page to a file. This function creates directories when needed and
	 * overwrites any previously existing file of the same name.
	 *
	 * @param reportPath the base directory to which the benchmark report is written
	 * @throws IOException if an IO error occurred
	 */
	public void write(String reportPath) throws IOException;

}
