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
package science.atlarge.graphalytics.report.html;

import science.atlarge.graphalytics.configuration.BuildInformation;
import science.atlarge.graphalytics.configuration.ConfigurationUtil;
import science.atlarge.graphalytics.configuration.InvalidConfigurationException;
import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;
import science.atlarge.graphalytics.report.BenchmarkReport;
import science.atlarge.graphalytics.report.BenchmarkReportFile;
import science.atlarge.graphalytics.report.BenchmarkReportGenerator;
import science.atlarge.graphalytics.report.json.JsonResultData;
import science.atlarge.graphalytics.report.json.ResultData;
import science.atlarge.graphalytics.report.result.BenchmarkMetric;
import science.atlarge.graphalytics.report.result.BenchmarkResult;
import science.atlarge.graphalytics.report.result.BenchmarkRunResult;
import science.atlarge.graphalytics.util.JsonUtil;
import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.domain.benchmark.BenchmarkExp;
import science.atlarge.graphalytics.domain.benchmark.BenchmarkJob;
import science.atlarge.graphalytics.util.TimeUtil;

import java.math.BigDecimal;
import java.net.URL;
import java.util.*;

/**
 * Utility class for generating an HTML-based BenchmarkReport from a BenchmarkResult.
 *
 * @author Mihai Capotă
 * @author Tim Hegeman
 * @author Wing Lung Ngai
 */
public class HtmlBenchmarkReportGenerator implements BenchmarkReportGenerator {

	private static final Logger LOG = LogManager.getLogger();
	public static final String PLATFORM_PROPERTIES_FILE = "platform.properties";
	public static final String ENVIRONMENT_PROPERTIES_FILE = "environment.properties";
	public static final String DRIVER_PROPERTIES_FILE = "driver.properties";
	public static final String PRICING_PROPERTIES_FILE = "pricing.properties";
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
	public BenchmarkReport generateReportFromResults(BenchmarkResult result) {

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
			URL resourceUrl = HtmlBenchmarkReportGenerator.class.getResource("/graphalytics/report/" + resource);
			reportFiles.add(new StaticResource(resourceUrl, resource));
		}

		return new BenchmarkReport(REPORT_TYPE_IDENTIFIER, reportFiles);
	}


	private ResultData parseResultEntries(BenchmarkResult result) {
		ResultData resultData = new ResultData();


		Configuration benchmarkConf = ConfigurationUtil.loadConfiguration(BENCHMARK_PROPERTIES_FILE);

		String description = benchmarkConf.getString("benchmark.description");
		resultData.setDescription(description);



		parseSystemEntries(resultData);
		parseBenchmarkEntries(result, resultData);
		parseResultEntries(result, resultData);

		return resultData;
	}

	private void parseSystemEntries(ResultData result) {

		try {
			Configuration platformConf = ConfigurationUtil.loadConfiguration(PLATFORM_PROPERTIES_FILE);
			Configuration envConf = ConfigurationUtil.loadConfiguration(ENVIRONMENT_PROPERTIES_FILE);
			Configuration pricingConf = ConfigurationUtil.loadConfiguration(PRICING_PROPERTIES_FILE);

			result.system.addPricing(pricingConf.getString("system.pricing"));

			String platformName = platformConf.getString("platform.name");
			String platformAcronym = platformConf.getString("platform.acronym");
			String platformVersion = platformConf.getString("platform.version");
			String platformLink = platformConf.getString("platform.link");
			result.system.addPlatform(platformName, platformAcronym, platformVersion, platformLink);

			String envName = envConf.getString("environment.name");
			String envAcronym = envConf.getString("environment.acronym");
			String envVersion = envConf.getString("environment.version");
			String envLink = envConf.getString("environment.link");
			result.system.addEnvironment(envName, envAcronym, envVersion, envLink);


			String machineQuantity = envConf.getString("environment.machine.quantity");
			String machineCpu = envConf.getString("environment.machine.cpu");
			String machineMemory = envConf.getString("environment.machine.memory");
			String machineNetwork = envConf.getString("environment.machine.network");
			String machineStorage = envConf.getString("environment.machine.storage");

			result.system.addMachine(machineQuantity, machineCpu, machineMemory, machineNetwork, machineStorage);


			String buildInfoFile;
			buildInfoFile = "/project/build/graphalytics-core.properties";
			try {
				Properties properties = BuildInformation.loadBuildPropertiesFile(buildInfoFile);

				String name = properties.getProperty("build.graphalytics-core.name");
				String version = properties.getProperty("build.graphalytics-core.version");
				String link = properties.getProperty("build.graphalytics-core.link");

				result.system.addTool(name, version, link);
			} catch (Exception e) {
				LOG.error(String.format("Failed to load versioning information from %s.", buildInfoFile));
			}

			buildInfoFile = "/project/build/platform.properties";
			try {
				Properties properties = BuildInformation.loadBuildPropertiesFile(buildInfoFile);

				String name = properties.getProperty("build.platform.name");
				String version = properties.getProperty("build.platform.version");
				String link = properties.getProperty("build.platform.link");

				result.system.addTool(name, version, link);
			} catch (Exception e) {
				LOG.error(String.format("Failed to load versioning information from %s.", buildInfoFile));
			}

		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	private void parseBenchmarkEntries(BenchmarkResult benchmarkResult, ResultData resultData) {
		try {
			Configuration benchmarkConf = ConfigurationUtil.loadConfiguration(BENCHMARK_PROPERTIES_FILE);

			String targetScale = benchmarkConf.getString("benchmark.standard.target-scale");
			resultData.benchmark.addTargetScale(targetScale);
			String name = benchmarkConf.getString("benchmark.name");
			resultData.benchmark.addName(name);
			String type = benchmarkConf.getString("benchmark.type");
			resultData.benchmark.addType(type);
			String duration = String.valueOf(benchmarkResult.getTotalDuration());
			resultData.benchmark.addDuration(duration);
			String timeout = String.valueOf(benchmarkResult.getBenchmark().getTimeout());
			resultData.benchmark.addTimeout(timeout);

			String outputRequired = "-";
			String outputDirectory = benchmarkConf.getString("graphs.output-directory");
			resultData.benchmark.addOutput(outputRequired, outputDirectory);

			String validationRequired = "-";
			String validationDirectory = benchmarkConf.getString("graphs.validation-directory");
			resultData.benchmark.addValidation(validationRequired, validationDirectory);

//			String resources[] = benchmarkConf.getStringArray("benchmark.resources");
//			for (String resource : resources) {
//				String resName = resource;
//				String[] resProperties = benchmarkConf.getStringArray("benchmark.resources." + resName);
//
//				String resBaseline = resProperties[0];
//				String resScalability = resProperties[2];
//				resultData.benchmark.addResource(resName, resBaseline, resScalability);
//			}

			Map<String, String> benchmarkConfs = getAllBenchmarkConfigurations();
			for (String key : benchmarkConfs.keySet()) {
				resultData.benchmark.addConfiguration(key, benchmarkConfs.get(key));
			}


		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	private void parseResultEntries(BenchmarkResult benchmarkResult, ResultData resultData) {

		for (BenchmarkExp experiment : benchmarkResult.getBenchmark().getExperiments()) {
			List<String> jobIds = new ArrayList<>();
			for (BenchmarkJob job : experiment.getJobs()) {
				jobIds.add(job.getId());
			}
			resultData.result.addExperiments(experiment.getId(), experiment.getType(), jobIds);
		}

		for (BenchmarkJob job : benchmarkResult.getBenchmark().getJobs()) {
			List<String> runIds = new ArrayList<>();
			for (BenchmarkRun benchmarkRun : job.getBenchmarkRuns()) {
				runIds.add(benchmarkRun.getId());
			}
			resultData.result.addJob(job.getId(),
					job.getAlgorithm().getAcronym(),job.getGraph().getName(),
					String.valueOf(job.getResourceSize()), String.valueOf(job.getRepetition()), runIds);

		}

		for (BenchmarkRunResult benchmarkRunResult : benchmarkResult.getBenchmarkRunResults()) {

			String id = benchmarkRunResult.getBenchmarkRun().getId();
			long timestamp = benchmarkRunResult.getStatus().getStartOfBenchmark().getTime();
			String success = String.valueOf(benchmarkRunResult.isSuccessful());
			BenchmarkMetric loadTime = benchmarkRunResult.getMetrics().getLoadTime();
			BenchmarkMetric makespan =  benchmarkRunResult.getMetrics().getMakespan();
			BenchmarkMetric processingTime = benchmarkRunResult.getMetrics().getProcessingTime();

			resultData.result.addRun(id, String.valueOf(timestamp), success,
					String.valueOf(loadTime), String.valueOf(makespan), String.valueOf(processingTime), pluginPageLinks.get(id));

		}
	}

	public void printOverview(BenchmarkResult benchmarkResult) {
		LOG.info("Reporting benchmark summary:");
		List<BenchmarkRunResult> resultList = new ArrayList(benchmarkResult.getBenchmarkRunResults());
		Collections.sort(resultList, new Comparator<BenchmarkRunResult>() {
			@Override
			public int compare(BenchmarkRunResult r1, BenchmarkRunResult r2) {
				if( r1.getBenchmarkRun().getAlgorithm().hashCode() > r2.getBenchmarkRun().getAlgorithm().hashCode()) {
					return -1;
				} else if( r1.getBenchmarkRun().getAlgorithm().hashCode() < r2.getBenchmarkRun().getAlgorithm().hashCode()) {
					return 1;
				} else {
					if(r1.getBenchmarkRun().getGraph().getName().hashCode() > r2.getBenchmarkRun().getGraph().getName().hashCode()) {
						return -1;
					} else if(r1.getBenchmarkRun().getGraph().getName().hashCode() < r2.getBenchmarkRun().getGraph().getName().hashCode()) {
						return 1;
					} else {
						return 0;
					}
				}
			}
		});

		Integer totalResult= 0;
		Integer successfulResult = 0;
		for (BenchmarkRunResult benchmarkRunResult : resultList) {

			BenchmarkRun benchmarkRun = benchmarkRunResult.getBenchmarkRun();
			BenchmarkMetric loadTime = benchmarkRunResult.getMetrics().getLoadTime();
			BenchmarkMetric makespan = benchmarkRunResult.getMetrics().getMakespan();
			BenchmarkMetric procTime = benchmarkRunResult.getMetrics().getProcessingTime();


			LOG.info(String.format("[%s] => %s, T_l=%s, T_m=%s, T_p=%s.",
					benchmarkRun,
					benchmarkRunResult.isSuccessful() ?
							"succeed" : "failed (" + benchmarkRunResult.getFailures() +")",
					!loadTime.isNan() ? loadTime + loadTime.getUnit() : loadTime,
					!makespan.isNan() ? makespan + makespan.getUnit() : makespan,
					!procTime.isNan() ? procTime + procTime.getUnit() : procTime));

			totalResult++;
			if(benchmarkRunResult.isSuccessful()) {
				successfulResult++;
			}
		}
		LOG.info(String.format("In total, [%s / %s] benchmark(s) succeed.", successfulResult, totalResult));
	}

	private Map<String, String> getAllBenchmarkConfigurations() {
		Map<String, String> confs = new HashMap<>();

		Set<String> keysWithStringArray = new HashSet<>();
		keysWithStringArray.add("graphs.names");

		Configuration benchmarkConf = ConfigurationUtil.loadConfiguration(BENCHMARK_PROPERTIES_FILE);

		Iterator<String> keys = benchmarkConf.getKeys();
		while(keys.hasNext()) {
			String key = keys.next();

			if(keysWithStringArray.contains(key)) {
				String[] valueArray = benchmarkConf.getStringArray(key);
				String summarizedValue = Arrays.asList(valueArray).toString();
				confs.put(key, summarizedValue);
			} else {
				confs.put(key, benchmarkConf.getString(key));
			}
		}
		return confs;
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
		void preGenerate(HtmlBenchmarkReportGenerator generator, BenchmarkResult result);


	}


}
