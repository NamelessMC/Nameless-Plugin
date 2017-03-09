package com.namelessmc.namelessplugin.spigot.commands.alone;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

import com.namelessmc.namelessplugin.spigot.API.NamelessAPI;
import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessMessages;

/*
 *  GetUserCommand CMD
 */

public class GetUserCommand extends Command implements PluginIdentifiableCommand {

	NamelessPlugin plugin;

	/*
	 * Constructer
	 */
	public GetUserCommand(NamelessPlugin pluginInstance, String name) {
		super(name);
		this.plugin = pluginInstance;
		this.setPermission(plugin.permissionAdmin + ".getuser");
		this.setPermissionMessage(plugin.getAPI().getChat()
				.convertColors(plugin.getAPI().getChat().getMessage(NamelessMessages.NO_PERMISSION)));
		this.usageMessage = "/" + name + "<user>";
	}

	/*
	 * Handle inputted command
	 */
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		// Try to get the user
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				// Ensure username or uuid set.
				if (args.length < 1 || args.length > 1) {
					NamelessChat chat = plugin.getAPI().getChat();
					sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.INCORRECT_USAGE_GETUSER)));
					return;
				} else {

					NamelessAPI api = plugin.getAPI();
					NamelessPlayer namelessPlayer = api.getPlayer(args[0]);
					NamelessChat chat = api.getChat();

					if (namelessPlayer.hasError()) {
						// Error with request
						sender.sendMessage(chat.convertColors("&4Error: &c" + namelessPlayer.getErrorMessage()));
					} else {

						// Display get user.
						String line = "&3&m--------------------------------";

						sender.sendMessage(chat.convertColors(line));
						sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.GETUSER_USERNAME)
								.replaceAll("%username%", namelessPlayer.getUserName())));
						sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.GETUSER_DISPLAYNAME)
								.replaceAll("%displayname%", namelessPlayer.getDisplayName())));
						sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.GETUSER_UUID)
								.replaceAll("%uuid%", namelessPlayer.getUUID())));
						sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.GETUSER_GROUP_ID)
								.replaceAll("%groupid%", namelessPlayer.getGroupID().toString())));
						sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.GETUSER_REGISTERED)
								.replaceAll("%registereddate%", namelessPlayer.getRegisteredDate().toString())));
						sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.GETUSER_REPUTATION)
								.replaceAll("%reputation%", namelessPlayer.getReputations().toString())));

						// check if validated
						if (namelessPlayer.isValidated()) {
							sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.GETUSER_VALIDATED)
									+ "&a: " + chat.getMessage(NamelessMessages.GETUSER_VALIDATED_YES)));
						} else {
							sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.GETUSER_VALIDATED)
									+ "&c: " + chat.getMessage(NamelessMessages.GETUSER_VALIDATED_NO)));
						}
						// check if banned
						if (namelessPlayer.isBanned()) {
							sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.GETUSER_BANNED)
									+ "&c: " + chat.getMessage(NamelessMessages.GETUSER_BANNED_YES)));
						} else {
							sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.GETUSER_BANNED)
									+ "&a: " + chat.getMessage(NamelessMessages.GETUSER_BANNED_NO)));
						}
						sender.sendMessage(chat.convertColors(line));
					}
				}
			}
		});
		return true;
	}

	@Override
	public Plugin getPlugin() {
		return plugin;
	}
}