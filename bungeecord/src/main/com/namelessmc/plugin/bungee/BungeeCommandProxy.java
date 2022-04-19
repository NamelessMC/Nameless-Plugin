package com.namelessmc.plugin.bungee;

import com.namelessmc.plugin.common.LanguageHandler;
import com.namelessmc.plugin.common.NamelessCommandSender;
import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.Reloadable;
import com.namelessmc.plugin.common.command.CommonCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

public class BungeeCommandProxy implements Reloadable {

	private final @NotNull BungeeNamelessPlugin bungeePlugin;
	private final @NotNull NamelessPlugin plugin;

	BungeeCommandProxy(final @NotNull BungeeNamelessPlugin bungeePlugin,
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
				public void execute(final CommandSender bungeeSender, final String[] args) {
					final NamelessCommandSender sender;
					if (bungeeSender instanceof ProxiedPlayer) {
						sender = plugin.audiences().player(((ProxiedPlayer) bungeeSender).getUniqueId());
					} else {
						sender = plugin.audiences().console();
					}

					if (!bungeeSender.hasPermission(permission)) {
						sender.sendMessage(plugin.language().getComponent(LanguageHandler.Term.COMMAND_NO_PERMISSION));
						return;
					}

					command.execute(sender, args);
				}
			};

			manager.registerCommand(this.bungeePlugin, bungeeCommand);
		});
	}

}
