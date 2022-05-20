package com.namelessmc.plugin.sponge7;

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
	public void info(final String string) {
		this.logger.info(string);
	}

	@Override
	public void warning(final String string) {
		this.logger.warn(string);
	}

	@Override
	public void severe(final String string) {
		this.logger.error(string);
	}

	@Override
	public void info(final Supplier<String> stringSupplier) {
		if (this.logger.isInfoEnabled()) {
			this.logger.info(stringSupplier.get());
		}
	}

	@Override
	public void warning(final Supplier<String> stringSupplier) {
		if (this.logger.isWarnEnabled()) {
			this.logger.warn(stringSupplier.get());
		}
	}

	@Override
	public void severe(final Supplier<String> stringSupplier) {
		if (this.logger.isErrorEnabled()) {
			this.logger.error(stringSupplier.get());
		}
	}

}
