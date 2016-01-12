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
import nl.tudelft.graphalytics.domain.Benchmark;
import nl.tudelft.graphalytics.domain.BenchmarkResult;
import nl.tudelft.graphalytics.domain.BenchmarkSuite;
import nl.tudelft.graphalytics.domain.BenchmarkSuiteResult;
import nl.tudelft.graphalytics.reporting.BenchmarkReportGenerator;
import nl.tudelft.graphalytics.reporting.BenchmarkReportWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created by tim on 12/11/15.
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

	public void preBenchmarkSuite(BenchmarkSuite benchmarkSuite) {
		for (Plugin plugin : plugins) {
			plugin.preBenchmarkSuite(benchmarkSuite);
		}
	}

	public void preBenchmark(Benchmark nextBenchmark) {
		for (Plugin plugin : plugins) {
			plugin.preBenchmark(nextBenchmark);
		}
	}

	public void postBenchmark(Benchmark completedBenchmark, BenchmarkResult benchmarkResult) {
		for (Plugin plugin : plugins) {
			plugin.postBenchmark(completedBenchmark, benchmarkResult);
		}
	}

	public void postBenchmarkSuite(BenchmarkSuite benchmarkSuite, BenchmarkSuiteResult benchmarkSuiteResult) {
		for (Plugin plugin : plugins) {
			plugin.postBenchmarkSuite(benchmarkSuite, benchmarkSuiteResult);
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

	public static Plugins discoverPluginsOnClasspath(Platform targetPlatform, BenchmarkSuite benchmarkSuite, BenchmarkReportWriter reportWriter) {
		Plugins plugins = new Plugins();

		try {
			Enumeration<URL> resources = Plugins.class.getClassLoader().getResources("META-INF/graphalytics/plugins");
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				Plugin pluginInstance = instantiatePluginFromResource(resource, targetPlatform, benchmarkSuite, reportWriter);
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
			BenchmarkSuite benchmarkSuite, BenchmarkReportWriter reportWriter) {
		try (Scanner pluginFileScanner = new Scanner(pluginSpecificationResource.openStream())) {
			String pluginFactoryClassName = pluginFileScanner.next();
			Class<? extends PluginFactory> pluginFactoryClass =
					Class.forName(pluginFactoryClassName).asSubclass(PluginFactory.class);
			PluginFactory pluginFactory = pluginFactoryClass.newInstance();
			return pluginFactory.instantiatePlugin(targetPlatform, benchmarkSuite, reportWriter);
		} catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			LOG.warn("Failed to load plugin \"" + pluginSpecificationResource.getFile() + "\":", e);
			return null;
		}
	}

}
