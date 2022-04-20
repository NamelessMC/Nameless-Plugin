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
import org.checkerframework.checker.nullness.qual.NonNull;

public class BungeeCommandProxy implements Reloadable {

	private final @NonNull BungeeNamelessPlugin bungeePlugin;
	private final @NonNull NamelessPlugin plugin;

	BungeeCommandProxy(final @NonNull BungeeNamelessPlugin bungeePlugin,
					   final @NonNull NamelessPlugin plugin) {
		this.bungeePlugin = bungeePlugin;
		this.plugin = plugin;
	}

	@Override
	public void reload() {
		final PluginManager manager = ProxyServer.getInstance().getPluginManager();

		manager.unregisterCommands(this.bungeePlugin);

		CommonCommand.enabledCommands(this.plugin).forEach(command -> {
			final String name = command.actualName();
			final String permission = command.permission().toString();

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
						sender.sendMessage(plugin.language().get(LanguageHandler.Term.COMMAND_NO_PERMISSION));
						return;
					}

					command.execute(sender, args);
				}
			};

			manager.registerCommand(this.bungeePlugin, bungeeCommand);
		});
	}

}
