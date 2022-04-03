package com.namelessmc.plugin.common;

import com.namelessmc.java_api.*;
import com.namelessmc.java_api.exception.UnknownNamelessVersionException;
import com.namelessmc.java_api.logger.ApiLogger;
import com.namelessmc.java_api.logger.JavaLoggerLogger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("OptionalAssignedToNull")
public class ApiProvider {

	private static final String USER_AGENT = "Nameless-Plugin";

	private Optional<NamelessAPI> cachedApi; // null if not cached

	private final @NotNull Logger logger;
	private final @NotNull ExceptionLogger exceptionLogger;
	private final @Nullable String apiUrl;
	private final @Nullable String apiKey;
	private final boolean debug;
	private final boolean usernames;
	private final int timeout;
	private final boolean bypassVersionCheck;

	public ApiProvider(final @NotNull Logger logger,
					   final @NotNull ExceptionLogger exceptionLogger,
					   final @Nullable String apiUrl,
					   final @Nullable String apiKey,
					   final boolean debug,
					   final boolean usernames,
					   final int timeout,
					   final boolean bypassVersionCheck) {
		this.logger = logger;
		this.exceptionLogger = exceptionLogger;
		this.apiUrl = apiUrl;
		this.apiKey = apiKey;
		this.debug = debug;
		this.usernames = usernames;
		this.timeout = timeout;
		this.bypassVersionCheck = bypassVersionCheck;

		if (this.usernames) {
			this.logger.warning("Username mode is enabled. This is NOT supported. If you do not run a cracked server, disable this option!");
		}
	}

	public synchronized Optional<NamelessAPI> getNamelessApi() {
		Objects.requireNonNull(exceptionLogger, "Exception logger not initialized before API was requested. This is a bug.");

		if (this.cachedApi != null) {
			return this.cachedApi;
		}

		final ApiLogger debugLogger = this.debug
				? new JavaLoggerLogger(this.logger, Level.INFO, "[Nameless-Java-API] ")
				: null;

		this.cachedApi = Optional.empty();

		try {
			if (this.apiUrl == null || this.apiUrl.isEmpty()) {
				this.logger.severe("You have not entered an API URL in the config. Please get your site's API URL from StaffCP > Configuration > API and reload the plugin.");
			} else if (this.apiKey == null || this.apiKey.isEmpty()) {
				this.logger.severe("You have not entered an API key in the config. Please get your site's API key from StaffCP > Configuration > API and reload the plugin.");
			} else {
				URL url = null;
				try {
					url = new URL(this.apiUrl);
				} catch (MalformedURLException e){
					this.logger.severe("You have entered an invalid API URL or not entered one at all. Please get an up-to-date API URL from StaffCP > Configuration > API and reload the plugin.");
					this.logger.severe("Error message: '" + e.getMessage() + "'");
				}

				if (url != null) {
					final NamelessAPI api = NamelessAPI.builder(url, this.apiKey)
							.userAgent(USER_AGENT)
							.withCustomDebugLogger(debugLogger)
							.withTimeoutMillis(this.timeout)
							.build();

					final Website info = api.getWebsite();
					try {
						NamelessVersion version = info.getParsedVersion();
						if (this.bypassVersionCheck) {
							this.logger.warning("Bypassing version checks, use at your own risk!");
							this.cachedApi = Optional.of(api);
						} else if (NamelessVersion.isSupportedByJavaApi(version)) {
							this.cachedApi = Optional.of(api);
						} else {
							this.logger.severe("Your website runs a version of NamelessMC (" + version + ") that is not supported by this version of the plugin. Please update your NamelessMC website and/or the plugin.");
						}
					} catch (final UnknownNamelessVersionException e) {
						this.logger.severe("The plugin doesn't recognize the NamelessMC version you are using. Ensure you are running a recent version of the plugin and NamelessMC v2.");
					}
				}
			}
		} catch (final ApiError e) {
			if (e.getError() == ApiError.INVALID_API_KEY) {
				this.logger.severe("You have entered an invalid API key. Please get an up-to-date API URL from StaffCP > Configuration > API and reload the plugin.");
			} else {
				this.logger.severe("Encountered an unexpected error code " + e.getError() + " while trying to connect to your website. Enable api debug mode in the config file for more details. When you think you've fixed the problem, reload the plugin to attempt connecting again.");
			}
		} catch (final NamelessException e) {
			this.logger.warning("Encountered an error while connecting to the website. This message is expected if your site is down temporarily and can be ignored if the plugin works fine otherwise. If the plugin doesn't work as expected, please enable api-debug-mode in the config and run /nlpl reload to get more information.");
			// Do not cache so it immediately tries again the next time. These types of errors may fix on their
			// own, so we don't want to break the plugin until the administrator reloads.
			if (this.debug) {
				this.logger.warning("Debug is enabled, printing full error message:");
				exceptionLogger.logException(e);
			}

			this.cachedApi = null;
			return Optional.empty();
		}

		return this.cachedApi;
	}

	public boolean useUsernames() {
		return usernames;
	}

	public Optional<NamelessUser> userFromPlayer(@NotNull NamelessAPI api, @NotNull Player player) throws NamelessException {
		return this.useUsernames() ? api.getUser(player.getName()) : api.getUser(player.getUniqueId());
	}

	public Optional<NamelessUser> userFromPlayer(@NotNull NamelessAPI api, @NotNull UUID uuid, @NotNull String name) throws NamelessException {
		return this.useUsernames() ? api.getUser(name) : api.getUser(uuid);
	}

}
