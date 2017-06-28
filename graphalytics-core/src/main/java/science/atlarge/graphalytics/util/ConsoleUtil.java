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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Wing Lung Ngai
 */
public class ConsoleUtil {

    private static final Logger LOG = LogManager.getLogger();

    public static void displayTrademark(String name) {
        int margin = 17;
        String text = "";
        text += String.format("Graphalytics benchmark for %s platform", name.toUpperCase());

        int maxTextWidth = 25;
        displayBlockInformation(text, maxTextWidth, margin);
    }


    public static void displayTextualInformation(String text) {
        LOG.info("");
        for (String line : text.split("\n")) {
            LOG.info(line);
        }
        LOG.info("");
    }


    public static void displayBlockInformation(String text, int maxTextWidth, int margin) {

        int textWidth = 0;
        for (String line : text.split("\n")) {
            textWidth = Math.max(line.length(), textWidth);
        }
        textWidth = Math.max(textWidth, maxTextWidth);

        String sharpLine = "";
        String emptyLine = "";
        for (int i = 0; i < textWidth + margin * 2; i++) {
            sharpLine += "#";
            emptyLine += " ";

        }

        LOG.info("");
        LOG.info("##" + sharpLine + "##");
        LOG.info("##" + sharpLine + "##");
        LOG.info("##" + emptyLine + "##");
        LOG.info("##" + emptyLine + "##");
        for (String line : text.split("\n")) {

            String leftEmptyText = "";
            for (int i = 0; i < margin; i++) {
                leftEmptyText += " ";
            }

            String rightEmptyText = "";
            for (int i = line.length(); i < textWidth + margin; i++) {
                rightEmptyText += " ";

            }
            LOG.info("##" + leftEmptyText + line + rightEmptyText + "##");
        }

        LOG.info("##" + emptyLine + "##");
        LOG.info("##" + emptyLine + "##");
        LOG.info("##" + sharpLine + "##");
        LOG.info("##" + sharpLine + "##");
        LOG.info("");
    }

}
