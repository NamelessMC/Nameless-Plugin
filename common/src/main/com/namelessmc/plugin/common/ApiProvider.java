package com.namelessmc.plugin.common;

import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessVersion;
import com.namelessmc.java_api.Website;
import com.namelessmc.java_api.exception.ApiError;
import com.namelessmc.java_api.exception.ApiException;
import com.namelessmc.plugin.common.command.AbstractScheduler;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import xyz.derkades.derkutils.Tristate;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Objects;

public class ApiProvider implements Reloadable {

	private static final String USER_AGENT = "Nameless-Plugin/"	 + MavenConstants.PROJECT_VERSION;

	private final @NonNull AbstractScheduler scheduler;
	private final @NonNull AbstractLogger logger;
	private final @NonNull ConfigurationHandler config;

	private Tristate<NamelessAPI> cachedApi;
	private @Nullable Throwable lastException = null;

	private @Nullable String apiUrl;
	private @Nullable String apiKey;
	private boolean debug;
	private @Nullable Duration timeout;
	private boolean bypassVersionCheck;

	public ApiProvider(final @NonNull AbstractScheduler scheduler,
					   final @NonNull AbstractLogger logger,
					   final @NonNull ConfigurationHandler config) {
		this.scheduler = scheduler;
		this.logger = logger;
		this.config = config;
		this.cachedApi = Tristate.unknown();
	}

	@Override
	public void reload() {
		final CommentedConfigurationNode config = this.config.main().node("api");
		this.apiUrl = config.node("url").getString();
		this.apiKey = config.node("key").getString();
		this.debug = config.node("debug").getBoolean();

		final Duration timeout = ConfigurationHandler.getDuration(config.node("timeout"));
		if (timeout != null) {
			this.timeout = timeout;
		} else {
			this.logger.warning("Invalid API timeout, using 10 seconds.");
			this.timeout = Duration.ofSeconds(10);
		}
		this.bypassVersionCheck = config.node("bypass-version-check").getBoolean();

		this.cachedApi = Tristate.unknown();
		this.lastException = null;

		scheduler.runAsync(this::api);
	}

	@Deprecated
	public @Nullable String isApiWorkingMetric() {
		if (!this.cachedApi.known()) {
			// In theory the API should always be cached, but in case it's not we
			// do not want to force load it because that would affect server performance.
			return "Unknown";
		}

		return this.cachedApi.present() ? "Working" : "Not working";
	}

	public synchronized @Nullable NamelessAPI api() {
		Objects.requireNonNull(this.logger, "Exception logger not initialized before API was requested. This is a bug.");
		Objects.requireNonNull(this.timeout, "API requested before config settings are loaded");

		if (this.cachedApi.known()) {
			return this.cachedApi.value();
		}

		if (this.apiUrl == null || this.apiUrl.isEmpty() || this.apiKey == null || this.apiKey.isEmpty()) {
			this.logger.severe("You have not entered an API URL and API key in the config. Please get your site's API URL and " +
					"API key from StaffCP > Configuration > API and reload the plugin.");
			this.cachedApi = Tristate.knownEmpty(); // This won't be resolved without reloading, we don't have to retry.
		} else {
			try {
				URL url = new URL(this.apiUrl);
				final NamelessAPI api = NamelessAPI.builder(url, this.apiKey)
						.userAgent(USER_AGENT)
						.customDebugLogger(this.debug ? this.logger.getApiLogger() : null)
						.timeout(this.timeout)
						.build();

				final Website info = api.website();
				NamelessVersion version = info.parsedVersion();
				if (this.bypassVersionCheck) {
					this.logger.warning("Bypassing version checks, use at your own risk!");
					this.cachedApi = Tristate.known(api); // Cache working API
				} else if (version == null) {
					this.logger.severe("The plugin doesn't recognize the NamelessMC version you are using. Ensure you are running a " +
							"recent version of the plugin and NamelessMC v2.");
					this.cachedApi = Tristate.knownEmpty(); // Probably won't resolve on its own, cache until reload
				} else if (NamelessVersion.isSupportedByJavaApi(version)) {
					this.logger.fine("Website connection appears to be working.");
					this.cachedApi = Tristate.known(api); // Cache working API
				} else {
					this.logger.severe("Your website runs a version of NamelessMC (" + version + ") that is not supported by this " +
							"version of the plugin. Please update your NamelessMC website and/or the plugin.");
					this.cachedApi = Tristate.knownEmpty(); // No need to retry, cache that it's not working
				}

				this.lastException = null;
			} catch (MalformedURLException e) {
				this.lastException = e;
				this.logger.severe("You have entered an invalid API URL. Please get an up-to-date API URL from StaffCP > " +
						"Configuration > API and reload the plugin.");
				this.logger.severe("Error message: '" + e.getMessage() + "'");
				this.cachedApi = Tristate.knownEmpty(); // This won't be resolved without reloading, we don't have to retry.
			} catch (final NamelessException e) {
				this.lastException = e;

				this.printNamelessException(e);

				if (e instanceof ApiException) {
					this.cachedApi = Tristate.knownEmpty(); // This won't be resolved without reloading, we don't have to retry.
				} else {
					// Do not cache, so it immediately tries again the next time. These types of errors may fix on their
					// own, so we don't want to break the plugin until the administrator reloads.
					this.cachedApi = Tristate.unknown();
				}
			}
		}

		return this.cachedApi.value();
	}

	public @Nullable NamelessAPI apiIfCached() {
		return this.cachedApi.present() ? this.cachedApi.value() : null;
	}

	private void printNamelessException(final NamelessException e) {
		if (e instanceof ApiException) {
			ApiError apiError = ((ApiException) e).apiError();
			switch(apiError) {
				case NAMELESS_API_IS_DISABLED:
					this.logger.severe("Cannot connect to your website, the API is disabled.");
					break;
				case NAMELESS_NOT_AUTHORIZED:
					this.logger.severe("Cannot connect to your website, the API key is invalid. Please get an " +
							"up-to-date API URL from StaffCP > Configuration > API and reload the plugin.");
					break;
				default:
					this.logger.severe("Cannot connect to your website, got an unexpected API error: " + e.getMessage());
					if (!this.debug) {
						this.logger.severe("For more information, enable API debug mode in the config file.");
					}
					break;
			}
		} else {
			final String pluginCommand = this.config.commands().node("plugin").getString();
			this.logger.warning("Encountered an error while connecting to the website. If your site is not down " +
					"temporarily, your website connection might not be configured correctly. For more information, " +
					"run the following command: /" + pluginCommand + " last_api_error");
		}

		if (this.debug) {
			this.logger.warning("Debug is enabled, printing full stack trace:");
			this.logger.logException(e);
		}
	}

	public @Nullable Throwable getLastException() {
		return this.lastException;
	}

}
