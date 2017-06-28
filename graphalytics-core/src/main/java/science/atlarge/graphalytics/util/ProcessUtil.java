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
package science.atlarge.graphalytics.util;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.configuration.ConfigurationUtil;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Wing Lung Ngai
 */
public class ProcessUtil {

    private static final Logger LOG = LogManager.getLogger();


    public static Process initProcess(Class mainClass, List<String> args) {

        Process process = null;
        try {

            String jvm = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            String classpath = System.getProperty("java.class.path");


            List<String> command = new ArrayList<>();
            command.add(jvm);
            command.add("-Xmx"+ setMaxMemory());
            command.add(mainClass.getCanonicalName());
            command.addAll(args);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            Map<String, String> environment = processBuilder.environment();
            environment.put("CLASSPATH", classpath);

            process = processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE).start();


            return process;
        } catch (IOException e) {
            LOG.error(e);
            e.printStackTrace();
            return null;
        }
    }

    public static void terminateProcess(Process process) {
        TimeUtil.waitFor(1);
        process.destroy();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Terminate process with a plaform-dependent implementation.
     * @param process
     * @param processId
     * @param port
     */
    public static void terminateProcess(Process process, int processId, int port) {
        process.destroy();
        TimeUtil.waitFor(1);
        long startTime = System.currentTimeMillis();
        while(!testPortAvailability(port)) {
            if(!TimeUtil.waitFor(startTime, 60, 10)) {
                LOG.error("Runner termination is not successful after 60 seconds.");
                LOG.error("Attempt to terminate runner process " + processId + " focibly.");
                try {
                    Runtime runtime = Runtime.getRuntime();
                    if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) {
                        runtime.exec("taskkill " + processId);
                    } else
                        runtime.exec("kill -9 " + processId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void monitorProcess(Process process, String runId)  {

        final String rId = runId;
        final Process runnerProcess = process;

        Thread thread = new Thread() {
            public void run() {
                InputStream is = runnerProcess.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;

                try {
                    while ((line = br.readLine()) != null) {
                        LOG.debug("[Runner "+rId+"] => " + line);

                    }
                } catch (IOException e) {
                    LOG.error(String.format("[Runner %s] => Failed to read from the benchmark runner.", rId));
                }
                try {
                    runnerProcess.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        };
        thread.start();
    }

    public static int getProcessId() {
        String processName = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(processName.split("@")[0]);

    }

    public static boolean testPortAvailability(int port) {
        try (Socket ignored = new Socket("localhost", port)) {
            return false;
        } catch (IOException ignored) {
            return true;
        }
    }

    /**
     * Set maximum memory size for benchmark runner.
     * Use the configured value if possible, otherwise use 3x the benchmark suite value.
     * By default, the maximum memory of the benchmark suite is set by "MaxHeapSize",
     * roughly equals to 1 / 4 of available memory in Linux systems.
     * @return maximum memory size
     */
    private static String setMaxMemory() {

        String benchmarkSuiteMaxMemory = (MemoryUtil.getMaxMemoryMB() * 3) + "m";

        String configuredMaxMemory = null;
        String BENCHMARK_PROPERTIES_FILE = "benchmark.properties";
        String MAX_MEMORY_KEY = "benchmark.runner.max-memory";

        try {
            Configuration benchmarkConfiguration = ConfigurationUtil.loadConfiguration(BENCHMARK_PROPERTIES_FILE);
            configuredMaxMemory = ConfigurationUtil.getString(benchmarkConfiguration, MAX_MEMORY_KEY);

            if (configuredMaxMemory.trim().isEmpty()) {
                return benchmarkSuiteMaxMemory;
            } else if (!(configuredMaxMemory.endsWith("g") ||
                    configuredMaxMemory.endsWith("m") ||
                    configuredMaxMemory.endsWith("k"))) {
                LOG.error("Failed to parse configuration " + MAX_MEMORY_KEY + ": " + configuredMaxMemory);
                return benchmarkSuiteMaxMemory;
            } else {
                return configuredMaxMemory;
            }

        } catch (Exception e) {
            LOG.error("Failed to found configuration " + MAX_MEMORY_KEY);
            return benchmarkSuiteMaxMemory;
        }
    }


}
