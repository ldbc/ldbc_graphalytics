/*
 * Copyright 2015 Delft University of Technology
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
package nl.tudelft.graphalytics.reporting.html;

import nl.tudelft.graphalytics.domain.*;
import nl.tudelft.graphalytics.domain.benchmark.BenchmarkExperiment;
import nl.tudelft.graphalytics.domain.benchmark.BenchmarkJob;
import nl.tudelft.graphalytics.reporting.BenchmarkReport;
import nl.tudelft.graphalytics.reporting.BenchmarkReportFile;
import nl.tudelft.graphalytics.reporting.BenchmarkReportGenerator;
import nl.tudelft.graphalytics.reporting.json.JsonResultData;
import nl.tudelft.graphalytics.reporting.json.ResultData;
import nl.tudelft.graphalytics.util.json.JsonUtil;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.*;

/**
 * Utility class for generating an HTML-based BenchmarkReport from a BenchmarkSuiteResult.
 *
 * @author Wing Lung Ngai
 */
public class HtmlBenchmarkReportGenerator implements BenchmarkReportGenerator {

	private static final Logger LOG = LogManager.getLogger();

	public static final String SYSTEM_PROPERTIES_FILE = "system.properties";
	public static final String BENCHMARK_PROPERTIES_FILE = "benchmark.properties";

	public static final String REPORT_TYPE_IDENTIFIER = "html";


	private Map<String, String> pluginPageLinks;
	private Map<String, String> pluginMetrics;


	private final List<Plugin> plugins = new LinkedList<>();

	private static final String[] STATIC_RESOURCES = new String[]{
			// Bootstrap CSS and JS
			"report.htm",
//			"data/benchmark-results.js",
			"html/lib/css/visualizer.css",
			"html/lib/js/system-page.js",
			"html/lib/js/benchmark-page.js",
			"html/lib/js/result-page.js",
			"html/lib/js/loader.js",
			"html/lib/js/utility.js",
			"html/lib/external/bootstrap.min.js",
			"html/lib/external/bootstrap.min.css",
			"html/lib/external/underscore-min.js",
			"html/lib/external/bootstrap-table.min.js",
			"html/lib/external/underscore.string.min.js",
			"html/lib/external/bootstrap-table.min.css"
	};

	@Override
	public BenchmarkReport generateReportFromResults(BenchmarkSuiteResult result) {

		pluginPageLinks = new HashMap<>();
		//TODO add plugin code here.
		for (Plugin plugin : plugins) {
			plugin.preGenerate(this, result);
		}


		// Generate the report files
		Collection<BenchmarkReportFile> reportFiles = new LinkedList<>();
		// 1. Generate the resultData
		ResultData benchmarkData = parseResultEntries(result);


		String resultData =  "var data = " + JsonUtil.toPrettyJson(benchmarkData);
		reportFiles.add(new HtmlResultData(resultData, "html/data", "data"));
		reportFiles.add(new JsonResultData(JsonUtil.toPrettyJson(benchmarkData), "json", "results"));
		// 2. Copy the static resources
		for (String resource : STATIC_RESOURCES) {
			URL resourceUrl = HtmlBenchmarkReportGenerator.class.getResource("/graphalytics/reporting/" + resource);
			reportFiles.add(new StaticResource(resourceUrl, resource));
		}

		return new BenchmarkReport(REPORT_TYPE_IDENTIFIER, reportFiles);
	}


	private ResultData parseResultEntries(BenchmarkSuiteResult result) {
		ResultData resultData = new ResultData();

		parseSystemEntries(resultData);
		parseBenchmarkEntries(result, resultData);
		parseResultEntries(result, resultData);

		return resultData;
	}

	private void parseSystemEntries(ResultData result) {

		try {
			Configuration systemConf = new PropertiesConfiguration(SYSTEM_PROPERTIES_FILE);
			String platformName = systemConf.getString("system.platform.name");
			String platformAcronym = systemConf.getString("system.platform.acronym");
			String platformVersion = systemConf.getString("system.platform.version");
			String platformLink = systemConf.getString("system.platform.link");
			result.system.addPlatform(platformName, platformAcronym, platformVersion, platformLink);

			String envName = systemConf.getString("system.environment.name");
			String envAcronym = systemConf.getString("system.environment.acronym");
			String envVersion = systemConf.getString("system.environment.version");
			String envLink = systemConf.getString("system.environment.link");
			result.system.addEnvironment(envName, envAcronym, envVersion, envLink);


			String machineQuantity = systemConf.getString("system.environment.machine.quantity");
			String machineCpu = systemConf.getString("system.environment.machine.cpu");
			String machineMemory = systemConf.getString("system.environment.machine.memory");
			String machineNetwork = systemConf.getString("system.environment.machine.network");
			String machineStorage = systemConf.getString("system.environment.machine.storage");

			result.system.addMachine(machineQuantity, machineCpu, machineMemory, machineNetwork, machineStorage);

			String tools[] = systemConf.getStringArray("system.tool");

			for (String tool : tools) {
				String toolName = tool;
				String toolVersion = systemConf.getString("system.tool." + toolName + ".version");
				String toolLink = systemConf.getString("system.tool." + toolName + ".link");
				result.system.addTool(toolName, toolVersion, toolLink);
			}

		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	private void parseBenchmarkEntries(BenchmarkSuiteResult benchmarkSuiteResult, ResultData resultData) {
		try {
			Configuration benchmarkConf = new PropertiesConfiguration(BENCHMARK_PROPERTIES_FILE);

			String targetScale = benchmarkConf.getString("benchmark.target-scale");
			resultData.benchmark.addTargetScale(targetScale);
			String name = benchmarkConf.getString("benchmark.name");
			resultData.benchmark.addName(name);
			String type = benchmarkConf.getString("benchmark.type");
			resultData.benchmark.addType(type);
			String duration = String.valueOf(benchmarkSuiteResult.getTotalDuration());
			resultData.benchmark.addDuration(duration);

			String outputRequired = benchmarkConf.getString("benchmark.run.output-required");
			String outputDirectory = benchmarkConf.getString("benchmark.run.output-directory");
			resultData.benchmark.addOutput(outputRequired, outputDirectory);

			String validationRequired = benchmarkConf.getString("benchmark.run.validation-required");
			String validationDirectory = benchmarkConf.getString("benchmark.run.validation-directory");
			resultData.benchmark.addValidation(validationRequired, validationDirectory);

			String resources[] = benchmarkConf.getStringArray("benchmark.resources");
			for (String resource : resources) {
				String resName = resource;
				String[] resProperties = benchmarkConf.getStringArray("benchmark.resources." + resName);

				String resBaseline = resProperties[0];
				String resScalability = resProperties[2];
				resultData.benchmark.addResource(resName, resBaseline, resScalability);
			}

		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	private void parseResultEntries(BenchmarkSuiteResult benchmarkSuiteResult, ResultData resultData) {

		for (BenchmarkExperiment experiment : benchmarkSuiteResult.getBenchmarkSuite().getExperiments()) {
			List<String> jobIds = new ArrayList<>();
			for (BenchmarkJob job : experiment.getJobs()) {
				jobIds.add(job.getId());
			}
			resultData.result.addExperiments(experiment.getId(), experiment.getType(), jobIds);
		}

		for (BenchmarkJob job : benchmarkSuiteResult.getBenchmarkSuite().getJobs()) {
			List<String> runIds = new ArrayList<>();
			for (Benchmark benchmark : job.getBenchmarks()) {
				runIds.add(benchmark.getId());
			}
			resultData.result.addJob(job.getId(),
					job.getAlgorithm().getAcronym(),job.getGraphSet().getName(),
					String.valueOf(job.getResourceSize()), String.valueOf(job.getRepetition()), runIds);

		}

		for (BenchmarkResult benchmarkResult : benchmarkSuiteResult.getBenchmarkResults()) {

			String id = benchmarkResult.getBenchmark().getId();
			long timestamp = benchmarkResult.getStartOfBenchmark().getTime();
			String success = String.valueOf(benchmarkResult.isSuccessful());
			long makespan =  benchmarkResult.getEndOfBenchmark().getTime() - benchmarkResult.getStartOfBenchmark().getTime();
			String processingTime = "-1";
			try {
				processingTime = String.valueOf(benchmarkResult.getMetrics().getProcessingTime());
			} catch (Exception e) {
				LOG.error(String.format("Processing time not found for benhmark %s.", id));
			}
			if(timestamp == 0) {
				LOG.info(String.format("Illegal state for benchmark %s, no timestamp", id));
			} else {
				LOG.info(String.format("Current state benchmark %s, with timestamp", id));
			}

			resultData.result.addRun(id, String.valueOf(timestamp), success, String.valueOf(makespan), processingTime, pluginPageLinks.get(id));

		}
	}


	public void registerPageLink(String runId, String pageLink) {
		pluginPageLinks.put(runId, pageLink);
	}


	/**
	 * Adds a plugin instance to the list of plugins that will receive callbacks throughout the generation process.
	 *
	 * @param plugin the plugin instance to add
	 */
	public void registerPlugin(HtmlBenchmarkReportGenerator.Plugin plugin) {
		plugins.add(plugin);
	}


	/**
	 * Callback interface for plugins to inject custom HTML pages and resources into the benchmark report.
	 */
	public interface Plugin {

		/**
		 * Callback before generation of the default Graphalytics benchmark report starts.
		 *
		 * @param generator the benchmark report generator instance
		 * @param result    the results of running a benchmark suite
		 */
		void preGenerate(HtmlBenchmarkReportGenerator generator, BenchmarkSuiteResult result);


	}


}
