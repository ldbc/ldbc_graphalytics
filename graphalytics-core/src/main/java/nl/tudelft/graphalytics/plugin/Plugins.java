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
package nl.tudelft.graphalytics.plugin;

import nl.tudelft.graphalytics.Platform;
import nl.tudelft.graphalytics.domain.benchmark.Benchmark;
import nl.tudelft.graphalytics.domain.benchmark.BenchmarkRun;
import nl.tudelft.graphalytics.report.result.BenchmarkResult;
import nl.tudelft.graphalytics.report.result.BenchmarkSuiteResult;
import nl.tudelft.graphalytics.report.BenchmarkReportGenerator;
import nl.tudelft.graphalytics.report.BenchmarkReportWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * @author Tim Hegeman
 */
public class Plugins implements Iterable<Plugin> {

	private static Logger LOG = LogManager.getLogger();

	private final List<Plugin> plugins;

	public Plugins() {
		plugins = new ArrayList<>();
	}

	public void addPlugin(Plugin plugin) {
		plugins.add(plugin);
	}

	public void preBenchmarkSuite(Benchmark benchmark) {
		for (Plugin plugin : plugins) {
			plugin.preBenchmarkSuite(benchmark);
		}
	}

	public void preBenchmark(BenchmarkRun nextBenchmarkRun) {
		for (Plugin plugin : plugins) {
			plugin.preBenchmark(nextBenchmarkRun);
		}
	}

	public void postBenchmark(BenchmarkRun completedBenchmarkRun, BenchmarkResult benchmarkResult) {
		for (Plugin plugin : plugins) {
			plugin.postBenchmark(completedBenchmarkRun, benchmarkResult);
		}
	}

	public void postBenchmarkSuite(Benchmark benchmark, BenchmarkSuiteResult benchmarkSuiteResult) {
		for (Plugin plugin : plugins) {
			plugin.postBenchmarkSuite(benchmark, benchmarkSuiteResult);
		}
	}

	public void shutdown() {
		for (Plugin plugin : plugins) {
			plugin.shutdown();
		}
	}

	public void preReportGeneration(BenchmarkReportGenerator reportGenerator) {
		for (Plugin plugin : plugins) {
			plugin.preReportGeneration(reportGenerator);
		}
	}

	@Override
	public Iterator<Plugin> iterator() {
		return plugins.iterator();
	}

	public static Plugins discoverPluginsOnClasspath(Platform targetPlatform, Benchmark benchmark, BenchmarkReportWriter reportWriter) {
		Plugins plugins = new Plugins();

		try {
			Enumeration<URL> resources = Plugins.class.getClassLoader().getResources("META-INF/graphalytics/plugins");
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				Plugin pluginInstance = instantiatePluginFromResource(resource, targetPlatform, benchmark, reportWriter);
				if (pluginInstance != null) {
					LOG.info("Loaded \"{}\" plugin:", pluginInstance.getPluginName());
					LOG.info("\t{}", pluginInstance.getPluginDescription());
					plugins.addPlugin(pluginInstance);
				}
			}
		} catch (IOException e) {
			LOG.error("Failed to enumerate classpath resources while loading plugins.");
		}

		return plugins;
	}

	private static Plugin instantiatePluginFromResource(URL pluginSpecificationResource, Platform targetPlatform,
                                                        Benchmark benchmark, BenchmarkReportWriter reportWriter) {
		try (Scanner pluginFileScanner = new Scanner(pluginSpecificationResource.openStream())) {
			String pluginFactoryClassName = pluginFileScanner.next();
			Class<? extends PluginFactory> pluginFactoryClass =
					Class.forName(pluginFactoryClassName).asSubclass(PluginFactory.class);
			PluginFactory pluginFactory = pluginFactoryClass.newInstance();
			return pluginFactory.instantiatePlugin(targetPlatform, benchmark, reportWriter);
		} catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			LOG.warn("Failed to load plugin \"" + pluginSpecificationResource.getFile() + "\":", e);
			return null;
		}
	}

}
