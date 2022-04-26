package com.namelessmc.plugin.bungee;

import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.logger.JulLogger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.nio.file.Path;

public class BungeeNamelessPlugin extends Plugin {

	private final @NonNull NamelessPlugin plugin;

	public BungeeNamelessPlugin() {
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

		ProxyServer.getInstance().getPluginManager().registerListener(this, new BungeeEventProxy(this.plugin));
	}

}
