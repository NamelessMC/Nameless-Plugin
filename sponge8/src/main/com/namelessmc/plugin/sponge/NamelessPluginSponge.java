package com.namelessmc.plugin.sponge;

import com.google.inject.Inject;
import com.namelessmc.plugin.common.NamelessPlugin;
import net.kyori.adventure.platform.spongeapi.SpongeAudiences;
import org.apache.logging.log4j.Logger;
import org.bstats.sponge.Metrics;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.nio.file.Path;

@Plugin("namelessmc")
public class NamelessPluginSponge {

	private final @NotNull NamelessPlugin plugin;
	private final @NotNull Metrics.Factory metricsFactory;

	@Inject
	public NamelessPluginSponge(final @NotNull SpongeAudiences audiences,
								final @NotNull @ConfigDir(sharedRoot = false) Path dataDirectory,
								final @NotNull Logger logger,
								final @NotNull Metrics.Factory metricsFactory,
								final @NotNull Game game,
								final @NotNull PluginContainer plugin) {
		this.metricsFactory = metricsFactory;
		this.plugin = new NamelessPlugin(
				dataDirectory,
				new SpongeScheduler(this),
				config -> new Log4jLogger(config, logger)
		);
		this.plugin.setAudienceProvider(new SpongeAudienceProvider(audiences, game.server()));
		this.plugin.registerReloadable(new SpongeCommandProxy(this.plugin));
		this.plugin.registerReloadable(new SpongeDataSender(this.plugin));
		Sponge.eventManager().registerListeners(plugin, new SpongeEventProxy(this.plugin));
	}

	@Listener
	public void onStarted(final StartedEngineEvent<Server> event) {
		this.plugin.reload();

		Metrics metrics = this.metricsFactory.make(14865);
		this.plugin.registerCustomCharts(metrics, Metrics.class);
	}

	@Listener
	public void reload(final RefreshGameEvent event) {
		this.plugin.reload();
	}

}
