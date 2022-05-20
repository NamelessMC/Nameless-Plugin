package com.namelessmc.plugin.sponge8;

import com.namelessmc.plugin.common.ConfigurationHandler;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class Log4jLogger extends AbstractLogger {

	private final @NotNull Logger logger;

	public Log4jLogger(final @NotNull ConfigurationHandler config,
					   final @NotNull Logger logger) {
		super(config);
		this.logger = logger;
	}

	@Override
	public void info(String string) {
		this.logger.info(string);
	}

	@Override
	public void warning(String string) {
		this.logger.warn(string);
	}

	@Override
	public void severe(String string) {
		this.logger.error(string);
	}

	@Override
	public void info(Supplier<String> stringSupplier) {
		this.logger.info(stringSupplier);
	}

	@Override
	public void warning(Supplier<String> stringSupplier) {
		this.logger.warn(stringSupplier);
	}

	@Override
	public void severe(Supplier<String> stringSupplier) {
		this.logger.error(stringSupplier);
	}

}
