/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cowave.commons.schedule.fom.logging.java;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.cowave.commons.schedule.fom.logging.AbstractLoggingSystem;
import com.cowave.commons.schedule.fom.logging.LogFile;
import com.cowave.commons.schedule.fom.logging.LogLevel;
import com.cowave.commons.schedule.fom.logging.LoggerConfiguration;
import com.cowave.commons.schedule.fom.logging.LoggingInitializationContext;
import com.cowave.commons.schedule.fom.logging.LoggingSystem;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * {@link LoggingSystem} for {@link Logger java.util.logging}.
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Andy Wilkinson
 * @author Ben Hale
 */
public class JavaLoggingSystem extends AbstractLoggingSystem {

	private static final LogLevels<Level> LEVELS = new LogLevels<>();

	static {
		LEVELS.map(LogLevel.TRACE, Level.FINEST);
		LEVELS.map(LogLevel.DEBUG, Level.FINE);
		LEVELS.map(LogLevel.INFO, Level.INFO);
		LEVELS.map(LogLevel.WARN, Level.WARNING);
		LEVELS.map(LogLevel.ERROR, Level.SEVERE);
		LEVELS.map(LogLevel.FATAL, Level.SEVERE);
		LEVELS.map(LogLevel.OFF, Level.OFF);
	}

	public JavaLoggingSystem(ClassLoader classLoader) {
		super(classLoader);
	}

	@Override
	protected String[] getStandardConfigLocations() {
		return new String[] { "logging.properties" };
	}

	@Override
	public void beforeInitialize() {
		super.beforeInitialize();
		Logger.getLogger("").setLevel(Level.SEVERE);
	}

	@Override
	protected void loadDefaults(LoggingInitializationContext initializationContext,
                                LogFile logFile) {
		if (logFile != null) {
			loadConfiguration(getPackagedConfigFile("logging-file.properties"), logFile);
		}
		else {
			loadConfiguration(getPackagedConfigFile("logging.properties"), logFile);
		}
	}

	@Override
	protected void loadConfiguration(LoggingInitializationContext initializationContext,
			String location, LogFile logFile) {
		loadConfiguration(location, logFile);
	}

	protected void loadConfiguration(String location, LogFile logFile) {
		Assert.notNull(location, "Location must not be null");
		try {
			String configuration = FileCopyUtils.copyToString(
					new InputStreamReader(ResourceUtils.getURL(location).openStream()));
			if (logFile != null) {
				configuration = configuration.replace("${LOG_FILE}",
						StringUtils.cleanPath(logFile.toString()));
			}
			LogManager.getLogManager().readConfiguration(
					new ByteArrayInputStream(configuration.getBytes()));
		}
		catch (Exception ex) {
			throw new IllegalStateException(
					"Could not initialize Java logging from " + location, ex);
		}
	}

	@Override
	public Set<LogLevel> getSupportedLogLevels() {
		return LEVELS.getSupported();
	}

	@Override
	public void setLogLevel(String loggerName, LogLevel level) {
		if (loggerName == null || ROOT_LOGGER_NAME.equals(loggerName)) {
			loggerName = "";
		}
		Logger logger = Logger.getLogger(loggerName);
		if (logger != null) {
			logger.setLevel(LEVELS.convertSystemToNative(level));
		}
	}

	@Override
	public List<LoggerConfiguration> getLoggerConfigurations() {
		List<LoggerConfiguration> result = new ArrayList<>();
		Enumeration<String> names = LogManager.getLogManager().getLoggerNames();
		while (names.hasMoreElements()) {
			result.add(getLoggerConfiguration(names.nextElement()));
		}
		result.sort(CONFIGURATION_COMPARATOR);
		return Collections.unmodifiableList(result);
	}

	@Override
	public LoggerConfiguration getLoggerConfiguration(String loggerName) {
		Logger logger = Logger.getLogger(loggerName);
		if (logger == null) {
			return null;
		}
		LogLevel level = LEVELS.convertNativeToSystem(logger.getLevel());
		LogLevel effectiveLevel = LEVELS.convertNativeToSystem(getEffectiveLevel(logger));
		String name = (StringUtils.hasLength(logger.getName()) ? logger.getName()
				: ROOT_LOGGER_NAME);
		return new LoggerConfiguration(name, level, effectiveLevel);
	}

	private Level getEffectiveLevel(Logger root) {
		Logger logger = root;
		while (logger.getLevel() == null) {
			logger = logger.getParent();
		}
		return logger.getLevel();
	}

	@Override
	public Runnable getShutdownHandler() {
		return new ShutdownHandler();
	}

	private final class ShutdownHandler implements Runnable {

		@Override
		public void run() {
			LogManager.getLogManager().reset();
		}

	}

}
