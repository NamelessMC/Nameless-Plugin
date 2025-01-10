package com.namelessmc.plugin.velocity;

import com.google.inject.Inject;
import com.namelessmc.plugin.common.MavenConstants;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.velocity.audiences.VelocityAudienceProvider;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.bstats.velocity.Metrics;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "namelessmc",
		name = "NamelessMC",
		version = MavenConstants.PROJECT_VERSION,
		url = "https://plugin.namelessmc.com/",
		description = "Integration with NamelessMC websites",
		authors = {"Derkades"})
public class VelocityNamelessPlugin {

	private final Metrics.@NonNull Factory metricsFactory;
	private final @NonNull ProxyServer server;
	private final @NonNull NamelessPlugin plugin;

	@Inject
	public VelocityNamelessPlugin(final @NonNull ProxyServer server,
								  final @NonNull Logger logger,
								  final @DataDirectory @NonNull Path dataDirectory,
								  final Metrics.@NonNull Factory metricsFactory) {
		this.server = server;
		this.metricsFactory = metricsFactory;
		this.plugin = new NamelessPlugin(
				dataDirectory,
				new VelocityScheduler(this, server.getScheduler()),
				config -> new Slf4jLogger(config, logger),
				Path.of("logs", "latest.log"),
				"velocity",
				server.getVersion().getVersion()
		);
		this.plugin.setAudienceProvider(new VelocityAudienceProvider(this.plugin.config(), server));
		this.plugin.registerReloadable(new VelocityDataSender(this.plugin, this.server));
	}

	@Subscribe
	public void onProxyInitialization(final ProxyInitializeEvent event) {
		this.plugin.load();
		this.server.getEventManager().register(this, new VelocityEventProxy(this.plugin));
		metricsFactory.make(this, 14863);
		VelocityCommandProxy.registerCommands(this, this.plugin, server);
	}

	// TODO call unload() when proxy server shuts down

	@Subscribe
	public void onReload(final ProxyReloadEvent event) {
		this.plugin.unload();
		this.plugin.load();
	}

}
