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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.nio.file.Path;

/**
 * @author Wing Lung Ngai
 */
public class LogUtil {

    public static void intializeLoggers() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setConfigurationName("logger");
        RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.DEBUG);
        builder.add(rootLogger);

        Configurator.initialize(builder.build());
    }

    public static void appendSimplifiedConsoleLogger(Level level) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        Layout layout = PatternLayout.createLayout("%msg%n", null, config, null, null, true, false, null, null);
        Appender appender = ConsoleAppender.createAppender(layout, null, ConsoleAppender.Target.SYSTEM_OUT, "stdout", true, true);
        appender.start();

        config.getRootLogger().addAppender(appender, level, null);
        ctx.updateLoggers();
    }

    public static void appendConsoleLogger(Level level) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        Layout layout = PatternLayout.createLayout("%highlight{%d{HH:mm} [%-5p] %msg%n}{STYLE=Logback}", null, config, null, null, true, true, null, null);
        Appender appender = ConsoleAppender.createAppender(layout, null, ConsoleAppender.Target.SYSTEM_OUT, "stdout", true, true);
        appender.start();

        config.getRootLogger().addAppender(appender, level, null);
        ctx.updateLoggers();
    }

    public static void appendFileLogger(Level level, String name, Path filePath) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        Layout layout = PatternLayout.createLayout("%d{HH:mm} [%-5p] %msg%n", null, config, null, null, true, false, null, null);

        Appender appender = FileAppender.createAppender(filePath.toFile().getAbsolutePath(), "true", "false", name, "true",
                "false", "false", "8192", layout, null, "false", null, config);
        appender.start();

        config.getRootLogger().addAppender(appender, level, null);
        ctx.updateLoggers();
    }



}