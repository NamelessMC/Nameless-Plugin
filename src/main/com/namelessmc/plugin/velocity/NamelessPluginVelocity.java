package com.namelessmc.plugin.velocity;

import com.google.inject.Inject;
import com.namelessmc.plugin.MavenConstants;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.logger.Slf4jLogger;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.bstats.velocity.Metrics;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "nameless-plugin",
		name = MavenConstants.PROJECT_NAME,
		version = MavenConstants.PROJECT_VERSION,
		url = "https://plugin.namelessmc.com/",
		description = "Integration with NamelessMC websites",
		authors = {"Derkades"})
public class NamelessPluginVelocity {

	private final @NotNull Metrics.Factory metricsFactory;
	private final @NotNull NamelessPlugin plugin;

	@Inject
	public NamelessPluginVelocity(final @NotNull ProxyServer server,
								  final @NotNull Logger logger,
								  final @NotNull @DataDirectory Path dataDirectory,
								  final @NotNull Metrics.Factory metricsFactory) {
		this.metricsFactory = metricsFactory;
		this.plugin = new NamelessPlugin(
				dataDirectory,
				new VelocityScheduler(this, server.getScheduler()),
				config -> new Slf4jLogger(config, logger),
				new VelocityAudienceProvider(server)
		);
		this.plugin.registerReloadable(new VelocityCommandProxy(this.plugin, server));
	}

	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
		this.plugin.reload();

		final Metrics metrics = metricsFactory.make(this, 14863);
		this.plugin.registerCustomCharts(metrics, Metrics.class);
	}

}
