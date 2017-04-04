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

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Wing Lung Ngai
 */
public class LogUtil {

    private static final Logger LOG = LogManager.getLogger();

    public static void logBenchmarkHeader(String name) {

        String seperator = "############################################################";
        String logLine = String.format("###### Running benchmark for %s platform #######", name.toUpperCase());
        LOG.info(seperator.substring(0, logLine.length()));
        LOG.info(seperator.substring(0, logLine.length()));
        LOG.info(logLine);
        LOG.info(seperator.substring(0, logLine.length()));
        LOG.info(seperator.substring(0, logLine.length()));
        LOG.info("");
    }

    public static void logMultipleLines(String text) {

        for (String line : text.split("\n")) {
            LOG.info(line);
        }
    }



}
