package com.namelessmc.namelessplugin.bungeecord.commands;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;
import com.namelessmc.namelessplugin.bungeecord.API.NamelessAPI;
import com.namelessmc.namelessplugin.bungeecord.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.bungeecord.API.Player.NamelessReportPlayer;
import com.namelessmc.namelessplugin.bungeecord.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.bungeecord.API.utils.NamelessMessages;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/*
 *  Report CMD for BungeeCord
 */

public class ReportCommand extends Command {

	NamelessPlugin plugin;
	String permission;

	public ReportCommand(NamelessPlugin pluginInstance, String name) {
		super(name);
		this.plugin = pluginInstance;
		this.permission = pluginInstance.permission;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		// check if player has permission Permission & ensure who inputted
		// command is a Player
		if (sender instanceof ProxiedPlayer && sender.hasPermission(permission + ".report")) {

			ProxiedPlayer player = (ProxiedPlayer) sender;

			// Try to register user
			ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
				@Override
				public void run() {
					NamelessAPI api = plugin.getAPI();
					NamelessChat chat = api.getChat();

					// Ensure email is set
					if (args.length < 2) {
						player.sendMessage(
								chat.convertColors(chat.getMessage(NamelessMessages.INCORRECT_USAGE_REPORT)));
						return;
					} else {
						NamelessPlayer namelessPlayer = api.getPlayer(player.getUniqueId().toString());
						NamelessReportPlayer report = namelessPlayer.reportPlayer(args);

						if (report.hasError()) {
							// Error with request
							player.sendMessage(TextComponent
									.fromLegacyText(ChatColor.RED + "Error: " + report.getErrorMessage()));
						} else {
							// Display success message to user
							player.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.REPORT_SUCCESS).replaceAll("%player%", args[0])));
						}
					}
				}
			});

		} else if (!sender.hasPermission(permission + ".report")){

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