package com.namelessmc.plugin.bungee;

import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.logger.JulLogger;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class NamelessPluginBungee extends Plugin {

	private final @NotNull NamelessPlugin plugin;

	public NamelessPluginBungee() {
		Path dataDirectory = this.getDataFolder().toPath();
		this.plugin = new NamelessPlugin(
				dataDirectory,
				new BungeeScheduler(this),
				config -> new JulLogger(config, this.getLogger())
		);
		this.plugin.registerReloadable(new BungeeCommandProxy(this, this.plugin));
		this.plugin.registerReloadable(new BungeeDataSender(this.plugin));
	}

	@Override
	public void onEnable() {
		this.plugin.setAudienceProvider(new BungeeAudienceProvider(this));
		this.plugin.reload();

		Metrics metrics = new Metrics(this, 14864);
		this.plugin.registerCustomCharts(metrics, Metrics.class);
	}

}
