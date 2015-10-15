/**
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
package nl.tudelft.graphalytics.reporting.logging;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.yarn.client.cli.LogsCLI;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Created by wlngai on 9-9-15.
 */
public class GraphalyticLogger {

    protected static final Logger LOG = LogManager.getLogger();
    protected static Level coreLogLevel = Level.INFO;

    public static void startCoreLogging() {
        addConsoleAppender("nl.tudelft.graphalytics", coreLogLevel);
    }

    public static void stopCoreLogging() {
        removeAppender("nl.tudelft.graphalytics");
    }

    protected static void waitInterval(int waitInterval) {
        try {
            TimeUnit.SECONDS.sleep(waitInterval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void addConsoleAppender(String name, Level level) {
        LoggerContext context = (LoggerContext) org.apache.logging.log4j.LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        String pattern = "%d [%t] %-5p[%c{1} (%M(%L))] %m%n";
        Layout layout = PatternLayout.createLayout(pattern, config, null,
                Charset.defaultCharset(), true, false, null, null);

        ConsoleAppender consoleAppender = ConsoleAppender.createAppender(
                layout, null, "SYSTEM_OUT", "console", null, null);
        consoleAppender.start();
        config.addAppender(consoleAppender);

        AppenderRef ref = AppenderRef.createAppenderRef("console", null, null);
        AppenderRef[] refs = new AppenderRef[] {ref};

        LoggerConfig consoleLoggerConfig = LoggerConfig.createLogger("false", level, name,
                "true", refs, null, config, null);
        consoleLoggerConfig.addAppender(consoleAppender, null, null);
        config.addLogger(name, consoleLoggerConfig);

        context.updateLoggers();
    }


    public static void addFileAppender(String name, Level level, String logFilePath) {

        LoggerContext context = (LoggerContext) org.apache.logging.log4j.LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        String pattern = "%d [%t] %-5p [%c{1} (%M(%L))] %m%n";
        Layout layout = PatternLayout.createLayout(pattern, config, null,
                Charset.defaultCharset(), true, false, null, null);

        Appender appender = FileAppender.createAppender(logFilePath, "false", "false", "File", "true",
                "false", "false", "4000", layout, null, "false", null, config);
        appender.start();
        config.addAppender(appender);
        AppenderRef ref = AppenderRef.createAppenderRef("File", null, null);
        AppenderRef[] refs = new AppenderRef[] {ref};
        LoggerConfig loggerConfig = LoggerConfig.createLogger("false", level, name,
                "true", refs, null, config, null );
        loggerConfig.addAppender(appender, null, null);
        config.addLogger(name, loggerConfig);
        context.updateLoggers();
    }

    public static void removeAppender(String name) {
        LoggerContext context = (LoggerContext) org.apache.logging.log4j.LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        config.removeLogger(name);
    }

    public static List<String> getYarnAppIds(String clientLogPath) {
        List<String> appIds = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(new File(clientLogPath)))) {

            String line;
            while ((line = br.readLine()) != null) {
                String appId = null;
                if(line.contains("Submitted application")) {
                    for (String word : line.split("\\s+")) {
                        appId = word;
                    }
                    appIds.add(appId);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(appIds.size() == 0) {
            LOG.error("Failed to find any yarn application ids in the driver log.");
        }
        return appIds;
    }

    public static void collectYarnLog(String applicationId, String yarnlogPath) {

        try {

            PrintStream console = System.out;

            File file = new File(yarnlogPath);
            FileOutputStream fos = new FileOutputStream(file);
            PrintStream ps = new PrintStream(fos);
            System.setOut(ps);
            waitInterval(20);

            LogsCLI logDumper = new LogsCLI();
            logDumper.setConf(new YarnConfiguration());

            String[] args = {"-applicationId", applicationId};

            logDumper.run(args);
            System.setOut(console);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void collectYarnLogs(String logDataPath) {
        List<String> appIds = GraphalyticLogger.getYarnAppIds(logDataPath + "/OperationLog/driver.logs");
        for (String appId : appIds) {
            GraphalyticLogger.collectYarnLog(appId, logDataPath + "/OperationLog/yarn" + appId + ".logs");
        }

    }


}
