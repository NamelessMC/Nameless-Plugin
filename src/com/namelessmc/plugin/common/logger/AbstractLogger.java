package com.namelessmc.plugin.common.logger;

import com.namelessmc.java_api.logger.ApiLogger;
import com.namelessmc.plugin.common.CommonObjectsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class AbstractLogger {

	private final boolean singleLineExceptions;
	private final @NotNull ApiLogger apiLogger = new ApiLoggerImpl();

	AbstractLogger(final @NotNull CommonObjectsProvider commonObjectsProvider) {
		this.singleLineExceptions = commonObjectsProvider.getConfiguration().getMainConfig()
				.getBoolean("single-line-exceptions", false);
	}

	public @NotNull ApiLogger getApiLogger() {
		return this.apiLogger;
	}

	public abstract void info(String string);

	public abstract void warning(String string);

	public abstract void severe(String string);

	public abstract void info(Supplier<String> stringSupplier);

	public abstract void warning(Supplier<String> stringSupplier);

	public abstract void severe(Supplier<String> stringSupplier);

	public void logException(Throwable t) {
		if (this.singleLineExceptions) {
			this.severe(t.getClass().getSimpleName() + " " + t.getMessage());
		} else {
			this.severe(t.getMessage());
			t.printStackTrace(); // TODO print stack trace using logger
		}
	}

	private class ApiLoggerImpl extends ApiLogger {

		@Override
		public void log(String string) {
			AbstractLogger.this.info("[Nameless-Java-API Debug]" + string);
		}

	}

}
