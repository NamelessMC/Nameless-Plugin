package com.namelessmc.namelessplugin.bungeecord.commands.alone;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;
import com.namelessmc.namelessplugin.bungeecord.API.NamelessAPI;
import com.namelessmc.namelessplugin.bungeecord.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.bungeecord.API.Player.NamelessReportPlayer;
import com.namelessmc.namelessplugin.bungeecord.API.Utils.NamelessChat;
import com.namelessmc.namelessplugin.bungeecord.API.Utils.NamelessMessages;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/*
 *  Report CMD
 */

public class ReportCommand extends Command {

	private NamelessPlugin plugin;
	private String commandName;

	/*
	 * Constructer
	 */
	public ReportCommand(NamelessPlugin pluginInstance, String name) {
		super(name);
		plugin = pluginInstance;
		commandName = name;
	}

	/*
	 * Handle inputted command
	 */
	@Override
	public void execute(CommandSender sender, String[] args) {
		// Instance is a Player
		if (sender instanceof ProxiedPlayer) {
			if (sender.hasPermission(NamelessPlugin.permission + ".report")) {
				ProxiedPlayer player = (ProxiedPlayer) sender;
				NamelessAPI api = plugin.getAPI();
				NamelessPlayer namelessPlayer = api.getPlayer(player.getUniqueId().toString());
				if (namelessPlayer.exists()) {
					if (namelessPlayer.isValidated()) {
						// Try to report
						ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
							@Override
							public void run() {
								if (args.length < 2) {
									sender.sendMessage(NamelessChat.convertColors(
											NamelessChat.getMessage(NamelessMessages.INCORRECT_USAGE_REPORT)
													.replaceAll("%command%", commandName)));
									return;
								} else {
									NamelessReportPlayer report = namelessPlayer.reportPlayer(args);

									if (report.hasError()) {
										// Error with request
										sender.sendMessage(
												NamelessChat.convertColors("&4Error: " + report.getErrorMessage()));
									} else {
										// Display success message to user
										sender.sendMessage(NamelessChat
												.convertColors(NamelessChat.getMessage(NamelessMessages.REPORT_SUCCESS)
														.replaceAll("%player%", args[0])));
									}
								}
							}
						});
					} else {
						sender.sendMessage(
								NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.PLAYER_NOT_VALID)));
					}
				} else {
					sender.sendMessage(
							NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.MUST_REGISTER)));
				}
			} else {
				sender.sendMessage(NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.NO_PERMISSION)));
			}
		} else {
			sender.sendMessage(NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.MUST_BE_INGAME)));
		}
	}

}