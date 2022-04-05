package com.namelessmc.plugin.bungee;

import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.logger.JulLogger;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class NamelessPluginBungee extends Plugin {

	private BungeeAudiences adventure;
	public BungeeAudiences adventure() { return this.adventure; }

	private final @NotNull NamelessPlugin plugin;

	public NamelessPluginBungee() {
		Path dataDirectory = this.getDataFolder().toPath();
		this.plugin = new NamelessPlugin(
				dataDirectory,
				new BungeeScheduler(this),
				config -> new JulLogger(config, this.getLogger())
		);
		this.plugin.registerReloadable(new BungeeCommandProxy(this, this.plugin));
		this.plugin.registerReloadable(new ServerDataSender(this.plugin));
	}

	@Override
	public void onEnable() {
		this.adventure = BungeeAudiences.create(this);
		this.plugin.reload();
	}

}
