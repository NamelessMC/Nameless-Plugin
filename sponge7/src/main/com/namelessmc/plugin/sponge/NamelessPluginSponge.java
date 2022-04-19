package com.namelessmc.plugin.sponge;

import com.google.inject.Inject;
import com.namelessmc.plugin.common.MavenConstants;
import com.namelessmc.plugin.common.NamelessPlugin;
import net.kyori.adventure.platform.spongeapi.SpongeAudiences;
import org.bstats.sponge.Metrics;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

import java.nio.file.Path;

@Plugin(id = "nameless-plugin",
		name = MavenConstants.PROJECT_NAME,
		version = MavenConstants.PROJECT_VERSION,
		description = "Integration with NamelessMC websites")
public class NamelessPluginSponge {

	private final @NotNull NamelessPlugin plugin;
	private final @NotNull Metrics.Factory metricsFactory;

	@Inject
	public NamelessPluginSponge(final @NotNull SpongeAudiences audiences,
								final @NotNull @ConfigDir(sharedRoot = false) Path dataDirectory,
								final @NotNull Logger logger,
								final @NotNull Metrics.Factory metricsFactory,
								final @NotNull Game game) {
		this.metricsFactory = metricsFactory;
		this.plugin = new NamelessPlugin(
				dataDirectory,
				new SpongeScheduler(this),
				config -> new Slf4jLogger(config, logger)
		);
		this.plugin.setAudienceProvider(new SpongeAudienceProvider(audiences, game.getServer()));
		this.plugin.registerReloadable(new SpongeCommandProxy(this.plugin));
		this.plugin.registerReloadable(new SpongeDataSender(this.plugin));
	}

	@Listener
	public void onServerStart(GameStartedServerEvent event) {
		this.plugin.reload();

		Metrics metrics = this.metricsFactory.make(14865);
		this.plugin.registerCustomCharts(metrics, Metrics.class);
	}

}
