package com.namelessmc.namelessplugin.spigot.commands.alone;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessPlayerNotifications;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessMessages;
import com.namelessmc.namelessplugin.spigot.commands.NamelessCommand;

/*
 *  GetNotifications CMD
 */

public class GetNotificationsCommand extends NamelessCommand{
	
	NamelessPlugin plugin;
	String commandName;

	/*
	 * Constructer
	 */
	public GetNotificationsCommand(NamelessPlugin pluginInstance, String name) {
		super(name);
		plugin = pluginInstance;
		setPermission(plugin.permission + ".notifications");
		setPermissionMessage(plugin.getAPI().getChat()
				.convertColors(plugin.getAPI().getChat().getMessage(NamelessMessages.NO_PERMISSION)));
		usageMessage = "/" + name;
		
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
			NamelessChat chat = plugin.getAPI().getChat();
			Player player = (Player) sender;
			NamelessPlayer namelessPlayer = plugin.getAPI().getPlayer(player.getUniqueId().toString());
			String mustRegister = chat.getMessage(NamelessMessages.MUST_REGISTER);
			if (namelessPlayer.exists()) {

				// Try to getNotifications
				Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
					@Override
					public void run() {
						if (args.length > 0) {
							player.sendMessage(plugin.getAPI().getChat().convertColors(plugin.getAPI().getChat()
									.getMessage(NamelessMessages.INCORRECT_USAGE_GETNOTIFICATIONS).replaceAll("%command%", commandName)));
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
								player.sendMessage(chat.convertColors("&4Error: " + errorMessage));
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

		} else {
			NamelessChat chat = plugin.getAPI().getChat();
			// User must be ingame to use register command
			sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.MUST_BE_INGAME)));
		}
		return true;
	}

	@Override
	public Plugin getPlugin() {
		return plugin;
	}
}