package nl.tudelft.graphalytics.reporting.html;

import nl.tudelft.graphalytics.reporting.BenchmarkReport;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * An HTML-based report for presenting the benchmark results of the Graphalytics benchmark suite.
 *
 * @author Tim Hegeman
 */
public class HtmlBenchmarkReport extends BenchmarkReport {

	private String templateDirectory;

	/**
	 * @param pages             a collection of pages that define the contents of the benchmark report
	 * @param templateDirectory the directory containing the HTML report template
	 */
	public HtmlBenchmarkReport(Collection<HtmlBenchmarkReportPage> pages,
	                           String templateDirectory) {
		super(pages);
		this.templateDirectory = templateDirectory;
	}

	@Override
	public void write(String path) throws IOException {
		// Write the individual report pages
		super.write(path);

		// Copy CSS and JavaScript from the template folder to the report folder
		FileUtils.copyDirectoryToDirectory(Paths.get(templateDirectory, "bootstrap").toFile(),
				Paths.get(path).toFile());
		FileUtils.copyFileToDirectory(Paths.get(templateDirectory, "report.css").toFile(),
				Paths.get(path).toFile());
	}
}
