package com.namelessmc.namelessplugin.bungeecord.commands.alone;

import com.namelessmc.namelessplugin.bungeecord.NamelessPlugin;
import com.namelessmc.namelessplugin.bungeecord.API.NamelessAPI;
import com.namelessmc.namelessplugin.bungeecord.API.Player.NamelessPlayer;
import com.namelessmc.namelessplugin.bungeecord.API.utils.NamelessChat;
import com.namelessmc.namelessplugin.bungeecord.API.utils.NamelessMessages;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/*
 *  Register CMD
 */

public class RegisterCommand extends Command {

	private NamelessPlugin plugin;
	private String commandName;

	/*
	 * Constructer
	 */
	public RegisterCommand(NamelessPlugin pluginInstance, String name) {
		super(name);
		plugin = pluginInstance;

		commandName = name;
	}

	/*
	 * Handle inputted command
	 */
	@Override
	public void execute(CommandSender sender, String[] args) {
		// Instance is Player
		if (sender instanceof ProxiedPlayer) {
			if (sender.hasPermission(NamelessPlugin.permission + ".register")) {

				ProxiedPlayer player = (ProxiedPlayer) sender;

				// Try to register user
				ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
					@Override
					public void run() {

						NamelessAPI api = plugin.getAPI();
						NamelessPlayer swPlayer = api.getPlayer(player.getUniqueId().toString());
						if (!swPlayer.exists()) {

							// Ensure email is set
							if (args.length < 1 || args.length > 1) {
								sender.sendMessage(NamelessChat.convertColors(
										NamelessChat.getMessage(NamelessMessages.INCORRECT_USAGE_REGISTER)
												.replaceAll("%command%", commandName)));
							} else {
								api.registerPlayer(player, args[0]);
							}
						}else{
							sender.sendMessage(NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.REGISTER_USERNAME_EXISTS)));
						}
					}
				});
			} else {
				sender.sendMessage(NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.NO_PERMISSION)));
			}

		} else {
			sender.sendMessage(NamelessChat.convertColors(NamelessChat.getMessage(NamelessMessages.MUST_BE_INGAME)));
		}
	}
}