package com.namelessmc.plugin.common.logger;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.logging.Logger;

public class JulLogger extends AbstractLogger {

	private final @NotNull Logger logger;

	public JulLogger(final boolean singleLineExceptions,
					 final @NotNull Logger logger) {
		super(singleLineExceptions);
		this.logger = logger;
	}

	@Override
	public void info(String string) {
		this.logger.info(string);
	}

	@Override
	public void warning(String string) {
		this.logger.warning(string);
	}

	@Override
	public void severe(String string) {
		this.logger.severe(string);
	}

	@Override
	public void info(Supplier<String> stringSupplier) {
		this.logger.info(stringSupplier);
	}

	@Override
	public void warning(Supplier<String> stringSupplier) {
		this.logger.warning(stringSupplier);
	}

	@Override
	public void severe(Supplier<String> stringSupplier) {
		this.logger.severe(stringSupplier);
	}

}
