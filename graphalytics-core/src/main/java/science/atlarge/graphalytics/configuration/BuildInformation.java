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
package science.atlarge.graphalytics.configuration;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.util.ConsoleUtil;
import science.atlarge.graphalytics.util.TimeUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Wing Lung Ngai
 */
public class BuildInformation {

    private static final Logger LOG = LogManager.getLogger();

    public static String loadCoreBuildInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        String buildInfoFile = "/project/build/graphalytics-core.properties";
        try {
            Properties properties = loadBuildPropertiesFile(buildInfoFile);

            String name = properties.getProperty("build.graphalytics-core.name");
            String version = properties.getProperty("build.graphalytics-core.version");
            String link = properties.getProperty("build.graphalytics-core.link");

            String gitSha1 = properties.getProperty("build.graphalytics-core.git-sha1");
            String branch = properties.getProperty("build.graphalytics-core.branch");
            long timestamp = Long.parseLong(properties.getProperty("build.graphalytics-core.timestamp"));
            boolean verification = true;
            try {
                String text = properties.getProperty("build.graphalytics-core.verification");
                verification = Boolean.parseBoolean(text);
                if(text.equals("${maven.buildNumber.doCheck}")) {
                    verification = true;
                }
            } catch (Exception e) {}

            stringBuilder.append(String.format("Name: %s (%s)\n", name, version));
            stringBuilder.append(String.format("Source: %s\n", link));
            stringBuilder.append(String.format("Build: branch %s, commit %s %s\n",
                    branch, gitSha1.substring(0, 6), verification ? "": "(modification allowed!)"));
            stringBuilder.append(String.format("Date: %s\n", TimeUtil.epoch2Date(timestamp)));

        } catch (Exception e) {
            LOG.error(String.format("Failed to load versioning information from %s.", buildInfoFile));
        }
        return stringBuilder.toString();
    }


    public static String loadPlatformBuildInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        String buildInfoFile = "/project/build/platform.properties";
        try {


            Properties properties = loadBuildPropertiesFile(buildInfoFile);

            String name = properties.getProperty("build.platform.name");
            String version = properties.getProperty("build.platform.version");
            String link = properties.getProperty("build.platform.link");

            String gitSha1 = properties.getProperty("build.platform.git-sha1");
            String branch = properties.getProperty("build.platform.branch");
            long timestamp = Long.parseLong(properties.getProperty("build.platform.timestamp"));
            boolean verification = true;
            try {
                String text = properties.getProperty("build.platform.verification");
                verification = Boolean.parseBoolean(text);
                if(text.equals("${maven.buildNumber.doCheck}")) {
                    verification = true;
                }
            } catch (Exception e) {}

            stringBuilder.append(String.format("Name: %s (%s)\n", name, version));
            stringBuilder.append(String.format("Source: %s\n", link));
            stringBuilder.append(String.format("Build: branch %s, commit %s %s\n",
                    branch, gitSha1.substring(0, 6), verification ? "": "(modification allowed!)"));
            stringBuilder.append(String.format("Date: %s\n", TimeUtil.epoch2Date(timestamp)));

        } catch (Exception e) {
            LOG.error(String.format("Failed to load versioning information from %s.", buildInfoFile));
        }
        return stringBuilder.toString();

    }

    public static Properties loadBuildPropertiesFile(String filePath) {
        InputStream inputStream = ConsoleUtil.class.getResourceAsStream(filePath);
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to read build properties file: %s.", filePath));
        }
        return properties;
    }
}
