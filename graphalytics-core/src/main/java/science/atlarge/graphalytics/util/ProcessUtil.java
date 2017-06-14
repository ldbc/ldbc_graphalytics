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
package science.atlarge.graphalytics.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wlngai on 6/14/17.
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
        process.destroy();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void monitorProcess(Process process, String id)  {

        final String runnerId = id;
        final Process runnerProcess = process;

        Thread thread = new Thread() {
            public void run() {
                InputStream is = runnerProcess.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;

                try {
                    while ((line = br.readLine()) != null) {
                        LOG.debug("[Runner "+runnerId+"] => " + line);

                    }
                } catch (IOException e) {
                    LOG.error(String.format("[Runner %s] => Failed to read from the benchmark runner.", runnerId));
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


}
