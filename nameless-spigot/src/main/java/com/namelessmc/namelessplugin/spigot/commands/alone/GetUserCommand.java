package com.namelessmc.namelessplugin.spigot.commands.alone;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.API.NamelessAPI;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessMessages;
import com.namelessmc.namelessplugin.spigot.commands.NamelessCommand;

/*
 *  GetUserCommand CMD
 */

public class GetUserCommand extends NamelessCommand {

	private NamelessPlugin plugin;
	private String commandName;

	/*
	 * Constructer
	 */
	public GetUserCommand(NamelessPlugin pluginInstance, String name) {
		super(name);
		plugin = pluginInstance;
		setPermission(NamelessPlugin.permissionAdmin + ".getuser");
		setPermissionMessage(NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.NO_PERMISSION)));
		setUsage("/" + name + "<user>");
		setDescription(NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_GETUSER));

		commandName = name;
	}

	/*
	 * Handle inputted command
	 */
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		System.out.println(args);
		// Try to get the user
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				// Ensure username or uuid set.
				if (args.length < 1 || args.length > 1) {
					sender.sendMessage(
							NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.INCORRECT_USAGE_GETUSER)
									.replaceAll("%command%", commandName)));
					return;
				} else {

					NamelessAPI api = plugin.getAPI();
					NamelessPlayer namelessPlayer = api.getPlayer(args[0]);

					if (namelessPlayer.hasError()) {
						// Error with request
						sender.sendMessage(
								NamelessChat.convertColors("&4Error: &c" + namelessPlayer.getErrorMessage()));
					} else {

						// Display get user.
						String line = "&3&m--------------------------------";

						sender.sendMessage(NamelessChat.convertColors(line));
						sender.sendMessage(
								NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.GETUSER_USERNAME)
										.replaceAll("%username%", namelessPlayer.getUserName())));
						sender.sendMessage(
								NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.GETUSER_DISPLAYNAME)
										.replaceAll("%displayname%", namelessPlayer.getDisplayName())));
						sender.sendMessage(
								NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.GETUSER_UUID)
										.replaceAll("%uuid%", namelessPlayer.getUUID())));
						sender.sendMessage(
								NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.GETUSER_GROUP_ID)
										.replaceAll("%groupid%", namelessPlayer.getGroupID().toString())));
						sender.sendMessage(NamelessChat
								.convertColors(NamelessChat.getMessage(NamelessMessages.GETUSER_REGISTERED).replaceAll(
										"%registereddate%", namelessPlayer.getRegisteredDate().toString())));
						sender.sendMessage(
								NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.GETUSER_REPUTATION)
										.replaceAll("%reputation%", namelessPlayer.getReputations().toString())));

						// check if validated
						if (namelessPlayer.isValidated()) {
							sender.sendMessage(NamelessChat
									.convertColors(NamelessChat.getMessage(NamelessMessages.GETUSER_VALIDATED) + "&a: "
											+ NamelessChat.getMessage(NamelessMessages.GETUSER_VALIDATED_YES)));
						} else {
							sender.sendMessage(NamelessChat
									.convertColors(NamelessChat.getMessage(NamelessMessages.GETUSER_VALIDATED) + "&c: "
											+ NamelessChat.getMessage(NamelessMessages.GETUSER_VALIDATED_NO)));
						}
						// check if banned
						if (namelessPlayer.isBanned()) {
							sender.sendMessage(
									NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.GETUSER_BANNED)
											+ "&c: " + NamelessChat.getMessage(NamelessMessages.GETUSER_BANNED_YES)));
						} else {
							sender.sendMessage(
									NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.GETUSER_BANNED)
											+ "&a: " + NamelessChat.getMessage(NamelessMessages.GETUSER_BANNED_NO)));
						}
						sender.sendMessage(NamelessChat.convertColors(line));
					}
				}
			}
		});
		return true;
	}
}