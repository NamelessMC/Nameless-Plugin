package com.namelessmc.namelessplugin.spigot.commands.alone;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.namelessmc.namelessplugin.spigot.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.API.NamelessAPI;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessMessages;
import com.namelessmc.namelessplugin.spigot.commands.NamelessCommand;

/*
 *  Register CMD
 */

public class RegisterCommand extends NamelessCommand {

	private NamelessPlugin plugin;
	private String commandName;

	/*
	 * Constructer
	 */
	public RegisterCommand(NamelessPlugin pluginInstance, String name) {
		super(name);
		plugin = pluginInstance;
		setPermission(NamelessPlugin.permission + ".register");
		setPermissionMessage(NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.NO_PERMISSION)));
		setUsage("/" + name + "<email>");
		setDescription(NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_REGISTER));

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
					NamelessPlayer swPlayer = api.getPlayer(player.getUniqueId().toString());
					if (!swPlayer.exists()) {

						// Ensure email is set
						if (args.length < 1 || args.length > 1) {
							player.sendMessage(NamelessChat
									.convertColors(NamelessChat.getMessage(NamelessMessages.INCORRECT_USAGE_REGISTER)
											.replaceAll("%command%", commandName)));
						} else {
							api.registerPlayer(player, args[0]);
						}
					} else {
						sender.sendMessage(NamelessChat
								.convertColors(NamelessChat.getMessage(NamelessMessages.REGISTER_USERNAME_EXISTS)));
					}
				}
			});

		} else {
			sender.sendMessage(NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.MUST_BE_INGAME)));
		}
		return true;
	}
}