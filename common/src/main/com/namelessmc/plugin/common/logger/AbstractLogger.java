package com.namelessmc.plugin.common.logger;

import com.google.gson.JsonSyntaxException;
import com.namelessmc.java_api.exception.ApiError;
import com.namelessmc.java_api.exception.ApiException;
import com.namelessmc.java_api.logger.ApiLogger;
import com.namelessmc.plugin.common.ConfigurationHandler;
import com.namelessmc.plugin.common.Reloadable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Supplier;

public abstract class AbstractLogger implements Reloadable {

	private final @NonNull ApiLogger apiLogger = new ApiLoggerImpl();
	private final @NonNull ConfigurationHandler config;
	private boolean verbose;
	private @Nullable Throwable lastException;

	protected AbstractLogger(final @NonNull ConfigurationHandler config) {
		this.config = config;
	}

	public @NonNull ApiLogger getApiLogger() {
		return this.apiLogger;
	}

	public abstract void info(String string);

	public abstract void warning(String string);

	public abstract void severe(String string);

	public void fine(String string) {
		if (verbose) {
			this.info(string);
		}
	}

	public abstract void info(Supplier<String> stringSupplier);

	public abstract void warning(Supplier<String> stringSupplier);

	public abstract void severe(Supplier<String> stringSupplier);

	public void fine(Supplier<String> stringSupplier) {
		if (verbose) {
			this.info(stringSupplier);
		}
	}

	public boolean isVerbose() {
		return this.verbose;
	}

	public void logException(Throwable t) {
		this.lastException = t;

		String pluginCommand = null;
		try {
			pluginCommand = this.config.commands().node("plugin").getString();
			// Even though it is very unlikely that retrieving an option from the config fails, catch any exception
			// just in case. We don't want to break the logger.
		} catch (Exception ignored) {
		}

		final String lastErrorCommand = "Run /" + (pluginCommand != null ? pluginCommand : "CONFIG_ERROR") +
				" last_error to retrieve the full stack trace.";

		if (!printReducedException(t, lastErrorCommand)) {
			// Couldn't print reduced exception, print full exception.
			this.severe("Unexpected error: " + t.getMessage());
			this.severe(stackTraceAsString(t));
		}
	}

	private boolean printReducedException(Throwable t, String lastErrorCommand) {
		if (t instanceof ApiException) {
			ApiError apiError = ((ApiException) t).apiError();
			switch (apiError) {
				case NAMELESS_API_IS_DISABLED:
					this.severe("Cannot connect to your website, the API is disabled. " + lastErrorCommand);
					break;
				case NAMELESS_NOT_AUTHORIZED:
					this.severe("Cannot connect to your website, the API key is invalid. Please get an " +
							"up-to-date API URL from StaffCP > Configuration > API and reload the plugin. " +
							lastErrorCommand);
					break;
				default:
					this.severe("Cannot connect to your website, got an unexpected API error: " +
							t.getMessage() + ". " + lastErrorCommand
					);
					break;
			}
			return true;
		}

		if (t instanceof JsonSyntaxException) {
			this.warning("The website didn't return a valid json response. " + lastErrorCommand);
			return true;
		}

		if (t.getCause() != null) {
			return printReducedException(t.getCause(), lastErrorCommand);
		}

		if (t instanceof IOException) {
			this.warning("Could not connect to your website due to a network connection error ('" +
					t.getClass().getSimpleName() + ": " + t.getMessage() + "'). " + lastErrorCommand);
			return true;
		}

		// Exception (and cause) is unknown, cannot print reduced exception
		return false;
	}

	public @Nullable String getLastExceptionStackTrace() {
		return this.lastException != null ? stackTraceAsString(this.lastException) : null;
	}

	public static String stackTraceAsString(Throwable t) {
		try (StringWriter out = new StringWriter();
			 PrintWriter writer = new PrintWriter(out)) {
			t.printStackTrace(writer);
			return out.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void unload() {
		this.lastException = null;
	}

	@Override
	public void load() {
		this.verbose = this.config.main().node("logging", "verbose").getBoolean();
	}

	private class ApiLoggerImpl extends ApiLogger {

		@Override
		public void log(final @NonNull String string) {
			AbstractLogger.this.info("[Nameless-Java-API Debug] " + string);
		}

	}

}
