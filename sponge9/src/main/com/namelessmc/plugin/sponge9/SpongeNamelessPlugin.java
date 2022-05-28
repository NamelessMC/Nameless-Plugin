package com.namelessmc.plugin.sponge9;

import com.google.inject.Inject;
import com.namelessmc.plugin.common.NamelessPlugin;
import org.apache.logging.log4j.Logger;
import org.bstats.sponge.Metrics;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.nio.file.Path;

@Plugin("namelessmc")
public class SpongeNamelessPlugin {

	private final NamelessPlugin plugin;
	private final PluginContainer container;
	private final Metrics.Factory metricsFactory;

	@Inject
	public SpongeNamelessPlugin(final @ConfigDir(sharedRoot = false) Path dataDirectory,
								final Logger logger,
								final Metrics.Factory metricsFactory,
								final PluginContainer container) {
		this.metricsFactory = metricsFactory;
		this.container = container;
		this.plugin = new NamelessPlugin(
				dataDirectory,
				new SpongeScheduler(container),
				config -> new Log4jLogger(config, logger),
				Path.of("logs", "latest.log"),
				"sponge9",
				Sponge.platform().minecraftVersion().name()
		);
		this.plugin.setAudienceProvider(new SpongeAudienceProvider());
		this.plugin.registerReloadable(new SpongeDataSender(this.plugin));
		Sponge.eventManager().registerListeners(container, new SpongeEventProxy(this.plugin));
	}

	@Listener
	public void registerCommands(final RegisterCommandEvent<Command> event) {
		SpongeCommandProxy.registerCommands(event, this.plugin, this.container);
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
