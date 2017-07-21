package com.namelessmc.namelessplugin.bungeecord.commands.alone;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;
import com.namelessmc.namelessplugin.bungeecord.API.NamelessAPI;
import com.namelessmc.namelessplugin.bungeecord.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.bungeecord.API.Player.NamelessPlayerSetGroup;
import com.namelessmc.namelessplugin.bungeecord.API.Utils.NamelessChat;
import com.namelessmc.namelessplugin.bungeecord.API.Utils.NamelessMessages;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

/*
 *  Register CMD
 */

public class SetGroupCommand extends Command {

	private NamelessPlugin plugin;
	private String commandName;

	/*
	 * Constructer
	 */
	public SetGroupCommand(NamelessPlugin pluginInstance, String name) {
		super(name);
		plugin = pluginInstance;
		commandName = name;
	}

	/*
	 * Handle inputted command
	 */
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender.hasPermission(NamelessPlugin.permissionAdmin + ".setgroup")) {

			// Try to setgroup
			ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
				@Override
				public void run() {

					NamelessAPI api = plugin.getAPI();

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
		} else {
			sender.sendMessage(NamelessChat
					.convertColors(NamelessChat.getMessage(NamelessMessages.NO_PERMISSION)));
		}
	}

}