package nl.tudelft.graphalytics.granula;

import nl.tudelft.graphalytics.domain.Benchmark;
import nl.tudelft.graphalytics.domain.BenchmarkResult;
import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult;
import nl.tudelft.graphalytics.reporting.BenchmarkReportFile;
import nl.tudelft.graphalytics.reporting.html.HtmlBenchmarkReportGenerator;
import nl.tudelft.graphalytics.reporting.html.StaticResource;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by tim on 12/17/15.
 */
public class GranulaHtmlGenerator implements HtmlBenchmarkReportGenerator.Plugin {

	private static final String STATIC_RESOURCES[] = new String[] {
			"lib/granula-visualizer/visualizer.htm",
			"lib/granula-visualizer/lib/vkbeautify/vkbeautify.js",
			"lib/granula-visualizer/lib/bootstrap/js/bootstrap.js",
			"lib/granula-visualizer/lib/bootstrap/css/carousel.css",
			"lib/granula-visualizer/lib/bootstrap/css/bootstrap.css",
			"lib/granula-visualizer/lib/bootstrap/fonts/glyphicons-halflings-regular.ttf",
			"lib/granula-visualizer/lib/bootstrap/fonts/glyphicons-halflings-regular.woff",
			"lib/granula-visualizer/lib/bootstrap/fonts/glyphicons-halflings-regular.eot",
			"lib/granula-visualizer/lib/bootstrap/fonts/glyphicons-halflings-regular.woff2",
			"lib/granula-visualizer/lib/bootstrap/fonts/glyphicons-halflings-regular.svg",
			"lib/granula-visualizer/lib/jquery-2.1.3.min.js",
			"lib/granula-visualizer/lib/snap.svg-min.js",
			"lib/granula-visualizer/lib/underscore.string.min.js",
			"lib/granula-visualizer/lib/prettify/prettify.css",
			"lib/granula-visualizer/lib/prettify/prettify.js",
			"lib/granula-visualizer/lib/underscore-min.js",
			"lib/granula-visualizer/visualizer/board.js",
			"lib/granula-visualizer/visualizer/modal.js",
			"lib/granula-visualizer/visualizer/visualizer.css",
			"lib/granula-visualizer/visualizer/utility.js",
			"lib/granula-visualizer/visualizer/model.js",
			"lib/granula-visualizer/visualizer/draw.js",
			"lib/granula-visualizer/visualizer/settings.js",
			"lib/granula-visualizer/visualizer/data.js",
			"lib/granula-visualizer/visualizer/visualizer.js",
			"lib/granula-visualizer/plugin/plugin.js"
	};

	@Override
	public void preGenerate(HtmlBenchmarkReportGenerator htmlBenchmarkReportGenerator, BenchmarkSuiteResult result) {
		for (BenchmarkResult benchmarkResult : result.getBenchmarkResults()) {
			if (benchmarkResult.isCompletedSuccessfully()) {
				htmlBenchmarkReportGenerator.registerPageLink(benchmarkResult.getBenchmark(),
						"lib/granula-visualizer/visualizer.htm?arc=../../data/archive/" +
								benchmarkResult.getBenchmark().getBenchmarkIdentificationString() + ".xml");
			}
		}
	}

	@Override
	public Collection<BenchmarkReportFile> generateAdditionalReportFiles(
			HtmlBenchmarkReportGenerator htmlBenchmarkReportGenerator, BenchmarkSuiteResult benchmarkSuiteResult) {
		List<BenchmarkReportFile> additionalFiles = new ArrayList<>(STATIC_RESOURCES.length);
		for (String resource : STATIC_RESOURCES) {
			URL resourceUrl = HtmlBenchmarkReportGenerator.class.getResource("/granula/reporting/html/" + resource);
			additionalFiles.add(new StaticResource(resourceUrl, resource));
		}
		return additionalFiles;
	}

}
