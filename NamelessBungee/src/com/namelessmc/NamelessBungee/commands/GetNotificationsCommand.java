package com.namelessmc.NamelessBungee.commands;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;
import com.namelessmc.namelessplugin.bungeecord.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.bungeecord.API.Player.NamelessPlayerNotifications;
import com.namelessmc.namelessplugin.bungeecord.API.Utils.NamelessChat;
import com.namelessmc.namelessplugin.bungeecord.API.Utils.NamelessMessages;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/*
 *  GetNotifications CMD
 */

public class GetNotificationsCommand extends Command {

	private String commandName;

	public GetNotificationsCommand(String name) {
		super(name);
		commandName = name;
	}

	/*
	 * Handle inputted command
	 */
	@Override
	public void execute(CommandSender sender, String[] args) {
		// check if player has permission Permission & ensure who inputted
		// command is a Player
		if (sender instanceof ProxiedPlayer) {
			if (sender.hasPermission(NamelessPlugin.permission + ".notifications")) {
				ProxiedPlayer player = (ProxiedPlayer) sender;
				NamelessPlayer namelessPlayer = plugin.getAPI().getPlayer(player.getUniqueId().toString());
				if (namelessPlayer.exists()) {
					if (namelessPlayer.isValidated()) {
						// Try to getNotifications
						ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
							@Override
							public void run() {
								if (args.length > 0) {
									sender.sendMessage(NamelessChat.convertColors(
											NamelessChat.getMessage(NamelessMessages.INCORRECT_USAGE_GETNOTIFICATIONS)
													.replaceAll("%command%", commandName)));
									return;
								} else {

									NamelessPlayerNotifications notifications = namelessPlayer.getNotifications();
									Integer pms = notifications.getPMs();
									Integer alerts = notifications.getAlerts();
									String errorMessage = notifications.getErrorMessage();
									boolean hasError = notifications.hasError();

									String pmMessage = NamelessChat
											.getMessage(NamelessMessages.PM_NOTIFICATIONS_MESSAGE)
											.replaceAll("%pms%", pms.toString());
									String alertMessage = NamelessChat
											.getMessage(NamelessMessages.ALERTS_NOTIFICATIONS_MESSAGE)
											.replaceAll("%alerts%", alerts.toString());
									String noNotifications = NamelessChat.getMessage(NamelessMessages.NO_NOTIFICATIONS);

									if (hasError) {
										// Error with request
										sender.sendMessage(NamelessChat.convertColors("&4Error: " + errorMessage));
									} else if (alerts.equals(0) && pms.equals(0)) {
										sender.sendMessage(NamelessChat.convertColors(noNotifications));
									} else if (alerts.equals(0)) {
										sender.sendMessage(NamelessChat.convertColors(pmMessage));
									} else if (pms.equals(0)) {
										sender.sendMessage(NamelessChat.convertColors(alertMessage));
									} else {
										sender.sendMessage(NamelessChat.convertColors(alertMessage));
										sender.sendMessage(NamelessChat.convertColors(pmMessage));
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
			// User must be ingame to use register command
			sender.sendMessage(NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.MUST_BE_INGAME)));
		}
	}

}