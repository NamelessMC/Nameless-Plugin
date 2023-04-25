package com.namelessmc.plugin.bungee;

import com.namelessmc.plugin.bungee.audiences.BungeeAudienceProvider;
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
				config -> new JulLogger(config, this.getLogger()),
				Path.of("logs", "latest.log"),
				"bungeecord",
				ProxyServer.getInstance().getVersion()
		);
		this.plugin.registerReloadable(new BungeeDataSender(this.plugin));
	}

	@Override
	public void onEnable() {
		this.plugin.setAudienceProvider(new BungeeAudienceProvider(this.plugin.config(), this));
		this.plugin.load();

		new Metrics(this, 14864);

		BungeeCommandProxy.registerCommands(this.plugin, this);

		ProxyServer.getInstance().getPluginManager().registerListener(this, new BungeeEventProxy(this.plugin));
	}

	@Override
	public void onDisable() {
		this.plugin.unload();
	}

}
