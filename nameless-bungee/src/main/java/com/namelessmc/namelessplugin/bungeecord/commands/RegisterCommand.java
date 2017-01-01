package com.namelessmc.namelessplugin.bungeecord.commands;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;
import com.namelessmc.namelessplugin.bungeecord.API.NamelessAPI;
import com.namelessmc.namelessplugin.bungeecord.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.bungeecord.API.utils.NamelessMessages;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/*
 * Register CMD for BungeeCord by IsS127
 */

public class RegisterCommand extends Command {

	NamelessPlugin plugin;
	String permission;

	public RegisterCommand(NamelessPlugin pluginInstance, String name) {
		super(name);
		this.plugin = pluginInstance;
		this.permission = pluginInstance.permission;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		// check if player has permission Permission & ensure who inputted
		// command is a Player
		if (sender instanceof ProxiedPlayer && sender.hasPermission(permission + ".register")) {

			ProxiedPlayer player = (ProxiedPlayer) sender;

			// Try to register user
			ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
				@Override
				public void run() {
					NamelessAPI api = plugin.getAPI();
					NamelessChat chat = api.getChat();

					// Ensure email is set
					if (args.length < 1 || args.length > 1) {
						player.sendMessage(
								chat.convertColors(chat.getMessage(NamelessMessages.INCORRECT_USAGE_REGISTER)));
					} else {
						 api.registerPlayer(player, args[0]);
					}
				}
			});

		} else if (!sender.hasPermission(permission + ".register")) {
			NamelessAPI api = plugin.getAPI();
			NamelessChat chat = api.getChat();
			sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.NO_PERMISSION)));
		} else {
			NamelessAPI api = plugin.getAPI();
			NamelessChat chat = api.getChat();
			sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.MUST_BE_INGAME)));
		}
	}
}