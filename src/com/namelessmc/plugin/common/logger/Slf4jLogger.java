package com.namelessmc.plugin.common.logger;

import com.namelessmc.plugin.common.CommonObjectsProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.function.Supplier;

public class Slf4jLogger extends AbstractLogger {

	private final @NotNull Logger logger;

	public Slf4jLogger(final @NotNull CommonObjectsProvider commonObjectsProvider,
					   final @NotNull Logger logger) {
		super(commonObjectsProvider);
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
