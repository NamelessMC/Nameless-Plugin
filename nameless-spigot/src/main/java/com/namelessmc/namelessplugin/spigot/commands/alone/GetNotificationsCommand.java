package com.namelessmc.namelessplugin.spigot.commands.alone;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessPlayerNotifications;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessMessages;
import com.namelessmc.namelessplugin.spigot.commands.NamelessCommand;

/*
 *  GetNotifications CMD
 */

public class GetNotificationsCommand extends NamelessCommand {

	NamelessPlugin plugin;
	String commandName;

	/*
	 * Constructer
	 */
	public GetNotificationsCommand(NamelessPlugin pluginInstance, String name) {
		super(name);
		plugin = pluginInstance;
		setPermission(NamelessPlugin.permission + ".notifications");
		setPermissionMessage(NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.NO_PERMISSION)));
		setUsage("/" + name);
		setDescription(NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_GETNOTIFICATIONS));
		commandName = name;
	}

	/*
	 * Handle inputted command
	 */
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		// check if player has permission Permission & ensure who inputted
		// command is a Player
		if (sender instanceof Player) {
			Player player = (Player) sender;
			NamelessPlayer namelessPlayer = plugin.getAPI().getPlayer(player.getUniqueId().toString());
			String mustRegister = NamelessChat.getMessage(NamelessMessages.MUST_REGISTER);
			if (namelessPlayer.exists()) {

				// Try to getNotifications
				Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
					@Override
					public void run() {
						if (args.length > 0) {
							player.sendMessage(NamelessChat.convertColors(
									NamelessChat.getMessage(NamelessMessages.INCORRECT_USAGE_GETNOTIFICATIONS)
											.replaceAll("%command%", commandName)));
							return;
						} else {

							NamelessPlayerNotifications notifications = namelessPlayer.getNotifications();
							Integer pms = notifications.getPMs();
							Integer alerts = notifications.getAlerts();
							String errorMessage = notifications.getErrorMessage();
							boolean hasError = notifications.hasError();

							String pmMessage = NamelessChat.getMessage(NamelessMessages.PM_NOTIFICATIONS_MESSAGE)
									.replaceAll("%pms%", pms.toString());
							String alertMessage = NamelessChat.getMessage(NamelessMessages.ALERTS_NOTIFICATIONS_MESSAGE)
									.replaceAll("%alerts%", alerts.toString());
							String noNotifications = NamelessChat.getMessage(NamelessMessages.NO_NOTIFICATIONS);

							if (hasError) {
								// Error with request
								player.sendMessage(NamelessChat.convertColors("&4Error: " + errorMessage));
							} else if (alerts.equals(0) && pms.equals(0)) {
								player.sendMessage(NamelessChat.convertColors(noNotifications));
							} else if (alerts.equals(0)) {
								player.sendMessage(NamelessChat.convertColors(pmMessage));
							} else if (pms.equals(0)) {
								player.sendMessage(NamelessChat.convertColors(alertMessage));
							} else {
								player.sendMessage(NamelessChat.convertColors(alertMessage));
								player.sendMessage(NamelessChat.convertColors(pmMessage));
							}
						}
					}
				});
			} else {
				player.sendMessage(NamelessChat.convertColors(mustRegister));
			}

		} else {
			// User must be ingame to use register command
			sender.sendMessage(NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.MUST_BE_INGAME)));
		}
		return true;
	}
}