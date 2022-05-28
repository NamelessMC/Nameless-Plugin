package com.namelessmc.plugin.common.logger;

import com.namelessmc.java_api.logger.ApiLogger;
import com.namelessmc.plugin.common.ConfigurationHandler;
import com.namelessmc.plugin.common.Reloadable;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Supplier;

public abstract class AbstractLogger implements Reloadable {

	private final @NonNull ApiLogger apiLogger = new ApiLoggerImpl();
	private final @NonNull ConfigurationHandler config;
	private boolean singleLineExceptions;

	protected AbstractLogger(final @NonNull ConfigurationHandler config) {
		this.config = config;
		this.singleLineExceptions = false;
	}

	public @NonNull ApiLogger getApiLogger() {
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
			// Write stack trace to string buffer, then send the resulting string to logger
			try (StringWriter out = new StringWriter();
				PrintWriter writer = new PrintWriter(out)) {
				t.printStackTrace(writer);
				this.severe(out.toString());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void reload() {
		this.singleLineExceptions = this.config.main().node("single-line-exceptions").getBoolean();
	}

	private class ApiLoggerImpl extends ApiLogger {

		@Override
		public void log(final @NonNull String string) {
			AbstractLogger.this.info("[Nameless-Java-API Debug] " + string);
		}

	}

}
