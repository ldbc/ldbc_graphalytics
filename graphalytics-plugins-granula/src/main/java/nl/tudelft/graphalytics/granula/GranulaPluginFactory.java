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
package nl.tudelft.graphalytics.granula;

import nl.tudelft.graphalytics.Platform;
import nl.tudelft.graphalytics.domain.benchmark.Benchmark;
import nl.tudelft.graphalytics.plugin.Plugin;
import nl.tudelft.graphalytics.plugin.PluginFactory;
import nl.tudelft.graphalytics.report.BenchmarkReportWriter;

/**
 * Created by tim on 12/11/15.
 */
public class GranulaPluginFactory implements PluginFactory {

	@Override
	public Plugin instantiatePlugin(Platform targetPlatform, Benchmark benchmark, BenchmarkReportWriter reportWriter) {
		if (targetPlatform instanceof GranulaAwarePlatform) {
			return new GranulaPlugin((GranulaAwarePlatform)targetPlatform, reportWriter);
		} else {
			return null;
		}
	}

}
