package com.namelessmc.namelessplugin.spigot.commands.alone;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.namelessmc.namelessplugin.spigot.API.NamelessAPI;
import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessMessages;
import com.namelessmc.namelessplugin.spigot.commands.NamelessCommand;

/*
 *  Register CMD
 */

public class RegisterCommand extends NamelessCommand {

	NamelessPlugin plugin;
	String commandName;

	/*
	 * Constructer
	 */
	public RegisterCommand(NamelessPlugin pluginInstance, String name) {
		super(name);
		plugin = pluginInstance;
		setPermission(plugin.permission + ".register");
		setPermissionMessage(plugin.getAPI().getChat()
				.convertColors(plugin.getAPI().getChat().getMessage(NamelessMessages.NO_PERMISSION)));
		usageMessage = "/" + name + "<email>";
		
		commandName = name;
	}

	/*
	 * Handle inputted command
	 */
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		// Instance is Player
		if (sender instanceof Player) {

			Player player = (Player) sender;

			// Try to register user
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				@Override
				public void run() {
					NamelessAPI api = plugin.getAPI();
					NamelessChat chat = api.getChat();

					// Ensure email is set
					if (args.length < 1 || args.length > 1) {
						player.sendMessage(
								chat.convertColors(chat.getMessage(NamelessMessages.INCORRECT_USAGE_REGISTER).replaceAll("%command%", commandName)));
					} else {
						api.registerPlayer(player, args[0]);
					}
				}
			});

		} else {
			NamelessAPI api = plugin.getAPI();
			NamelessChat chat = api.getChat();
			sender.sendMessage(chat.convertColors(chat.getMessage(NamelessMessages.MUST_BE_INGAME)));
		}
		return true;
	}

	@Override
	public Plugin getPlugin() {
		return plugin;
	}
}