package com.namelessmc.plugin.velocity;

import com.namelessmc.plugin.common.ConfigurationHandler;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

import java.util.function.Supplier;

public class Slf4jLogger extends AbstractLogger {

	private final @NonNull Logger logger;

	public Slf4jLogger(final @NonNull ConfigurationHandler config,
					   final @NonNull Logger logger) {
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
		if (this.logger.isInfoEnabled()) {
			this.logger.info(stringSupplier.get());
		}
	}

	@Override
	public void warning(Supplier<String> stringSupplier) {
		if (this.logger.isWarnEnabled()) {
			this.logger.warn(stringSupplier.get());
		}
	}

	@Override
	public void severe(Supplier<String> stringSupplier) {
		if (this.logger.isErrorEnabled()) {
			this.logger.error(stringSupplier.get());
		}
	}

}
