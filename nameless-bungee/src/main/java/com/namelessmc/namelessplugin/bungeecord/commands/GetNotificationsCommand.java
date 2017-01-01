package com.namelessmc.namelessplugin.bungeecord.commands;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;
import com.namelessmc.namelessplugin.bungeecord.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.bungeecord.API.Player.NamelessPlayerNotifications;
import com.namelessmc.namelessplugin.bungeecord.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.bungeecord.API.utils.NamelessMessages;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/*
 *  GetNotifications CMD
 */

public class GetNotificationsCommand extends Command {

	NamelessPlugin plugin;
	String permission;

	/*
	 * Constructer
	 */
	public GetNotificationsCommand(NamelessPlugin pluginInstance, String name) {
		super(name);
		this.plugin = pluginInstance;
		this.permission = plugin.permission;
	}

	/*
	 * Handle inputted command
	 */
	@Override
	public void execute(CommandSender sender, String[] args) {
		// check if player has permission Permission & ensure who inputted
		// command is a Player
		if (sender instanceof ProxiedPlayer && sender.hasPermission(permission + ".notifications")) {
			NamelessChat chat = plugin.getAPI().getChat();
			ProxiedPlayer player = (ProxiedPlayer) sender;
			NamelessPlayer namelessPlayer = plugin.getAPI().getPlayer(player.getUniqueId().toString());
			String mustRegister = chat.getMessage(NamelessMessages.MUST_REGISTER);
			if (namelessPlayer.exists()) {

				// Try to register user
				ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
					@Override
					public void run() {
						// Ensure email is set
						if (args.length < 0 || args.length > 0) {
							player.sendMessage(plugin.getAPI().getChat().convertColors(plugin.getAPI().getChat()
									.getMessage(NamelessMessages.INCORRECT_USAGE_GETNOTIFICATIONS)));
							return;
						} else {

							NamelessPlayerNotifications notifications = namelessPlayer.getNotifications();
							Integer pms = notifications.getPMs();
							Integer alerts = notifications.getAlerts();
							String errorMessage = notifications.getErrorMessage();
							boolean hasError = notifications.hasError();

							String pmMessage = chat.getMessage(NamelessMessages.PM_NOTIFICATIONS_MESSAGE)
									.replaceAll("%pms%", pms.toString());
							String alertMessage = chat.getMessage(NamelessMessages.ALERTS_NOTIFICATIONS_MESSAGE)
									.replaceAll("%alerts%", alerts.toString());
							String noNotifications = chat.getMessage(NamelessMessages.NO_NOTIFICATIONS);

							if (hasError) {
								// Error with request
								player.sendMessage(
										TextComponent.fromLegacyText(ChatColor.RED + "Error: " + errorMessage));
							} else if (alerts.equals(0) && pms.equals(0)) {
								player.sendMessage(chat.convertColors(noNotifications));
							} else if (alerts.equals(0)) {
								player.sendMessage(chat.convertColors(pmMessage));
							} else if (pms.equals(0)) {
								player.sendMessage(chat.convertColors(alertMessage));
							} else {
								player.sendMessage(chat.convertColors(alertMessage));
								player.sendMessage(chat.convertColors(pmMessage));
							}
						}
					}
				});
			} else {
				player.sendMessage(chat.convertColors(mustRegister));
			}

		} else if (!sender.hasPermission(permission + ".notifications")) {
			NamelessChat chat = plugin.getAPI().getChat();
			sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.NO_PERMISSION)));
		} else {
			NamelessChat chat = plugin.getAPI().getChat();
			// User must be ingame to use register command
			sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.MUST_BE_INGAME)));
		}
	}
}