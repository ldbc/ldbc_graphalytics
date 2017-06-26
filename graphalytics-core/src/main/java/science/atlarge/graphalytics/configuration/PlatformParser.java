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

import org.apache.commons.lang.StringUtils;
import science.atlarge.graphalytics.execution.BenchmarkRunner;
import science.atlarge.graphalytics.execution.Platform;
import science.atlarge.graphalytics.util.TimeUtil;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

/**
 * @author Wing Lung Ngai
 */
public class PlatformParser {

    public static Platform loadPlatformFromCommandLineArgs() {
        String platformName = getPlatformName();
        String platformClassName = getPlatformClassNameByMagic(platformName);
        Class<? extends Platform> platformClass = getPlatformClassForName(platformClassName);
        return instantiatePlatformClass(platformClass);
    }

    private static String getPlatformName() {
        String buildInfoFile = "/project/build/platform.properties";
        try {
            Properties properties = BuildInformation.loadBuildPropertiesFile(buildInfoFile);

            String name = properties.getProperty("build.platform.name");
            return name.replace("graphalytics-platforms-", "");

        } catch (Exception e) {
            throw new IllegalStateException(
                    String.format("Failed to load platform name from %s.", buildInfoFile));
        }

    }

    private static String getPlatformClassNameByMagic(String platformName) {

        String modelClassName = String.format("science.atlarge.graphalytics.%s.%sPlatform", platformName, StringUtils.capitalize(platformName));
        return modelClassName;
    }

    private static String getPlatformClassName(String platformName) {
        InputStream platformFileStream = openPlatformFileStream(platformName);
        String platformClassName;
        try (Scanner platformScanner = new Scanner(platformFileStream)) {
            if (!platformScanner.hasNext()) {
                throw new GraphalyticsLoaderException("Expected a single line with a class name in \"" + platformName +
                        ".platform\", got an empty file.");
            }

            platformClassName = platformScanner.next();

            if (platformScanner.hasNext()) {
                throw new GraphalyticsLoaderException("Expected a single line with a class name in \"" + platformName +
                        ".platform\", got multiple words.");
            }
        }
        return platformClassName;
    }

    private static InputStream openPlatformFileStream(String platformName) {
        // Read the <platform>.platform file that should be in the classpath to determine which class to load
        InputStream platformFileStream = BenchmarkRunner.class.getResourceAsStream("/" + platformName + ".platform");
        if (platformFileStream == null) {
            throw new GraphalyticsLoaderException("Missing resource \"" + platformName + ".platform\".");
        }
        return platformFileStream;
    }

    private static Class<? extends Platform> getPlatformClassForName(String platformClassName) {
        // Load the class by name
        Class<? extends Platform> platformClass;
        try {
            Class<?> platformClassUncasted = Class.forName(platformClassName);
            if (!Platform.class.isAssignableFrom(platformClassUncasted)) {
                throw new GraphalyticsLoaderException("Expected class \"" + platformClassName +
                        "\" to be a subclass of \"science.atlarge.graphalytics.execution.Platform\".");
            }

            platformClass = platformClassUncasted.asSubclass(Platform.class);
        } catch (ClassNotFoundException e) {
            throw new GraphalyticsLoaderException("Could not find class \"" + platformClassName + "\".", e);
        }
        return platformClass;
    }

    private static Platform instantiatePlatformClass(Class<? extends Platform> platformClass) {
        Platform platformInstance;
        try {
            platformInstance = platformClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new GraphalyticsLoaderException("Failed to instantiate platform class \"" +
                    platformClass.getSimpleName() + "\".", e);
        }
        return platformInstance;
    }
}
