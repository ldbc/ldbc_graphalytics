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
package science.atlarge.graphalytics.plugin;

import science.atlarge.graphalytics.execution.RunSpecification;
import science.atlarge.graphalytics.execution.Platform;
import science.atlarge.graphalytics.domain.benchmark.Benchmark;
import science.atlarge.graphalytics.report.result.BenchmarkResult;
import science.atlarge.graphalytics.report.result.BenchmarkRunResult;
import science.atlarge.graphalytics.report.BenchmarkReportGenerator;
import science.atlarge.graphalytics.report.BenchmarkReportWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * @author Tim Hegeman
 * @author Wing Lung Ngai
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

	public void prepare(RunSpecification runSpecification) {
		for (Plugin plugin : plugins) {
			plugin.prepare(runSpecification);
		}
	}

	public void terminate(RunSpecification runSpecification, BenchmarkRunResult benchmarkRunResult) {
		for (Plugin plugin : plugins) {
			plugin.terminate(runSpecification, benchmarkRunResult);
		}
	}

	public void postBenchmarkSuite(Benchmark benchmark, BenchmarkResult benchmarkResult) {
		for (Plugin plugin : plugins) {
			plugin.postBenchmarkSuite(benchmark, benchmarkResult);
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
					LOG.debug("\t{}", pluginInstance.getPluginDescription());
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
