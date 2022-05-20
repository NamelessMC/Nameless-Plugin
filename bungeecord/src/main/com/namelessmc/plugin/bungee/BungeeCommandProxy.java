package com.namelessmc.plugin.bungee;

import com.namelessmc.plugin.common.NamelessPlugin;
import com.namelessmc.plugin.common.audiences.NamelessCommandSender;
import com.namelessmc.plugin.common.command.CommonCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.checkerframework.checker.nullness.qual.NonNull;

import static com.namelessmc.plugin.common.LanguageHandler.Term.COMMAND_NO_PERMISSION;

public class BungeeCommandProxy {

	static void registerCommands(final NamelessPlugin plugin, final BungeeNamelessPlugin bungeePlugin) {
		CommonCommand.commands(plugin).forEach(command -> {
			final String name = command.actualName();
			if (name == null) {
				return; // Command is disabled
			}
			final String permission = command.permission().toString();

			Command bungeeCommand = new Command(name, permission) {
				@Override
				public void execute(final @NonNull CommandSender bungeeSender, final String[] args) {
					final NamelessCommandSender sender;
					if (bungeeSender instanceof ProxiedPlayer) {
						sender = plugin.audiences().player(((ProxiedPlayer) bungeeSender).getUniqueId());
					} else {
						sender = plugin.audiences().console();
					}

					if (sender == null) {
						bungeeSender.sendMessage(new TextComponent("ERROR: null audience"));
						return;
					}

					if (!bungeeSender.hasPermission(permission)) {
						sender.sendMessage(plugin.language().get(COMMAND_NO_PERMISSION));
						return;
					}

					command.execute(sender, args);
				}
			};

			ProxyServer.getInstance().getPluginManager().registerCommand(bungeePlugin, bungeeCommand);
		});
	}

}
