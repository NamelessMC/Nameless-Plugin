package com.namelessmc.namelessplugin.spigot.commands.alone;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.namelessmc.namelessplugin.spigot.NamelessPlugin;
import com.namelessmc.namelessplugin.spigot.API.NamelessAPI;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.spigot.API.Player.NamelessPlayerSetGroup;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.spigot.API.utils.NamelessMessages;
import com.namelessmc.namelessplugin.spigot.commands.NamelessCommand;

/*
 *  Register CMD
 */

public class SetGroupCommand extends NamelessCommand {

	NamelessPlugin plugin;
	String commandName;

	/*
	 * Constructer
	 */
	public SetGroupCommand(NamelessPlugin pluginInstance, String name) {
		super(name);
		plugin = pluginInstance;
		setPermission(NamelessPlugin.permissionAdmin + ".setgroup");
		setPermissionMessage(NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.NO_PERMISSION)));
		setUsage("/" + name + "<user> <groupID>");
		this.setDescription(NamelessChat.getMessage(NamelessMessages.HELP_DESCRIPTION_SETGROUP));
	}

	/*
	 * Handle inputted command
	 */
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		// Try to setgroup
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {

				NamelessAPI api = plugin.getAPI();

				// Ensure email is set
				if (args.length < 2 || args.length > 2) {
					sender.sendMessage(NamelessChat
							.convertColors(NamelessChat.getMessage(NamelessMessages.INCORRECT_USAGE_SETGROUP)
									.replaceAll("%command%", commandName)));
				} else {
					NamelessPlayer namelessPlayer = api.getPlayer(args[0]);
					Integer previousgroup = namelessPlayer.getGroupID();
					NamelessPlayerSetGroup group = namelessPlayer.setGroupID(args[1]);

					if (group.hasError()) {
						sender.sendMessage(NamelessChat.convertColors("&cError: " + group.getErrorMessage()));
					} else {
						String success = NamelessChat.getMessage(NamelessMessages.SETGROUP_SUCCESS)
								.replaceAll("%player%", group.getID())
								.replaceAll("%previousgroup%", previousgroup.toString())
								.replaceAll("%newgroup%", group.getNewGroup().toString());
						sender.sendMessage(NamelessChat.convertColors(success));
					}
				}
			}
		});
		return true;
	}
}