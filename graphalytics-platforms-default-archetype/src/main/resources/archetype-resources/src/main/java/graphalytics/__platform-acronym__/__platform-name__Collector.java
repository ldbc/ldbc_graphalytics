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
package science.atlarge.graphalytics.${platform-acronym};

import org.apache.commons.io.output.TeeOutputStream;
import science.atlarge.graphalytics.configuration.GraphalyticsExecutionException;
import science.atlarge.graphalytics.report.result.BenchmarkMetric;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 *
 * @author ${developer-name}
 */
public class ${platform-name}Collector {

	protected static final Logger LOG = LogManager.getLogger();

	private static PrintStream defaultSysOut;
	private static PrintStream deafultSysErr;

	public static void startPlatformLogging(Path fileName) {
		defaultSysOut = System.out;
		deafultSysErr = System.err;
		try {
			File file = fileName.toFile();
			file.getParentFile().mkdirs();
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			TeeOutputStream bothStream = new TeeOutputStream(System.out, fos);
			PrintStream ps = new PrintStream(bothStream);
			System.setOut(ps);
			System.setErr(ps);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("Failed to redirect to log file %s", fileName);
			throw new GraphalyticsExecutionException("Failed to log the benchmark run. Benchmark run aborted.");
		}
	}

	public static void stopPlatformLogging() {
		System.setOut(defaultSysOut);
		System.setErr(deafultSysErr);
	}

	public static BenchmarkMetric collectProcessingTime(Path logPath) throws Exception {
		BigDecimal procTime;

		final AtomicLong startTime = new AtomicLong(-1);
		final AtomicLong endTime = new AtomicLong(-1);

		Files.walkFileTree(logPath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

				try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile()))) {
					String line;
					while ((line = reader.readLine()) != null) {
						try {
							if (line.contains(ProcTimeLog.START_PROC_TIME)) {
								String[] lineParts = line.split("\\s+");
								startTime.set(Long.parseLong(lineParts[lineParts.length - 1]));
							}

							if (line.contains(ProcTimeLog.END_PROC_TIME)) {
								String[] lineParts = line.split("\\s+");
								endTime.set(Long.parseLong(lineParts[lineParts.length - 1]));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				return FileVisitResult.CONTINUE;
			}
		});

		if (startTime.get() != -1 && endTime.get() != -1) {
			procTime = (new BigDecimal(endTime.get() - startTime.get()))
					.divide(new BigDecimal(1000), 3, BigDecimal.ROUND_CEILING);

			return new BenchmarkMetric(procTime, "s");
		} else {
			throw new IllegalArgumentException("Failed to extract processing time");
		}
	}

}
