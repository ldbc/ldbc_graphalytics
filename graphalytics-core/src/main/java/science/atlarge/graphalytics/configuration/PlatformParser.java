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
package science.atlarge.graphalytics.configuration;

import science.atlarge.graphalytics.execution.BenchmarkRunner;
import science.atlarge.graphalytics.execution.Platform;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PlatformParser {

    public static Platform loadPlatformFromCommandLineArgs(String[] args) {
        String platformName = getPlatformName(args);
        String platformClassName = getPlatformClassNameByMagic(platformName);
        Class<? extends Platform> platformClass = getPlatformClassForName(platformClassName);
        return instantiatePlatformClass(platformClass);
    }

    private static String getPlatformName(String[] args) {
        // Get the first command-line argument (platform name)
        if (args.length < 1) {
            throw new GraphalyticsLoaderException("Missing argument <platform>.");
        }
        return args[0];
    }

    private static String getPlatformClassNameByMagic(String platformName) {

        Map<String, String> platformNames = new HashMap<>();
        platformNames.put("giraph", "Giraph");
        platformNames.put("graphx", "Graphx");
        platformNames.put("powergraph", "Powergraph");
        platformNames.put("openg", "Openg");
        platformNames.put("graphmat", "Graphmat");
        platformNames.put("pgxd", "Pgxd");
        platformNames.put("reference", "Reference");

        String modelClassName = String.format("science.atlarge.graphalytics.%s.%sPlatform", platformName, platformNames.get(platformName));
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
