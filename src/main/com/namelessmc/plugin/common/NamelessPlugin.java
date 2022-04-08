package com.namelessmc.plugin.common;

import com.namelessmc.plugin.common.command.AbstractScheduler;
import com.namelessmc.plugin.common.logger.AbstractLogger;
import net.md_5.bungee.config.Configuration;
import org.bstats.MetricsBase;
import org.bstats.charts.SimplePie;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class NamelessPlugin {

	private final @NotNull AbstractScheduler scheduler;
	private final @NotNull ConfigurationHandler configuration;
	private final @NotNull AbstractLogger logger;
	private final @NotNull ApiProvider api;
	private final @NotNull LanguageHandler language;
	private final @NotNull DateFormatter dateFormatter;

	private final @NotNull List<Reloadable> reloadables = new ArrayList<>();

	private AbstractAudienceProvider audienceProvider;

	public NamelessPlugin(final @NotNull Path dataDirectory,
						  final @NotNull AbstractScheduler scheduler,
						  final @NotNull Function<ConfigurationHandler, AbstractLogger> loggerInstantiator) {
		this.scheduler = scheduler;

		this.configuration = this.registerReloadable(
				new ConfigurationHandler(dataDirectory)
		);
		this.logger = this.registerReloadable(
				loggerInstantiator.apply(this.configuration)
		);
		this.api = this.registerReloadable(
				new ApiProvider(scheduler, this.logger, this.configuration)
		);
		this.language = this.registerReloadable(
				new LanguageHandler(dataDirectory, this.configuration, this.logger)
		);
		this.dateFormatter = new DateFormatter(this.configuration);
	}

	public ConfigurationHandler config() {
		return this.configuration;
	}

	public AbstractLogger logger() {
		return this.logger;
	}

	public ApiProvider api() {
		return this.api;
	}

	public LanguageHandler language() {
		return this.language;
	}

	public DateFormatter dateFormatter() {
		return this.dateFormatter;
	}

	public AbstractScheduler scheduler() {
		return this.scheduler;
	}

	public AbstractAudienceProvider audiences() {
		return this.audienceProvider;
	}

	public void setAudienceProvider(final @NotNull AbstractAudienceProvider audienceProvider) {
		this.audienceProvider = audienceProvider;
	}

	public void reload() {
		for (Reloadable reloadable : reloadables) {
			reloadable.reload();
		}
	}

	public <T extends Reloadable> T registerReloadable(T reloadable) {
		this.reloadables.add(reloadable);
		return reloadable;
	}

	private @Nullable MetricsBase extractMetricsBase(Object metrics, Class<?> metricsClass) {
		try {
			Field baseField = metricsClass.getDeclaredField("metricsBase");
			baseField.setAccessible(true);
			return (MetricsBase) baseField.get(metrics);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	public <T> void registerCustomCharts(final @NotNull T platformMetrics,
										 final @NotNull Class<T> platformMetricsClass) {
		final MetricsBase metrics = extractMetricsBase(platformMetrics, platformMetricsClass);

		if (metrics == null) {
			this.logger.warning("Failed to extract MetricsBase, not adding custom charts.");
			return;
		}

		Configuration config = this.config().getMainConfig();

		metrics.addCustomChart(new SimplePie("api_working", () ->
				this.api().isApiWorkingMetric()));

		metrics.addCustomChart(new SimplePie("server_data_sender_enabled", () ->
				config.getInt("server-id") > 0
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("upload_placeholders_enabled", () ->
				config.getBoolean("upload-placeholders.enabled")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("language", () ->
				this.language().getActiveLanguageCode()));

		metrics.addCustomChart(new SimplePie("auto_ban_on_website", () ->
				config.getBoolean("auto-ban-on-website")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("not_registered_join_message", () ->
				config.getBoolean("not-registered-join-message")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("api_usernames_enabled", () ->
				config.getBoolean("api-usernames")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("user_sync_whitelist_enabled", () ->
				config.getBoolean("user-sync.whitelist.enabled")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("user_sync_bans_enabled", () ->
				config.getBoolean("user-sync.bans.enabled")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("announcements_enabled", () ->
				config.getInt("announcements.interval") > 0
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("websend_command_executor_enabled", () ->
				config.getBoolean("websend.command-executor.enabled")
						? "Enabled" : "Disabled"));

		metrics.addCustomChart(new SimplePie("websend_console_capture_enabled", () ->
				config.getBoolean("websend.console-capture.enabled")
						? "Enabled" : "Disabled"));
	}

}
