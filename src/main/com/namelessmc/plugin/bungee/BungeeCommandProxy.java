package com.namelessmc.plugin.bungee;

import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Reloadable;
import com.namelessmc.plugin.common.command.CommonCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

public class BungeeCommandProxy implements Reloadable {

	private final @NotNull NamelessPluginBungee bungeePlugin;
	private final @NotNull NamelessPlugin plugin;

	BungeeCommandProxy(final @NotNull NamelessPluginBungee bungeePlugin,
					   final @NotNull NamelessPlugin plugin) {
		this.bungeePlugin = bungeePlugin;
		this.plugin = plugin;
	}

	@Override
	public void reload() {
		final PluginManager manager = ProxyServer.getInstance().getPluginManager();

		manager.unregisterCommands(this.bungeePlugin);

		CommonCommand.getEnabledCommands(this.plugin).forEach(command -> {
			final String name = command.getActualName();
			final String permission = command.getPermission().toString();

			Command bungeeCommand = new Command(name, permission) {
				@Override
				public void execute(final CommandSender commandSender, final String[] args) {
					final BungeeCommandSender bungeeCommandSender = new BungeeCommandSender(bungeePlugin.adventure(), commandSender);
					command.execute(bungeeCommandSender, args);
				}
			};

			manager.registerCommand(this.bungeePlugin, bungeeCommand);
		});
	}

}
